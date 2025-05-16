package net.minecraft.client.renderer.entity.layers;

import ipana.Ipana;
import ipana.irc.user.PlayerCosmetics;
import ipana.renders.ingame.cosmetics.CosmeticsGui;
import ipana.utils.shader.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderAmongUs;
import net.minecraft.client.renderer.entity.RenderEla;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.bok.SmoothCapeRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class LayerCape implements LayerRenderer<AbstractClientPlayer> {
    private final RendererLivingEntity<AbstractClientPlayer> playerRenderer;
    private long startMS;
    private static int partCount = SmoothCapeRenderer.layerCount;
    private SmoothCapeRenderer smoothCapeRenderer = new SmoothCapeRenderer();

    public LayerCape(RendererLivingEntity<AbstractClientPlayer> playerRendererIn) {
        this.playerRenderer = playerRendererIn;
        this.startMS = System.currentTimeMillis();
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        RenderEla ela = null;
        RenderPlayer player = null;
        RenderAmongUs amongUs = null;
        var amongus = false;
        if (this.playerRenderer instanceof RenderPlayer || this.playerRenderer instanceof RenderEla || this.playerRenderer instanceof RenderAmongUs) {
            if (this.playerRenderer instanceof RenderPlayer renderPlayer) {
                player = renderPlayer;
            } else {
                if (this.playerRenderer instanceof RenderAmongUs renderAmongUs) {
                    amongus = true;
                    amongUs = renderAmongUs;
                } else {
                    ela = (RenderEla) this.playerRenderer;
                }
            }
        } else return;
        var user = Ipana.mainIRC().getUser(entitylivingbaseIn.getName());
        var capeName = String.valueOf(user.cosmetics().getCosmetic(1).params()[0]);
        var cape = !"none".equals(capeName) && !"".equals(capeName);
        if (entitylivingbaseIn.hasPlayerInfo() && !entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE) && entitylivingbaseIn.getLocationCape() != null || cape) {
            var capeType = PlayerCosmetics.CapeType.valueOf(String.valueOf(user.cosmetics().getCosmetic(1).params()[2]));
            if (capeType == PlayerCosmetics.CapeType.PHYSICS) return;
            var out = false;
            var usedShaders = false;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (cape) {
                var shader = ShaderManager.getInstance();
                var capeTexture = (ResourceLocation) user.cosmetics().getCosmetic(1).params()[3];
                if (capeType == PlayerCosmetics.CapeType.LOCAL) {
                    this.playerRenderer.bindTexture(capeTexture);
                } else if (capeType == PlayerCosmetics.CapeType.SHADERS) {
                    usedShaders = true;
                    var shaderName = (String) user.cosmetics().getCosmetic(1).params()[1];
                    shader.loadShader(shaderName);
                    shader.loadData(shaderName, "millis", (int) (System.currentTimeMillis() - this.startMS));
                    this.playerRenderer.bindTexture(capeTexture);
                } else if (capeType == PlayerCosmetics.CapeType.URL) {
                    this.playerRenderer.bindTexture(capeTexture);
                }
                out = true;
            } else
            if (entitylivingbaseIn.hasPlayerInfo() && !entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE) && entitylivingbaseIn.getLocationCape() != null) {
                this.playerRenderer.bindTexture(entitylivingbaseIn.getLocationCape());
                out = true;
            }
            if (out) {
                GlStateManager.pushMatrix();
                if (amongus) {
                    var asdsad = 0.25D;
                    GlStateManager.scale(asdsad, asdsad, asdsad);
                }
                if (Ipana.cosmeticsGUI == null) {
                    Ipana.cosmeticsGUI = new CosmeticsGui();
                }
                var renderSmoothCape = Ipana.cosmeticsGUI.physicsCape && user != Ipana.mainIRC().NULL_USER;
                GlStateManager.translate(amongus ? -0.0625F : 0.0F, amongus ? -1.15F : 0.0F,
                        amongus ? 0.75F : entitylivingbaseIn.getCurrentArmor(2) != null ? renderSmoothCape ? 0.05F : 0.2 : renderSmoothCape ? 0 : 0.125F);
                var d0 = entitylivingbaseIn.prevChasingPosX + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * partialTicks
                        - (entitylivingbaseIn.prevPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * partialTicks);
                var d1 = entitylivingbaseIn.prevChasingPosY + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * partialTicks
                        - (entitylivingbaseIn.prevPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * partialTicks);
                var d2 = entitylivingbaseIn.prevChasingPosZ + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * partialTicks
                        - (entitylivingbaseIn.prevPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * partialTicks);
                var f = entitylivingbaseIn.prevRotationYaw + (entitylivingbaseIn.rotationYaw - entitylivingbaseIn.prevRotationYaw) * partialTicks;
                var notRealEntity = entitylivingbaseIn.getEntityId() < 0;
                double d3 = notRealEntity ? 0.0F : MathHelper.sin(f * 3.1415927F / 180.0F);
                double d4 = notRealEntity ? 0.0F : -MathHelper.cos(f * 3.1415927F / 180.0F);
                var f1 = (float) d1 * 10.0F;
                f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
                var f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                var f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }
                if (f2 > 165.0F) {
                    f2 = 165.0F;
                }
                var f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
                f1 += MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6.0F)
                        * 32.0F * f4;
                if (entitylivingbaseIn.isSneaking()) {
                    f1 += 25.0F;
                    if (!renderSmoothCape) {
                        GlStateManager.translate(0.0F, 0.142F, -0.0178F);
                    }
                }
                var angle = 6.0F + f2 / 2.0F + f1;
                if (!renderSmoothCape) {
                    GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
                }
                if (!amongus && !renderSmoothCape) {
                    GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
                }
                if (!renderSmoothCape) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }
                if (renderSmoothCape) {
                    var isLightningEnabled = GlStateManager.lightingState.state();
                    GlStateManager.disableLighting();
                    entitylivingbaseIn.updateSimulation(entitylivingbaseIn, partCount);
                    smoothCapeRenderer.renderSmoothCape(this, entitylivingbaseIn, partialTicks);
                    if (isLightningEnabled) {
                        GlStateManager.enableLighting();
                    }
                } else {
                    if (amongUs != null) {
                        amongUs.getMainModel().renderCape(0.0625F);
                    } else if (ela != null) {
                        ela.getMainModel().renderCape(0.0625F);
                    } else {
                        player.getMainModel().renderCape(0.0625F);
                    }
                }
                if (usedShaders) {
                    ShaderManager.getInstance().stop((String) user.cosmetics().getCosmetic(1).params()[1]);
                }
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    public float interpolate(float yaw1, float yaw2, float percent) {
        var rotation = (yaw1 + (yaw2 - yaw1) * percent) % 360.0F;
        if (rotation < 0.0F) {
            rotation += 360.0F;
        }
        return rotation;
    }

    public float getWind(AbstractClientPlayer player, int part) {
        var i = 5;
        var highlightedPart = System.currentTimeMillis() / i % 360;
        var relativePart = (float) (part + 1) / partCount;
        //return Safe.GET.user().isGhost() ? 0 : (float) (Math.sin(Math.toRadians(relativePart * 360 - highlightedPart)) * i);
        return 0;
    }
}
