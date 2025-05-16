package net.minecraft.client.renderer.entity.layers.bok;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.matrix.Matrix4f;
import net.minecraft.util.matrix.MatrixStack;
import net.minecraft.util.matrix.MatrixVec3f;
import net.minecraft.util.matrix.Vector4f;
import optifine.MathUtils;

public class SmoothCapeRenderer {
    public static int layerCount = 16;

    public void renderSmoothCape(LayerCape layer, AbstractClientPlayer abstractClientPlayer, float delta) {
        var worldrenderer = Tessellator.getInstance().getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        MatrixStack poseStack = new MatrixStack();
        poseStack.pushPose();
        Matrix4f oldPositionMatrix = null;
        for (var part = 0; part < layerCount; part++) {
            modifyPoseStack(layer, poseStack, abstractClientPlayer, delta, part);
            if (oldPositionMatrix == null) {
                oldPositionMatrix = poseStack.getLast().getMatrix();
            }
            if (part == 0) {
                addTopVertex(worldrenderer, poseStack.getLast().getMatrix(), oldPositionMatrix, 0.3F, 0, 0F, -0.3F, 0, -0.06F, part);
            }
            if (part == layerCount - 1) {
                addBottomVertex(worldrenderer, poseStack.getLast().getMatrix(), poseStack.getLast().getMatrix(), 0.3F, (part + 1) * (0.96F / layerCount), 0F, -0.3F, (part + 1) * (0.96F / layerCount), -0.06F, part);
            }
            addLeftVertex(worldrenderer, poseStack.getLast().getMatrix(), oldPositionMatrix, -0.3F, (part + 1) * (0.96F / layerCount), 0F, -0.3F, part * (0.96F / layerCount), -0.06F, part);
            addRightVertex(worldrenderer, poseStack.getLast().getMatrix(), oldPositionMatrix, 0.3F, (part + 1) * (0.96F / layerCount), 0F, 0.3F, part * (0.96F / layerCount), -0.06F, part);
            addBackVertex(worldrenderer, poseStack.getLast().getMatrix(), oldPositionMatrix, 0.3F, (part + 1) * (0.96F / layerCount), -0.06F, -0.3F, part * (0.96F / layerCount), -0.06F, part);
            addFrontVertex(worldrenderer, oldPositionMatrix, poseStack.getLast().getMatrix(), 0.3F, (part + 1) * (0.96F / layerCount), 0F, -0.3F, part * (0.96F / layerCount), 0F, part);
            oldPositionMatrix = poseStack.getLast().getMatrix();
            poseStack.popPose();
        }
        Tessellator.getInstance().draw();
    }

    void modifyPoseStack(LayerCape layer, MatrixStack poseStack, AbstractClientPlayer abstractClientPlayer, float h, int part) {
        modifyPoseStackSimulation(layer, poseStack, abstractClientPlayer, h, part);
        //modifyPoseStackVanilla(layer, poseStack, abstractClientPlayer, h, part);
    }//bence bunun push içinde push supportu yok

    private void modifyPoseStackSimulation(LayerCape layer, MatrixStack poseStack, AbstractClientPlayer abstractClientPlayer, float delta, int part) {
        var simulation = abstractClientPlayer.stickSimulation;
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.125D);
        var z = simulation.points.get(part).getLerpX(delta) - simulation.points.get(0).getLerpX(delta);
        if (z > 0) {
            z = 0;
        }
        var y = simulation.points.get(0).getLerpY(delta) - part - simulation.points.get(part).getLerpY(delta);
        var sidewaysRotationOffset = 0F;
        var partRotation = (float) -Math.atan2(y, z);
        partRotation = Math.max(partRotation, 0);
        if (partRotation != 0) {
            partRotation = (float) (Math.PI - partRotation);
        }
        partRotation *= 57.2958;
        partRotation *= 2;
        var height = 0F;
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0F;
            poseStack.translate(0, 0.15F, 0);
        }
        var naturalWindSwing = layer.getWind(abstractClientPlayer, part);
        // vanilla rotating and wind
        poseStack.rotate(MatrixVec3f.XP.rotationDegrees(6.0F + height + naturalWindSwing));
        poseStack.rotate(MatrixVec3f.ZP.rotationDegrees(sidewaysRotationOffset / 2.0F));
        poseStack.rotate(MatrixVec3f.YP.rotationDegrees(180.0F - sidewaysRotationOffset / 2.0F));
        poseStack.translate(0, y / layerCount, z / layerCount); // movement from the simulation
        // offsetting so the rotation is on the cape part
        // float offset = (float) (part * (16 / layerCount))/16; // to fold the entire cape into one position for debugging
        poseStack.translate(0, /*-offset*/ +(0.48 / layerCount), -(0.48 / layerCount)); // (0.48/16)
        poseStack.translate(0, part * 1f / layerCount, part * 0D / layerCount);
        poseStack.rotate(MatrixVec3f.XP.rotationDegrees(-partRotation)); // apply actual rotation
        // undoing the rotation
        poseStack.translate(0, -part * 1f / layerCount, -part * 0D / layerCount);
        poseStack.translate(0, -(0.48 / layerCount), 0.48 / layerCount);
    }

    private void modifyPoseStackVanilla(LayerCape layer, MatrixStack poseStack, AbstractClientPlayer abstractClientPlayer, float h, int part) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.125D);
        var d = MathHelper.lerp(h, abstractClientPlayer.prevChasingPosX, abstractClientPlayer.chasingPosX) - MathHelper.lerp(h, abstractClientPlayer.prevPosX, abstractClientPlayer.posX);
        var e = MathHelper.lerp(h, abstractClientPlayer.prevChasingPosY, abstractClientPlayer.chasingPosY) - MathHelper.lerp(h, abstractClientPlayer.prevPosY, abstractClientPlayer.posY);
        var m = MathHelper.lerp(h, abstractClientPlayer.prevChasingPosZ, abstractClientPlayer.chasingPosZ) - MathHelper.lerp(h, abstractClientPlayer.prevPosZ, abstractClientPlayer.posZ);
        var n = abstractClientPlayer.prevRenderYawOffset + abstractClientPlayer.renderYawOffset - abstractClientPlayer.prevRenderYawOffset;
        var o = Math.sin(n * 0.017453292F);
        var p = -Math.cos(n * 0.017453292F);
        var height = (float) e * 10.0F;
        height = MathHelper.clamp_float(height, -6.0F, 32.0F);
        var swing = (float) (d * o + m * p) * easeOutSine(1.0F / layerCount * part) * 100;
        swing = MathHelper.clamp_float(swing, 0.0F, 150.0F * easeOutSine(1F / layerCount * part));
        var sidewaysRotationOffset = (float) (d * p - m * o) * 100.0F;
        sidewaysRotationOffset = MathHelper.clamp_float(sidewaysRotationOffset, -20.0F, 20.0F);
        var t = MathHelper.lerp(h, abstractClientPlayer.prevCameraYaw, abstractClientPlayer.cameraYaw);
        height += Math.sin(MathHelper.lerp(h, abstractClientPlayer.prevDistanceWalkedModified, abstractClientPlayer.distanceWalkedModified) * 6.0F) * 32.0F * t;
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0F;
            poseStack.translate(0, 0.15F, 0);
        }
        var naturalWindSwing = layer.getWind(abstractClientPlayer, part);
        poseStack.rotate(MatrixVec3f.XP.rotationDegrees(6.0F + swing / 2.0F + height + naturalWindSwing));
        poseStack.rotate(MatrixVec3f.ZP.rotationDegrees(sidewaysRotationOffset / 2.0F));
        poseStack.rotate(MatrixVec3f.YP.rotationDegrees(180.0F - sidewaysRotationOffset / 2.0F));
    }

    private static void addBackVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        Matrix4f k;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
            k = matrix;
            matrix = oldMatrix;
            oldMatrix = k;
        }
        var minU = .015625F;
        var maxU = .171875F;
        var minV = .03125F;
        var maxV = .53125F;
        var deltaV = maxV - minV;
        var vPerPart = deltaV / layerCount;
        maxV = minV + vPerPart * (part + 1);
        minV = minV + vPerPart * part;
        // oldMatrix
        vertex(worldrenderer, oldMatrix, x1, y2, z1).tex(maxU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(minU, minV).normal(1, 0, 0).endVertex();
        // matrix
        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y1, z2).tex(maxU, maxV).normal(1, 0, 0).endVertex();
    }

    private static void addFrontVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        Matrix4f k;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
            k = matrix;
            matrix = oldMatrix;
            oldMatrix = k;
        }
        var minU = .1875F;
        var maxU = .34375F;
        var minV = .03125F;
        var maxV = .53125F;
        var deltaV = maxV - minV;
        var vPerPart = deltaV / layerCount;
        maxV = minV + vPerPart * (part + 1);
        minV = minV + vPerPart * part;
        // oldMatrix
        vertex(worldrenderer, oldMatrix, x1, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y1, z1).tex(minU, maxV).normal(1, 0, 0).endVertex();
        // matrix
        vertex(worldrenderer, matrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y2, z2).tex(maxU, minV).normal(1, 0, 0).endVertex();
    }

    private static void addLeftVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        var minU = 0F;
        var maxU = .015625F;
        var minV = .03125F;
        var maxV = .53125F;
        var deltaV = maxV - minV;
        var vPerPart = deltaV / layerCount;
        maxV = minV + vPerPart * (part + 1);
        minV = minV + vPerPart * part;
        // matrix
        vertex(worldrenderer, matrix, x2, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, maxV).normal(1, 0, 0).endVertex();
        // oldMatrix
        vertex(worldrenderer, oldMatrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(maxU, minV).normal(1, 0, 0).endVertex();
    }

    private static void addRightVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        var minU = .171875F;
        var maxU = .1875F;
        var minV = .03125F;
        var maxV = .53125F;
        var deltaV = maxV - minV;
        var vPerPart = deltaV / layerCount;
        maxV = minV + vPerPart * (part + 1);
        minV = minV + vPerPart * part;
        // matrix
        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x2, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();
        // oldMatrix
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(maxU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();
    }

    private static void addBottomVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        var minU = .171875F;
        var maxU = .328125F;
        var minV = 0F;
        var maxV = .03125F;
        var deltaV = maxV - minV;
        var vPerPart = deltaV / layerCount;
        maxV = minV + vPerPart * (part + 1);
        minV = minV + vPerPart * part;
        // oldMatrix
        vertex(worldrenderer, oldMatrix, x1, y2, z2).tex(maxU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();
        // newMatrix
        vertex(worldrenderer, matrix, x2, y1, z1).tex(minU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();
    }

    private static WorldRenderer vertex(WorldRenderer worldrenderer, Matrix4f matrix4f, float f, float g, float h) {
        var vector4f = new Vector4f(f, g, h, 1.0F);
        vector4f.transform(matrix4f);
        worldrenderer.pos(vector4f.x(), vector4f.y(), vector4f.z());
        return worldrenderer;
    }

    private static void addTopVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        var minU = .015625F;
        var maxU = .171875F;
        var minV = 0F;
        var maxV = .03125F;
        var deltaV = maxV - minV;
        var vPerPart = deltaV / layerCount;
        maxV = minV + vPerPart * (part + 1);
        minV = minV + vPerPart * part;
        // oldMatrix
        vertex(worldrenderer, oldMatrix, x1, y2, z1).tex(maxU, maxV).normal(0, 1, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(minU, maxV).normal(0, 1, 0).endVertex();
        // newMatrix
        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, minV).normal(0, 1, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y1, z2).tex(maxU, minV).normal(0, 1, 0).endVertex();
    }

    private static float easeOutSine(float x) {
        return (float) Math.sin(x * Math.PI / 2f);
    }
}
