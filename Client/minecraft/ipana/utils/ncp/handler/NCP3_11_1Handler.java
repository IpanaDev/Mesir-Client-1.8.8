package ipana.utils.ncp.handler;

import ipana.events.EventPacketReceive;
import ipana.events.EventPacketSend;
import ipana.events.EventTick;
import ipana.utils.StringUtil;
import ipana.utils.ncp.combined.Combined;
import ipana.utils.ncp.fight.FightSpeed;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.world.World;
import pisi.unitedmeows.eventapi.event.listener.Listener;


public class NCP3_11_1Handler extends Handler {
    public Combined combined = new Combined();
    public FightSpeed fightSpeed = new FightSpeed();
    private World prevWorld;
    private boolean worldChanged;
    private float lastYaw;

    private boolean onDamage(Entity attacked) {
        boolean cancelled = false;
        long now = System.currentTimeMillis();
        int tick = Minecraft.getRunTick();
        int lastAttackTick = fightSpeed.data().lastAttackedTick;
        EntityOtherPlayerMP mp = null;
        if (attacked instanceof EntityOtherPlayerMP) {
            mp = (EntityOtherPlayerMP) attacked;
        }
        boolean hurtTimeCheck = mp == null || mp.hurtResistantTime <= 10;
        double normalizedMove;
        if (fightSpeed.data().lastAttackedX == Double.MAX_VALUE || tick < lastAttackTick || worldChanged || tick - lastAttackTick > 20) {
            normalizedMove = 0.0;
        } else {
            if (mp != null && hurtTimeCheck) {
                normalizedMove = distance(fightSpeed.data().lastAttackedX, fightSpeed.data().lastAttackedZ, mp.otherPlayerMPX, mp.otherPlayerMPZ);
            } else {
                normalizedMove = distance(fightSpeed.data().lastAttackedX, fightSpeed.data().lastAttackedZ, attacked.posX, attacked.posZ);
            }
        }

        if (attacked.isDead) {
            cancelled = true;
        }

        if (!cancelled && hurtTimeCheck) {
            if (fightSpeed.check(player(), now)) {
                cancelled = true;
                if (fightSpeed.data().speedVL > 50.0) {
                    combined.improbable().checkImprobable(2.0f, now);
                } else {
                    combined.improbable().feed(2.0f, now);
                }
            } else if (normalizedMove > 2 && combined.improbable().checkImprobable(1.0f, now)) {
                cancelled = true;
            }
        }

        if (hurtTimeCheck && combined.checkYawRate(lastYaw, now, worldChanged, fightSpeed.cc().yawRateCheck)) {
            cancelled = true;
        }

        if (mp != null && hurtTimeCheck) {
            fightSpeed.data().lastAttackedTick = Minecraft.getRunTick();
            fightSpeed.data().lastAttackedX = mp.otherPlayerMPX;
            fightSpeed.data().lastAttackedY = mp.otherPlayerMPY;
            fightSpeed.data().lastAttackedZ = mp.otherPlayerMPZ;
        } else {
            fightSpeed.data().lastAttackedTick = Minecraft.getRunTick();
            fightSpeed.data().lastAttackedX = attacked.posX;
            fightSpeed.data().lastAttackedY = attacked.posY;
            fightSpeed.data().lastAttackedZ = attacked.posZ;
        }
        return cancelled;
    }

    private Listener<EventPacketSend> onSend = new Listener<EventPacketSend>(event -> {
        if (event.getPacket() instanceof C02PacketUseEntity attack) {
            if (onDamage(attack.getEntityFromWorld(world()))) {
                event.setCancelled(true);
            }
        } else if (event.getPacket() instanceof C03PacketPlayer c03) {
            if (c03 instanceof C03PacketPlayer.C05PacketPlayerLook || c03 instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
                combined.feedYawRate(c03.getYaw(), System.currentTimeMillis(), worldChanged);
                lastYaw = c03.getYaw();
            }
        } else if (event.getPacket() instanceof C0BPacketEntityAction action) {
            switch (action.getAction()) {
                case START_SPRINTING,STOP_SPRINTING,START_SNEAKING,STOP_SNEAKING -> combined.improbable().feed(0.35f, System.currentTimeMillis());
            }
        }
    }).filter(e -> e.getState() == EventPacketSend.PacketState.PRE);

    private Listener<EventPacketReceive> onReceive = new Listener<EventPacketReceive>(event -> {
        if (event.getPacket() instanceof S18PacketEntityTeleport teleport) {
            if (teleport.getEntityId() == player().getEntityId()) {
                combined.resetYawRate(teleport.getYaw(), System.currentTimeMillis(), true);
            }
        }
    }).filter(e -> e.getState() == EventPacketReceive.PacketState.PRE);

    private Listener<EventTick> onTick = new Listener<>(event -> {
        worldChanged = prevWorld != world();
        prevWorld = world();
    });

    private double distance(double x1, double z1, double x2, double z2) {
        double dx = Math.abs(x1 - x2);
        double dz = Math.abs(z1 - z2);
        return Math.sqrt(dx * dx + dz * dz);
    }

    private EntityPlayerSP player() {
        return Minecraft.getMinecraft().thePlayer;
    }
    private World world() {
        return Minecraft.getMinecraft().theWorld;
    }
}
