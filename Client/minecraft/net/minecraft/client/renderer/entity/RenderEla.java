package net.minecraft.client.renderer.entity;

import ipana.Ipana;
import ipana.irc.user.PlayerCosmetics;
import ipana.irc.user.User;
import ipana.renders.account.Fakekekke;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElaBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderEla  extends RendererLivingEntity<AbstractClientPlayer> {

    public RenderEla(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelElaBase(), 0.5f);
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerCatEars(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
    }

    @Override
    public ModelElaBase getMainModel() {
        return (ModelElaBase)super.getMainModel();
    }

    @Override
    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        setModelVisibilities(entity);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
    protected void preRenderCallback(AbstractClientPlayer entitylivingbaseIn, float partialTickTime)
    {
        User user = Ipana.mainIRC().getUser(entitylivingbaseIn.getName());
        float scale = (float) user.cosmetics().getCosmetic(PlayerCosmetics.CHILD).params()[0];
        float f = 0.9375F*scale;
        GlStateManager.scale(f, f, f);
    }

    private void setModelVisibilities(AbstractClientPlayer clientPlayer) {
        ModelElaBase modelplayer = this.getMainModel();

        if (clientPlayer.isSpectator()) {
            modelplayer.setInvisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        } else {
            boolean justBrr = false;
            if (clientPlayer instanceof Fakekekke) {
                clientPlayer = Ipana.accountManager.player;
                justBrr = true;
            }
            if (clientPlayer == null) {
                return;
            }
            ItemStack itemstack = clientPlayer.inventory.getCurrentItem();
            modelplayer.setInvisible(true);
            modelplayer.bipedHeadwear.showModel = justBrr || clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.bipedBodyWear.showModel = justBrr || clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
            modelplayer.bipedLeftLegwear.showModel = justBrr || clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedRightLegwear.showModel = justBrr || clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedLeftArmwear.showModel = justBrr || clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedRightArmwear.showModel = justBrr || clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);

            modelplayer.heldItemLeft = 0;
            modelplayer.aimedBow = false;
            modelplayer.isSneak = clientPlayer.isSneaking();

            if (itemstack == null) {
                modelplayer.heldItemRight = 0;
            } else {
                modelplayer.heldItemRight = 1;

                if (clientPlayer.getItemInUseCount() > 0) {
                    EnumAction enumaction = itemstack.getItemUseAction();

                    if (enumaction == EnumAction.BLOCK) {
                        modelplayer.heldItemRight = 3;
                    } else if (enumaction == EnumAction.BOW) {
                        modelplayer.aimedBow = true;
                    } else if (enumaction == EnumAction.EAT || enumaction == EnumAction.DRINK) {
                        modelplayer.heldItemRight = 4;
                    }
                }
            }
        }
    }
    @Override
    protected ResourceLocation getEntityTexture(AbstractClientPlayer entity) {
        User user = Ipana.mainIRC().getUser(entity.getName());
        return (ResourceLocation) user.cosmetics().getCosmetic(PlayerCosmetics.MODELS).params()[1];
    }
}
