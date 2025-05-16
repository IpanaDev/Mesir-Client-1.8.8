package ipana.utils.gamepad;

import ipana.Ipana;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

import static ipana.managements.module.Modules.*;
import static ipana.managements.module.Modules.FLY;

public class GamePadManager {
    private static List<GamePad> pads = new ArrayList<>();
    private static Minecraft mc = Minecraft.getMinecraft();

    public static void initGamePads() {
        pads.clear();
        if (ControllerEnvironment.getDefaultEnvironment() != null) {
            for (Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
                if (controller.getName().startsWith("Controller") && controller.getType() == Controller.Type.GAMEPAD) {
                    String rawName = controller.getName().replace("Controller (", "").replace(")", "");
                    System.out.println("Added Controller: " + rawName);
                    pads.add(new GamePad(rawName, controller));
                }
            }
        }
    }
    private static boolean preA,preB,preX,preY,preRT;
    private static GamePad.PovDirection prePOV;

    public static void manage() {
        boolean a = Ipana.getPad().buttonA();
        boolean b = Ipana.getPad().buttonB();
        boolean x = Ipana.getPad().buttonX();
        boolean y = Ipana.getPad().buttonY();
        boolean RT = Ipana.getPad().rightThumb();
        boolean LT = Ipana.getPad().leftThumb();
        GamePad.PovDirection pov = Ipana.getPad().pov();
        if (!preB && b) {
            KILL_AURA.toggle();
        }
        if (!preA && a) {
            SPEED.toggle();
        }
        if (!preX && x) {
            PHASE.toggle();
        }
        if (!preY && y) {
            FLY.toggle();
        }
        if (!preRT && RT) {
            mc.clickMouse();
        }
        if (LT) {
            mc.rightClickMouse();
        }
        if (prePOV != GamePad.PovDirection.RIGHT && pov == GamePad.PovDirection.RIGHT) {
            mc.thePlayer.inventory.currentItem += 1;
            if (mc.thePlayer.inventory.currentItem >= 9) {
                mc.thePlayer.inventory.currentItem = 0;
            }
            mc.playerController.syncCurrentPlayItem();
        }
        if (prePOV != GamePad.PovDirection.LEFT && pov == GamePad.PovDirection.LEFT) {
            mc.thePlayer.inventory.currentItem -= 1;
            if (mc.thePlayer.inventory.currentItem < 0) {
                mc.thePlayer.inventory.currentItem = 8;
            }
            mc.playerController.syncCurrentPlayItem();
        }
        prePOV = pov;
        preRT = RT;
        preA = a;
        preB = b;
        preX = x;
        preY = y;
    }
    public static List<GamePad> getPads() {
        return pads;
    }
}
