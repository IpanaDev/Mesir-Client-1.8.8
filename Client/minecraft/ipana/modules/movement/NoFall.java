package ipana.modules.movement;

import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.List;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", Keyboard.KEY_NONE, Category.Movement, "No fall damage.");
    }
    private BoolValue antiVoid = new BoolValue("AntiVoid", this, false, "Prevent from falling to the void.");
    private double x,y,z;

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        List<AxisAlignedBB> collidingList2 = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -1E-4, 0));
        if (!collidingList2.isEmpty()) {
            x = mc.thePlayer.posX;
            y = mc.thePlayer.posY;
            z = mc.thePlayer.posZ;
        }
        if (mc.thePlayer.fallDistance > 2.5f) {
            event.setOnGround(true);
            if (antiVoid.getValue()) {
                boolean atat = false;
                for (int i = 0; i < 10; i++) {
                    List<AxisAlignedBB> collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -i, 0.0D));
                    if (!collidingList.isEmpty()) {
                        atat = true;
                        break;
                    }
                }
                if (!atat) {
                    mc.thePlayer.fallDistance = 0;
                    mc.thePlayer.motionY = 0;
                    event.setX(x);
                    event.setY(y);
                    event.setZ(z);
                }
            }
        }
    }).weight(Event.Weight.HIGHEST);
}
