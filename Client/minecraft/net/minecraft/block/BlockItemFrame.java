package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class BlockItemFrame extends BlockDirectional {
    public static final PropertyBool IS_MAP = PropertyBool.create("map");

    public BlockItemFrame() {
        super(Material.circuits);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.WEST).withProperty(IS_MAP, false));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        return null;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean isFullCube()
    {
        return false;
    }

    public IBlockState getStateFromEntity(EntityItemFrame entity) {
        boolean isMap = entity.getDisplayedItem() != null && entity.getDisplayedItem().getItem() == Items.filled_map;
        return getDefaultState().withProperty(IS_MAP, isMap).withProperty(FACING, entity.facingDirection);
    }

    protected BlockState createBlockState() {
        return new BlockState(this, IS_MAP, FACING);
    }

    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = switch (meta & 7) {
            case 0 -> EnumFacing.DOWN;
            case 1 -> EnumFacing.EAST;
            case 2 -> EnumFacing.WEST;
            case 3 -> EnumFacing.SOUTH;
            case 4 -> EnumFacing.NORTH;
            default -> EnumFacing.UP;
        };

        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(IS_MAP, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = switch (state.getValue(FACING)) {
            case EAST -> 1;
            case WEST -> 2;
            case SOUTH -> 3;
            case NORTH -> 4;
            default -> 5;
            case DOWN -> 0;
        };
        if (state.getValue(IS_MAP)) {
            i |= 8;
        }
        return i;
    }

    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }
}
