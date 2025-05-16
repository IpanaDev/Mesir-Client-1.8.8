package ipana.utils.tail;

import ipana.utils.font.FontHelper;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Tail3D {

    private List<Bone> bones = new ArrayList<>();
    private long boneMs;
    private int boneTicks;
    private Minecraft mc = Minecraft.getMinecraft();
    private Color color1 = new Color(0, 167, 255);
    private Color color2 = new Color(255, 0, 234);
    private long ms;
    private float tailWidth = 10f;
    private double[] renderPosition = new double[3];
    private double[] translates = new double[3];

    public void render(boolean depth, boolean moveCheck) {
        EntityPlayerSP p = mc.thePlayer;
        double x = renderPosition[0];
        double y = renderPosition[1];
        double z = renderPosition[2];

        boolean m = !moveCheck || (p.posX != p.prevPosX || p.posY != p.prevPosY || p.posZ != p.prevPosZ);
        if (System.currentTimeMillis()-ms > boneMs && m) {
            bones.add(new Bone(x, y+0.1, z,boneTicks));
            ms = System.currentTimeMillis();
        }
        float colorR = color2.getRed();
        float colorG = color2.getGreen();
        float colorB = color2.getBlue();
        int alpha = 0;
        GlStateManager.pushMatrix();
        //GlStateManager.translate(x-mc.getRenderManager().renderPosX, y-mc.getRenderManager().renderPosY, z-mc.getRenderManager().renderPosZ);
        GlStateManager.translate(translates[0], translates[1], translates[2]);
        GL11.glEnable(GL_LINE_SMOOTH);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (depth) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }
        GL11.glLineWidth(tailWidth);
        GL11.glBegin(3);
        for (Bone bone : bones) {
            colorR += (color1.getRed()-color2.getRed()) / (float)bones.size();
            colorG += (color1.getGreen()-color2.getGreen()) / (float)bones.size();
            colorB += (color1.getBlue()-color2.getBlue()) / (float)bones.size();
            GlStateManager.color(colorR/255f,colorG/255f,colorB/255f, alpha/255f);
            alpha += 255f / bones.size();
            GL11.glVertex3d(bone.x - x, bone.y - y, bone.z - z);
        }
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
        GL11.glEnd();
        if (!depth) {
            GL11.glEnable(GL_DEPTH_TEST);
        }
        GL11.glDisable(GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public void update() {
        int i = 0;
        int size = bones.size();
        while (i < size) {
            Bone bone = bones.get(i);
            bone.time--;
            if (bone.time <= 0) {
                bones.remove(i);
                size -= 1;
            } else {
                i++;
            }
        }
    }

    public void setRenderPosition(double x, double y, double z) {
        renderPosition[0] = x;
        renderPosition[1] = y;
        renderPosition[2] = z;
    }
    public void setTranslates(double x, double y, double z) {
        translates[0] = x;
        translates[1] = y;
        translates[2] = z;
    }
    public void setBoneMs(int ms) {
        this.boneMs = ms;
    }

    public void setBoneTicks(int boneTicks) {
        this.boneTicks = boneTicks;
    }

    public void setTailWidth(float tailWidth) {
        this.tailWidth = tailWidth;
    }

    public void setColor1(Color color1) {
        this.color1 = color1;
    }

    public void setColor2(Color color2) {
        this.color2 = color2;
    }

    public int length() {
        return bones.size();
    }

    public void breakBones() {
        bones.clear();
    }

    class Bone {
        double x,y,z;
        int time;

        public Bone(double x, double y, double z, int time) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
        }
    }
}
