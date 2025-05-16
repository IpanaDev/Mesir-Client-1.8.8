package ipana.modules.movement.modes.fly;

import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.modules.movement.Fly;
import ipana.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.MathHelper;
import optifine.MathUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Vehicle extends FlyMode{
    public Vehicle(Fly parent) {
        super("Vehicle", parent);
    }

    private Entity entity;
    private List<Entity> entities;
    private boolean naber;

    @Override
    public void onPre(EventPreUpdate event) {
        entities = mc.theWorld.loadedEntityList.stream().filter(this::isVehicle).collect(Collectors.toList());
        if (!entities.isEmpty()) {
            entity = entities.get(0);
            //ongorudn
            if (!PlayerUtils.isMoving2()) {
                event.setYaw(event.getYaw()+(naber ? 0 : -180));
            } else {
                double forward = mc.thePlayer.movementInput.moveForward;
                double strafe = mc.thePlayer.movementInput.moveStrafe;
                float yaw = mc.thePlayer.rotationYaw;
                yaw = MathHelper.wrapAngleTo180_float(yaw);
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                if (forward < 0) {
                    yaw-=180;
                } else if (forward == 0) {
                    if (strafe > 0.0D) {
                        yaw -= 135;
                    } else if (strafe < 0.0D) {
                        yaw += 135;
                    }
                }
                event.setYaw(yaw);
            }
        }
        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        if (!entities.isEmpty()) {
            if (mc.gameSettings.keyBindJump.pressed) {
                for (int i = 0; i < 2; i++) {
                    PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    PlayerUtils.packet(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
                }
            } else if (!mc.gameSettings.keyBindSneak.pressed) {
                if (entity instanceof EntityBoat) {
                    if (mc.thePlayer.ticksExisted % 2 == 0 && mc.thePlayer.ticksExisted % 5 != 0) {
                        PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                        PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        PlayerUtils.packet(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
                        naber=!naber;
                    }
                } else {
                    if (mc.thePlayer.ticksExisted % 5 == 0) {
                        for (int i = 0; i < 2; i++) {
                            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                            PlayerUtils.packet(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
                        }
                        naber=!naber;
                    }
                }
            }
        }
        super.onPost(event);
    }

    private boolean isVehicle(Entity entity) {
        return (entity instanceof EntityBoat || entity instanceof EntityMinecart) && mc.thePlayer.ridingEntity == entity;
    }
}
