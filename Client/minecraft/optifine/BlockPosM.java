package optifine;

import com.google.common.collect.AbstractIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class BlockPosM extends BlockPos
{
    private int mx;
    private int my;
    private int mz;
    private int level;
    private BlockPosM[] facings;
    private boolean needsUpdate;

    public BlockPosM(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ(), 0);
    }
    public BlockPosM(int x, int y, int z) {
        this(x, y, z, 0);
    }
    public BlockPosM(double x, double y, double z) {
        this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }

    public BlockPosM(int x, int y, int z, int level) {
        super(0, 0, 0);
        this.mx = x;
        this.my = y;
        this.mz = z;
        this.level = level;
    }

    /**
     * Get the X coordinate
     */
    public int getX()
    {
        return this.mx;
    }

    /**
     * Get the Y coordinate
     */
    public int getY()
    {
        return this.my;
    }

    /**
     * Get the Z coordinate
     */
    public int getZ()
    {
        return this.mz;
    }

    public void setXyz(int p_setXyz_1_, int p_setXyz_2_, int p_setXyz_3_)
    {
        this.mx = p_setXyz_1_;
        this.my = p_setXyz_2_;
        this.mz = p_setXyz_3_;
        this.needsUpdate = true;
    }

    public void setXyz(double p_setXyz_1_, double p_setXyz_3_, double p_setXyz_5_)
    {
        this.setXyz(MathHelper.floor_double(p_setXyz_1_), MathHelper.floor_double(p_setXyz_3_), MathHelper.floor_double(p_setXyz_5_));
    }

    /**
     * Offset this BlockPos 1 block in the given direction
     */
    public BlockPos offset(EnumFacing facing)
    {
        if (this.level <= 0)
        {
            return super.offset(facing, 1);
        }
        else
        {
            if (this.facings == null)
            {
                this.facings = new BlockPosM[EnumFacing.VALUES.length];
            }

            if (this.needsUpdate)
            {
                this.update();
            }

            int i = facing.getIndex();
            BlockPosM blockposm = this.facings[i];

            if (blockposm == null)
            {
                int j = this.mx + facing.getFrontOffsetX();
                int k = this.my + facing.getFrontOffsetY();
                int l = this.mz + facing.getFrontOffsetZ();
                blockposm = new BlockPosM(j, k, l, this.level - 1);
                this.facings[i] = blockposm;
            }

            return blockposm;
        }
    }

    /**
     * Offsets this BlockPos n blocks in the given direction
     */
    public BlockPos offset(EnumFacing facing, int n)
    {
        return n == 1 ? this.offset(facing) : super.offset(facing, n);
    }

    private void update()
    {
        //PlayerUtils.debug(":skkull:");
        for (int i = 0; i < 6; ++i)
        {
            BlockPosM blockposm = this.facings[i];

            if (blockposm != null)
            {
                EnumFacing enumfacing = EnumFacing.VALUES[i];
                int j = this.mx + enumfacing.getFrontOffsetX();
                int k = this.my + enumfacing.getFrontOffsetY();
                int l = this.mz + enumfacing.getFrontOffsetZ();
                blockposm.setXyz(j, k, l);
            }
        }

        this.needsUpdate = false;
    }

    public static Iterable<BlockPosM> getAllInBoxMutableM(BlockPos p_getAllInBoxMutable_0_, BlockPos p_getAllInBoxMutable_1_) {
        final BlockPosM blockpos = new BlockPosM(Math.min(p_getAllInBoxMutable_0_.getX(), p_getAllInBoxMutable_1_.getX()), Math.min(p_getAllInBoxMutable_0_.getY(), p_getAllInBoxMutable_1_.getY()), Math.min(p_getAllInBoxMutable_0_.getZ(), p_getAllInBoxMutable_1_.getZ()));
        final BlockPosM blockpos1 = new BlockPosM(Math.max(p_getAllInBoxMutable_0_.getX(), p_getAllInBoxMutable_1_.getX()), Math.max(p_getAllInBoxMutable_0_.getY(), p_getAllInBoxMutable_1_.getY()), Math.max(p_getAllInBoxMutable_0_.getZ(), p_getAllInBoxMutable_1_.getZ()));
        return new Iterable<>() {
            public Iterator<BlockPosM> iterator() {
                return new AbstractIterator<>() {
                    private BlockPosM theBlockPosM = null;

                    private BlockPosM computeNext0() {
                        if (this.theBlockPosM == null) {
                            this.theBlockPosM = new BlockPosM(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 3);
                            return this.theBlockPosM;
                        } else if (this.theBlockPosM.equals(blockpos1)) {
                            return this.endOfData();
                        } else {
                            int i = this.theBlockPosM.getX();
                            int j = this.theBlockPosM.getY();
                            int k = this.theBlockPosM.getZ();

                            if (i < blockpos1.getX()) {
                                ++i;
                            } else if (j < blockpos1.getY()) {
                                i = blockpos.getX();
                                ++j;
                            } else if (k < blockpos1.getZ()) {
                                i = blockpos.getX();
                                j = blockpos.getY();
                                ++k;
                            }

                            this.theBlockPosM.setXyz(i, j, k);
                            return this.theBlockPosM;
                        }
                    }

                    protected BlockPosM computeNext() {
                        return this.computeNext0();
                    }
                };
            }
        };
    }

    public static List<BlockPosM> getAllInBoxMutableMList(BlockPos pos, BlockPos pos1) {
        final BlockPosM minPos = new BlockPosM(Math.min(pos.getX(), pos1.getX()), Math.min(pos.getY(), pos1.getY()), Math.min(pos.getZ(), pos1.getZ()));
        final BlockPosM maxPos = new BlockPosM(Math.max(pos.getX(), pos1.getX()), Math.max(pos.getY(), pos1.getY()), Math.max(pos.getZ(), pos1.getZ()));
        List<BlockPosM> list = new ArrayList<>(
                (maxPos.getY() - minPos.getY()) *
                        (maxPos.getX() - minPos.getX()) *
                        (maxPos.getZ() - minPos.getZ()));
        for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
            for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                    list.add(new BlockPosM(x,y,z,3));
                }
            }
        }
        return list;
    }
}
