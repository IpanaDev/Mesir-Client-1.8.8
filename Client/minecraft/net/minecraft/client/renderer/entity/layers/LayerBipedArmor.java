package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RendererLivingEntity;

public class LayerBipedArmor extends LayerArmorBase<ModelBiped>
{
    public LayerBipedArmor(RendererLivingEntity<?> rendererIn)
    {
        super(rendererIn);
    }

    protected void initArmor()
    {
        this.field_177189_c = new ModelBiped(0.5F);
        this.field_177186_d = new ModelBiped(1.0F);
    }

    protected void setShown(ModelBiped model, int armorSlot)
    {
        model.setInvisible(false);

        switch (armorSlot) {
            case 1 -> {
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
            }
            case 2 -> {
                model.bipedBody.showModel = true;
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
            }
            case 3 -> {
                model.bipedBody.showModel = true;
                model.bipedRightArm.showModel = true;
                model.bipedLeftArm.showModel = true;
            }
            case 4 -> {
                model.bipedHead.showModel = true;
                model.bipedHeadwear.showModel = true;
            }
        }
    }
}
