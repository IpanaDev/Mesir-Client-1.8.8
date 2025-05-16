package ipana.modules.combat.modes.target;

import ipana.events.EventMoving;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.combat.Target;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;

import java.util.HashMap;

public class EinsteinTarget extends TargetMode {

    public EinsteinTarget(Target parent) {
        super("Einstein", parent);
    }

    @Override
    public void onMove(EventMoving event) {
        //TODO:

    }


}
