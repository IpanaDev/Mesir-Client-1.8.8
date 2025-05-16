package net.minecraft.client.model;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

public class ModelPlayer extends ModelBiped {
    public ModelRenderer bipedLeftArmwear;
    public ModelRenderer bipedRightArmwear;
    public ModelRenderer bipedRightArmFP;
    public ModelRenderer bipedRightArmwearFP;
    public ModelRenderer bipedLeftLegwear;
    public ModelRenderer bipedRightLegwear;
    public ModelRenderer bipedBodyWear;
    private ModelRenderer bipedCape;
    private ModelRenderer bipedDeadmau5Head;
    private boolean smallArms;
    public ModelRenderer leftEarBottom;
    public ModelRenderer leftEarRearTop;
    public ModelRenderer leftEarRearLayer1;
    public ModelRenderer leftEarRearBottom;
    public ModelRenderer leftEarLayer1;
    public ModelRenderer leftEarTop;
    public ModelRenderer leftEarLayer3;
    public ModelRenderer leftEarLayer2;
    public ModelRenderer rightEarBottom;
    public ModelRenderer rightEarLayer1;
    public ModelRenderer rightEarRearTop;
    public ModelRenderer rightEarRearLayer1;
    public ModelRenderer rightEarRearBottom;
    public ModelRenderer rightEarLayer2;
    public ModelRenderer rightEarTop;
    public ModelRenderer rightEarLayer3;

    public ModelPlayer(float p_i46304_1_, boolean p_i46304_2_, int width, int height) {
        super(p_i46304_1_, 0.0F, width, height);
        initModels(p_i46304_1_,p_i46304_2_);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
    {
        super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
        GlStateManager.pushMatrix();
        if (this.isChild) {
            float f = 2.0F;
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
        } else {
            if (entityIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
        }
        /*
        if (!bipedLeftLegwear.compiled || !bipedRightLegwear.compiled || !bipedLeftArmwear.compiled || !bipedRightArmwear.compiled || !bipedBodyWear.compiled) {
            buffer.clear();
            bipedLeftLegwear.compileDisplayList(0.0625f);
            bipedRightLegwear.compileDisplayList(0.0625f);
            bipedLeftArmwear.compileDisplayList(0.0625f);
            bipedRightArmwear.compileDisplayList(0.0625f);
            bipedBodyWear.compileDisplayList(0.0625f);
            buffer.put(bipedLeftLegwear.displayList);
            buffer.put(bipedRightLegwear.displayList);
            buffer.put(bipedLeftArmwear.displayList);
            buffer.put(bipedRightArmwear.displayList);
            buffer.put(bipedBodyWear.displayList);
        }
        buffer.position(0);
        GL11.glCallLists(buffer);

         */


        this.bipedLeftLegwear.render(scale);
        this.bipedRightLegwear.render(scale);
        this.bipedLeftArmwear.render(scale);
        this.bipedRightArmwear.render(scale);
        this.bipedBodyWear.render(scale);
        GlStateManager.popMatrix();
    }

    public void renderDeadmau5Head(float p_178727_1_) {
        copyModelAngles(this.bipedHead, this.bipedDeadmau5Head);
        this.bipedDeadmau5Head.rotationPointX = 0.0F;
        this.bipedDeadmau5Head.rotationPointY = 0.0F;
        this.bipedDeadmau5Head.render(p_178727_1_);
    }

    public void renderCape(float p_178728_1_)
    {
        this.bipedCape.render(p_178728_1_);
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn)
    {
        super.setRotationAngles(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, entityIn);
        copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
        copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
        copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
        copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
        copyModelAngles(this.bipedBody, this.bipedBodyWear);
    }

    public void renderRightArm()
    {
        this.bipedRightArm.render(0.0625F);
        this.bipedRightArmwear.render(0.0625F);
    }

    public void renderRightArmFP() {
        if (this.bipedRightArmFP != null && this.bipedRightArmwearFP != null) {
            this.bipedRightArmFP.render(0.0625F);
            this.bipedRightArmwearFP.render(0.0625F);
        }
    }

    public void renderLeftArm()
    {
        this.bipedLeftArm.render(0.0625F);
        this.bipedLeftArmwear.render(0.0625F);
    }

    public void setInvisible(boolean invisible)
    {
        super.setInvisible(invisible);
        this.bipedLeftArmwear.showModel = invisible;
        this.bipedRightArmwear.showModel = invisible;
        this.bipedLeftLegwear.showModel = invisible;
        this.bipedRightLegwear.showModel = invisible;
        this.bipedBodyWear.showModel = invisible;
        this.bipedCape.showModel = invisible;
        this.bipedDeadmau5Head.showModel = invisible;
    }

    public void postRenderArm(float scale)
    {
        if (this.smallArms)
        {
            ++this.bipedRightArm.rotationPointX;
            this.bipedRightArm.postRender(scale);
            --this.bipedRightArm.rotationPointX;
        }
        else
        {
            this.bipedRightArm.postRender(scale);
        }
    }

    public void initModels(float p_i46304_1_, boolean p_i46304_2_) {
        this.smallArms = p_i46304_2_;
        this.bipedDeadmau5Head = new ModelRenderer(this, 24, 0);
        this.bipedDeadmau5Head.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, p_i46304_1_);
        this.bipedCape = new ModelRenderer(this, 0, 0);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, p_i46304_1_);

        if (p_i46304_2_)
        {
            this.bipedLeftArm = new ModelRenderer(this, 32, 48);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
            this.bipedRightArm = new ModelRenderer(this, 40, 16);
            this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);

            this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_ + 0.25F);
            this.bipedLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
            this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_ + 0.25F);
            this.bipedRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);

            this.bipedRightArmFP = new ModelRenderer(this, 40, 16);
            this.bipedRightArmFP.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_, 1 , 4);
            this.bipedRightArmFP.setRotationPoint(-5.0F, 2.5F, 0.0F);
            this.bipedRightArmwearFP = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwearFP.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_ + 0.25F, 1, 4);
            this.bipedRightArmwearFP.setRotationPoint(-5.0F, 2.5F, 10.0F);
        }
        else
        {
            this.bipedLeftArm = new ModelRenderer(this, 32, 48);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F);
            this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F);
            this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);

            this.bipedRightArmFP = new ModelRenderer(this, 40, 16);
            this.bipedRightArmFP.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, p_i46304_1_, 1 , 4);
            this.bipedRightArmFP.setRotationPoint(-5.0F, 2.0F, 0.0F);
            this.bipedRightArmwearFP = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwearFP.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F, 1, 4);
            this.bipedRightArmwearFP.setRotationPoint(-5.0F, 2.0F, 10.0F);
        }

        this.bipedLeftLeg = new ModelRenderer(this, 16, 48);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, p_i46304_1_);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F);
        this.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedRightLegwear = new ModelRenderer(this, 0, 32);
        this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F);
        this.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedBodyWear = new ModelRenderer(this, 16, 32);
        this.bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, p_i46304_1_ + 0.25F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.leftEarBottom = new ModelRenderer(this, 0, 0);
        this.leftEarBottom.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1);
        this.leftEarBottom.setRotationPoint(4.0F, -8.0F, 0.0F);
        this.leftEarBottom.setTextureSize(64, 32);
        this.leftEarBottom.mirror = true;
        this.leftEarRearTop = new ModelRenderer(this, 0, 16);
        this.leftEarRearTop.addBox(0.0F, -3.0F, 1.0F, 1, 1, 1);
        this.leftEarRearTop.setRotationPoint(4.0F, -8.0F, 0.0F);
        this.leftEarRearTop.setTextureSize(64, 32);
        this.leftEarRearTop.mirror = true;
        this.leftEarRearLayer1 = new ModelRenderer(this, 0, 14);
        this.leftEarRearLayer1.addBox(-1.0F, -2.0F, 1.0F, 2, 1, 1);
        this.leftEarRearLayer1.setRotationPoint(4.0F, -8.0F, 0.0F);
        this.leftEarRearLayer1.setTextureSize(64, 32);
        this.leftEarRearLayer1.mirror = true;
        this.leftEarRearBottom = new ModelRenderer(this, 0, 12);
        this.leftEarRearBottom.addBox(-2.0F, -1.0F, 1.0F, 3, 1, 1);
        this.leftEarRearBottom.setRotationPoint(4.0F, -8.0F, 0.0F);
        this.leftEarRearBottom.setTextureSize(64, 32);
        this.leftEarRearBottom.mirror = true;
        this.leftEarLayer1 = new ModelRenderer(this, 0, 2);
        this.leftEarLayer1.addBox(-3.0F, -1.0F, 0.0F, 5, 1, 1);
        this.leftEarLayer1.setRotationPoint(4.0F, -8.0F, 0.0F);
        this.leftEarLayer1.setTextureSize(64, 32);
        this.leftEarLayer1.mirror = true;
        this.leftEarTop = new ModelRenderer(this, 0, 8);
        this.leftEarTop.addBox(0.0F, -4.0F, 0.0F, 1, 1, 1);
        this.leftEarTop.setRotationPoint(4.0F, -8.0F, 0.0F);
        this.leftEarTop.setTextureSize(64, 32);
        this.leftEarTop.mirror = true;
        this.leftEarLayer3 = new ModelRenderer(this, 0, 6);
        this.leftEarLayer3.addBox(-1.0F, -3.0F, 0.0F, 3, 1, 1);
        this.leftEarLayer3.setRotationPoint(4.0F, -8.0F, 0.0F);
        this.leftEarLayer3.setTextureSize(64, 32);
        this.leftEarLayer3.mirror = true;
        this.leftEarLayer2 = new ModelRenderer(this, 0, 4);
        this.leftEarLayer2.addBox(-2.0F, -2.0F, 0.0F, 4, 1, 1);
        this.leftEarLayer2.setRotationPoint(4.0F, -8.0F, 0.0F);
        this.leftEarLayer2.setTextureSize(64, 32);
        this.leftEarLayer2.mirror = true;
        this.rightEarBottom = new ModelRenderer(this, 13, 0);
        this.rightEarBottom.addBox(-1.0F, 0.0F, 0.0F, 1, 1, 1);
        this.rightEarBottom.setRotationPoint(-4.0F, -8.0F, 0.0F);
        this.rightEarBottom.setTextureSize(64, 32);
        this.rightEarBottom.mirror = true;
        this.rightEarLayer1 = new ModelRenderer(this, 13, 2);
        this.rightEarLayer1.addBox(-2.0F, -1.0F, 0.0F, 5, 1, 1);
        this.rightEarLayer1.setRotationPoint(-4.0F, -8.0F, 0.0F);
        this.rightEarLayer1.setTextureSize(64, 32);
        this.rightEarLayer1.mirror = true;
        this.rightEarRearTop = new ModelRenderer(this, 13, 16);
        this.rightEarRearTop.addBox(-1.0F, -3.0F, 1.0F, 1, 1, 1);
        this.rightEarRearTop.setRotationPoint(-4.0F, -8.0F, 0.0F);
        this.rightEarRearTop.setTextureSize(64, 32);
        this.rightEarRearTop.mirror = true;
        this.rightEarRearLayer1 = new ModelRenderer(this, 13, 14);
        this.rightEarRearLayer1.addBox(-1.0F, -2.0F, 1.0F, 2, 1, 1);
        this.rightEarRearLayer1.setRotationPoint(-4.0F, -8.0F, 0.0F);
        this.rightEarRearLayer1.setTextureSize(64, 32);
        this.rightEarRearLayer1.mirror = true;
        this.rightEarRearBottom = new ModelRenderer(this, 13, 12);
        this.rightEarRearBottom.addBox(-1.0F, -1.0F, 1.0F, 3, 1, 1);
        this.rightEarRearBottom.setRotationPoint(-4.0F, -8.0F, 0.0F);
        this.rightEarRearBottom.setTextureSize(64, 32);
        this.rightEarRearBottom.mirror = true;
        this.rightEarLayer2 = new ModelRenderer(this, 13, 4);
        this.rightEarLayer2.addBox(-2.0F, -2.0F, 0.0F, 4, 1, 1);
        this.rightEarLayer2.setRotationPoint(-4.0F, -8.0F, 0.0F);
        this.rightEarLayer2.setTextureSize(64, 32);
        this.rightEarLayer2.mirror = true;
        this.rightEarTop = new ModelRenderer(this, 13, 8);
        this.rightEarTop.addBox(-1.0F, -4.0F, 0.0F, 1, 1, 1);
        this.rightEarTop.setRotationPoint(-4.0F, -8.0F, 0.0F);
        this.rightEarTop.setTextureSize(64, 32);
        this.rightEarTop.mirror = true;
        this.rightEarLayer3 = new ModelRenderer(this, 13, 6);
        this.rightEarLayer3.addBox(-2.0F, -3.0F, 0.0F, 3, 1, 1);
        this.rightEarLayer3.setRotationPoint(-4.0F, -8.0F, 0.0F);
        this.rightEarLayer3.setTextureSize(64, 32);
        this.rightEarLayer3.mirror = true;
        this.leftEarBottom.textureWidth = 32.0F;
        this.leftEarBottom.textureHeight = 32.0F;
        this.leftEarRearTop.textureWidth = 32.0F;
        this.leftEarRearLayer1.textureWidth = 32.0F;
        this.leftEarRearBottom.textureWidth = 32.0F;
        this.leftEarLayer1.textureWidth = 32.0F;
        this.leftEarTop.textureWidth = 32.0F;
        this.leftEarLayer3.textureWidth = 32.0F;
        this.leftEarLayer2.textureWidth = 32.0F;
        this.rightEarBottom.textureWidth = 32.0F;
        this.rightEarLayer1.textureWidth = 32.0F;
        this.rightEarRearTop.textureWidth = 32.0F;
        this.rightEarRearLayer1.textureWidth = 32.0F;
        this.rightEarRearBottom.textureWidth = 32.0F;
        this.rightEarLayer2.textureWidth = 32.0F;
        this.rightEarTop.textureWidth = 32.0F;
        this.rightEarLayer3.textureWidth = 32.0F;
    }
}
