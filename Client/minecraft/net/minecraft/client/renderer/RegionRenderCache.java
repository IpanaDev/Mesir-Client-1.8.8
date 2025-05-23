package net.minecraft.client.renderer;

import java.util.ArrayDeque;
import java.util.Arrays;

import ipana.managements.module.Modules;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import optifine.Config;
import optifine.DynamicLights;

public class RegionRenderCache extends ChunkCache
{
    private static final IBlockState DEFAULT_STATE = Blocks.air.getDefaultState();
    private final BlockPos position;
    private int[] combinedLights;
    private IBlockState[] blockStates;
    private static int maxCacheSize;
    private static final ArrayDeque<int[]> cacheLights = new ArrayDeque<>();
    private static final ArrayDeque<IBlockState[]> cacheStates = new ArrayDeque<>();

    public RegionRenderCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn, int maxCacheSize)
    {
        super(worldIn, posFromIn, posToIn, subIn);
        this.position = posFromIn.subtract(new Vec3i(subIn, subIn, subIn));
        this.combinedLights = allocateLights();
        Arrays.fill(this.combinedLights, - 1);
        this.blockStates = allocateStates();
        RegionRenderCache.maxCacheSize = maxCacheSize;
    }

    public TileEntity getTileEntity(BlockPos pos)
    {
        int i = (pos.getX() >> 4) - this.chunkX;
        int j = (pos.getZ() >> 4) - this.chunkZ;
        return this.chunkArray[i][j].getTileEntity(pos, Chunk.EnumCreateEntityType.QUEUED);
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        if (Modules.BRIGHTNESS.isEnabled()) {
            return 15;
        }
        int i = this.getPositionIndex(pos);
        int j = this.combinedLights[i];

        if (j == -1)
        {
            j = super.getCombinedLight(pos, lightValue);

            if (Config.isDynamicLights() && !this.getBlockState(pos).getBlock().isOpaqueCube())
            {
                j = DynamicLights.getCombinedLight(pos, j);
            }

            this.combinedLights[i] = j;
        }
        return j;
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        int i = this.getPositionIndex(pos);
        IBlockState iblockstate = this.blockStates[i];

        if (iblockstate == null)
        {
            iblockstate = this.getBlockStateRaw(pos);
            this.blockStates[i] = iblockstate;
        }

        return iblockstate;
    }

    private IBlockState getBlockStateRaw(BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;
            return this.chunkArray[i][j].getBlockState(pos);
        }
        else
        {
            return DEFAULT_STATE;
        }
    }

    public int getPositionIndex(BlockPos p_175630_1_)
    {
        int i = p_175630_1_.getX() - this.position.getX();
        int j = p_175630_1_.getY() - this.position.getY();
        int k = p_175630_1_.getZ() - this.position.getZ();
        return i * 400 + k * 20 + j;
    }

    public IBlockState[] blockStates() {
        return blockStates;
    }

    public void freeBuffers()
    {
        freeLights(this.combinedLights);
        freeStates(this.blockStates);
    }

    private static int[] allocateLights()
    {
        synchronized (cacheLights)
        {
            int[] aint = cacheLights.pollLast();

            if (aint == null || aint.length < 8000)
            {
                aint = new int[8000];
            }

            return aint;
        }
    }

    public static void freeLights(int[] p_freeLights_0_)
    {
        synchronized (cacheLights)
        {
            if (cacheLights.size() < maxCacheSize)
            {
                cacheLights.add(p_freeLights_0_);
            }
        }
    }

    private static IBlockState[] allocateStates()
    {
        synchronized (cacheStates)
        {
            IBlockState[] aiblockstate = cacheStates.pollLast();

            if (aiblockstate != null && aiblockstate.length >= 8000)
            {
                Arrays.fill(aiblockstate, null);
            }
            else
            {
                aiblockstate = new IBlockState[8000];
            }

            return aiblockstate;
        }
    }

    public static void freeStates(IBlockState[] p_freeStates_0_)
    {
        synchronized (cacheStates)
        {
            if (cacheStates.size() < maxCacheSize)
            {
                cacheStates.add(p_freeStates_0_);
            }
        }
    }
}
