package ipana.modules.combat;

import ipana.managements.friend.Friend;
import ipana.managements.module.Modules;
import ipana.managements.value.values.*;
import ipana.events.*;
import ipana.managements.friend.FriendManager;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.modules.combat.modes.killaura.*;
import ipana.modules.movement.Fly;
import ipana.modules.movement.modes.fly.OldNCPGlide;
import ipana.modules.player.AutoDrink;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KillAura extends Module {
    //public NumberValue<Integer> cps = new NumberValue<>("CPS",this,12,1,20,1,"Attack speed.");
    public NumberValue<Float> range = new NumberValue<>("Range",this,4.0f,1.0f,6.0f,0.1f,"Attack range.");
    public BoolValue autoBlock = new BoolValue("AutoBlock",this,false,"Automatically blocks for you.");
    public BoolValue critCrack = new BoolValue("Particles",this,false,"Fake critical particles.");
    public BoolValue players = new BoolValue("Players",this,false,"Attack players.");
    public BoolValue animals = new BoolValue("Animals",this,false,"Attack animals.");
    public BoolValue mobs = new BoolValue("Mobs",this,false,"Attack mobs.");
    public BoolValue villagers = new BoolValue("Villagers",this,false,"Attack villagers.");
    public BoolValue teams = new BoolValue("Teams",this,false,"Attack your teammates.");
    public BoolValue coolRots = new BoolValue("Rotations",this,false,"Cool perspective rotations.");
    public BoolValue dura = new BoolValue("Dura",this,false,"Breaks armor faster (Tick mode).");
    public BoolValue moreKb = new BoolValue("MoreKB",this,false,"Does more knock back to target.");
    public BoolValue improbable = new BoolValue("Improbable",this,false,"Combined flag gn.");
    public BoolValue targetInfo = new BoolValue("TargetInfo",this,false,"Shows info of target.");
    public PositionValue infoPosition = new PositionValue("InfoPosition",this,15,15,"Target info position.", () -> targetInfo.getValue());
    public ModeValue<KaMode> mode = new ModeValue<>("Mode",this,"Aura methods.",Single.class, TickMode.class, HurtTimeMode.class, RTE2025.class, Legit.class, Streax.class);
    public EnumValue<SortMode> sortMode = new EnumValue<>("SortMode",this,SortMode.class,"Sort methods.");
    public boolean hit;
    public List<EntityLivingBase> targets = new ArrayList<>();
    public EntityLivingBase curTar;
    private ItemStack lastUsedItem;

    public KillAura() {
        super("KillAura", Keyboard.KEY_Y,Category.Combat,"Attacks entities around you.");
        //cps.setCondition(() -> mode.getValue() instanceof Legit);
    }

    public Listener<EventInputAction> onAction = new Listener<>(event -> mode.getValue().onAction(event));
    public Listener<EventTravel> onTravel = new Listener<>(event -> mode.getValue().onTravel(event));

    public Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        setSuffix(mode.getValue().getName());
        targets = PlayerUtils.getLivingList().stream().filter(this::canAttack).collect(Collectors.toList());
        mode.getValue().sortTargets();
        mode.getValue().onPre(event);
    });

    public Listener<EventPostUpdate> onPost = new Listener<>(event -> mode.getValue().onPost(event));

    public Listener<EventTick> onTick = new Listener<>(event -> {
        if (targetInfo.getValue()) {
            TargetInfo.INSTANCE.onTick(curTar);
        }
        mode.getValue().onTick(event);
    });
    public Listener<EventFrame> onFrame = new Listener<>(event -> mode.getValue().onFrame(event));
    public Listener<EventExcuseMeWTF> onBruh = new Listener<>(event -> mode.getValue().bruh(event));
    public Listener<EventPacketReceive> onReceive = new Listener<>(event -> {
        mode.getValue().onReceive(event);
        if (event.getState() == EventPacketReceive.PacketState.POST && event.getPacket() instanceof S2DPacketOpenWindow && !targets.isEmpty()) {
            PlayerUtils.debug("Closed container");
            mc.thePlayer.closeScreen();
        }
    });
    public Listener<EventPacketSend> onSend = new Listener<>(event -> {
        mode.getValue().onSend(event);
        if (event.getState() == EventPacketSend.PacketState.PRE) {
            if (event.getPacket() instanceof C07PacketPlayerDigging c07) {
                if (c07.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                    lastUsedItem = null;
                }
            } else if (event.getPacket() instanceof C0EPacketClickWindow || event.getPacket() instanceof C09PacketHeldItemChange) {
                lastUsedItem = null;
            } else if (event.getPacket() instanceof C08PacketPlayerBlockPlacement c08) {
                if (c08.getPlacedBlockDirection() == 255) {
                    lastUsedItem = c08.getStack();
                }
            }
        }
    });

    public Listener<EventRender2D> onRender2D = new Listener<EventRender2D>(event ->
            TargetInfo.INSTANCE.draw(curTar, event.partialTicks())).filter(filter -> targetInfo.getValue());

    @Override
    public void onDisable() {
        mode.getValue().onDisable();
        hit = false;
        targets.clear();
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        mc.timer.timerSpeed = 1;
        mode.getValue().onEnable();
        TargetInfo.INSTANCE.prevScale = 0;
        TargetInfo.INSTANCE.scale = 0;
        super.onEnable();
    }

    public boolean canAttack(EntityLivingBase ent) {
        boolean player = ent instanceof EntityPlayer && players.getValue();
        boolean animal = ent instanceof EntityAnimal && animals.getValue();
        boolean mob = ent instanceof EntityMob && mobs.getValue();
        boolean villager = ent instanceof EntityVillager && villagers.getValue();
        boolean entities = player || animal || mob || villager;
        boolean strategy = Modules.AUTO_DRINK.isEnabled() && Modules.AUTO_DRINK.drinkStrat.getValue() && Modules.AUTO_DRINK.moveStrat.getValue();
        boolean blockCheck = mc.thePlayer.getDistanceToEntity(ent) <= (strategy ? 20 : range.getValue()+4);
        Fly fly = Modules.FLY;
        Fly.GlideMode glideMode = fly.glideMode.getValue();
        boolean isManual = glideMode == Fly.GlideMode.Manual;
        if (!isManual && fly.isEnabled() && fly.mode.getValue() instanceof OldNCPGlide) {
            blockCheck = canReach(ent);
        }
        //CREDITS TO ERIQ KARANLIX SLOWCHEETAH AXIS FOR CODE
        if (ent instanceof EntityTameable tameable) {
            if (tameable.getOwner() == mc.thePlayer)
                return false;
            if (tameable.isTamed())
                for (Friend friend : FriendManager.getFriends()) {
                    if (friend.name.equalsIgnoreCase(tameable.getOwner().getName()))
                        return false;
                }
        }
        return entities && (teams.getValue() || !mc.thePlayer.isOnSameTeam(ent)) && !FriendManager.isFriend(ent.getName()) && ent.ticksExisted > 5 && ent != mc.thePlayer && mc.thePlayer.getHealth() > 0 && (ent.getHealth() > 0 || mode.getValue() instanceof Legit) && blockCheck;
    }

    public boolean canBlock() {
        return mc.thePlayer != null && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public void reBlock() {
        if (!PlayerUtils.isMoving2() || !Modules.NO_SLOW_DOWN.isEnabled()) {
            PlayerUtils.packet(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
    }

    public void unBlock() {
        PlayerUtils.packet(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
    }

    public void basicAttack(EntityLivingBase curTar) {
        boolean block = (mc.thePlayer.isBlocking() && canBlock());
        if (block) {
            unBlock();
        }
        if (mc.thePlayer.isSprinting()) {
            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        }
        mc.thePlayer.swingItem();
        PlayerUtils.packet(new C02PacketUseEntity(curTar, C02PacketUseEntity.Action.ATTACK));
        if (block) {
            reBlock();
        }
    }

    public boolean canReach() {
        return canReach(curTar);
    }

    public boolean canReach(EntityLivingBase base) {
        if (mc.thePlayer == null || base == null) {
            return false;
        }
        AutoDrink autoDrink = Modules.AUTO_DRINK;
        boolean strategy = autoDrink.isEnabled() && autoDrink.drinkStrat.getValue() && autoDrink.moveStrat.getValue() && autoDrink.moveStrategy.blinkTicks > 0;
        if (strategy) {
            return base.getDistance(autoDrink.lastBlinkX, autoDrink.lastBlinkY, autoDrink.lastBlinkZ) <= range.getValue();
        }
        Fly fly = Modules.FLY;
        Fly.GlideMode glideMode = fly.glideMode.getValue();
        boolean isManual = glideMode == Fly.GlideMode.Manual;
        if (!isManual && fly.isEnabled() && fly.mode.getValue() instanceof OldNCPGlide) {
            double diffX = mc.thePlayer.posX - base.posX;
            double diffY = mc.thePlayer.posY - base.posY;
            double diffZ = mc.thePlayer.posZ - base.posZ;
            double hDist = Math.sqrt(diffX * diffX + diffZ * diffZ);
            return hDist <= range.getValue() && diffY > -2 && diffY < 12;
        }

        double range = this.range.getValue();

        if (base instanceof EntityOtherPlayerMP mp) {
            return mc.thePlayer.getDistance(mp.otherPlayerMPX, mp.otherPlayerMPY, mp.otherPlayerMPZ) <= range;
        }
        return mc.thePlayer.getDistanceToEntity(base) <= range;
    }

    public void block() {
        if (lastUsedItem == null || !mc.thePlayer.getHeldItem().getIsItemStackEqual(lastUsedItem) || !mc.thePlayer.isBlocking()) {
            //PlayerUtils.debug("block");
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
        }
    }

    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().getName());
        super.onSuffixChange();
    }

    public enum SortMode {
        Distance, Health, Angle, Hurts
    }
}
