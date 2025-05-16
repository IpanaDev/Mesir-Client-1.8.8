package ipana.utils.gamepad;

import ipana.Ipana;
import ipana.managements.module.ModuleManager;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GamePadRender {
    private static ResourceLocation pad = new ResourceLocation("mesir/gamepads/Gamepad F310/Gamepad F310.png");
    private static ResourceLocation stick = new ResourceLocation("mesir/gamepads/Gamepad F310/STICK.png");
    private static ResourceLocation pov = new ResourceLocation("mesir/gamepads/Gamepad F310/POV.png");
    private static ResourceLocation lt = new ResourceLocation("mesir/gamepads/Gamepad F310/LT.png");
    private static ResourceLocation rt = new ResourceLocation("mesir/gamepads/Gamepad F310/RT.png");
    private static ResourceLocation A = new ResourceLocation("mesir/gamepads/Gamepad F310/A.png");
    private static ResourceLocation B = new ResourceLocation("mesir/gamepads/Gamepad F310/B.png");
    private static ResourceLocation X = new ResourceLocation("mesir/gamepads/Gamepad F310/X.png");
    private static ResourceLocation Y = new ResourceLocation("mesir/gamepads/Gamepad F310/Y.png");

    public static void render(int x, int y) {
        if (!GamePadManager.getPads().isEmpty()) {
            ScaledResolution sr = RenderUtils.SCALED_RES;
            GamePad gamePad = Ipana.getPad();
            int width = 150;
            int height = 90;

            RenderUtils.drawImage(x,y,width,height,pad);
            drawButtons(gamePad,x,y,width,height);
            drawSticks(gamePad,x,y,width,height);
            drawThumbs(gamePad,x,y,width,height);
        }
    }
    private static void drawSticks(GamePad gamePad, int x, int y, int width, int height) {
        float[] left = gamePad.leftStick();
        float[] right = gamePad.rightStick();
        RenderUtils.drawImage(x+left[0]*3f,y+left[1]*3f,width,height,stick);
        RenderUtils.drawImage(x+right[0]*3f+35.5,y+right[1]*3f,width,height,stick);
    }
    private static void drawThumbs(GamePad gamePad, int x, int y, int width, int height) {
        if (gamePad.leftThumb()) {
            RenderUtils.drawImage(x,y,width,height,lt);
        }
        if (gamePad.rightThumb()) {
            RenderUtils.drawImage(x,y,width,height,rt);
        }
    }

    private static void drawButtons(GamePad gamePad, int x, int y, int width, int height) {
        if (gamePad.buttonA()) {
            RenderUtils.drawImage(x,y,width,height,A);
        }
        if (gamePad.buttonB()) {
            RenderUtils.drawImage(x,y,width,height,B);
        }
        if (gamePad.buttonX()) {
            RenderUtils.drawImage(x,y,width,height,X);
        }
        if (gamePad.buttonY()) {
            RenderUtils.drawImage(x,y,width,height,Y);
        }

    }
}
