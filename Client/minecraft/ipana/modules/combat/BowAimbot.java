package ipana.modules.combat;

import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.friend.FriendManager;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.utils.net.Pinger;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BowAimbot extends Module {
    private List<TickPos> differences = new ArrayList<>();
    private EntityLivingBase prevTarget;
    private boolean canShoot;
    private int bowTicks;

    public BowAimbot() {
        super("BowAimbot", Keyboard.KEY_NONE, Category.Combat,"Auto aims to closest enemy");
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        if (hasBow() && mc.thePlayer.isUsingItem()) {
            List<EntityLivingBase> entities = load();
            if (!entities.isEmpty()) {
                EntityLivingBase target = entities.get(0);
                if (prevTarget != target) {
                    differences.clear();
                }
                int smoothness = 1;
                double xDiff = (target.posX - target.lastTickPosX);
                double zDiff = (target.posZ - target.lastTickPosZ);
                differences.removeIf(t -> Minecraft.getRunTick()-t.tick >= smoothness);
                differences.add(new TickPos(Minecraft.getRunTick(), xDiff, zDiff));
                xDiff = 0;
                zDiff = 0;
                for (TickPos tickPos : differences) {
                    xDiff += tickPos.x;
                    zDiff += tickPos.z;
                }
                xDiff /= differences.size();
                zDiff /= differences.size();
                float distance = mc.thePlayer.getHorizontalDistanceToEntity(target);
                float yaw0 = RotationUtils.getRotations(target)[0] - 180;
                float yaw1 = (float) ((Math.atan2(zDiff, xDiff) * 180.0D / (Math.PI)) - 90.0F);
                float angleA = RotationUtils.getDistanceBetweenAngles(yaw0, yaw1);
                double cosA = Math.cos(angleA);
                double arrowHSpeed = getArrowHSpeed(20);
                double hSpeed = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                //Based on: https://media.discordapp.net/attachments/1345134000611786844/1370457386674028665/image.png?ex=681f9166&is=681e3fe6&hm=eef3cca450c528a946c5bf21d6396594a46698fbaabe6b1765441450f6ad0843&=&format=webp&quality=lossless
                //ax^2 + bx + c --->
                //(hSpeed^2 - aSpeed^2)*X^2 + (-2*hSpeed*distance*cosA)*X + distance^2
                double a = hSpeed*hSpeed - arrowHSpeed*arrowHSpeed;
                double b = -2 * hSpeed * distance * cosA;
                double c = distance * distance;
                double delta = b*b - 4*a*c;
                double X = -2173;
                if (delta > 0) {
                    //X = (-b + Math.sqrt(delta)) / (2*a);
                    X = (-b - Math.sqrt(delta)) / (2*a);
                } else if (delta == 0) {
                    X = -b/(2*a);
                }

                if (X == -2173) {
                    PlayerUtils.debug("Unable to shoot.");
                    return;
                }
                double pingDiff = Pinger.ping() / 50.0 + 1;
                X += pingDiff;

                double estimatedArrowDistance = 3*X;
                double calculatedArrowDistance = getArrowDistance(Math.round(X - 1));

                X *= estimatedArrowDistance / calculatedArrowDistance;
                //PlayerUtils.debug("Ticks: "+X);
                double x = target.posX + xDiff * X;
                double z = target.posZ + zDiff * X;
                double y = target.posY;
                float[] rotations = RotationUtils.getRotationFromPosition(x, z, y);
                if (Float.isNaN(rotations[0]) || Float.isNaN(rotations[1])) {
                    return;
                }
                event.setYaw(rotations[0]);
                event.setPitch(rotations[1] - distance / 5f + 0.75f);//distance / 5 only usable for bow strength 20
                mc.thePlayer.rotationYawHead = event.getYaw();
                mc.thePlayer.renderYawOffset = event.getYaw();
                mc.thePlayer.rotationPitchHead = event.getPitch();
                canShoot = true;
                prevTarget = target;
            }
        }
    });

    private Listener<EventPostUpdate> onPost = new Listener<>(event -> {
        if (canShoot) {
            if (mc.thePlayer.getItemInUseDuration() >= 20) {
                mc.playerController.onStoppedUsingItem(mc.thePlayer);
                if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                }
            }
            canShoot = false;
        }
    });

    private double getArrowHSpeed(int ticks) {
        float f = ticks / 20.0F;
        f *= f + 2.0f;
        if (f < 0.3f) {
            return 0;
        }
        if (f > 3.0F) {
            f = 3.0F;
        }
        return f;
    }

    private double getArrowDistance(long X) {
        if (X <= 0)
            return 3;

        return getArrowDistance(X - 1) + getArrowDistance(0) * Math.pow(0.99, X);
    }


    private List<EntityLivingBase> load() {
        List<EntityLivingBase> entityList = new ArrayList<>();
        for (EntityLivingBase entityLivingBase : mc.theWorld.livingEntities) {
            if (entityLivingBase.getHealth() > 0 && entityLivingBase != mc.thePlayer && entityLivingBase instanceof EntityPlayer && !FriendManager.isFriend(entityLivingBase.getName())) {//TODO: more checks
                entityList.add(entityLivingBase);
            }
        }
        entityList.sort(Comparator.comparingDouble(ent -> {
            float[] rotations = RotationUtils.getRotations(ent);
            double xDistance = RotationUtils.getDistanceBetweenAngles(rotations[0], mc.thePlayer.rotationYaw);
            double yDistance = RotationUtils.getDistanceBetweenAngles(rotations[1], mc.thePlayer.rotationPitch);
            return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        }));
        return entityList;
    }

    private boolean hasBow() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow;
    }

    class TickPos {
        int tick;
        double x, z;

        public TickPos(int tick, double x, double z) {
            this.tick = tick;
            this.x = x;
            this.z = z;
        }
    }
}
