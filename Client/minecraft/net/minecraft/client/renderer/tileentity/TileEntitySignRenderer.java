package net.minecraft.client.renderer.tileentity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import ipana.utils.gl.GLCall;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import optifine.Config;
import optifine.CustomColors;


public class TileEntitySignRenderer extends TileEntitySpecialRenderer<TileEntitySign>
{
    private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation("textures/entity/sign.png");

    /** The ModelSign instance for use in this renderer */
    private final ModelSign model = new ModelSign();
    private final Minecraft mc = Minecraft.getMinecraft();

    public void renderTileEntityAt(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage) {
        Block block = te.getBlockType();
        EnumFacing frameFace = te.facing;
        boolean direction = (mc.thePlayer.getFaces() != null && (mc.thePlayer.getFaces()[0].isSame(frameFace) || mc.thePlayer.getFaces()[1].isSame(frameFace)));
        if (block != Blocks.standing_sign && direction && te.hangedBlock != null && te.hangedBlock.isOpaqueCube()) {
            if (te.distForRender > 1 && (te.distForRender > 4 || RotationUtils.getDistanceBetweenAngles(mc.thePlayer.rotationYaw, RotationUtils.getRotationFromPosition(te.getPos().getX(), te.getPos().getZ(), te.getPos().getY())[0]) > 25)) {
                return;
            }
        }
        GlStateManager.pushMatrix();
        float f = 0.6666667F;

        if (block == Blocks.standing_sign) {
            GlStateManager.translate((float)x + 0.5F, (float)y + 0.75F * f, (float)z + 0.5F);
            float f2 = (float)(te.getBlockMetadata() * 360) / 16.0F;
            GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
            this.model.signStick.showModel = true;
        } else {
            int k = te.getBlockMetadata();
            float f1 = 0.0F;

            if (k == 2)
            {
                f1 = 180.0F;
            }

            if (k == 4)
            {
                f1 = 90.0F;
            }

            if (k == 5)
            {
                f1 = -90.0F;
            }

            GlStateManager.translate((float)x + 0.5F, (float)y + 0.75F * f, (float)z + 0.5F);
            GlStateManager.rotate(-f1, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
            this.model.signStick.showModel = false;
        }

        if (destroyStage >= 0) {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            this.bindTexture(SIGN_TEXTURE);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        //GlStateManager.disableDepth();
        GlStateManager.scale(f, -f, -f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770,771);
        GlStateManager.color(1,1,1,1);
        if (block == Blocks.standing_sign) {
            this.model.renderSign();
        } else {
            this.model.signBoard.render(0.0625f);
        }
        GlStateManager.popMatrix();
        FontRenderer fontrenderer = this.getFontRenderer();
        float f3 = 0.015625F * f;
        GlStateManager.translate(0.0F, 0.5F * f, 0.07F * f);
        GlStateManager.scale(f3, -f3, f3);
        //GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
        GlStateManager.depthMask(false);
        int i = 0;

        if (Config.isCustomColors()) {
            i = CustomColors.getSignTextColor(i);
        }

        if (destroyStage < 0) {
            int finalI = i;
            //int t = GlStateManager.getBoundTexture();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();

            if (Config.isGlCalls() && te.needsUpdate) {
                te.signTexts.compile(c -> {
                    fontrenderer.setAutoBegin(false);
                    fontrenderer.begin();
                    for (int j = 0; j < te.signText.length; ++j) {
                        if (te.signText[j] != null) {
                            IChatComponent ichatcomponent = te.signText[j];
                            List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(ichatcomponent, 90, fontrenderer, false, true);
                            String s = !list.isEmpty() ? list.get(0).getFormattedText() : "";
                            int y1 = j * 10 - te.signText.length * 5;
                            if (j == te.lineBeingEdited) {
                                s = "> " + s + " <";
                            }
                            fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2f, y1, finalI);
                        }
                    }
                    fontrenderer.end();
                    fontrenderer.setAutoBegin(true);
                });
                te.needsUpdate = false;
            }
            GLCall.drawDirect(te.signTexts);
            GlStateManager.resetTextureState();
        }
        GlStateManager.depthMask(true);
        //GlStateManager.resetColor();
        //GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //GlStateManager.enableDepth();
        GlStateManager.popMatrix();

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
