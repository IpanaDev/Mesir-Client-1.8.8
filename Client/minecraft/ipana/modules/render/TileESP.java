package ipana.modules.render;

import ipana.Ipana;
import ipana.events.EventRender3D;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.*;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.awt.*;

public class TileESP extends Module {
    public TileESP() {
        super("TileESP", Keyboard.CHAR_NONE, Category.Render,"PlayerESP for chests,ender chests,spawners.");
    }
    private BoolValue chest = new BoolValue("Chest",this,true,"Render chests.");
    private BoolValue enderChest = new BoolValue("EnderChest",this,true,"Render ender chests.");
    private BoolValue spawner = new BoolValue("Spawners",this,true,"Render spawners.");


    private Listener<EventRender3D> onRender = new Listener<>(event -> {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770,771);
        GlStateManager.disableTexture2D();
        for (TileEntity ent : mc.theWorld.loadedTileEntityList) {
            if (!event.camera().isBoundingBoxInFrustum(new AxisAlignedBB(
                    ent.getPos().getX(),
                    ent.getPos().getY(),
                    ent.getPos().getZ(),
                    ent.getPos().getX()+1,
                    ent.getPos().getY()+1,
                    ent.getPos().getZ()+1))) {
                continue;
            }
            if (!(ent instanceof TileEntityChest) && !(ent instanceof TileEntityMobSpawner) && !(ent instanceof TileEntityDispenser) && !(ent instanceof TileEntityEnderChest)) {
                continue;
            }
            if (ent instanceof TileEntityChest && !chest.getValue()) {
                continue;
            }
            if (ent instanceof TileEntityMobSpawner && !spawner.getValue()) {
                continue;
            }
            if (ent instanceof TileEntityEnderChest && !enderChest.getValue()) {
                continue;
            }

            this.drawEsp(ent, event.partialTicks());
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    });



    private void drawEsp(TileEntity ent, float pTicks) {
        final double x1 = ent.getPos().getX() - mc.getRenderManager().renderPosX;
        final double y1 = ent.getPos().getY() - mc.getRenderManager().renderPosY;
        final double z1 = ent.getPos().getZ() - mc.getRenderManager().renderPosZ;
        final float[] color = this.getColor(ent);
        AxisAlignedBB box = new AxisAlignedBB(x1, y1, z1, x1 + 1.0, y1 + 1.0, z1 + 1.0);
        if (ent instanceof TileEntityChest) {
            final TileEntityChest chest = TileEntityChest.class.cast(ent);
            if (chest.adjacentChestZPos != null) {
                box = new AxisAlignedBB(x1 + 0.0625, y1, z1 + 0.0625, x1 + 0.9375, y1 + 0.875, z1 + 1.9375);
            } else if (chest.adjacentChestXPos != null) {
                box = new AxisAlignedBB(x1 + 0.0625, y1, z1 + 0.0625, x1 + 1.9375, y1 + 0.875, z1 + 0.9375);
            } else {
                if (chest.adjacentChestZNeg != null || chest.adjacentChestXNeg != null) {
                    return;
                }
                box = new AxisAlignedBB(x1 + 0.0625, y1, z1 + 0.0625, x1 + 0.9375, y1 + 0.875, z1 + 0.9375);
            }
        } else if (ent instanceof TileEntityEnderChest) {
            box = new AxisAlignedBB(x1 + 0.0625, y1, z1 + 0.0625, x1 + 0.9375, y1 + 0.875, z1 + 0.9375);
        } else if (ent instanceof TileEntityMobSpawner tileEntityMobSpawner) {
            GlStateManager.pushMatrix();
            GlStateManager.enableTexture2D();
            double x = tileEntityMobSpawner.getPos().getX() - mc.getRenderManager().renderPosX;
            double y = tileEntityMobSpawner.getPos().getY() - mc.getRenderManager().renderPosY;
            double z = tileEntityMobSpawner.getPos().getZ() - mc.getRenderManager().renderPosZ;
            Entity entity = tileEntityMobSpawner.getSpawnerBaseLogic().func_180612_a(mc.theWorld);
            if (entity == null) {
                return;
            }
            String name = entity.getName();
            GlStateManager.translate(x+0.5,y+2,z+0.5);
            float scale = 0.009F;
            double dist = mc.thePlayer.getDistance(tileEntityMobSpawner.getPos().getX(),tileEntityMobSpawner.getPos().getY(),tileEntityMobSpawner.getPos().getZ()) / 5;
            if (dist < 1) {
                dist = 1;
            }
            GlStateManager.scale(-scale * dist, -scale * dist, -scale * dist);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            mc.fontRendererObj.drawStringWithShadow(name,-mc.fontRendererObj.getStringWidth(name)/2.0F,-1, Ipana.getClientColor().getRGB());
            GlStateManager.disableTexture2D();
            GlStateManager.popMatrix();
            box = new AxisAlignedBB(x1, y1, z1, x1 + 1, y1 + 1, z1 + 1);
        }
        drawOutlineBox(box, new Color(color[0], color[1], color[2]));
    }
    private void drawOutlineBox(AxisAlignedBB p_181561_0_,Color color)
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

    private float[] getColor(TileEntity ent) {
        if ((ent instanceof TileEntityChest)) {
            CGui m = Modules.CLICK_GUI;
            return new float[]{Ipana.getClientColor().getRed()/255f, Ipana.getClientColor().getGreen()/255f, Ipana.getClientColor().getBlue()/255f};
        }
        if ((ent instanceof TileEntityEnderChest)) {
            return new float[]{0.3F, 0.0F, 0.3F};
        }
        if ((ent instanceof TileEntityMobSpawner)) {
            return new float[]{0.1F, 0.5F, 0.1F};
        }
        return new float[]{1.0F, 1.0F, 1.0F};
    }
}