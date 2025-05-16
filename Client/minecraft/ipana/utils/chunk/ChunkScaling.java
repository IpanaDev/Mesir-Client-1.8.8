package ipana.utils.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import optifine.BlockPosM;

import java.util.ArrayList;
import java.util.List;

public class ChunkScaling {
    private List<SourceObject> combinedMeshes = new ArrayList<>();
    private World world;

    public ChunkScaling(World world) {
        this.world = world;
    }

    public BlockPosM combineMeshes(int x, int y, int z, BlockPosM maxPos, RegionRenderCache regionRenderCache, BlockPosM source, IBlockState sourceState, Block sourceBlock) {
        if (!sourceBlock.isFullBlock()) {
            return null;
        }
        List<BlockPosM> expandableBlocks = new ArrayList<>();
        boolean[] sourceCulling = new boolean[6];
        for (EnumFacing facing : EnumFacing.VALUES) {
            sourceCulling[facing.ordinal()] = sourceBlock.shouldSideBeRendered(world, source, source.offset(facing), facing);
        }
        for (int offX = x; offX <= maxPos.getX(); offX++) {
            for (int offZ = z; offZ <= maxPos.getZ(); offZ++) {
                BlockPosM offsetPos = new BlockPosM(offX, y, offZ);
                if (offsetPos.equals(source)) {
                    continue;
                }
                IBlockState offsetStates = regionRenderCache.getBlockState(offsetPos);
                Block offsetBlock = offsetStates.getBlock();

                boolean[] offsetCulling = new boolean[6];
                for (EnumFacing facing : EnumFacing.VALUES) {
                    offsetCulling[facing.ordinal()] = offsetBlock.shouldSideBeRendered(world, offsetPos, offsetPos.offset(facing), facing);
                }

                if (contains(offsetPos) || !equalSides(sourceCulling, offsetCulling) || sourceState != offsetStates) {
                    if (offZ == z) {
                        return result(source, expandableBlocks);
                    }
                    expandableBlocks.add(new BlockPosM(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ() - 1));
                    break;
                } else if (offZ == maxPos.getZ()) {
                    expandableBlocks.add(offsetPos);
                }
            }
        }

        return result(source, expandableBlocks);
    }
    public boolean equalSides(boolean[] b, boolean[] b2) {
        for (int i = 0; i < b.length; i++) {
            if (b[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }
    private List<BlockPosM> quadraticIteration(List<BlockPosM> expandableBlocks) {
        BlockPosM wantedZ = expandableBlocks.get(0);
        List<BlockPosM> quadraticBlocks = new ArrayList<>();
        quadraticBlocks.add(wantedZ);
        for (int i = 1; i < expandableBlocks.size(); i++) {
            BlockPosM otherZ = expandableBlocks.get(i);
            if (otherZ.getZ() >= wantedZ.getZ()) {
                quadraticBlocks.add(new BlockPosM(otherZ.getX(), otherZ.getY(), Math.min(otherZ.getZ(), wantedZ.getZ())));
            } else {
                return quadraticBlocks;
            }
        }
        return quadraticBlocks;
    }

    public BlockPosM result(BlockPosM source, List<BlockPosM> expandableBlocks) {
        if (expandableBlocks.isEmpty()) {
            return null;
        }
        List<BlockPosM> prepareBlocks = quadraticIteration(expandableBlocks);
        combinedMeshes.add(new SourceObject(source, prepareBlocks.getLast()));
        return prepareBlocks.getLast();
    }

    public int expand(int num) {
        int n = Math.abs(num);
        return n+1;
    }

    public boolean contains(BlockPosM blockPosM) {
        for (SourceObject sourceObject : combinedMeshes) {
            if (sourceObject.end.getX() >= blockPosM.getX() &&
                    sourceObject.end.getY() >= blockPosM.getY() &&
                    sourceObject.end.getZ() >= blockPosM.getZ() &&
                    sourceObject.source.getX() <= blockPosM.getX() &&
                    sourceObject.source.getY() <= blockPosM.getY() &&
                    sourceObject.source.getZ() <= blockPosM.getZ()) {
                return true;
            }
        }
        return false;
    }

    public class SourceObject {
        private BlockPosM source;
        private BlockPosM end;


        public SourceObject(BlockPosM source, BlockPosM end) {
            this.source = source;
            this.end = end;
        }
    }
}
