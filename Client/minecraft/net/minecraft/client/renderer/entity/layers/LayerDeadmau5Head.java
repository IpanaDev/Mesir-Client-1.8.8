package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEla;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;

public class LayerDeadmau5Head implements LayerRenderer<AbstractClientPlayer>
{
    private final RendererLivingEntity<AbstractClientPlayer> playerRenderer;

    public LayerDeadmau5Head(RendererLivingEntity<AbstractClientPlayer> playerRendererIn)
    {
        this.playerRenderer = playerRendererIn;
    }

    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        RenderEla ela = null;
        RenderPlayer player = null;
        if (playerRenderer instanceof RenderPlayer || playerRenderer instanceof RenderEla) {
            if (playerRenderer instanceof RenderPlayer)  {
                player = (RenderPlayer) playerRenderer;
            } else {
                ela = (RenderEla) playerRenderer;
            }
        } else {
            return;
        }
        if (entitylivingbaseIn.getName().equals("deadmau5") && entitylivingbaseIn.hasSkin() && !entitylivingbaseIn.isInvisible())
        {
            this.playerRenderer.bindTexture(entitylivingbaseIn.getLocationSkin());

            for (int i = 0; i < 2; ++i)
            {
                float f = entitylivingbaseIn.prevRotationYaw + (entitylivingbaseIn.rotationYaw - entitylivingbaseIn.prevRotationYaw) * partialTicks - (entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks);
                float f1 = entitylivingbaseIn.prevRotationPitch + (entitylivingbaseIn.rotationPitch - entitylivingbaseIn.prevRotationPitch) * partialTicks;
                GlStateManager.pushMatrix();
                GlStateManager.rotate(f, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f1, 1.0F, 0.0F, 0.0F);
                GlStateManager.translate(0.375F * (float)(i * 2 - 1), 0.0F, 0.0F);
                GlStateManager.translate(0.0F, -0.375F, 0.0F);
                GlStateManager.rotate(-f1, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-f, 0.0F, 1.0F, 0.0F);
                float f2 = 1.3333334F;
                GlStateManager.scale(f2, f2, f2);
                if (ela != null) {
                    ela.getMainModel().renderDeadmau5Head(0.0625F);
                } else {
                    player.getMainModel().renderDeadmau5Head(0.0625F);
                }
                GlStateManager.popMatrix();
            }
        }
    }



    public boolean shouldCombineTextures()
    {
        return true;
    }
}
