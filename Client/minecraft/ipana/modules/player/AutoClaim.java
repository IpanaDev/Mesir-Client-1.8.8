package ipana.modules.player;


import ipana.Ipana;
import ipana.events.EventMouse;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.events.EventRender3D;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import pisi.unitedmeows.eventapi.event.listener.Listener;

public class AutoClaim extends Module {
    public AutoClaim() {
        super("AutoClaim", 0, Category.Player,"Claims automatically");
    }

    private BlockPos pos1 = BlockPos.ORIGIN;
    private BlockPos pos2 = BlockPos.ORIGIN;
    private BlockPos target = BlockPos.ORIGIN;
    private int state;

    @Override
    public void onEnable() {
        pos1 = BlockPos.ORIGIN;
        pos2 = BlockPos.ORIGIN;
        target = BlockPos.ORIGIN;
        state = 0;
        super.onEnable();
    }

    private Listener<EventMouse> onMouse = new Listener<>(event -> {
        BlockPos mousePos = mc.objectMouseOver.getBlockPos();
        if (event.getKey() == 3) {
            pos1 = mousePos;
        } else if (event.getKey() == 4) {
            pos2 = mousePos;
        }

    });
    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        if (pos1 == BlockPos.ORIGIN || pos2 == BlockPos.ORIGIN) {
            return;
        }
        target = state == 0 ? pos1 : pos2;
        float[] rot = RotationUtils.getRotationFromPosition(target.getX() + 0.5, target.getZ() + 0.5, target.getY());
        event.setYaw(rot[0]);
        event.setPitch(rot[1]+0.05f);
        mc.thePlayer.rotationYaw = event.getYaw();
        mc.thePlayer.rotationYawHead = event.getYaw();

        mc.thePlayer.rotationPitch = event.getPitch();
        mc.thePlayer.rotationPitchHead = event.getPitch();
    }).filter(filter -> mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemTool);

    private Listener<EventPostUpdate> onPost = new Listener<EventPostUpdate>(event -> {
        if (pos1 == BlockPos.ORIGIN || pos2 == BlockPos.ORIGIN) {
            return;
        }
        mc.entityRenderer.getMouseOver(1f);
        if (rayTrace(target)) {
            PlayerUtils.debug("clicked: "+target);
            mc.rightClickMouse();
            state++;
        }
        if (state >= 2) {
            this.toggle();
        }
    }).filter(filter -> mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemTool);

    private Listener<EventRender3D> onRender3D = new Listener<>(event -> {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GL11.glLineWidth(1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        drawOutlineBox(new AxisAlignedBB(pos1.getX() - mc.getRenderManager().renderPosX, pos1.getY() - mc.getRenderManager().renderPosY, pos1.getZ() - mc.getRenderManager().renderPosZ, pos1.getX() + 1.0D - mc.getRenderManager().renderPosX, pos1.getY() + 1.0D - mc.getRenderManager().renderPosY, pos1.getZ() + 1.0D - mc.getRenderManager().renderPosZ));
        drawOutlineBox(new AxisAlignedBB(pos2.getX() - mc.getRenderManager().renderPosX, pos2.getY() - mc.getRenderManager().renderPosY, pos2.getZ() - mc.getRenderManager().renderPosZ, pos2.getX() + 1.0D - mc.getRenderManager().renderPosX, pos2.getY() + 1.0D - mc.getRenderManager().renderPosY, pos2.getZ() + 1.0D - mc.getRenderManager().renderPosZ));

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    });
    private void drawOutlineBox(AxisAlignedBB p_181561_0_) {
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

    private boolean rayTrace(BlockPos block) {
        return mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.getBlockPos().equals(block);
    }
}