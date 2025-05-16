package ipana.modules.combat;

import ipana.Ipana;
import ipana.managements.value.values.EnumValue;
import ipana.utils.pathfind.astar.AStar;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.events.EventRender3D;
import ipana.managements.friend.FriendManager;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.Timer;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ipana.modules.combat.TpAura.TpMode.*;
import static ipana.utils.player.PlayerUtils.*;

public class TpAura extends Module {
    EnumValue<TpMode> mode = new EnumValue<>("Mode",this,TpMode.class,"Teleport methods.");

    private BoolValue block = new BoolValue("Block",this,false,"Auto block.");
    public TpAura() {
        super("TpAura", Keyboard.KEY_H,Category.Combat,"Teleport-hit to player.");
    }

    private List<EntityLivingBase> targets = new ArrayList<>();
    private EntityLivingBase curTar;
    private boolean hit;
    private boolean postHit;
    private Timer timer = new Timer();
    private List<Vec3> list = new ArrayList<>();
    private AStar aStar = new AStar();

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        setSuffix(mode.getValue().name());
        if (mc.currentScreen != null) {
            return;
        }
        targets = getTargets();
        if (targets.size() > 0) {
            targets.stream().sorted(Comparator.comparingDouble(ent -> -mc.thePlayer.getDistanceToEntity(ent))).forEach(ent -> curTar = ent);

            event.setYaw(RotationUtils.getRotations(curTar)[0]);
            event.setPitch(RotationUtils.getRotations(curTar)[1]);
            float delay = 0;
            boolean postWait = false;
            switch (mode.getValue()) {
                case Vanilla -> delay = 600;
                case NCP -> {
                    delay = (float) (mc.thePlayer.getDistanceToEntity(curTar) / getBaseMoveSpeed() * 50) * 2f;
                    if (delay < 500) {
                        delay = 500;
                    }
                    event.setCancelPackets(!postHit);
                }
            }

            if (timer.delay(delay)) {
                hit = true;
                timer.reset();
            } else {
                hit = false;
            }
            if (timer.delay(delay-50) && postWait) {
                //event.setYaw(event.getYaw()+ MathUtils.random(-4,5));
                event.setCancelPackets(false);
            }
        } else {
            list.clear();
        }
        postHit = false;
    });
    // events niye yaziyon ne gerek varq: DDDD
    private Listener<EventPostUpdate> onPost = new Listener<EventPostUpdate>(event -> {
        if (hit) {
            postHit = true;
            if (mc.thePlayer.isBlocking()) {
                packet(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
            boolean sprint = mc.thePlayer.isSprinting();
            if (sprint) {
                packet(new C0BPacketEntityAction(mc.thePlayer,C0BPacketEntityAction.Action.STOP_SPRINTING));
            }
            List<Vec3> setPosList = new ArrayList<>();
            mc.thePlayer.swingItem();
            if (mode.getValue() == NCP) {
                //mc.thePlayer.setPosition(lastX,lastY,lastZ);
                PlayerUtils.sendOffset(0,-0.2,0,true);
                boolean yes = false;
                for(double i = 0; i < mc.thePlayer.getDistance(curTar.posX,curTar.posY,curTar.posZ)-1; i += getBaseMoveSpeed()) {
                    float yaw = RotationUtils.getRotations(curTar)[0];
                    double[] c = PlayerUtils.calculate2(i, yaw, 1);
                    final double dX = c[0];
                    final double dZ = c[1];
                    setPosList.add(new Vec3(mc.thePlayer.posX+dX,mc.thePlayer.posY,mc.thePlayer.posZ+dZ));
                    yes=!yes;
                }
                for (Vec3 vec3 : setPosList) {
                    float[] rot = RotationUtils.getRotationFromPosition(curTar.posX, curTar.posZ, curTar.posY, vec3.xCoord, vec3.yCoord, vec3.zCoord);
                    PlayerUtils.send(vec3.xCoord,vec3.yCoord,vec3.zCoord, rot[0], rot[1],true);
                }
            } else if (mode.getValue() == Vanilla){
                teleport();
            }
            packet(new C02PacketUseEntity(curTar, C02PacketUseEntity.Action.ATTACK));
            if (sprint) {
                packet(new C0BPacketEntityAction(mc.thePlayer,C0BPacketEntityAction.Action.START_SPRINTING));
            }
            if (mode.getValue() == Vanilla) {
                Collections.reverse(list);
                for (Vec3 p : list) {
                    PlayerUtils.send(p.xCoord,p.yCoord,p.zCoord,true);
                }
            } else if (mode.getValue() == NCP) {
                if (!setPosList.isEmpty()) {
                    Vec3 last = setPosList.get(setPosList.size() - 1);
                    PlayerUtils.send(last.xCoord,last.yCoord-0.2,last.zCoord,true);
                }
                Collections.reverse(setPosList);
                for (Vec3 vec3 : setPosList) {
                    PlayerUtils.send(vec3.xCoord,vec3.yCoord,vec3.zCoord,true);
                }
                PlayerUtils.sendOffset(0,-0.2,0,true);
            }
        }
        if (block.getValue() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            mc.playerController.sendUseItem(mc.thePlayer,mc.theWorld,mc.thePlayer.getHeldItem());
        }
    }).filter(filter -> targets.size() > 0 && mc.thePlayer.ticksExisted > 100);

    private Listener<EventRender3D> onRender = new Listener<>(event -> {
        for (Vec3 point : list) {
            double posX = point.xCoord - mc.getRenderManager().renderPosX;
            double posY = point.yCoord - mc.getRenderManager().renderPosY;
            double posZ = point.zCoord - mc.getRenderManager().renderPosZ;
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            AxisAlignedBB bb = new AxisAlignedBB(posX-0.5,posY,posZ,posX+0.5,posY+1.8,posZ+1);

            drawOutlineBox(bb);

            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    });

    private void teleport() {
        list.clear();
        for (BlockPos pos : aStar.findPath(new BlockPos(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ),curTar.getPosition(),100)) {
            list.add(new Vec3(pos.x+0.5,pos.y,pos.z));
        }
        for (Vec3 p : list) {
            PlayerUtils.send(p.xCoord,p.yCoord,p.zCoord,true);
        }
    }

    @Override
    public void onDisable() {
        targets.clear();
        list.clear();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        timer.reset();
        super.onEnable();
    }

    private List<EntityLivingBase> getTargets() {
        List<EntityLivingBase> listsssss = new ArrayList<>();
        for (Object o : mc.theWorld.loadedEntityList) {
            if (o instanceof EntityLivingBase) {
                if (canHit((EntityLivingBase)o)) {
                    listsssss.add((EntityLivingBase)o);
                }
            }
        }
        return listsssss;
    }

    private boolean canHit(EntityLivingBase o) {
        if (FriendManager.isFriend(o.getName())) {
            return false;
        }
        if (!(o instanceof EntityPlayer)) {
            return false;
        }
        return o != mc.thePlayer && mc.thePlayer.getDistanceToEntity(o) <= (50) && mc.thePlayer.getHealth() > 0 && o.getHealth() > 0 && !(o instanceof EntityArmorStand);
    }

    private void drawOutlineBox(AxisAlignedBB p_181561_0_)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color((float) Ipana.getClientColor().getRed()/255, (float)Ipana.getClientColor().getGreen()/255, (float)Ipana.getClientColor().getBlue()/255, 1);
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        tessellator.draw();
    }
    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().name());
        super.onSuffixChange();
    }

    enum TpMode{
        Vanilla, NCP
    }
}
