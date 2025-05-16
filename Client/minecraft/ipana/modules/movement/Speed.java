package ipana.modules.movement;

import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.ModeValue;
import ipana.modules.movement.modes.speed.*;
import ipana.utils.chunk.ChunkFacings;
import ipana.utils.net.Pinger;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.Arrays;

public class Speed extends Module {
    public ModeValue<SpeedMode> mode = new ModeValue<>("Mode", this, "Speed methods.", OldNCP.class, Fantasy.class, Hop3111.class, OldNCPHop.class, LatestNCPHop.class, NCPOnGround.class, Cold.class);
    public BoolValue korkunc = new BoolValue("Korkunc", this, true, "Korkunc speed.", () -> mode.getValue() instanceof Fantasy);
    public BoolValue ncp316 = new BoolValue("NCP 3.16",this, false,"NCP 3.16 mode.", () -> mode.getValue() instanceof NCPOnGround);
    public BoolValue autoW = new BoolValue("AutoW",this, false,"hako baba.");
    public BoolValue timer = new BoolValue("Timer",this, true,"Speed up the game a bit.");
    public double lastDist;
    private long ms;
    public double spd;
    public int ticks;
    private double idk;
    public boolean boost;
    public int za;

    public Speed() {
        super("Speed", Keyboard.KEY_B,Category.Movement,"Move faster.");
    }

    private Listener<EventMoving> onMoving = new Listener<>(event -> {
        mode.getValue().onMoving(event);
    });

    private Listener<EventMoveInput> onMove = new Listener<>(event -> {
        mode.getValue().onMove(event);
        if (autoW.getValue()) {
            event.setForward(mc.thePlayer.movementInput.moveForward = 0.98f);
            event.setStrafe(mc.thePlayer.movementInput.moveStrafe = 0.98f);
        }
    });

    private Listener<EventTick> onTick = new Listener<>(event -> mode.getValue().onTick(event));
    private Listener<EventFrame> onFrame = new Listener<>(event -> mode.getValue().onFrame(event));
    private Listener<EventExcuseMeWTF> onBeforeUpdate = new Listener<>(event -> mode.getValue().onBeforeUpdate(event));
    private Listener<EventSetBack> onSetBack = new Listener<>(event -> mode.getValue().onSetBack(event));
    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> mode.getValue().onReceive(event));
    private Listener<EventPacketSend> onSend = new Listener<>(event -> mode.getValue().onSend(event));

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        setSuffix(mode.getValue().getName());
        idk++;
        if (!mc.thePlayer.isCollidedHorizontally && PlayerUtils.isMoving()) {
            //PlayerUtils.debug(idk+" : "+(System.currentTimeMillis()-ms));
        } else {
            idk = 0;
            ms = System.currentTimeMillis();
        }
        mode.getValue().onPre(event);
        if (!PlayerUtils.isMoving2()) {
            mc.timer.timerSpeed = 1.0f;
        }
    });
    private Listener<EventPostUpdate> onPost = new Listener<>(event -> {
        mode.getValue().onPost(event);
    });
    private Listener<EventStep> onStep = new Listener<>(event -> {
        mode.getValue().onStep(event);
    });

    public double nextSpeed() {
        return mode.getValue().nextSpeed();
    }
    public double nextY() {
        return mode.getValue().nextY();
    }
    public void timerBoost() {
        //float TIMER = 1.1f;
        float TIMER = 1.089f;
        mc.timer.timerSpeed = timer.getValue() ? TIMER : 1;
    }


    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().getName());
        super.onSuffixChange();
    }

    @Override
    public void onEnable() {
        /*for (int i = 0; i < 1000; i++) {
            mc.thePlayer.sendChatMessage("/summon Pig ~ ~ ~ {Attributes:[{Name:generic.maxHealth,Base:10000f}],Health:10000.0f, NoAI:1b}");
        }*/
        mode.getValue().onEnable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        ticks = 0;
        boost = false;
        lastDist = 0;
        spd = PlayerUtils.getBaseMoveSpeed();
        za = 0;
        mode.getValue().onDisable();
        mc.thePlayer.stepHeight = 0.6f;
        super.onDisable();
    }
}
