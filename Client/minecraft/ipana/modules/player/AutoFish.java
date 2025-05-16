package ipana.modules.player;

import ipana.events.EventClearItemUse;
import ipana.events.EventPacketReceive;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.Timer;
import ipana.utils.math.MathUtils;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class AutoFish extends Module {
    public AutoFish() {
        super("AutoFish", Keyboard.KEY_NONE, Category.Player, "Catch fish automatically.");
    }

    private BoolValue afk = new BoolValue("Afk", this, false, "");

    private Timer timer = new Timer();

    private long lastVelTime;
    private int lastAfkTick;
    private double prevY;

    public Listener<EventPreUpdate> updateEvent = new Listener<>(event -> {
        if (!isHoldingFishingRod()) {
            getOtherRods();
            return;
        }
        fixRod();

        if (!afk.getValue() && mc.thePlayer.fishEntity == null && timer.delay(1000)) {
            mc.rightClickMouse();
            lastVelTime = System.currentTimeMillis();
            timer.reset();
        }
        boolean canAfk = Minecraft.getRunTick() - lastAfkTick > 21 * 10 ;
        if (afk.getValue() && mc.thePlayer.fishEntity == null && canAfk) {
            PlayerUtils.debug("Start fishing...");
            mc.rightClickMouse();
            mc.thePlayer.sendChatMessage("/afk");
            lastVelTime = System.currentTimeMillis();
            lastAfkTick = Minecraft.getRunTick();
        }
    });

    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> {
        if (event.getState() != EventPacketReceive.PacketState.PRE) {
            return;
        }
        if (mc.thePlayer == null || !isHoldingFishingRod() || mc.thePlayer.fishEntity == null) {
            return;
        }
        var fish = mc.thePlayer.fishEntity;
        if (event.getPacket() instanceof S12PacketEntityVelocity packet) {

            if (packet.getEntityID() == fish.getEntityId()) {
                if (System.currentTimeMillis() - lastVelTime >= 2000) {
                    pullBack(packet.getMotionY() / 8000.0);
                }
                lastVelTime = System.currentTimeMillis();
            }
        } else if (event.getPacket() instanceof S29PacketSoundEffect packet) {
            double dX = packet.getX() - fish.posX;
            double dY = packet.getY() - fish.posY;
            double dZ = packet.getZ() - fish.posZ;
            double dist = Math.sqrt(dX*dX + dY*dY + dZ*dZ);
            if (!afk.getValue() && packet.getSoundName().equals("random.splash") && dist <= 0.1) {
                pullBack(Math.hypot(dX,  dZ));
            }
        }
    });

    private Listener<EventClearItemUse> onClear = new Listener<>(event -> {
        event.setCancelled(true);
    });

    private void pullBack(double additionalValue) {
        if (afk.getValue()) {
            boolean canAfk = Minecraft.getRunTick() - lastAfkTick > 21 * 10;//Additional 4 ticks
            if (canAfk) {
                mc.rightClickMouse();
                mc.rightClickMouse();
                mc.thePlayer.sendChatMessage("/afk");
                lastAfkTick = Minecraft.getRunTick();
            } else {
                PlayerUtils.debug("Skipping because can't afk ("+(Minecraft.getRunTick() - lastAfkTick)+")");
            }
        } else {
            mc.rightClickMouse();
            mc.rightClickMouse();
        }

        long diff = System.currentTimeMillis() - lastVelTime;
        PlayerUtils.debug("Fish Caught ("+diff/1000+"s, "+additionalValue+")");
    }

    private boolean isHoldingFishingRod() {
        final var heldItem = mc.thePlayer.getCurrentEquippedItem();
        return heldItem != null && heldItem.getItem() instanceof ItemFishingRod && heldItem.getMaxDamage()-heldItem.getItemDamage() > 1;
    }

    private void fixRod() {
        if (mc.thePlayer.fishEntity == null) {
            for (var entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof EntityFishHook fishHook) {
                    if (fishHook.angler == mc.thePlayer) {
                        mc.thePlayer.fishEntity = fishHook;
                    }
                }
            }
        } else if (mc.thePlayer.fishEntity.isDead || !mc.thePlayer.fishEntity.isEntityAlive()) {
            mc.thePlayer.fishEntity = null;
        }
    }

    private void getOtherRods() {
        for (int i = 0; i < 45; i++) {
            var slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack() && slot.getStack().getItem() instanceof ItemFishingRod && slot.getStack().getMaxDamage()-slot.getStack().getItemDamage() > 1) {
                PlayerUtils.swapItem(i, mc.thePlayer.inventory.currentItem);
                return;
            }
        }
    }
}
