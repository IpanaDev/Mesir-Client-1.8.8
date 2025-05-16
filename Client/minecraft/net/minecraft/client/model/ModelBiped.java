package net.minecraft.client.model;

import ipana.managements.module.Modules;
import ipana.modules.render.BlockAnim;
import ipana.utils.render.Anims;
import ipana.utils.render.PlayerAnim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import optifine.Config;
import optifine.ModelSprite;
import org.lwjgl.opengl.GL11;
import shadersmod.client.SVertexFormat;

public class ModelBiped extends ModelBase
{
    public ModelRenderer bipedHead;

    /** The Biped's Headwear. Used for the outer layer of player skins. */
    public ModelRenderer bipedHeadwear;
    public ModelRenderer bipedBody;

    /** The Biped's Right Arm */
    public ModelRenderer bipedRightArm;

    /** The Biped's Left Arm */
    public ModelRenderer bipedLeftArm;

    /** The Biped's Right Leg */
    public ModelRenderer bipedRightLeg;

    /** The Biped's Left Leg */
    public ModelRenderer bipedLeftLeg;

    /**
     * Records whether the model should be rendered holding an item in the left hand, and if that item is a block.
     */
    public int heldItemLeft;

    /**
     * Records whether the model should be rendered holding an item in the right hand, and if that item is a block.
     */
    public int heldItemRight;
    public boolean isSneak;

    /** Records whether the model should be rendered aiming a bow. */
    public boolean aimedBow;

    public ModelBiped() {
        this(0.0F);
    }

    public ModelBiped(float modelSize) {
        this(modelSize, 0.0F, 64, 32);
    }

    public ModelBiped(float modelSize, float p_i1149_2_, int textureWidthIn, int textureHeightIn) {
        this.textureWidth = textureWidthIn;
        this.textureHeight = textureHeightIn;
        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize);
        this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1149_2_, 0.0F);
        this.bipedHeadwear = new ModelRenderer(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F + p_i1149_2_, 0.0F);
        this.bipedBody = new ModelRenderer(this, 16, 16);
        this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + p_i1149_2_, 0.0F);
        this.bipedRightArm = new ModelRenderer(this, 40, 16);
        this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + p_i1149_2_, 0.0F);
        this.bipedLeftArm = new ModelRenderer(this, 40, 16);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + p_i1149_2_, 0.0F);
        this.bipedRightLeg = new ModelRenderer(this, 0, 16);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F + p_i1149_2_, 0.0F);
        this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F + p_i1149_2_, 0.0F);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        GlStateManager.pushMatrix();

        if (this.isChild) {
            float f = 2.0F;
            GlStateManager.scale(1.5F / f, 1.5F / f, 1.5F / f);
            GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            this.bipedHead.render(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            this.bipedBody.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            this.bipedHeadwear.render(scale);
        } else {
            if (entityIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            boolean ataturk = Modules.CAMERA.isEnabled() || !Modules.COOL_PERSPECTIVE.isEnabled() || (entityIn != Minecraft.getMinecraft().thePlayer || Minecraft.getMinecraft().gameSettings.thirdPersonView > 0);
            if (ataturk) {
                this.bipedHead.render(scale);
                this.bipedBody.render(scale);
                this.bipedRightArm.render(scale);
                this.bipedLeftArm.render(scale);
                this.bipedRightLeg.render(scale);
                this.bipedLeftLeg.render(scale);
                this.bipedHeadwear.render(scale);
            } else {
                this.bipedBody.render(scale);
                this.bipedRightArm.render(scale);
                this.bipedLeftArm.render(scale);
                this.bipedRightLeg.render(scale);
                this.bipedLeftLeg.render(scale);
            }
        }

        GlStateManager.popMatrix();
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */

    public void setRotationAnglesRightArm() {
        this.bipedRightArm.rotateAngleX = 0.0f;
        if (this.isRiding) {
            this.bipedRightArm.rotateAngleX -= ((float) Math.PI / 5F);
        }
        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedRightArm.rotateAngleZ = 0.1F;
        this.bipedRightArm.rotationPointZ = 0;
        this.bipedRightArm.rotationPointX = -5.0F;
    }

    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn) {
        boolean za = heldItemRight == 3;
        float pi180 = 180F / (float)Math.PI;
        this.bipedHead.rotateAngleY = p_78087_4_ / pi180;
        this.bipedHead.rotateAngleX = p_78087_5_ / pi180;
        float var0 = p_78087_1_ * 0.6662F;
        float var1 = 2.0F * p_78087_2_ * 0.5F;
        float rightPartCos = MathHelper.cos(var0 + Math.PI);
        float leftPartCos = MathHelper.cos(var0);
        this.bipedRightArm.rotateAngleX = rightPartCos * var1;
        this.bipedLeftArm.rotateAngleX = leftPartCos * var1;
        if (za && Modules.BLOCK_ANIM.mode.getValue() == BlockAnim.Mode.CUSTOM) {
            this.bipedLeftArm.rotateAngleX = this.bipedRightArm.rotateAngleX;
        }
        this.bipedLeftArm.rotateAngleZ = 0.0F;
        float var2 = 1.4F * p_78087_2_;
        this.bipedRightLeg.rotateAngleX = leftPartCos * var2;
        this.bipedLeftLeg.rotateAngleX = rightPartCos * var2;
        this.bipedRightLeg.rotateAngleY = 0.0F;
        this.bipedLeftLeg.rotateAngleY = 0.0F;

        if (this.isRiding)
        {
            float pi5 = (float)Math.PI / 5F;
            float pi25 = pi5 * 2F;
            float pi10 = pi5 / 2F;
            this.bipedRightArm.rotateAngleX -= pi5;
            this.bipedLeftArm.rotateAngleX -= pi5;
            this.bipedRightLeg.rotateAngleX = -pi25;
            this.bipedLeftLeg.rotateAngleX = -pi25;
            this.bipedRightLeg.rotateAngleY = pi10;
            this.bipedLeftLeg.rotateAngleY = -pi10;
        }

        if (this.heldItemLeft != 0)
        {
            this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * (float)this.heldItemLeft;
        }

        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleY = 0.0F;
        switch (this.heldItemRight) {
            case 1 -> this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * (float) this.heldItemRight;
            case 3 -> {
                switch (Modules.BLOCK_ANIM.mode.getValue()) {
                    case CUSTOM -> {
                        this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * (float) this.heldItemRight - 0.3f;
                        this.bipedRightArm.rotateAngleY = -0.8f;
                        this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * (float) this.heldItemRight - 0.3f;
                        this.bipedLeftArm.rotateAngleY = 0.8f;
                    }
                    case OLD ->
                            this.bipedRightArm.rotateAngleX = (this.bipedRightArm.rotateAngleX * 0.5F - 0.31415927F * this.heldItemRight);
                    case NEW -> {
                        this.bipedRightArm.rotateAngleX = (this.bipedRightArm.rotateAngleX * 0.5F - 0.31415927F * this.heldItemRight);
                        this.bipedRightArm.rotateAngleY = -0.5235988F;
                    }
                }
            }
            case 4 -> {
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * (float) 5;
                this.bipedRightArm.rotateAngleY = -0.7F;
            }
        }


        if (this.swingProgress > -9990.0F) {
            float f = this.swingProgress;
            this.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f) * (float)Math.PI * 2.0F) * 0.2F;
            float bodyAngleYSin = MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
            float bodyAngleYCos = MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
            this.bipedRightArm.rotationPointZ = bodyAngleYSin;
            this.bipedRightArm.rotationPointX = -bodyAngleYCos;
            this.bipedLeftArm.rotationPointZ = -bodyAngleYSin;
            this.bipedLeftArm.rotationPointX = bodyAngleYCos;
            this.bipedRightArm.rotateAngleY += this.bipedBody.rotateAngleY;
            this.bipedLeftArm.rotateAngleY += this.bipedBody.rotateAngleY;
            float minusSwing = 1.0F - this.swingProgress;
            f = 1.0F - (minusSwing * minusSwing * minusSwing * minusSwing);
            float swingTimesPiSin = MathHelper.sin(this.swingProgress * (float)Math.PI);
            float f1 = MathHelper.sin(f * (float)Math.PI);
            float f2 = swingTimesPiSin * -(this.bipedHead.rotateAngleX - 0.7F) * 0.75F;
            this.bipedRightArm.rotateAngleX = (float)((double)this.bipedRightArm.rotateAngleX - ((double)f1 * 1.2D + (double)f2));
            this.bipedRightArm.rotateAngleY += this.bipedBody.rotateAngleY * 2.0F;
            this.bipedRightArm.rotateAngleZ += swingTimesPiSin * -0.4F;
            if (za && Modules.BLOCK_ANIM.mode.getValue() == BlockAnim.Mode.CUSTOM) {
                this.bipedLeftArm.rotateAngleX = (float)((double)this.bipedLeftArm.rotateAngleX - ((double)f1 * 1.2D + (double)f2));
                this.bipedLeftArm.rotateAngleY += this.bipedBody.rotateAngleY * 2.0F;
                this.bipedLeftArm.rotateAngleZ += swingTimesPiSin * -0.4F;
            }
        }

        if (this.isSneak)
        {
            this.bipedBody.rotateAngleX = 0.5F;
            this.bipedRightArm.rotateAngleX += 0.4F;
            this.bipedLeftArm.rotateAngleX += 0.4F;
            this.bipedRightLeg.rotationPointZ = 4.0F;
            this.bipedLeftLeg.rotationPointZ = 4.0F;
            this.bipedRightLeg.rotationPointY = 9.0F;
            this.bipedLeftLeg.rotationPointY = 9.0F;
            this.bipedHead.rotationPointY = 1.0F;
        }
        else
        {
            this.bipedBody.rotateAngleX = 0.0F;
            this.bipedRightLeg.rotationPointZ = 0.1F;
            this.bipedLeftLeg.rotationPointZ = 0.1F;
            this.bipedRightLeg.rotationPointY = 12.0F;
            this.bipedLeftLeg.rotationPointY = 12.0F;
            this.bipedHead.rotationPointY = 0.0F;
        }
        float angleCos = MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        float angleSin = MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
        this.bipedRightArm.rotateAngleZ += angleCos;
        this.bipedLeftArm.rotateAngleZ -= angleCos;
        this.bipedRightArm.rotateAngleX += angleSin;
        this.bipedLeftArm.rotateAngleX -= angleSin;

        if (this.aimedBow)
        {
            float f3 = 0.0F;
            float f4 = 0.0F;
            this.bipedRightArm.rotateAngleZ = 0.0F;
            this.bipedLeftArm.rotateAngleZ = 0.0F;
            float var4 = 0.1F - f3 * 0.6F;
            float var5 = -((float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
            float var6 = f3 * 1.2F - f4 * 0.4F;
            this.bipedRightArm.rotateAngleY = -var4 + this.bipedHead.rotateAngleY;
            this.bipedLeftArm.rotateAngleY = var4 + this.bipedHead.rotateAngleY + 0.4F;
            this.bipedRightArm.rotateAngleX = var5;
            this.bipedLeftArm.rotateAngleX = var5;
            this.bipedRightArm.rotateAngleX -= var6;
            this.bipedLeftArm.rotateAngleX -= var6;
            this.bipedRightArm.rotateAngleZ += angleCos;
            this.bipedLeftArm.rotateAngleZ -= angleCos;
            this.bipedRightArm.rotateAngleX += angleSin;
            this.bipedLeftArm.rotateAngleX -= angleSin;
        }
        if (entityIn instanceof EntityPlayer) {
            PlayerAnim anim = Anims.getAnimByName(entityIn, ":nah:");
            if (anim != null) {
                anim.getPlayer().renderYawOffset = anim.getPlayer().rotationYawHead;
                anim.getPlayer().prevRenderYawOffset = anim.getPlayer().prevRotationYawHead;
                this.bipedRightArm.rotateAngleX = -1.5f + entityIn.rotationPitch / 90;
                this.bipedRightArm.rotateAngleY = -0.2F;
                this.bipedLeftArm.rotateAngleX = -1.5f + entityIn.rotationPitch / 90;
                this.bipedLeftArm.rotateAngleY = 1.0F;
            }
        }
        copyModelAngles(this.bipedHead, this.bipedHeadwear);
    }

    public void setModelAttributes(ModelBase model)
    {
        super.setModelAttributes(model);

        if (model instanceof ModelBiped)
        {
            ModelBiped modelbiped = (ModelBiped)model;
            this.heldItemLeft = modelbiped.heldItemLeft;
            this.heldItemRight = modelbiped.heldItemRight;
            this.isSneak = modelbiped.isSneak;
            this.aimedBow = modelbiped.aimedBow;
        }
    }

    public void setInvisible(boolean invisible)
    {
        this.bipedHead.showModel = invisible;
        this.bipedHeadwear.showModel = invisible;
        this.bipedBody.showModel = invisible;
        this.bipedRightArm.showModel = invisible;
        this.bipedLeftArm.showModel = invisible;
        this.bipedRightLeg.showModel = invisible;
        this.bipedLeftLeg.showModel = invisible;
    }

    public void postRenderArm(float scale)
    {
        this.bipedRightArm.postRender(scale);
    }
}
