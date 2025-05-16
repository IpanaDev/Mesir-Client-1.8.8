package ipana.modules.movement;

import ipana.events.EventMoving;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.ModeValue;

import ipana.utils.player.PlayerUtils;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class FastLadder extends Module {

    public FastLadder() {
        super("FastLadder", Keyboard.KEY_NONE, Category.Movement, "Climb ladders faster.");
    }

    private EnumValue<LadderMode> mode = new EnumValue<>("Mode",this, LadderMode.class,"ladder methods.");

    private static final double SPEED_3_11_1 = 0.29-1E-8;
    private static final double SPEED_3_13_7 = 0.2872;
    private boolean onLadder;

    private Listener<EventMoving> onMove = new Listener<>(event -> {
        if (mc.thePlayer.isOnLadder()) {
            boolean troll = mode.getValue() == LadderMode.NCP311;
            double upwards = troll ? mc.thePlayer.onGround ? 1.34 : SPEED_3_11_1 : SPEED_3_13_7;
            double downwards = troll ? 1 : SPEED_3_13_7;
            if (troll) {
                mc.thePlayer.stepHeight = 9.9f;
                onLadder = true;
            }
            if (mc.gameSettings.keyBindJump.pressed) {
                mc.thePlayer.motionY = upwards;
            } else if (mc.gameSettings.keyBindSneak.pressed) {
                mc.thePlayer.motionY = -downwards;
            } else if (!PlayerUtils.isMoving2()) {
                mc.thePlayer.motionY = 0;
            }
            event.setY(mc.thePlayer.motionY);
        } else if (onLadder) {
            mc.thePlayer.stepHeight = 0.6f;
            onLadder = false;
        }
    });

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {

    });

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6f;
        super.onDisable();
    }
    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().enumName());
        super.onSuffixChange();
    }

    enum LadderMode {
        NCP311("3.11.1"),
        NCP313("3.13.7");

        private String enumName;

        LadderMode(String enumName) {
            this.enumName = enumName;
        }

        public String enumName() {
            return enumName;
        }

    }
}
