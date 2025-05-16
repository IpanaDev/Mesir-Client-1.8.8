package ipana.modules.combat;

import ipana.events.EventClearItemUse;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.NumberValue;
import ipana.utils.player.PlayerUtils;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class FastBow extends Module {
    public FastBow() {
        super("FastBow", Keyboard.KEY_NONE,Category.Combat,"Shoot bow faster");
    }
    private NumberValue<Integer> time = new NumberValue<>("Time",this,16,1,25,1,"Shoot time");

    private Listener<EventPreUpdate> onPre = new Listener<>(e -> {
        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
            if (mc.thePlayer.getItemInUseDuration() == time.getValue()) {
                for (int i = 0; i < 21-time.getValue(); i++) {
                    PlayerUtils.skipTicks();
                }
                mc.playerController.onStoppedUsingItem(mc.thePlayer);
                if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                }
            }
        }
    });

    private Listener<EventClearItemUse> onClear = new Listener<>(event -> {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem()) {
            event.setCancelled(true);
        }
    });

}
