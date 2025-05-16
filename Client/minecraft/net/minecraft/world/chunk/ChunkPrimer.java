package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeCache;

public class ChunkPrimer
{
    private final IBlockState[] stateData = new IBlockState[65536];
    private final IBlockState defaultState = Blocks.air.getDefaultState();

    public IBlockState getBlockState(int x, int y, int z)
    {
        int i = x << 12 | z << 8 | y;
        return this.getBlockState(i);
    }

    public IBlockState getBlockState(int index)
    {
        if (index >= 0 && index < this.stateData.length)
        {
            IBlockState iblockstate = stateData[index];
            return iblockstate != null ? iblockstate : this.defaultState;
        }
        else
        {
            throw new IndexOutOfBoundsException("The coordinate is out of range");
        }
    }

    public void setBlockState(int x, int y, int z, IBlockState state)
    {
        int i = x << 12 | z << 8 | y;
        this.setBlockState(i, state);
    }

    public void setBlockState(int index, IBlockState state)
    {
        if (index >= 0 && index < this.stateData.length)
        {
            this.stateData[index] = state;
        }
        else
        {
            throw new IndexOutOfBoundsException("The coordinate is out of range");
        }
    }
}
