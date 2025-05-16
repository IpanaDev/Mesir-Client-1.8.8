package net.minecraft.client.renderer.chunk;

import ipana.utils.baritone.BaritoneHelper;
import ipana.utils.chunk.ChunkBuildType;
import ipana.utils.chunk.ChunkScaling;
import ipana.utils.chunk.ChunkTypePair;
import ipana.utils.player.PlayerUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockItemFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import optifine.BlockPosM;
import optifine.Config;
import optifine.ReflectorForge;
import shadersmod.client.SVertexBuilder;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderChunk {
    private World world;
    private final RenderGlobal renderGlobal;
    public static int renderChunksUpdated;
    private BlockPos position;
    private final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final VertexBuffer[] vertexBuffers = new VertexBuffer[EnumWorldBlockLayer.VALUES.length];
    public volatile AxisAlignedBB boundingBox, fullBox;
    private RenderChunk[] neighbourChunks = new RenderChunk[EnumFacing.VALUES.length];
    private Chunk chunk;
    public int facingIndex;
    private int frameCount;
    private CompiledChunk compiledChunk = CompiledChunk.DUMMY;
    private AtomicBoolean needsUpdate = new AtomicBoolean(false);

    public RenderChunk(World worldIn, RenderGlobal renderGlobalIn, BlockPos blockPosIn) {
        this.world = worldIn;
        this.renderGlobal = renderGlobalIn;

        if (!blockPosIn.equals(this.getPosition())) {
            this.setPosition(blockPosIn);
        }

        if (OpenGlHelper.useVbo()) {
            for (int i = 0; i < EnumWorldBlockLayer.VALUES.length; ++i) {
                this.vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
            }
        }
    }

    public VertexBuffer getVertexBufferByLayer(int layer) {
        return this.vertexBuffers[layer];
    }

    public void setPosition(BlockPos pos) {
        this.stopCompileTask();
        if (this.boundingBox != null && this.position != null) {
            double xDiff = (pos.getX() - this.position.getX());
            double yDiff = (pos.getY() - this.position.getY());
            double zDiff = (pos.getZ() - this.position.getZ());
            this.boundingBox.offset(xDiff, yDiff, zDiff);
        }
        this.fullBox = new AxisAlignedBB(pos, pos.add(16,16,16));
        this.position = pos;
        this.initModelviewMatrix();

        Arrays.fill(this.neighbourChunks, null);

        this.chunk = null;
    }

    public void resortTransparency(float x, float y, float z, RegionRenderCacheBuilder cacheBuilder) {
        CompiledChunk compiledchunk = getCompiledChunk();
        if (world != null && compiledchunk.getState() != null && compiledchunk.isLayerUsed(EnumWorldBlockLayer.TRANSLUCENT)) {
            WorldRenderer worldrenderer = cacheBuilder.getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT);
            this.preRenderBlocks(worldrenderer, this.position);
            worldrenderer.setVertexState(compiledchunk.getState());
            this.postRenderBlocks(EnumWorldBlockLayer.TRANSLUCENT, x, y, z, worldrenderer, compiledchunk);
        }
    }

    public void rebuildChunk(double x, double y, double z, RegionRenderCacheBuilder cacheBuilder, CompiledChunk compiledChunk) {
        if (this.world == null) {
            return;
        }
        BlockPos blockpos = this.position;
        BlockPos blockpos1 = blockpos.add(15, 15, 15);
        RegionRenderCache regionrendercache = this.createRegionRenderCache(this.world, blockpos.add(-1, -1, -1), blockpos1.add(1, 1, 1));
        boolean emptyChunk = true;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        VisGraph visGraph = new VisGraph();
        if (!isChunkRegionEmpty(blockpos)) {
            ++renderChunksUpdated;
            boolean[] aboolean = new boolean[EnumWorldBlockLayer.VALUES.length];
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            final BlockPosM minPos = new BlockPosM(Math.min(blockpos.getX(), blockpos1.getX()), Math.min(blockpos.getY(), blockpos1.getY()), Math.min(blockpos.getZ(), blockpos1.getZ()));
            final BlockPosM maxPos = new BlockPosM(Math.max(blockpos.getX(), blockpos1.getX()), Math.max(blockpos.getY(), blockpos1.getY()), Math.max(blockpos.getZ(), blockpos1.getZ()));
            ChunkScaling chunkScaling = new ChunkScaling(world);
            for (int posY = minPos.getY(); posY <= maxPos.getY(); posY++) {
                for (int posX = minPos.getX(); posX <= maxPos.getX(); posX++) {
                    for (int posZ = minPos.getZ(); posZ <= maxPos.getZ(); posZ++) {
                        BlockPosM blockPosM = new BlockPosM(posX,posY,posZ,3);
                        IBlockState iblockstate = BaritoneHelper.getBlockState(regionrendercache, blockPosM);
                        Block block = iblockstate.getBlock();
                        if (ReflectorForge.blockHasTileEntity(iblockstate)) {
                            regionrendercache.getTileEntity(new BlockPos(blockPosM));
                        }
                        if (block.isOpaqueCube()) {
                            visGraph.setOpaqueCube(blockPosM);
                        }
                        EnumWorldBlockLayer enumworldblocklayer = RenderUtils.fixBlockLayer(block, block.getBlockLayer());
                        int j = enumworldblocklayer.ordinal();
                        if (block.getRenderType() != -1) {
                            WorldRenderer worldrenderer = cacheBuilder.getWorldRendererByLayerId(j);
                            worldrenderer.setBlockLayer(enumworldblocklayer);
                            if (!compiledChunk.isLayerStarted(enumworldblocklayer)) {
                                compiledChunk.setLayerStarted(enumworldblocklayer);
                                this.preRenderBlocks(worldrenderer, blockpos);
                            }
                            if (Minecraft.getMinecraft().gameSettings.chunkScaling) {
                                if (!chunkScaling.contains(blockPosM)) {
                                    BlockPosM expandedBlock = chunkScaling.combineMeshes(posX, posY, posZ, maxPos, regionrendercache, blockPosM, iblockstate, block);
                                    int diffX = 0;
                                    int diffY = 0;
                                    int diffZ = 0;
                                    if (expandedBlock != null) {
                                        diffX = expandedBlock.getX() - blockPosM.getX() + 1;
                                        diffY = 1;
                                        diffZ = expandedBlock.getZ() - blockPosM.getZ() + 1;
                                    }
                                    //CONDITIONS:
                                    //SOURCE BLOCK'S UP FACING MAY NOT BE RENDERED WHICH WILL CAUSE NO RENDERING
                                    //

                                    aboolean[j] |= blockrendererdispatcher.renderBlock(iblockstate, blockPosM, regionrendercache, worldrenderer, diffX, diffY, diffZ);
                                }
                            } else {
                                aboolean[j] |= blockrendererdispatcher.renderBlock(iblockstate, blockPosM, regionrendercache, worldrenderer, 0, 0, 0);
                            }
                            if (aboolean[j]) {
                                emptyChunk = false;
                                maxX = Math.max(maxX, blockPosM.getX());
                                maxY = Math.max(maxY, blockPosM.getY());
                                maxZ = Math.max(maxZ, blockPosM.getZ());
                                minX = Math.min(minX, blockPosM.getX());
                                minY = Math.min(minY, blockPosM.getY());
                                minZ = Math.min(minZ, blockPosM.getZ());
                            }
                        }
                    }
                }
            }
            for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
                if (aboolean[layer.ordinal()]) {
                    compiledChunk.setLayerUsed(layer);
                }

                if (compiledChunk.isLayerStarted(layer)) {
                    if (Config.isShaders()) {
                        SVertexBuilder.calcNormalChunkLayer(cacheBuilder.getWorldRendererByLayer(layer));
                    }
                    this.postRenderBlocks(layer, (float) x, (float) y, (float) z, cacheBuilder.getWorldRendererByLayer(layer), compiledChunk);
                }
            }
        }
        compiledChunk.setVisibility(visGraph.computeVisibility());
        if (!emptyChunk) {
            this.boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        } else {
            this.boundingBox = null;
        }
        this.setCompiledChunk(compiledChunk);
    }

    public Chunk getChunk() {
        return this.getChunk(this.position);
    }

    public Chunk getChunk(BlockPos p_getChunk_1_) {
        if (this.world == null) {
            return this.chunk;
        }
        if (this.chunk != null && this.chunk.isLoaded()) {
            return this.chunk;
        } else {
            return this.chunk = this.world.getChunkFromBlockCoords(p_getChunk_1_);
        }
    }

    public void finishCompileTask() {

    }

    public ChunkCompileTaskGenerator makeCompileTaskChunk() {
        return new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.REBUILD_CHUNK);
    }

    public ChunkCompileTaskGenerator makeCompileTaskTransparency() {
        return new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY);
    }

    private void preRenderBlocks(WorldRenderer worldRendererIn, BlockPos pos) {
        worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
        if (Config.isVBORegions() && OpenGlHelper.useVbo()) {
            //worldRendererIn.setTranslation((-j), (-k), (-l));
            worldRendererIn.setTranslation(-renderGlobal.viewerRegionX, 0, -renderGlobal.viewerRegionZ);
        } else {
            worldRendererIn.setTranslation((-pos.getX()), (-pos.getY()), (-pos.getZ()));
        }
    }

    private void postRenderBlocks(EnumWorldBlockLayer layer, float x, float y, float z, WorldRenderer worldRendererIn, CompiledChunk compiledChunkIn) {
        if (layer == EnumWorldBlockLayer.TRANSLUCENT && compiledChunkIn.isLayerUsed(layer)) {
            worldRendererIn.sortVertexData(x, y, z);
            compiledChunkIn.setState(worldRendererIn.getVertexData());
        }

        worldRendererIn.finishDrawing();
    }

    private void initModelviewMatrix() {
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        float f = 1.000001F;
        GlStateManager.translate(-8.0F, -8.0F, -8.0F);
        GlStateManager.scale(f, f, f);
        GlStateManager.translate(8.0F, 8.0F, 8.0F);
        GlStateManager.getFloat(2982, this.modelviewMatrix);
        GlStateManager.popMatrix();
    }

    public void multModelviewMatrix() {
        GlStateManager.multMatrix(this.modelviewMatrix);
    }

    public CompiledChunk getCompiledChunk() {
        return this.compiledChunk;
    }

    public void setCompiledChunk(CompiledChunk compiledChunkIn) {
        this.compiledChunk = compiledChunkIn;
    }

    public void stopCompileTask() {
        this.finishCompileTask();
        this.compiledChunk = CompiledChunk.DUMMY;
    }

    public void deleteGlResources() {
        this.stopCompileTask();
        this.world = null;

        for (int i = 0; i < EnumWorldBlockLayer.VALUES.length; ++i) {
            if (this.vertexBuffers[i] != null) {
                this.vertexBuffers[i].deleteGlBuffers();
            }
        }
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public void setNeedsUpdate(boolean needsUpdateIn) {
        if (needsUpdateIn && !needsUpdate.get()) {
            renderGlobal.chunksToUpdate.add(this);
        }
        this.needsUpdate.set(needsUpdateIn);

        /*if (this.needsUpdate) {
            if (this.isWorldPlayerUpdate()) {
                this.playerUpdate = true;
            }
        } else {
            this.playerUpdate = false;
        }*/
    }
    public boolean isNeedsUpdate() {
        return this.needsUpdate.get();
    }

    public RenderChunk getNeighbour(EnumFacing facing) {
        return neighbourChunks[facing.ordinal()];
    }

    public RenderChunk setNeighbour(EnumFacing facing, RenderChunk renderChunk) {
        return neighbourChunks[facing.ordinal()] = renderChunk;
    }

    /*private boolean isWorldPlayerUpdate() {
        if (this.world instanceof WorldClient worldclient) {
            return worldclient.isPlayerUpdate();
        } else {
            return false;
        }
    }*/

    private boolean isChunkRegionEmpty(BlockPos chunkPos) {
        int minY = chunkPos.getY();
        //int maxY = minY + 15;
        Chunk chunk = this.getChunk(chunkPos);
        return chunk == null || chunk.isLevelEmpty(minY);
    }

    public RegionRenderCache createRegionRenderCache(World p_createRegionRenderCache_1_, BlockPos p_createRegionRenderCache_2_, BlockPos p_createRegionRenderCache_3_) {
        return new RegionRenderCache(p_createRegionRenderCache_1_, p_createRegionRenderCache_2_, p_createRegionRenderCache_3_, 1, 1);
    }

    public boolean isNewFrame(int frameCount) {
        if (this.frameCount != frameCount) {
            this.frameCount = frameCount;
            return true;
        }
        return false;
    }
}
