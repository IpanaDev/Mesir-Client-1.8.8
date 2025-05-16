package ipana.modules.movement;

import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.ModeValue;
import ipana.modules.movement.modes.scaffold.Blatant;
import ipana.modules.movement.modes.scaffold.Legit;
import ipana.modules.movement.modes.scaffold.ScaffoldMode;
import ipana.utils.player.RotationUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold", Keyboard.KEY_ADD, Category.Movement, "Place blocks to under you.");
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindUseItem.pressed = false;
        super.onDisable();
    }
    public ModeValue<ScaffoldMode> mode = new ModeValue<>("Mode", this, "Scaffold modes.", Blatant.class, Legit.class);
    public EnumValue<LookMode> lookMode = new EnumValue<>("LookMode", this, LookMode.class, "Looking methods.");
    public BoolValue sneak = new BoolValue("Sneak", this, false, "Sneaks when place the block.");
    public BoolValue sprint = new BoolValue("Sprint", this, true, "Keep sprinting while placing blocks.");
    public BoolValue safeWalk = new BoolValue("SafeWalk", this, true, "Don't fall from block.");

    private Listener<EventTick> onTick = new Listener<>(event -> mode.getValue().onTick(event));
    private Listener<EventTravel> onTravel = new Listener<>(event -> mode.getValue().onTravel(event));
    private Listener<EventMoving> onMove = new Listener<EventMoving>(event -> mode.getValue().onMove(event)).weight(Event.Weight.LOW);
    private Listener<EventPreUpdate> onPre = new Listener<>(event -> mode.getValue().onPre(event));
    private Listener<EventPostUpdate> onPost = new Listener<>(event -> mode.getValue().onPost(event));
    private Listener<EventRender3D> onRender3D = new Listener<>(event -> mode.getValue().onRender(event));


    @Override
    public void onSuffixChange() {
        super.onSuffixChange();
    }

    public enum LookMode {
        Forward, Backward
    }
}
