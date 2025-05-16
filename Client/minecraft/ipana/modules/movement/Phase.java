package ipana.modules.movement;

import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.ModeValue;
import ipana.modules.movement.modes.phase.*;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class Phase extends Module {
    public ModeValue<PhaseMode> mode = new ModeValue<>("Mode", this, "Phase methods.", NCP311.class, NCP313.class, NCP316.class, EfsaneCraft.class);
    public BoolValue passable = new BoolValue("Passable",this,false,"Passable.", () -> mode.getValue() instanceof NCP316);
    public BoolValue freePlace = new BoolValue("FreePlace",this,false,"Free Place.");
    public BoolValue fastMove = new BoolValue("FastMove",this,true,"Fast Move.", () -> mode.getValue() instanceof NCP311);

    public Phase() {
        super("Phase", Keyboard.KEY_N,Category.Movement,"Going through blocks.");
    }

    private Listener<EventMoving> onMove = new Listener<EventMoving>(event -> mode.getValue().onMove(event)).weight(Event.Weight.LOWEST);

    private Listener<EventExcuseMeWTF> onNbr = new Listener<>(event -> mode.getValue().onBeforePre(event));

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        setSuffix(mode.getValue().getName());
        mode.getValue().onPre(event);
    });
    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().getName());
        super.onSuffixChange();
    }
    private Listener<EventPostUpdate> onPost = new Listener<>(event -> mode.getValue().onPost(event));

    private Listener<EventBoundingBox> onEBB = new Listener<>(event -> mode.getValue().onBB(event));

    private Listener<EventMoveInput> pos = new Listener<>(event -> mode.getValue().onMove(event));

    @Override
    public void onEnable() {
        /*for (int i = 0; i < 1000; i++) {
            PlayerUtils.packet(new C01PacketChatMessage("/summon Pig ~ ~ ~ {NoAI:1b}"));
        }*/
        //mc.thePlayer.inventory.setCurrentItem(new ItemStack(Items.diamond_sword));
        //PlayerUtils.packet(new C17PacketCustomPayload("WinterNW|SpeedPatch", new PacketBuffer(Unpooled.buffer())));
        mode.getValue().onEnable();
        super.onEnable();
    }

    private Listener<EventPacketSend> onSend = new Listener<>(event -> mode.getValue().onSend(event));
    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> mode.getValue().onReceive(event));
    private Listener<EventSetBack> onSetBack = new Listener<>(event -> mode.getValue().onSetBack(event));
    private Listener<EventFrame> onFrame = new Listener<>(event -> mode.getValue().onFrame(event));

    @Override
    public void onDisable() {
        mode.getValue().onDisable();
        //PlayerUtils.fixInventory();
        mc.thePlayer.noClip = false;
        mc.timer.timerSpeed = 1.0F;
        mc.thePlayer.stepHeight = 0.6f;
        super.onDisable();
    }
}
