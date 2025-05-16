package net.minecraft.client.renderer.entity;

import ipana.Ipana;
import ipana.irc.user.PlayerCosmetics;
import ipana.irc.user.User;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelAmongUs;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderAmongUs extends RendererLivingEntity<AbstractClientPlayer> {

    public RenderAmongUs(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelAmongUs(), 0.5F);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerCape(this));
    }
    public void renderArms() {
        ModelAmongUs model = this.getMainModel();
        model.bipedLeftArm.render(0.0625f);
        model.bipedRightArm.render(0.0625f);
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

    public ModelAmongUs getMainModel() {
        return (ModelAmongUs)super.getMainModel();
    }
    private void setModelVisibilities(final AbstractClientPlayer clientPlayer) {
        final ModelAmongUs modelplayer = this.getMainModel();
        if (clientPlayer.isSpectator()) {
            modelplayer.setInvisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        } else {
            final ItemStack itemstack = clientPlayer.inventory.getCurrentItem();
            modelplayer.setInvisible(true);
            modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.heldItemLeft = 0;
            modelplayer.aimedBow = false;
            modelplayer.isSneak = clientPlayer.isSneaking();
            if (itemstack == null) {
                modelplayer.heldItemRight = 0;
            } else {
                modelplayer.heldItemRight = 1;
                if (clientPlayer.getItemInUseCount() > 0) {
                    final EnumAction enumaction = itemstack.getItemUseAction();
                    if (enumaction == EnumAction.BLOCK) {
                        modelplayer.heldItemRight = 3;
                    } else if (enumaction == EnumAction.BOW) {
                        modelplayer.aimedBow = true;
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
