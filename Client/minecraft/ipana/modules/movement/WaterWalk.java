package ipana.modules.movement;

import ipana.managements.module.Modules;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.ModeValue;
import ipana.modules.movement.modes.waterwalk.*;
import ipana.modules.movement.modes.waterwalk.WaterMode;
import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;


public class WaterWalk extends Module {
    public WaterWalk() {
        super("WaterWalk", Keyboard.KEY_J,Category.Movement,"Walk on liquids");
    }
    public ModeValue<WaterMode> mode = new ModeValue<>("Mode",this,"Fly methods.", NCP311.class, NCP313.class, Dolphin.class, Mini.class);

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> mode.getValue().onPre(event));
    private Listener<EventPostUpdate> onPost = new Listener<>(event -> mode.getValue().onPost(event));
    private Listener<EventMoving> onMove = new Listener<>(event -> mode.getValue().onMove(event));
    private Listener<EventPacketSend> onSend = new Listener<>(event -> mode.getValue().onSend(event));
    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> mode.getValue().onReceive(event));
    private Listener<EventBoundingBox> onBB = new Listener<>(event -> mode.getValue().onBB(event));

    @Override
    public void onEnable() {
        mode.getValue().onEnable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mode.getValue().onDisable();
        super.onDisable();
    }
    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().getName());
        super.onSuffixChange();
    }
}
