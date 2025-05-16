package net.minecraft.client.model;

import com.google.common.collect.Lists;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import ipana.utils.gl.GLMatrix;
import ipana.utils.player.PlayerUtils;
import ipana.utils.shader.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import optifine.Config;
import optifine.ModelSprite;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import shadersmod.client.SVertexFormat;

public class ModelRenderer {
    /**
     * The size of the texture file's width in pixels.
     */
    public float textureWidth;

    /**
     * The size of the texture file's height in pixels.
     */
    public float textureHeight;

    /**
     * The X offset into the texture used for displaying this model
     */
    private int textureOffsetX;

    /**
     * The Y offset into the texture used for displaying this model
     */
    private int textureOffsetY;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public boolean compiled;

    /**
     * The GL display list rendered by the Tessellator for this model
     */
    public int displayList;
    public boolean mirror;
    public boolean showModel;

    /**
     * Hides the model.
     */
    public boolean isHidden;
    public List<ModelBox> cubeList;
    public List<ModelRenderer> childModels;
    public final String boxName;
    private ModelBase baseModel;
    public float offsetX;
    public float offsetY;
    public float offsetZ;

    public List<ModelSprite> spriteList;
    public boolean mirrorV;
    float savedScale;

    public ModelRenderer(ModelBase model, String boxNameIn) {
        this.spriteList = new ArrayList<>();
        this.mirrorV = false;
        this.textureWidth = 64.0F;
        this.textureHeight = 32.0F;
        this.showModel = true;
        this.cubeList = Lists.newArrayList();
        this.baseModel = model;
        model.boxList.add(this);
        this.boxName = boxNameIn;
        this.setTextureSize(model.textureWidth, model.textureHeight);
    }

    public ModelRenderer(ModelBase model) {
        this(model, null);
    }

    public ModelRenderer(ModelBase model, int texOffX, int texOffY) {
        this(model);
        this.setTextureOffset(texOffX, texOffY);
    }

    /**
     * Sets the current box's rotation points and rotation angles to another box.
     */
    public void addChild(ModelRenderer renderer) {
        if (this.childModels == null) {
            this.childModels = Lists.newArrayList();
        }

        this.childModels.add(renderer);
    }

    public ModelRenderer setTextureOffset(int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth) {
        partName = this.boxName + "." + partName;
        TextureOffset textureoffset = this.baseModel.getTextureOffset(partName);
        this.setTextureOffset(textureoffset.textureOffsetX, textureoffset.textureOffsetY);
        this.cubeList.add((new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F)).setBoxName(partName));
        return this;
    }

    public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth) {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F));
        return this;
    }

    public ModelRenderer addBox(float p_178769_1_, float p_178769_2_, float p_178769_3_, int p_178769_4_, int p_178769_5_, int p_178769_6_, boolean p_178769_7_) {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, p_178769_1_, p_178769_2_, p_178769_3_, p_178769_4_, p_178769_5_, p_178769_6_, 0.0F, p_178769_7_));
        return this;
    }

    /**
     * Creates a textured box. Args: originX, originY, originZ, width, height, depth, scaleFactor.
     */
    public void addBox(float p_78790_1_, float p_78790_2_, float p_78790_3_, int width, int height, int depth, float scaleFactor) {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, p_78790_1_, p_78790_2_, p_78790_3_, width, height, depth, scaleFactor));
    }

    public void addBox(float p_78790_1_, float p_78790_2_, float p_78790_3_, int width, int height, int depth, float scaleFactor, int... quadIndex) {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, p_78790_1_, p_78790_2_, p_78790_3_, width, height, depth, scaleFactor, this.mirror, quadIndex));
    }

    public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {
        this.rotationPointX = rotationPointXIn;
        this.rotationPointY = rotationPointYIn;
        this.rotationPointZ = rotationPointZIn;
    }

    public boolean noAngle() {
        return rotateAngleX == 0 && rotateAngleY == 0 && rotateAngleZ == 0;
    }

    /*private float lastAngleX, lastAngleY, lastAngleZ;
    private float lastPointX, lastPointY, lastPointZ;
    private float lastOffsetX, lastOffsetY, lastOffsetZ;

    private GLMatrix glMatrix = new GLMatrix(GL11.GL_MODELVIEW_MATRIX, c -> {

    });

    boolean pushed = false;
            if (hasOffsets || hasAngles || hasPoints) {
                if (lastAngleX != rotateAngleX || lastAngleY != rotateAngleY || lastAngleZ != rotateAngleZ || lastPointX != rotationPointX || lastPointY != rotationPointY || lastPointZ != rotationPointZ || lastOffsetX != offsetX || lastOffsetY != offsetY || lastOffsetZ != offsetZ) {
                    glMatrix.setupMatrix(GL11.GL_MODELVIEW_MATRIX, c -> {
                        if (hasOffsets) {
                            GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
                        }
                        if (hasPoints) {
                            GL11.glTranslatef(this.rotationPointX * p_78785_1_, this.rotationPointY * p_78785_1_, this.rotationPointZ * p_78785_1_);
                        }
                        if (hasAngles) {
                            if (this.rotateAngleZ != 0.0F)
                                GL11.glRotatef(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                            if (this.rotateAngleY != 0.0F)
                                GL11.glRotatef(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                            if (this.rotateAngleX != 0.0F)
                                GL11.glRotatef(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                        }
                    });
                    lastAngleX = rotateAngleX;
                    lastAngleY = rotateAngleY;
                    lastAngleZ = rotateAngleZ;
                    lastPointX = rotationPointX;
                    lastPointY = rotationPointY;
                    lastPointZ = rotationPointZ;
                    lastOffsetX = offsetX;
                    lastOffsetY = offsetY;
                    lastOffsetZ = offsetZ;
                }
                pushed = true;
                GL11.glPushMatrix();
                glMatrix.multMatrix();
            }
            GL11.glCallList(this.displayList);
            if (this.childModels != null) {
                for (ModelRenderer childModel : this.childModels) {
                    childModel.render(p_78785_1_);
                }
            }
            if (pushed) {
                GL11.glPopMatrix();
            }

     */
    public void renderOld(float scale) {
        if (!this.isHidden && this.showModel) {
            if (!this.compiled) {
                this.compileDisplayList(scale);
            }
            boolean hasOffsets = this.offsetX != 0 || this.offsetY != 0 || this.offsetZ != 0;
            boolean hasAngles = this.rotateAngleX != 0 || this.rotateAngleY != 0 || this.rotateAngleZ != 0;
            boolean hasPoints = this.rotationPointX != 0 || this.rotationPointY != 0 || this.rotationPointZ != 0;
            if (hasOffsets) {
                GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
            }

            if (!hasAngles) {
                if (!hasPoints) {
                    GL11.glCallList(this.displayList);

                    if (this.childModels != null) {
                        for (ModelRenderer childModel : this.childModels) {
                            childModel.render(scale);
                        }
                    }
                } else {
                    GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                    GL11.glCallList(this.displayList);

                    if (this.childModels != null) {
                        for (ModelRenderer childModel : this.childModels) {
                            childModel.render(scale);
                        }
                    }

                    GL11.glTranslatef(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
                }
            } else {
                GL11.glPushMatrix();
                GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                if (this.rotateAngleZ != 0.0F) {
                    GL11.glRotatef(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GL11.glRotatef(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GL11.glRotatef(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }

                GL11.glCallList(this.displayList);

                if (this.childModels != null) {
                    for (ModelRenderer childModel : this.childModels) {
                        childModel.render(scale);
                    }
                }

                GL11.glPopMatrix();
            }
            if (hasOffsets) {
                GL11.glTranslatef(-this.offsetX, -this.offsetY, -this.offsetZ);
            }
        }
    }
    public void render(float scale) {
        if (!this.isHidden && this.showModel) {
            if (!this.compiled) {
                this.compileDisplayList(scale);
            }
            boolean hasOffsets = this.offsetX != 0 || this.offsetY != 0 || this.offsetZ != 0;
            boolean hasAngles = this.rotateAngleX != 0 || this.rotateAngleY != 0 || this.rotateAngleZ != 0;
            boolean hasPoints = this.rotationPointX != 0 || this.rotationPointY != 0 || this.rotationPointZ != 0;
            if (hasOffsets) {
                GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
            }

            if (!hasAngles) {
                if (!hasPoints) {
                    GL11.glCallList(this.displayList);

                    if (this.childModels != null) {
                        for (ModelRenderer childModel : this.childModels) {
                            childModel.render(scale);
                        }
                    }
                } else {
                    GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                    GL11.glCallList(this.displayList);

                    if (this.childModels != null) {
                        for (ModelRenderer childModel : this.childModels) {
                            childModel.render(scale);
                        }
                    }

                    GL11.glTranslatef(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
                }
            } else {
                GL11.glPushMatrix();
                GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                if (this.rotateAngleZ != 0.0F) {
                    GL11.glRotatef(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GL11.glRotatef(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GL11.glRotatef(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }

                GL11.glCallList(this.displayList);

                if (this.childModels != null) {
                    for (ModelRenderer childModel : this.childModels) {
                        childModel.render(scale);
                    }
                }

                GL11.glPopMatrix();
            }
            if (hasOffsets) {
                GL11.glTranslatef(-this.offsetX, -this.offsetY, -this.offsetZ);
            }
        }
    }

    public void renderWithRotation(float scale) {
        if (!this.isHidden && this.showModel) {
            if (!this.compiled) {
                this.compileDisplayList(scale);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

            if (this.rotateAngleY != 0.0F) {
                GlStateManager.rotate(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if (this.rotateAngleX != 0.0F) {
                GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
            }

            if (this.rotateAngleZ != 0.0F) {
                GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.callList(this.displayList);
            GlStateManager.popMatrix();
        }
    }

    /**
     * Allows the changing of Angles after a box has been rendered
     */
    public void postRender(float scale) {
        if (!this.isHidden && this.showModel) {
            if (!this.compiled) {
                this.compileDisplayList(scale);
            }

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
                if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
                    GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                }
            } else {
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

                if (this.rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    /**
     * Compiles a GL display list for this model
     */
    public void compileDisplayList(float scale) {
        if (this.displayList == 0) {
            this.savedScale = scale;
            this.displayList = GLAllocation.generateDisplayLists(1);
        }

        GL11.glNewList(this.displayList, GL11.GL_COMPILE);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        if (Config.isShaders()) {
            worldrenderer.begin(7, SVertexFormat.defVertexFormatTextured);
        } else {
            worldrenderer.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        }
        for (ModelBox modelBox : this.cubeList) {
            modelBox.render(worldrenderer, scale);
        }
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        for (ModelSprite modelsprite : this.spriteList) {
            worldrenderer.setTranslation(modelsprite.posX * scale, modelsprite.posY * scale, modelsprite.posZ * scale);
            modelsprite.render(false, Tessellator.getInstance(), scale);
            worldrenderer.setTranslation(-modelsprite.posX * scale, -modelsprite.posY * scale, -modelsprite.posZ * scale);
        }
        tessellator.draw();

        GL11.glEndList();
        this.compiled = true;
    }

    /**
     * Returns the model renderer with the new texture parameters.
     */
    public ModelRenderer setTextureSize(int textureWidthIn, int textureHeightIn) {
        this.textureWidth = (float) textureWidthIn;
        this.textureHeight = (float) textureHeightIn;
        return this;
    }

    public void addSprite(float p_addSprite_1_, float p_addSprite_2_, float p_addSprite_3_, int p_addSprite_4_, int p_addSprite_5_, int p_addSprite_6_, float p_addSprite_7_) {
        this.spriteList.add(new ModelSprite(this, this.textureOffsetX, this.textureOffsetY, p_addSprite_1_, p_addSprite_2_, p_addSprite_3_, p_addSprite_4_, p_addSprite_5_, p_addSprite_6_, p_addSprite_7_));
    }

    public void resetDisplayList() {
        if (this.compiled) {
            this.compiled = false;
            this.compileDisplayList(this.savedScale);
        }
    }
}
