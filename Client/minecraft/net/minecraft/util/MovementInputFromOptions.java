package net.minecraft.util;

import ipana.Ipana;
import ipana.managements.module.ModuleManager;
import ipana.managements.module.Modules;
import ipana.modules.player.InvMove;
import ipana.modules.render.Camera;
import ipana.renders.ide.IDEScreen;
import ipana.utils.gamepad.GamePad;
import ipana.utils.gamepad.GamePadManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState() {
        if ((Modules.INV_MOVE.isEnabled()) && (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat) && !(Minecraft.getMinecraft().currentScreen instanceof IDEScreen))) {
            this.moveStrafe = 0.0F;
            this.moveForward = 0.0F;
            Camera camera = Modules.CAMERA;
            if (!camera.isEnabled() || !camera.listening) {
                moveForward += GamePadManager.getPads().isEmpty() ? 0 : Ipana.getPad().rightTrigger()+Ipana.getPad().leftTrigger();

                if (Keyboard.isKeyDown(this.gameSettings.keyBindForward.getKeyCode())) {
                    this.moveForward += 1.0F;
                }
                if (Keyboard.isKeyDown(this.gameSettings.keyBindBack.getKeyCode())) {
                    this.moveForward -= 1.0F;
                }
                if (Keyboard.isKeyDown(this.gameSettings.keyBindLeft.getKeyCode())) {
                    this.moveStrafe += 1.0F;
                }
                if (Keyboard.isKeyDown(this.gameSettings.keyBindRight.getKeyCode())) {
                    this.moveStrafe -= 1.0F;
                }
                this.jump = Keyboard.isKeyDown(this.gameSettings.keyBindJump.getKeyCode()) || (!GamePadManager.getPads().isEmpty() &&  Ipana.getPad().pov() == GamePad.PovDirection.UP);
                this.sneak = this.gameSettings.keyBindSneak.isKeyDown() || (!GamePadManager.getPads().isEmpty() && Ipana.getPad().pov() == GamePad.PovDirection.DOWN);

                if (this.sneak) {
                    this.moveStrafe = (float) ((double) this.moveStrafe * 0.3D);
                    this.moveForward = (float) ((double) this.moveForward * 0.3D);
                }
            }
        } else {
            this.moveStrafe = 0.0F;
            this.moveForward = 0.0F;
            Camera camera = Modules.CAMERA;
            if (!camera.isEnabled() || !camera.listening) {
                moveForward += GamePadManager.getPads().isEmpty() ? 0 : Ipana.getPad().rightTrigger()+Ipana.getPad().leftTrigger();
                if (this.gameSettings.keyBindForward.isKeyDown()) {
                    this.moveForward += 1.0F;
                }
                if (this.gameSettings.keyBindBack.isKeyDown()) {
                    this.moveForward -= 1.0F;
                }
                if (this.gameSettings.keyBindLeft.isKeyDown()) {
                    this.moveStrafe += 1.0F;
                }
                if (this.gameSettings.keyBindRight.isKeyDown()) {
                    this.moveStrafe -= 1.0F;
                }
            }
            this.jump = this.gameSettings.keyBindJump.isKeyDown() || (!GamePadManager.getPads().isEmpty() && Ipana.getPad().pov() == GamePad.PovDirection.UP);
            this.sneak = this.gameSettings.keyBindSneak.isKeyDown() || (!GamePadManager.getPads().isEmpty() && Ipana.getPad().pov() == GamePad.PovDirection.DOWN);
            if (this.sneak)
            {
                this.moveStrafe = ((float)(this.moveStrafe * 0.3D));
                this.moveForward = ((float)(this.moveForward * 0.3D));
            }
        }
    }
}
