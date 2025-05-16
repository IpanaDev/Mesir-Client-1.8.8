package net.minecraft.client.model;

import ipana.Ipana;
import ipana.utils.shader.ShaderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import optifine.Config;
import optifine.ModelSprite;
import org.lwjgl.opengl.GL11;

import ipana.irc.user.PlayerCosmetics;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import shadersmod.client.SVertexFormat;

public class ModelAmongUs extends ModelBiped {
    public ModelRenderer body;
    public ModelRenderer leg1;
    public ModelRenderer leg2;
    public ModelRenderer backpack;
    public ModelRenderer glass;

    public ModelRenderer bodyLgbt;
    public ModelRenderer leg1Lgbt;
    public ModelRenderer leg2Lgbt;
    public ModelRenderer backpackLgbt;

    // sorry for brain loss
    public final ModelRenderer leftEarBottom;
    public final ModelRenderer leftEarRearTop;
    public final ModelRenderer leftEarRearLayer1;
    public final ModelRenderer leftEarRearBottom;
    public final ModelRenderer leftEarLayer1;
    public final ModelRenderer leftEarTop;
    public final ModelRenderer leftEarLayer3;
    public final ModelRenderer leftEarLayer2;
    public final ModelRenderer rightEarBottom;
    public final ModelRenderer rightEarLayer1;
    public final ModelRenderer rightEarRearTop;
    public final ModelRenderer rightEarRearLayer1;
    public final ModelRenderer rightEarRearBottom;
    public final ModelRenderer rightEarLayer2;
    public final ModelRenderer rightEarTop;
    public final ModelRenderer rightEarLayer3;
    private final ModelRenderer bipedCape;


    private long ms = -1;
    private Offset[] backOffset = new Offset[] {
            new Offset(2, 0), new Offset(3, 400)
    };
    private Offset[] bodyOffset = new Offset[] {
            new Offset(2, 0), new Offset(3, 550)
    };
    private Offset[] leg1Offset = new Offset[] {
            new Offset(2, 70), new Offset(3, 250)
    };
    private Offset[] leg2Offset = new Offset[] {
            new Offset(2, 70), new Offset(3, 250)
    };

    public ModelAmongUs() {
        this(0.0F);
    }

    public ModelAmongUs(float p_i46366_1_) {
        int i = 6;
        this.body = new ModelRenderer(this, 17, -4);
        this.body.addBox(-8.0F, -5.0F, -2.0F, 14, 17, 8, p_i46366_1_);
        this.body.setRotationPoint(0.0F, i, 0.0F);
        this.backpack = new ModelRenderer(this, 0, 0);
        this.backpack.addBox(-6.0F, -3.0F, 6.0F, 10, 12, 4, p_i46366_1_);
        this.backpack.setRotationPoint(0.0F, i, 0.0F);
        this.glass = new ModelRenderer(this, 0, 24);
        this.glass.addBox(-6.0F, -3.0F, -4.0F, 10, 6, 2, p_i46366_1_);
        this.glass.setRotationPoint(0.0F, i, 0.0F);
        this.leg1 = new ModelRenderer(this, 0, 0);
        this.leg1.addBox(-5.0F, -0.0F, -5.0F, 5, 6, 6, p_i46366_1_);
        this.leg1.setRotationPoint(-2.0F, 12 + i, 4.0F);
        this.leg2 = new ModelRenderer(this, 0, 0);
        this.leg2.addBox(-2.0F, -0.0F, -5.0F, 5, 6, 6, p_i46366_1_);
        this.leg2.setRotationPoint(2.0F, 12 + i, 4.0F);

        this.bodyLgbt = new ModelRenderer(this, 17, -4);
        this.bodyLgbt.addBox(-8.0F, -5.0F, -2.0F, 14, 17, 8, p_i46366_1_);
        this.bodyLgbt.setRotationPoint(0.0F, i, 0.0F);
        this.backpackLgbt = new ModelRenderer(this, 0, 0);
        this.backpackLgbt.addBox(-6.0F, -3.0F, 6.0F, 10, 12, 4, p_i46366_1_);
        this.backpackLgbt.setRotationPoint(0.0F, i, 0.0F);
        this.leg1Lgbt = new ModelRenderer(this, 0, 0);
        this.leg1Lgbt.addBox(-5.0F, -0.0F, -5.0F, 5, 6, 6, p_i46366_1_);
        this.leg1Lgbt.setRotationPoint(-2.0F, 12 + i, 4.0F);
        this.leg2Lgbt = new ModelRenderer(this, 0, 0);
        this.leg2Lgbt.addBox(-2.0F, -0.0F, -5.0F, 5, 6, 6, p_i46366_1_);
        this.leg2Lgbt.setRotationPoint(2.0F, 12 + i, 4.0F);

        this.bipedCape = new ModelRenderer(this, 0, 0);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, p_i46366_1_);
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

    @Override
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        this.glass.render(scale);
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) entityIn;
        boolean isLGBT = Ipana.mainIRC().getUser(clientPlayer.getName()).cosmetics().getCosmetic(PlayerCosmetics.MODELS).params()[0].equals("lgbt_amongus");
        if (ms == -1) {
            ms = System.currentTimeMillis();
        }
        GlStateManager.enableLighting();
        if (isLGBT) {
            renderLgbt(backpackLgbt, scale, 0, backOffset);
            renderLgbt(bodyLgbt, scale, 0, bodyOffset);
            renderLgbt(leg1Lgbt, scale, 500, leg1Offset);
            renderLgbt(leg2Lgbt, scale, 500, leg2Offset);
            renderChildLgbt(scale);
        } else {
            backpack.render(scale);
            body.render(scale);
            leg1.render(scale);
            leg2.render(scale);
            renderChild(scale);
        }
    }

    public void renderChild(float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -0.35, 0);
        GlStateManager.scale(0.3, 0.3, 0.3);

        this.glass.render(scale);
        this.backpack.render(scale);
        this.body.render(scale);
        float rX1 = leg1.rotateAngleX;
        float rZ1 = leg1.rotateAngleZ;
        float rX2 = leg2.rotateAngleX;
        float rZ2 = leg2.rotateAngleZ;
        leg1.rotateAngleX = 0;
        leg1.rotateAngleZ = 0;
        leg2.rotateAngleX = 0;
        leg2.rotateAngleZ = 0;
        this.leg1.render(scale);
        this.leg2.render(scale);
        leg1.rotateAngleX = rX1;
        leg1.rotateAngleZ = rZ1;
        leg2.rotateAngleX = rX2;
        leg2.rotateAngleZ = rZ2;
        GlStateManager.popMatrix();
    }
    public void renderChildLgbt(float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -0.35, 0);
        GlStateManager.scale(0.3, 0.3, 0.3);

        this.glass.render(scale);
        this.backpackLgbt.render(scale);
        this.bodyLgbt.render(scale);
        float rX1 = leg1Lgbt.rotateAngleX;
        float rZ1 = leg1Lgbt.rotateAngleZ;
        float rX2 = leg2Lgbt.rotateAngleX;
        float rZ2 = leg2Lgbt.rotateAngleZ;
        leg1Lgbt.rotateAngleX = 0;
        leg1Lgbt.rotateAngleZ = 0;
        leg2Lgbt.rotateAngleX = 0;
        leg2Lgbt.rotateAngleZ = 0;
        this.leg1Lgbt.render(scale);
        this.leg2Lgbt.render(scale);
        leg1Lgbt.rotateAngleX = rX1;
        leg1Lgbt.rotateAngleZ = rZ1;
        leg2Lgbt.rotateAngleX = rX2;
        leg2Lgbt.rotateAngleZ = rZ2;
        GlStateManager.popMatrix();
    }
    public void renderLgbt(ModelRenderer model, float scale, int offset, Offset[] ignoreOffset) {
        renderModel(model, scale, offset, ignoreOffset);
    }
    public void renderCape(float scake) {
        this.bipedCape.render(scake);
    }
    public void compileDisplayList(ModelRenderer model, float scale, int offset, Offset[] ignoreOffset) {
        if (model.displayList == 0) {
            model.displayList = GLAllocation.generateDisplayLists(1);
        }
        GL11.glNewList(model.displayList, GL11.GL_COMPILE);
        GlStateManager.enableLighting();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        ShaderManager shaderManager = ShaderManager.getInstance();
        for (ModelBox modelBox : model.cubeList) {
            for (int i = 0; i < modelBox.quadList.length; i++) {
                TexturedQuad texturedquad = modelBox.quadList[i];
                if (texturedquad != null) {
                    shaderManager.loadShader("fade");
                    Offset modelOffset = get(i, ignoreOffset);
                    boolean b = modelOffset != null;
                    shaderManager.loadData("fade", "millis", (int) (b ? System.currentTimeMillis() + 160 + modelOffset.offset + offset - ms : System.currentTimeMillis() + 50 + offset - ms));
                    shaderManager.loadData("fade", "removeOffset", b ? 1 : 0);
                    if (Config.isShaders()) {
                        worldrenderer.begin(7, SVertexFormat.defVertexFormatTextured);
                    } else {
                        worldrenderer.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
                    }
                    texturedquad.draw(worldrenderer, scale);
                    tessellator.draw();
                    shaderManager.stop("fade");
                }
            }
        }
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        for (ModelSprite modelsprite : model.spriteList) {
            modelsprite.render(Tessellator.getInstance(), scale);
        }
        tessellator.draw();
        GL11.glEndList();
        model.compiled = true;
    }

    private Offset get(int object, Offset[] array) {
        for (Offset i : array) {
            if (i.index == object) return i;
        }
        return null;
    }

    public void renderModel(ModelRenderer model, float p_78785_1_, int offset, Offset[] ignoreOffset) {
        if (!model.isHidden && model.showModel) {
            compileDisplayList(model, p_78785_1_, offset, ignoreOffset);
            GlStateManager.translate(model.offsetX, model.offsetY, model.offsetZ);
            if (model.rotateAngleX == 0.0F && model.rotateAngleY == 0.0F && model.rotateAngleZ == 0.0F) {
                if (model.rotationPointX == 0.0F && model.rotationPointY == 0.0F && model.rotationPointZ == 0.0F) {
                    GlStateManager.callList(model.displayList);
                    if (model.childModels != null) {
                        for (ModelRenderer childModel : model.childModels) {
                            childModel.render(p_78785_1_);
                        }
                    }
                } else {
                    GlStateManager.translate(model.rotationPointX * p_78785_1_, model.rotationPointY * p_78785_1_, model.rotationPointZ * p_78785_1_);
                    GlStateManager.callList(model.displayList);
                    if (model.childModels != null) {
                        for (ModelRenderer childModel : model.childModels) {
                            childModel.render(p_78785_1_);
                        }
                    }
                    GlStateManager.translate(-model.rotationPointX * p_78785_1_, -model.rotationPointY * p_78785_1_, -model.rotationPointZ * p_78785_1_);
                }
            } else {
                GlStateManager.pushMatrix();
                GlStateManager.translate(model.rotationPointX * p_78785_1_, model.rotationPointY * p_78785_1_, model.rotationPointZ * p_78785_1_);
                if (model.rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(model.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }
                if (model.rotateAngleY != 0.0F) {
                    GlStateManager.rotate(model.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }
                if (model.rotateAngleX != 0.0F) {
                    GlStateManager.rotate(model.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }
                GlStateManager.callList(model.displayList);
                if (model.childModels != null) {
                    for (ModelRenderer childModel : model.childModels) {
                        childModel.render(p_78785_1_);
                    }
                }
                GlStateManager.popMatrix();
            }
            GlStateManager.translate(-model.offsetX, -model.offsetY, -model.offsetZ);
        }
    }

    private class Offset {
        private int index;
        private int offset;

        public Offset(int index, int offset) {
            this.index = index;
            this.offset = offset;
        }
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    @Override
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn) {
        this.leg1.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
        this.leg2.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float) Math.PI) * 1.4F * p_78087_2_;
        this.leg1Lgbt.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
        this.leg2Lgbt.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float) Math.PI) * 1.4F * p_78087_2_;
        super.setRotationAngles(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, entityIn);
    }
}