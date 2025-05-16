package ipana.utils.ncp.listener;

import ipana.eventapi.EventManager;
import ipana.events.EventPacketReceive;
import ipana.events.EventPacketSend;
import ipana.events.EventSetBack;
import ipana.utils.QueueList;
import ipana.utils.math.AvgList;
import ipana.utils.ncp.utilities.VelocityData;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayList;
import java.util.List;

public class NCPListener {
    private VelocityData data = new VelocityData();
    public double prevX, prevY, prevZ, lastYDist;
    public double sfHorizontalBuffer;
    private Minecraft mc = Minecraft.getMinecraft();
    private AvgList combinedHorizontal = new AvgList(30);
    public QueueList<SlotProperty> ignoredSlots = new QueueList<>(1000);
    public QueueList<WindowProperty> ignoredWindows = new QueueList<>(1000);
    public QueueList<TransactionProperty> ignoredTransactions = new QueueList<>(1000);
    public QueueList<BlockPos> ignoredBlockChanges = new QueueList<>(1000);

    public NCPListener() {
        EventManager.eventSystem.subscribeAll(this);
    }

    private Listener<EventPacketReceive> onReceivePre = new Listener<EventPacketReceive>(event -> {
        if (event.getPacket() instanceof S12PacketEntityVelocity velocity) {
            if (mc.theWorld.getEntityByID(velocity.getEntityID()) == Minecraft.getMinecraft().thePlayer) {
                double adjustment = 0.0;
                //NCP CALCULATES P2P VELOCITY WRONG (only p2p).
                //It's always 0.1 more than what we receive.
                //There is no precise way to define our velocity is made by a player or not.
                //Common flag scenario: BowFly's first hit has same values with p2p velocity however it's not a p2p velocity.
                //Also, NCP adds velocity per damage not per hit. Which means you will get insane boost from a TickAura.
                //Summary: Code below will stay commented.

                    /*double hVel = Math.hypot(velocity.getMotionX() / 8000.0F, velocity.getMotionZ() / 8000.0F);
                    String str = String.valueOf(hVel);
                    int dot = str.indexOf('.');
                    if (str.charAt(dot + 2) == '9' && str.charAt(dot + 3) == '9' && (str.charAt(dot + 4) == '9' || str.charAt(dot + 4) == '8')) {
                        adjustment = 0.1;
                    }*/
                data.addVelocity(velocity.getMotionX() / 8000.0, velocity.getMotionY() / 8000.0, velocity.getMotionZ() / 8000.0, adjustment);
            }
        } else if (event.getPacket() instanceof S2FPacketSetSlot packet) {
            if (ignoredSlots.removeFirstMatch(slotProperty ->
                    packet.windowId() == slotProperty.windowId() &&
                    packet.slot() == slotProperty.slotId() &&
                    (slotProperty.nullItem() == (packet.itemStack() == null)))) {
                event.setCancelled(true);
            }
            //PlayerUtils.debug(event.isCancelled()+", "+packet.windowId()+", "+packet.slot()+", "+packet.itemStack());
        } else if (event.getPacket() instanceof S30PacketWindowItems packet) {
            if (ignoredWindows.removeFirstMatch(windowProperty -> {
                boolean match = packet.getItemStacks() != null &&
                        windowProperty.slotId() >= 0 &&
                        packet.getItemStacks().length > windowProperty.slotId();
                var packetStack = packet.getItemStacks()[windowProperty.slotId()];
                if (match && packetStack != null) {
                    mc.thePlayer.inventory.setItemStack(packetStack);
                }
                return match;
            })) {
                event.setCancelled(true);
            }
        } else if (event.getPacket() instanceof S22PacketMultiBlockChange packet) {
            List<S22PacketMultiBlockChange.BlockUpdateData> newData = new ArrayList<>();
            for (var data : packet.getChangedBlocks()) {
                if (!ignoredBlockChanges.hasMatch(f -> f.equals(data.getPos()))) {
                    newData.add(data);
                }
            }
            packet.setChangedBlocks(newData.toArray(new S22PacketMultiBlockChange.BlockUpdateData[0]));
        }
    }).filter(event -> event.getState() == EventPacketReceive.PacketState.PRE && mc.theWorld != null && mc.thePlayer != null);

    private Listener<EventSetBack> onSetBack = new Listener<EventSetBack>(event -> {
        if (!event.cancelSetPos() && !event.cancelPacket()) {
            sfHorizontalBuffer = 0;
            data.clearHorVel();
            combinedHorizontal.clear();
        }
    }).filter(eventSetBack -> eventSetBack.state() == EventSetBack.State.PRE).weight(Event.Weight.LOWEST);

    private Listener<EventPacketSend> onSend = new Listener<EventPacketSend>(event -> {
        //PlayerUtils.debug(event.getPacket());
        if (event.getPacket() instanceof C03PacketPlayer c03) {
            if (event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition || event.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
                double diffX = c03.getPositionX()-prevX;
                double diffY = c03.getPositionY()-prevY;
                double diffZ = c03.getPositionZ()-prevZ;
                data.velocityTick(Minecraft.getRunTick() - VelocityData.ACTIVATION_TICKS);
                double base = PlayerUtils.getBaseMoveSpeed();
                double hDistance = Math.hypot(diffX, diffZ);
                double hDistanceAboveLimit = hDistance-base;
                boolean isSamePos = diffX == 0 && diffZ == 0;
                //PlayerUtils.debug(hDistance + " : "+c03.getPositionY());
                if (hDistance <= 0.0625 && diffY != 0 || hDistance <= base) {
                    data.clearActiveHorVel();
                }
                if (hDistanceAboveLimit < 0) {
                    data.clearActiveHorVel();
                }
                if (hDistanceAboveLimit > 0 && sfHorizontalBuffer > 0) {
                    double amount = Math.min(sfHorizontalBuffer, hDistanceAboveLimit);
                    hDistanceAboveLimit -= amount;
                    sfHorizontalBuffer = Math.max(0, sfHorizontalBuffer - amount);
                }

                if (hDistanceAboveLimit < 0.0 && !isSamePos && sfHorizontalBuffer < 1.0) {
                    this.hBufRegain(hDistance, Math.min(0.2, Math.abs(hDistanceAboveLimit)));
                }
                prevX = c03.getPositionX();
                prevY = c03.getPositionY();
                prevZ = c03.getPositionZ();
                lastYDist = diffY;
            }
            PlayerUtils.updatePotionEffects();
        } else if (event.getPacket() instanceof C0FPacketConfirmTransaction transaction) {
            if (ignoredTransactions.removeFirstMatch(transactionProperty -> transaction.getUid() == transactionProperty.transaction())) {
                event.setCancelled(true);
            }
        }
    }).weight(Event.Weight.LOWEST).filter(event -> event.getState() == EventPacketSend.PacketState.PRE && !event.isCancelled());

    private void hBufRegain(double hDistance, double amount) {
        sfHorizontalBuffer = Math.min(1.0, sfHorizontalBuffer + amount);
    }

    public double combinedMove() {
        return combinedHorizontal.average();
    }

    public double predictCombinedMove(double hMove) {
        return combinedHorizontal.checkUp(hMove);
    }

    public VelocityData data() {
        return data;
    }

    public void setVelData(VelocityData newData) {
        data = newData;
    }

    public record WindowProperty(ItemStack itemStack, int slotId, long ms) {

    }
    public record SlotProperty(int windowId, int slotId, boolean nullItem, long ms) {

    }
    public record TransactionProperty(long transaction, long ms) {

    }
}
