package ipana.utils.render;

import net.minecraft.client.gui.Gui;

import java.awt.*;

import static ipana.utils.render.EffectRenderUtils.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

public class MultipleGLScissor {
    private static final int max = 100;
    private static MultipleGLScissor[] objects = new MultipleGLScissor[max];
    private static int lastObject = -1;

    private int index;

    private float left;
    private float right;
    private float top;
    private float bottom;

    public MultipleGLScissor(float x, float y, float width, float height)
    {
        lastObject++;
        if (lastObject < max)
        {
            index = lastObject;
            objects[index] = this;

            left = x;
            right = x + width - 1;
            top = y;
            bottom = y + height - 1;

            if (index > 0)
            {
                MultipleGLScissor parent = objects[index - 1];

                if (left < parent.left) left = parent.left;
                if (right > parent.right) right = parent.right;
                if (top < parent.top) top = parent.top;
                if (bottom > parent.bottom) bottom = parent.bottom;
            }

            resume();
        }
        else
        {
            System.out.println("Scissor count limit reached: " + max);
        }
    }

    private void resume()
    {
        glScissor((int)left, (int)top, (int)right - (int)left + 1, (int)bottom - (int)top + 1);
        glEnable(GL_SCISSOR_TEST);
    }

    public void destroy()
    {
        if (index < lastObject)
        {
            System.out.println("There are scissors below this one");
        }

        glDisable(GL_SCISSOR_TEST);

        objects[index] = null;
        lastObject--;

        if (lastObject > -1)
            objects[lastObject].resume(); // Resuming previous scissor
    }

    protected void finalize()
    {
        destroy();
    }

    private static void glScissor(int x, int y, int width, int height)
    {
        if (width < 0) width = 0;
        if (height < 0) height = 0;

        org.lwjgl.opengl.GL11.glScissor(x, y, width, height);
    }
    public static void rectBlurry(float x, float y, float x1, float y1){
        Gui.drawRect((int)x, (int)y, (int)x1, (int)y1, new Color(1,1,1,1).getRGB());
        blurArea((int)x, (int)y, (int)x1 - (int)x, (int)y1 - (int)y, 10,1,0);
        blurArea((int)x, (int)y, (int)x1 - (int)x, (int)y1 - (int)y, 10,0,1);
    }
    public static void rectEffect(float x, float y, float x1, float y1,String effect){
        Gui.drawRect((int)x, (int)y, (int)x1, (int)y1, new Color(1,1,1,1).getRGB());
        EffectRenderUtils r = new EffectRenderUtils(effect);
        r.effectArea((int)x, (int)y, (int)x1 - (int)x, (int)y1 - (int)y);
    }
    public static void rectPhosphor(float x, float y, float x1, float y1,float phosphorMultiplier){
        Gui.drawRect((int)x, (int)y, (int)x1, (int)y1, new Color(1,1,1,1).getRGB());
        EffectRenderUtils r = new EffectRenderUtils("phosphor");
        r.phosphorArea((int)x, (int)y, (int)x1 - (int)x, (int)y1 - (int)y,phosphorMultiplier);
    }
}
