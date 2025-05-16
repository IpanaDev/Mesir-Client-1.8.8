package ipana.modules.player;

import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.Value;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.ModeValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class Nuker  extends Module {
    public Nuker() {
        super("Nuker", Keyboard.KEY_NONE,Category.Player,"Breaks blocks around you.");
    }

    private int posX;
    private int posY;
    private int posZ;
    private boolean breakBlock;
    private NumberValue<Integer> distance = new NumberValue<>("Distance",this,3,1,5,1,"Break distance.");
    private EnumValue<Mode> mode = new EnumValue<>("Mode",this,Mode.class,"Break block mode.");

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        this.breakBlock = false;
        BlockPos pos = getBlockPos();
        if (pos != null) {
            this.breakBlock = true;
            float[] angles = this.setRotations(pos, event);
            if (!(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).equals(pos))) {
                if (mode.getValue() == Mode.Legit)
                    return;
                mc.thePlayer.swingItem();
                //event.setCancelPackets(true);
                if (mc.thePlayer.isCollidedVertically) {
                    PlayerUtils.packet(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, angles[0], angles[1], true));
                }
                mc.playerController.onPlayerDamageBlock(pos, getFacing(pos));
            }
        }
    });

    public float[] setRotations(BlockPos pos, EventPreUpdate event) {
        float[] angles = getFacingRotations(this.posX, this.posY, this.posZ, getFacing(pos));
        if (event != null) {
            event.setYaw(angles[0]);
            event.setPitch(angles[1]);
        }
        mc.thePlayer.rotationYawHead = angles[0];
        mc.thePlayer.renderYawOffset = angles[0];
        mc.thePlayer.rotationPitchHead = angles[1];
        return angles;
    }

    public BlockPos getBlockPos() {
        int radius = distance.getValue();
        for (int y = radius; y > -radius; y--) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    this.posX = ((int) Math.floor(mc.thePlayer.posX) + x);
                    this.posY = ((int) Math.floor(mc.thePlayer.posY) + y);
                    this.posZ = ((int) Math.floor(mc.thePlayer.posZ) + z);
                    if (mc.thePlayer.getDistanceSq(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z) <= 16) {
                        Block block = getBlock(this.posX, this.posY, this.posZ);
                        boolean blockChecks = canSeeBlock(this.posX, this.posY, this.posZ) && !(block instanceof BlockAir);
                        /*blockChecks = (blockChecks) && ((block.getBlockHardness() != -1.0F) || (mc.playerController.isInCreativeMode()))*/
                        ;
                        if (blockChecks) {
                            return new BlockPos(this.posX, this.posY, this.posZ);
                        }
                    }
                }
            }
        }
        return null;
    }

    private Listener<EventPostUpdate> onPost = new Listener<EventPostUpdate>(event -> {
        mc.gameSettings.keyBindAttack.pressed = true;
    }).filter(filter -> this.breakBlock && !(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).equals(new BlockPos(posX, posY, posZ))) && mode.getValue() == Mode.Legit);

    public EnumFacing getFacing(BlockPos pos) {
        EnumFacing[] orderedValues = { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.DOWN };
        for (EnumFacing facing : orderedValues) {
            Entity temp = new EntitySnowball(mc.theWorld);
            temp.posX = (pos.getX() + 0.5D);
            temp.posY = (pos.getY() + 0.5D);
            temp.posZ = (pos.getZ() + 0.5D);
            temp.posX += facing.getDirectionVec().getX() * 0.5D;
            temp.posY += facing.getDirectionVec().getY() * 0.5D;
            temp.posZ += facing.getDirectionVec().getZ() * 0.5D;
            if (mc.thePlayer.canEntityBeSeen(temp)) {
                return facing;
            }
        }
        return null;
    }
    public boolean canSeeBlock(int x, int y, int z) {
        return getFacing(new BlockPos(x, y, z)) != null;
    }

    public Block getBlock(int x, int y, int z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public Block getBlock(double x, double y, double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }
    public float[] getFacingRotations(int x, int y, int z, EnumFacing facing) {
        Entity temp = new EntitySnowball(mc.theWorld);
        temp.posX = (x + 0.5D);
        temp.posY = (y + 0.5D);
        temp.posZ = (z + 0.5D);
        temp.posX += facing.getDirectionVec().getX() * 0.25D;
        temp.posY += facing.getDirectionVec().getY() * 0.25D;
        temp.posZ += facing.getDirectionVec().getZ() * 0.25D;
        return getAngles(temp);
    }
    public float[] getAngles(Entity e) {
        return new float[] {getYawChangeToEntity(e) + mc.thePlayer.rotationYaw, getPitchChangeToEntity(e) + mc.thePlayer.rotationPitch };
    }
    public float getYawChangeToEntity(Entity entity) {
        double deltaX = entity.posX - mc.thePlayer.posX;
        double deltaZ = entity.posZ - mc.thePlayer.posZ;
        double yawToEntity;
        if ((deltaZ < 0.0D) && (deltaX < 0.0D))
        {
            yawToEntity = 90.0D + Math.toDegrees(Math.atan(deltaZ / deltaX));
        } else {
            if ((deltaZ < 0.0D) && (deltaX > 0.0D)) {
                yawToEntity = -90.0D + Math.toDegrees(Math.atan(deltaZ / deltaX));
            } else {
                yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
            }
        }
        return MathHelper.wrapAngleTo180_float(-(mc.thePlayer.rotationYaw - (float)yawToEntity));
    }

    public float getPitchChangeToEntity(Entity entity) {
        double deltaX = entity.posX - mc.thePlayer.posX;
        double deltaZ = entity.posZ - mc.thePlayer.posZ;
        double deltaY = entity.posY - 1.6D + entity.getEyeHeight() - mc.thePlayer.posY;
        double distanceXZ = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);

        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ));

        return -MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch - (float)pitchToEntity);
    }

    enum Mode {
        Legit, Blatant
    }
}
