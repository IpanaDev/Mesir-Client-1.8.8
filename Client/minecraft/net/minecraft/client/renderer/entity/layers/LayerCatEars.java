package net.minecraft.client.renderer.entity.layers;

import ipana.Ipana;
import ipana.irc.user.User;
import ipana.managements.module.Modules;
import ipana.utils.shader.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.model.ModelAmongUs;
import net.minecraft.client.model.ModelElaBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderAmongUs;
import net.minecraft.client.renderer.entity.RenderEla;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import static ipana.irc.user.PlayerCosmetics.EARS;

public class LayerCatEars implements LayerRenderer<AbstractClientPlayer> {
    private final RendererLivingEntity<AbstractClientPlayer> playerRenderer;
    private final long ms;
    private static Minecraft mc = Minecraft.getMinecraft();
    
    public LayerCatEars(final RendererLivingEntity<AbstractClientPlayer> playerRendererIn) {
        this.ms = System.currentTimeMillis();
        this.playerRenderer = playerRendererIn;
    }

    @Override
    public void doRenderLayer(final AbstractClientPlayer clientPlayer, final float p_177166_2_, final float p_177166_3_, final float p_177166_4_, final float p_177166_5_, final float p_177166_6_,
                              final float p_177166_7_, final float p_177166_8_) {
        RenderEla ela = null;
        RenderPlayer player = null;
        RenderAmongUs amongUs = null;
        boolean kankaXDDD = false;
        if (playerRenderer instanceof RenderPlayer || playerRenderer instanceof RenderEla || playerRenderer instanceof RenderAmongUs) {
            if (playerRenderer instanceof RenderPlayer playerRenderer)  {
                player =  playerRenderer;
            } else if (playerRenderer instanceof RenderAmongUs playerRenderer)  {
                amongUs = playerRenderer;
                kankaXDDD = true;
            } else {
                ela = (RenderEla) playerRenderer;
            }
        } else {
            return;
        }
        if (clientPlayer instanceof EntityPlayerSP && Modules.COOL_PERSPECTIVE.isEnabled() && mc.gameSettings.thirdPersonView == 0)
            return;
        User user = Ipana.mainIRC().getUser(clientPlayer.getName());
        int color = user == null ? -2173 : (int) (user.cosmetics().getCosmetic(EARS).params()[0]);
        if (!(mc.currentScreen instanceof GuiInventory) || clientPlayer != mc.thePlayer) {
            if (user == Ipana.mainIRC().self() && color != -2173 || (user != Ipana.mainIRC().self() && user != null && user.cosmetics().doesPlayerHave(EARS))) {
                boolean isShaders = color == 2173;
                GlStateManager.pushMatrix();
                if (clientPlayer.isSneaking()) {
                    GlStateManager.translate(0.0F, 0.3F, 0.0F);
                }
                final float f = mc.currentScreen != null ? 1F : mc.timer.renderPartialTicks;
                final float f2 = getFirstRotationX(clientPlayer, f);
                final float f3 = getSecondRotationX(clientPlayer, f);
                float yaw = 0F;
                if (kankaXDDD) {
                    ModelAmongUs mainModel = (amongUs).getMainModel();
                    yaw = (float) (mainModel.body.rotateAngleY * (180 / Math.PI)); // netHeadYaw / (180F / (float) Math.PI)
                }
                GlStateManager.rotate(kankaXDDD ? yaw : f2, 0.0F, 1.0F, 0.0F);
                if (!kankaXDDD) {
                    GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
                }
                if (isShaders) {
                    final ShaderManager shaderManager = ShaderManager.getInstance();
                    shaderManager.loadShader("rainbow");
                    shaderManager.loadData("rainbow", "amount", (this.ms - System.currentTimeMillis()) / 2000.0F);
                    shaderManager.loadData("rainbow", "offset", 1.0F);
                    shaderManager.loadData("rainbow", "yCount", 0.0F);
                    shaderManager.loadData("rainbow", "direction", 0);
                } else {
                    float a = (float)(color >> 24 & 255) / 255.0F;
                    float r = (float)(color >> 16 & 255) / 255.0F;
                    float g = (float)(color >> 8 & 255) / 255.0F;
                    float b = (float)(color & 255) / 255.0F;
                    GlStateManager.color(r,g,b,a);
                }
                mc.getTextureManager().bindTexture(new ResourceLocation("mesir/void.png"));
                if(amongUs != null) {
                    ModelAmongUs mainModel = amongUs.getMainModel();
                    GlStateManager.translate(-.02, -.085, 0);
                    if (clientPlayer.isSneaking()) {
                        GlStateManager.translate(0.0F, -.3F, 0.0F);
                    }
                    float scale = 1 / 16F;
                    float ez = .5F;
                    GlStateManager.scale(ez, ez, ez);
                    mainModel.leftEarBottom.render(scale);
                    mainModel.leftEarRearTop.render(scale);
                    mainModel.leftEarRearLayer1.render(scale);
                    mainModel.leftEarRearBottom.render(scale);
                    mainModel.leftEarLayer1.render(scale);
                    mainModel.leftEarTop.render(scale);
                    mainModel.leftEarLayer3.render(scale);
                    mainModel.leftEarLayer2.render(scale);
                    mainModel.rightEarBottom.render(scale);
                    mainModel.rightEarLayer1.render(scale);
                    mainModel.rightEarRearTop.render(scale);
                    mainModel.rightEarRearLayer1.render(scale);
                    mainModel.rightEarRearBottom.render(scale);
                    mainModel.rightEarLayer2.render(scale);
                    mainModel.rightEarTop.render(scale);
                    mainModel.rightEarLayer3.render(scale);
                }
                else if (ela != null) {
                    ModelElaBase mainModel = ela.getMainModel();
                    mainModel.leftEarBottom.render(0.0635F);
                    mainModel.leftEarRearTop.render(0.0635F);
                    mainModel.leftEarRearLayer1.render(0.0635F);
                    mainModel.leftEarRearBottom.render(0.0635F);
                    mainModel.leftEarLayer1.render(0.0635F);
                    mainModel.leftEarTop.render(0.0635F);
                    mainModel.leftEarLayer3.render(0.0635F);
                    mainModel.leftEarLayer2.render(0.0635F);
                    mainModel.rightEarBottom.render(0.0635F);
                    mainModel.rightEarLayer1.render(0.0635F);
                    mainModel.rightEarRearTop.render(0.0635F);
                    mainModel.rightEarRearLayer1.render(0.0635F);
                    mainModel.rightEarRearBottom.render(0.0635F);
                    mainModel.rightEarLayer2.render(0.0635F);
                    mainModel.rightEarTop.render(0.0635F);
                    mainModel.rightEarLayer3.render(0.0635F);
                } else {
                    ModelPlayer mainModel = player.getMainModel();
                    mainModel.leftEarBottom.render(0.0635F);
                    mainModel.leftEarRearTop.render(0.0635F);
                    mainModel.leftEarRearLayer1.render(0.0635F);
                    mainModel.leftEarRearBottom.render(0.0635F);
                    mainModel.leftEarLayer1.render(0.0635F);
                    mainModel.leftEarTop.render(0.0635F);
                    mainModel.leftEarLayer3.render(0.0635F);
                    mainModel.leftEarLayer2.render(0.0635F);
                    mainModel.rightEarBottom.render(0.0635F);
                    mainModel.rightEarLayer1.render(0.0635F);
                    mainModel.rightEarRearTop.render(0.0635F);
                    mainModel.rightEarRearLayer1.render(0.0635F);
                    mainModel.rightEarRearBottom.render(0.0635F);
                    mainModel.rightEarLayer2.render(0.0635F);
                    mainModel.rightEarTop.render(0.0635F);
                    mainModel.rightEarLayer3.render(0.0635F);
                }
                if (isShaders) {
                    ShaderManager.getInstance().stop("rainbow");
                }
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    public static float getFirstRotationX(final AbstractClientPlayer clientPlayer, final float partialTicks) {
        if (clientPlayer == mc.thePlayer) {
            final float f1 = interpolateRotation(mc.thePlayer.prevRenderYawOffset, mc.thePlayer.renderYawOffset, partialTicks);
            final float f4 = interpolateRotation(mc.thePlayer.prevRotationYawHead, mc.thePlayer.rotationYawHead, partialTicks);
            final float f5 = f4 - f1;
            if (clientPlayer.isRiding() && clientPlayer.ridingEntity instanceof final EntityLivingBase entitylivingbase) {
                double angle;
                final double head = interpolateRotation(clientPlayer.prevRotationYawHead, clientPlayer.rotationYawHead, partialTicks);
                angle = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                double wrapped = MathHelper.wrapAngleTo180_double(head - angle);
                if (wrapped < -85.0D) {
                    wrapped = -85.0D;
                }
                if (wrapped >= 85.0D) {
                    wrapped = 85.0D;
                }
                angle = head - wrapped;
                if (wrapped * wrapped > 2500.0D) {
                    angle += wrapped * 0.20000000298023224D;
                }
                return (float) Math.toRadians(angle);
            }
            if (clientPlayer.isRiding() && clientPlayer.ridingEntity != null) {
                if (mc.thePlayer.rotationYaw <= 45.0F && mc.thePlayer.rotationYaw >= -45.0F) return mc.thePlayer.rotationYawHead;
                final double head = interpolateRotation(clientPlayer.prevRotationYawHead, clientPlayer.rotationYawHead, partialTicks);
                double wrapped = MathHelper.wrapAngleTo180_double(head);
                if (wrapped < -85.0D) {
                    wrapped = -85.0D;
                }
                if (wrapped >= 85.0D) {
                    wrapped = 85.0D;
                }
                return (float) Math.toRadians(wrapped);
            }
            return f5;
        }
        float f = interpolateRotation(clientPlayer.prevRenderYawOffset, clientPlayer.renderYawOffset, partialTicks);
        final float f2 = interpolateRotation(clientPlayer.prevRotationYawHead, clientPlayer.rotationYawHead, partialTicks);
        float f3 = f2 - f;
        if (clientPlayer.isRiding() && clientPlayer.ridingEntity instanceof final EntityLivingBase entitylivingbase) {
            f = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
            f3 = f2 - f;
            float f4 = MathHelper.wrapAngleTo180_float(f3);
            if (f4 < -85.0F) {
                f4 = -85.0F;
            }
            if (f4 >= 85.0F) {
                f4 = 85.0F;
            }
        }
        return f3;
    }

    public static float getSecondRotationX(final AbstractClientPlayer clientPlayer, final float partialTicks) {
        final boolean test = clientPlayer.isRiding();
        if (test) return clientPlayer.rotationPitch;
        if (clientPlayer == mc.thePlayer) {
            return interpolateRotation(mc.thePlayer.prevRotationPitchHead, mc.thePlayer.rotationPitchHead, partialTicks);
        }
        return clientPlayer.prevRotationPitch + (clientPlayer.rotationPitch - clientPlayer.prevRotationPitch) * partialTicks;
    }

    public static float interpolateRotation(final float par1, final float par2, final float par3) {
        float f;
        f = par2 - par1;
        while (f < -180.0F) {
            f += 360.0F;
        }
        while (f >= 180.0F) {
            f -= 360.0F;
        }
        return par1 + par3 * f;
    }
}