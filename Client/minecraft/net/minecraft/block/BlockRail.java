package net.minecraft.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.World;

public class BlockRail extends BlockRailBase
{
    public static final PropertyEnum<BlockRailBase.EnumRailDirection> SHAPE = PropertyEnum.<BlockRailBase.EnumRailDirection>create("shape", BlockRailBase.EnumRailDirection.class);

    protected BlockRail()
    {
        super(false);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH));
    }

    protected void onNeighborChangedInternal(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (neighborBlock.canProvidePower() && (new BlockRailBase.Rail(worldIn, pos, state)).countAdjacentRails() == 3)
        {
            this.func_176564_a(worldIn, pos, state, false);
        }
    }

    public IProperty<BlockRailBase.EnumRailDirection> getShapeProperty()
    {
        return SHAPE;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return ((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)).getMetadata();
    }
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        switch (rot)
        {
            case CLOCKWISE_180:
                switch ((BlockRailBase.EnumRailDirection)state.getValue(SHAPE))
                {
                    case ASCENDING_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);

                    case ASCENDING_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);

                    case ASCENDING_NORTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);

                    case ASCENDING_SOUTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);

                    case SOUTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);

                    case SOUTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);

                    case NORTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);

                    case NORTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
                }

            case COUNTERCLOCKWISE_90:
                switch ((BlockRailBase.EnumRailDirection)state.getValue(SHAPE))
                {
                    case ASCENDING_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);

                    case ASCENDING_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);

                    case ASCENDING_NORTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);

                    case ASCENDING_SOUTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);

                    case SOUTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);

                    case SOUTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);

                    case NORTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);

                    case NORTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);

                    case NORTH_SOUTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);

                    case EAST_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
                }

            case CLOCKWISE_90:
                switch ((BlockRailBase.EnumRailDirection)state.getValue(SHAPE))
                {
                    case ASCENDING_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);

                    case ASCENDING_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);

                    case ASCENDING_NORTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);

                    case ASCENDING_SOUTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);

                    case SOUTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);

                    case SOUTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);

                    case NORTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);

                    case NORTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);

                    case NORTH_SOUTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);

                    case EAST_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
                }

            default:
                return state;
        }
    }

    @SuppressWarnings("incomplete-switch")

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)state.getValue(SHAPE);

        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                switch (blockrailbase$enumraildirection)
                {
                    case ASCENDING_NORTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);

                    case ASCENDING_SOUTH:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);

                    case SOUTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);

                    case SOUTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);

                    case NORTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);

                    case NORTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);

                    default:
                        return super.withMirror(state, mirrorIn);
                }

            case FRONT_BACK:
                switch (blockrailbase$enumraildirection)
                {
                    case ASCENDING_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);

                    case ASCENDING_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);

                    case ASCENDING_NORTH:
                    case ASCENDING_SOUTH:
                    default:
                        break;

                    case SOUTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);

                    case SOUTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);

                    case NORTH_WEST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);

                    case NORTH_EAST:
                        return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
                }
        }

        return super.withMirror(state, mirrorIn);
    }
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {SHAPE});
    }
}
