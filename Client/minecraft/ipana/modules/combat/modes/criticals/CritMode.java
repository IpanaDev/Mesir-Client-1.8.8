package ipana.modules.combat.modes.criticals;

import ipana.events.EventPacketSend;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Module;
import ipana.managements.value.Mode;
import ipana.modules.combat.Criticals;
import net.minecraft.client.Minecraft;

public class CritMode extends Mode<Criticals> {
    protected Minecraft mc = Minecraft.getMinecraft();

    public CritMode(String name, Criticals parent) {
        super(name, parent);
    }


    public void onPre(EventPreUpdate event) {

    }
    public void onSend(EventPacketSend event) {

    }
}
