package ipana.modules.combat;

import ipana.Ipana;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.events.EventRender3D;
import ipana.managements.value.values.BoolValue;
import ipana.events.EventMoving;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.ModeValue;
import ipana.managements.value.values.NumberValue;
import ipana.modules.combat.modes.target.DefaultTarget;
import ipana.modules.combat.modes.target.EinsteinTarget;
import ipana.modules.combat.modes.target.TargetMode;
import ipana.modules.movement.Speed;
import ipana.modules.movement.modes.speed.NCPOnGround;
import ipana.modules.player.AutoDrink;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import ipana.utils.tail.Tail3D;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.awt.*;

public class Target extends Module {
    public Target() {
        super("AutoTarget", Keyboard.KEY_NONE, Category.Combat, "anan za baban za unio");
    }
    public ModeValue<TargetMode> mode = new ModeValue<>("Mode", this, "Modes.", DefaultTarget.class, EinsteinTarget.class);
    public NumberValue<Double> speed = new NumberValue<>("Speed",this,1.0,1.0,10.0,0.1, "Target speed.");
    public NumberValue<Float> angle = new NumberValue<>("StrafeAngle",this,70f,30f,180f,5f, "Angle of the strafe.");
    public BoolValue blink = new BoolValue("BlinkPrediction",this,false,"Predicting if the target uses blink.");
    public BoolValue sword = new BoolValue("Sword",this,true,"zaaaaaaaaa");
    public BoolValue safeWalk = new BoolValue("SafeWalk",this,true,"velet check");
    public BoolValue tailEffect = new BoolValue("Tail",this,false,"tail like esp");
    private EntityLivingBase target;

    private Listener<EventMoving> onMove = new Listener<EventMoving>(event -> mode.getValue().onMove(event)).filter(filter -> mode.getValue().check()).weight(Event.Weight.LOWEST);

    private Tail3D tail3D = new Tail3D();
    private Color color1 = new Color(255,255,255);
    private double x,y,z;
    private float yaw;
    private long ms;

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        if (tailEffect.getValue()) {
            KillAura ka = Modules.KILL_AURA;
            if (!ka.targets.isEmpty() && ka.isEnabled()) {
                if (target != ka.curTar) {
                    target = ka.curTar;
                    tail3D.breakBones();
                }
            } else {
                target = null;
                tail3D.breakBones();
            }
            if (target != null) {
                tail3D.setBoneMs(10);
                tail3D.setBoneTicks(15);
                tail3D.setTailWidth(7);
                tail3D.setColor1(color1);
                tail3D.setColor2(Ipana.getClientColor());
                tail3D.update();
            }
        }
        mode.getValue().onPre(event);
    });

    private Listener<EventPostUpdate> onPost = new Listener<>(e -> {

    });

    private Listener<EventRender3D> onRender = new Listener<EventRender3D>(event -> {
        if (System.currentTimeMillis()-ms >= 25) {
            if (target != null) {
                double[] c = PlayerUtils.calculate2(angle.getValue()/80f, yaw, 1);
                x = c[0];
                y = 1;
                z = c[1];
                yaw += 12;
            }
            ms = System.currentTimeMillis();
        }
        double posX = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX;
        double posY = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY;
        double posZ = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ;
        tail3D.setRenderPosition(x, y, z);
        tail3D.setTranslates(posX+x,posY,posZ+z);
        tail3D.render(false, false);
    }).filter(e -> tailEffect.getValue() && target != null);
}
