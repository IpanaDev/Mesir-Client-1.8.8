package ipana.modules.movement.modes.scaffold;

import ipana.events.EventMoving;
import ipana.events.EventPreUpdate;
import ipana.events.EventTravel;
import ipana.modules.movement.Scaffold;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

public class Legit extends ScaffoldMode {
    public Legit(Scaffold parent) {
        super("Legit", parent);
    }

    private boolean rotationsChanged;
    private float changedYaw;
    private float changedPitch;

    @Override
    public void onTravel(EventTravel event) {
        ItemStack itemstack = mc.thePlayer.inventory.getCurrentItem();
        if (!(itemstack != null && itemstack.getItem() instanceof ItemBlock)) {
            return;
        }
        float f = event.strafe() * event.strafe() + event.forward() * event.forward();
        if (f < 1.0E-4F) {
            return;
        }
        f = MathHelper.sqrt_float(f);

        if (f < 1.0F) {
            f = 1.0F;
        }

        f = 1 / f;
        float f1 = MathHelper.sin(mc.thePlayer.rotationYaw * (float)Math.PI / 180.0F);
        float f2 = MathHelper.cos(mc.thePlayer.rotationYaw * (float)Math.PI / 180.0F);
        double motionX = (event.strafe() * f * f2 - event.forward() * f * f1);
        double motionZ = (event.forward() * f * f2 + event.strafe() * f * f1);

        float yawX = (float) ((Math.atan2(0, -motionX) * 180.0D / (Math.PI)) - 90.0F);
        float yawZ = (float) ((Math.atan2(-motionZ, 0) * 180.0D / (Math.PI)) - 90.0F);
        EnumFacing faceX = EnumFacing.fromAngle(yawX);
        EnumFacing faceZ = EnumFacing.fromAngle(yawZ);
        BlockPos wantedPos = null;
        EnumFacing wantedFace = null;
        var underPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1E-4, mc.thePlayer.posZ);
        var state = mc.theWorld.getBlockState(underPos);
        var block = state.getBlock();
        if (block instanceof BlockAir) {
            var posX = underPos.offset(faceX);
            var stateX = mc.theWorld.getBlockState(posX);
            var blockX = stateX.getBlock();
            if (blockX.isFullBlock()) {
                wantedPos = posX;
                wantedFace = faceX;
            }
            if (wantedPos == null) {
                var posZ = underPos.offset(faceZ);
                var stateZ = mc.theWorld.getBlockState(posZ);
                var blockZ = stateZ.getBlock();
                if (blockZ.isFullBlock()) {
                    wantedPos = posZ;
                    wantedFace = faceZ;
                }
            }
        }

        var bbs = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -1E-4, 0.0));
        EnumFacing nextFace = (Math.abs(motionX) > Math.abs(motionZ) ? faceX : faceZ).getOpposite();
        BlockPos nextPos = underPos.offset(nextFace);
        if (mc.theWorld.getBlockState(nextPos).getBlock() instanceof BlockAir) {
            for (var bb : bbs) {
                nextPos = new BlockPos(bb.minX, bb.minY, bb.minZ);
            }
        }

        float blockYaw = mc.thePlayer.rotationYaw + RotationUtils.yawTo(mc.thePlayer.rotationYaw,
                nextPos.getX() + 0.5 + nextFace.getFrontOffsetX() * 1.5,
                nextPos.getZ() + 0.5 + nextFace.getFrontOffsetZ() * 1.5);

        event.setYaw(changedYaw = blockYaw);
        changedPitch = mc.thePlayer.rotationPitch;
        rotationsChanged = true;

        if (wantedPos != null) {
            double x = wantedPos.getX() + 0.5 - wantedFace.getFrontOffsetX() * 0.5;
            double y = wantedPos.getY() + 0.5 - (mc.thePlayer.getEyeHeight() - 0.6);
            double z = wantedPos.getZ() + 0.5 - wantedFace.getFrontOffsetZ() * 0.5;
            float[] rotations = RotationUtils.getRotationFromPosition(x, z, y);

            float yaw = changedYaw - 180;
            var mouseOver = PlayerUtils.getMouseOver(yaw, rotations[1], false);
            if (mouseOver != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockpos = mouseOver.getBlockPos();
                double xDiff = x - mouseOver.hitVec.xCoord;
                double yDiff = Math.abs(y - mouseOver.hitVec.yCoord);
                double zDiff = z - mouseOver.hitVec.zCoord;
                double hDist = Math.hypot(xDiff, zDiff);
                if (hDist <= 0.6 && yDiff <= mc.thePlayer.height) {
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemstack, blockpos, mouseOver.sideHit, mouseOver.hitVec)) {

                        event.setYaw(changedYaw = yaw);
                        changedPitch = rotations[1];

                        mc.thePlayer.setSprinting(false);
                        event.setForward(-event.forward());
                        event.setStrafe(-event.strafe());

                        mc.thePlayer.swingItem();
                    }
                }
            }
        }
    }

    @Override
    public void onMove(EventMoving event) {

        //tick
        //move
        //pre

        double speed = Math.hypot(event.getX(), event.getZ());
        boolean isOnGround = !mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -1E-4, 0.0)).isEmpty();
        boolean isNextOnGround = !mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(event.getX(), -1E-4, event.getZ())).isEmpty();


        if (isOnGround && !isNextOnGround) {
            mc.thePlayer.movementInput.sneak = true;
            mc.thePlayer.setSneaking(true);
            mc.thePlayer.setSprinting(false);
            event.setX(event.getX() * 0.3);
            event.setZ(event.getZ() * 0.3);
        }
        mc.thePlayer.motionX = event.getX();
        mc.thePlayer.motionZ = event.getZ();
    }

    @Override
    public void onPre(EventPreUpdate event) {
        if (rotationsChanged) {
            event.setYaw(changedYaw);
            event.setPitch(changedPitch);
            rotationsChanged = false;
        }
        super.onPre(event);
    }
}
