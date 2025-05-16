package ipana.utils.chunk;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumWorldBlockLayer;

import java.nio.ByteBuffer;

public class ChunkBuilder implements Runnable {

    private final AsyncChunkBuilder chunkBuilder;
    private final RegionRenderCacheBuilder cacheBuilder;

    public ChunkBuilder(AsyncChunkBuilder chunkBuilder) {
        this.chunkBuilder = chunkBuilder;
        this.cacheBuilder = new RegionRenderCacheBuilder();
    }

    @Override
    public void run() {
        while (true) {
            try {
                ChunkTypePair pair = chunkBuilder.queueChunkUpdates.take();
                this.chunkBuilder.chunkUpdates.getAndIncrement();

                Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
                float x = 0;
                float y = 0;
                float z = 0;
                if (viewEntity != null) {
                    x = (float) viewEntity.posX;
                    y = (float) viewEntity.posY + viewEntity.getEyeHeight();
                    z = (float) viewEntity.posZ;
                }
                if (pair.type() == ChunkBuildType.REBUILD_CHUNK) {
                    CompiledChunk compiledChunk = new CompiledChunk();
                    pair.chunk().rebuildChunk(x, y, z, cacheBuilder, compiledChunk);
                    for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
                        var worldRenderer = cacheBuilder.getWorldRendererByLayer(layer);
                        worldRenderer.reset();
                        if (compiledChunk.isLayerStarted(layer)) {
                            this.uploadChunk(layer, worldRenderer, pair.chunk());
                        }
                    }
                } else if (pair.type() == ChunkBuildType.RESORT_TRANSPARENCY) {
                    pair.chunk().resortTransparency(x, y, z, cacheBuilder);
                    this.uploadChunk(EnumWorldBlockLayer.TRANSLUCENT, cacheBuilder.getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT), pair.chunk());
                }

                pair.chunk().setNeedsUpdate(false);

                this.chunkBuilder.chunkUpdates.getAndDecrement();
            } catch (InterruptedException e) {
                System.out.println("EXIT: "+Thread.currentThread());
                return;
            }
        }
    }

    private void uploadChunk(final EnumWorldBlockLayer layer, final WorldRenderer worldRenderer, final RenderChunk renderChunk) {
        var bufLength = worldRenderer.getByteBuffer().limit();
        var buffer = ByteBuffer.allocateDirect(bufLength);
        buffer.put(worldRenderer.getByteBuffer());
        buffer.flip();

        chunkBuilder.uploadLock.lock();
        this.chunkBuilder.queueChunkUploads.add(() -> renderChunk.getVertexBufferByLayer(layer.ordinal()).uploadBufferData(buffer));
        chunkBuilder.uploadLock.unlock();
    }
}
