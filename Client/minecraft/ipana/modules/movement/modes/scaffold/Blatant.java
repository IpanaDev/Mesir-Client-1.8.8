package ipana.modules.movement.modes.scaffold;

import ipana.events.EventMoving;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.events.EventRender3D;
import ipana.modules.movement.Scaffold;
import ipana.utils.player.RotationUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Blatant extends ScaffoldMode {
    public Blatant(Scaffold parent) {
        super("Blatant", parent);
    }

    private BlockData blockData;
    private boolean sneaked;

    @Override
    public void onMove(EventMoving event) {
        //TODO: no idea if it can see the side where he wants to place so make a raytrace check
        double m = 2;
        List<AxisAlignedBB> bb = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, -0.0625, 0));
        List<AxisAlignedBB> bbOffset = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(event.getX()*m, -0.0625, event.getZ()*m));
        BlockPos underBlock = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY-1, mc.thePlayer.posZ);
        IBlockState state = mc.theWorld.getBlockState(underBlock);
        Block block = state.getBlock();
        if (!bb.isEmpty() && bbOffset.isEmpty()) {
            double distToPos = Integer.MAX_VALUE;
            if (block instanceof BlockAir) {
                //Offsetting cause minecraft sucks
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            continue;
                        }
                        BlockPos offsetPos = underBlock.add(x, 0, z);
                        IBlockState offsetState = mc.theWorld.getBlockState(offsetPos);
                        Block offsetBlock = offsetState.getBlock();
                        double xDiff = mc.thePlayer.posX - (offsetPos.getX()+0.5);
                        double zDiff = mc.thePlayer.posZ - (offsetPos.getZ()+0.5);
                        double dist = Math.hypot(xDiff, zDiff);
                        if (distToPos > dist && !(offsetBlock instanceof BlockAir)) {
                            underBlock = offsetPos;
                            block = offsetBlock;
                            distToPos = dist;
                        }
                    }
                }
            }
            if (!(block instanceof BlockAir)) {
                blockData = getFromUnder(underBlock);
            }
        } else if (bb.isEmpty() && bbOffset.isEmpty()) {
            if (block instanceof BlockAir) {
                blockData = getClutch(underBlock);
            }
        }
        if (blockData != null) {
            IBlockState dataState = mc.theWorld.getBlockState(blockData.position);
            Block dataBlock = dataState.getBlock();
            //TODO: check for all placeable blocks
            if (!(dataBlock instanceof BlockAir)) {
                blockData = null;
            }
        }
        if (blockData != null && !getParent().sprint.getValue()) {
            mc.thePlayer.setSprinting(false);
            event.setX(event.getX()/1.5);
            event.setZ(event.getZ()/1.5);
        }
    }

    @Override
    public void onPre(EventPreUpdate event) {
        if (blockData != null) {
            BlockPos previous = blockData.position.offset(blockData.face.getOpposite());
            double rotationX = previous.getX()+0.5 + blockData.face.getFrontOffsetX()*0.5;
            double rotationY = previous.getY()+0.5 - mc.thePlayer.getEyeHeight()+0.6;
            double rotationZ = previous.getZ()+0.5 + blockData.face.getFrontOffsetZ()*0.5;
            float[] rots = RotationUtils.getRotationFromPosition(rotationX, rotationZ, rotationY);
            event.setYaw(rots[0]);
            event.setPitch(rots[1]);
            if (getParent().lookMode.getValue() == Scaffold.LookMode.Forward) {
                event.setYaw(event.getYaw()-180);
                event.setPitch(90);
            }
            //mc.thePlayer.rotationYawHead = event.getYaw();
            //mc.thePlayer.renderYawOffset = event.getYaw();
            //mc.thePlayer.rotationPitchHead = event.getPitch();
        }
    }

    @Override
    public void onPost(EventPostUpdate event) {
        if (sneaked) {
            mc.gameSettings.keyBindSneak.pressed = false;
            sneaked = false;
            //PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        }
        if (blockData != null) {
            if (getParent().sneak.getValue()) {
                mc.gameSettings.keyBindSneak.pressed = true;
                sneaked = true;
                //PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
            }
            BlockPos previous = blockData.position.offset(blockData.face.getOpposite());
            double rotationX = previous.getX()+0.5 + blockData.face.getFrontOffsetX()*0.5;
            double rotationY = previous.getY()+0.5 - mc.thePlayer.getEyeHeight()+0.6;
            double rotationZ = previous.getZ()+0.5 + blockData.face.getFrontOffsetZ()*0.5;
            mc.thePlayer.swingItem();
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), previous, blockData.face, new Vec3(rotationX, rotationY, rotationZ));
            blockData = null;
        }
    }

    private class BlockData {
        public BlockPos position;
        public EnumFacing face;

        private BlockData(final BlockPos position, final EnumFacing face)
        {
            this.position = position;
            this.face = face;
        }
    }

    private BlockData getFromUnder(BlockPos underBlock) {
        double diffX = mc.thePlayer.posX - (underBlock.getX()+0.5);
        double diffZ = mc.thePlayer.posZ - (underBlock.getZ()+0.5);
        if (Math.abs(diffX) > Math.abs(diffZ)) {
            if (diffX > 0) {
                return new BlockData(underBlock.add(1, 0, 0), EnumFacing.EAST);
            } else if (diffX < 0) {
                return new BlockData(underBlock.add(-1, 0, 0), EnumFacing.WEST);
            }
        } else if (Math.abs(diffZ) > Math.abs(diffX)) {
            if (diffZ > 0) {
                return new BlockData(underBlock.add(0, 0, 1), EnumFacing.SOUTH);
            } else if (diffZ < 0) {
                return new BlockData(underBlock.add(0, 0, -1), EnumFacing.NORTH);
            }
        }
        return null;
    }

    private BlockData getClutch(BlockPos underBlock) {
        ArrayList<BlockPos> checkPos = new ArrayList<>();
        ArrayDeque<BlockPos> bokIndir = new ArrayDeque<>();
        bokIndir.add(underBlock);
        int expandV = 3;
        while (!bokIndir.isEmpty()) {
            BlockPos currentPos = bokIndir.poll();
            for (int y = -expandV; y <= 0; y++) {
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    BlockPos offsetPos = currentPos.add(facing.getFrontOffsetX(), y, facing.getFrontOffsetZ());
                    if (checkPos.contains(offsetPos)) {
                        continue;
                    }
                    IBlockState offsetState = mc.theWorld.getBlockState(offsetPos);
                    Block offsetBlock = offsetState.getBlock();
                    double xDiff = mc.thePlayer.posX - (offsetPos.getX() + 0.5);
                    double yDiff = mc.thePlayer.posY - (offsetPos.getY() + 0.5);
                    double zDiff = mc.thePlayer.posZ - (offsetPos.getZ() + 0.5);
                    double dist = Math.sqrt(xDiff*xDiff + yDiff*yDiff + zDiff*zDiff);
                    if (dist <= 3.5) {
                        if (!(offsetBlock instanceof BlockAir)) {
                            if (Math.abs(xDiff) > Math.abs(zDiff)) {
                                if (xDiff > 0) {
                                    return new BlockData(offsetPos.add(1, 0, 0), EnumFacing.EAST);
                                } else if (xDiff < 0) {
                                    return new BlockData(offsetPos.add(-1, 0, 0), EnumFacing.WEST);
                                }
                            } else if (Math.abs(zDiff) > Math.abs(xDiff)) {
                                if (zDiff > 0) {
                                    return new BlockData(offsetPos.add(0, 0, 1), EnumFacing.SOUTH);
                                } else if (zDiff < 0) {
                                    return new BlockData(offsetPos.add(0, 0, -1), EnumFacing.NORTH);
                                }
                            }
                        } else {
                            bokIndir.add(offsetPos);
                        }
                        checkPos.add(offsetPos);
                    }
                }
            }
        }
        checkPos.clear();
        return null;
    }

    @Override
    public void onRender(EventRender3D event) {
        if (blockData == null) {
            return;
        }
        double posX = blockData.position.getX() - mc.getRenderManager().renderPosX;
        double posY = blockData.position.getY() - mc.getRenderManager().renderPosY;
        double posZ = blockData.position.getZ() - mc.getRenderManager().renderPosZ;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GL11.glLineWidth(1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        AxisAlignedBB bb = new AxisAlignedBB(posX,posY,posZ,posX+1,posY+1,posZ+1);

        RenderUtils.drawOutlineBox(bb);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
