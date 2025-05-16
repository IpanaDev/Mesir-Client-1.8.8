package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import optifine.Config;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import shadersmod.client.ShadersRender;

public class ChunkRenderDispatcher
{
    private static final ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat("Chunk Batcher %d").setDaemon(true).build();
    private final BlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates = Queues.newArrayBlockingQueue(100);
    private final BlockingQueue<RegionRenderCacheBuilder> queueFreeRenderBuilders;
    public final WorldVertexBufferUploader worldVertexUploader = new WorldVertexBufferUploader();
    private final VertexBufferUploader vertexUploader = new VertexBufferUploader();
    public final Queue <ListenableFutureTask<?>> queueChunkUploads = Queues.newArrayDeque();
    private final ChunkRenderWorker renderWorker;
    private final int countRenderBuilders;

    public ChunkRenderDispatcher() {
        this(-1);
    }
    public ChunkRenderDispatcher(int builders) {

        int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3D) / 10485760);
        int j = Math.max(1, MathHelper.clamp_int(Runtime.getRuntime().availableProcessors() - 2, 1, i / 5));

        if (builders < 0) {
            this.countRenderBuilders = MathHelper.clamp_int(j * 8, 1, i);
        } else {
            this.countRenderBuilders = builders;
        }
        System.out.println("Count of Render Builders: "+countRenderBuilders);
        for (int k = 0; k < j; ++k) {
            ChunkRenderWorker chunkrenderworker = new ChunkRenderWorker(this);
            Thread thread = threadFactory.newThread(chunkrenderworker);
            thread.start();
        }
        this.queueFreeRenderBuilders = Queues.newArrayBlockingQueue(this.countRenderBuilders);

        for (int l = 0; l < this.countRenderBuilders; ++l)
        {
            this.queueFreeRenderBuilders.add(new RegionRenderCacheBuilder());
        }

        this.renderWorker = new ChunkRenderWorker(this, new RegionRenderCacheBuilder());
    }

    public ChunkRenderWorker getRenderWorker() {
        return renderWorker;
    }

    public String getDebugInfo()
    {
        return String.format("pC: %03d, pU: %1d, aB: %1d", this.queueChunkUpdates.size(), this.queueChunkUploads.size(), this.queueFreeRenderBuilders.size());
    }

    public boolean runChunkUploads(long p_178516_1_) {
        boolean flag = false;

        while (true) {
            boolean flag1 = false;

            synchronized (this.queueChunkUploads) {
                if (!this.queueChunkUploads.isEmpty()) {
                    this.queueChunkUploads.poll().run();
                    flag1 = true;
                    flag = true;
                }
            }

            if (p_178516_1_ == 0L || !flag1) {
                break;
            }

            long i = p_178516_1_ - System.nanoTime();

            if (i < 0L) {
                break;
            }
        }

        return flag;
    }

    public boolean updateChunkLater(RenderChunk chunkRenderer)
    {
        //chunkRenderer.getLockCompileTask().lock();
        boolean flag1;

        try
        {
            final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();
            chunkcompiletaskgenerator.addFinishRunnable(() -> ChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkcompiletaskgenerator));
            boolean flag = this.queueChunkUpdates.offer(chunkcompiletaskgenerator);

            if (!flag)
            {
                chunkcompiletaskgenerator.finish();
            }

            flag1 = flag;
        }
        finally
        {
            //chunkRenderer.getLockCompileTask().unlock();
        }

        return flag1;
    }

    public boolean updateChunkNow(RenderChunk chunkRenderer)
    {
        //chunkRenderer.getLockCompileTask().lock();

        try
        {
            ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();

            try
            {
                this.renderWorker.processTask(chunkcompiletaskgenerator);
            }
            catch (InterruptedException var7) {
                var7.printStackTrace();
            }

        }
        finally
        {
            //chunkRenderer.getLockCompileTask().unlock();
        }
        return true;
    }

    public void stopChunkUpdates() {
        this.clearChunkUpdates();

        while (true) {
            if (!this.runChunkUploads(0L))
                break;

        }

        List<RegionRenderCacheBuilder> list = Lists.newArrayList();
        while (list.size() < countRenderBuilders) {
            boolean ataturk = false;
            try {
                if (!queueFreeRenderBuilders.isEmpty()) {
                    list.add(this.allocateRenderBuilder());
                } else {
                    ataturk = true;
                }
            } catch (InterruptedException var3) {
                var3.printStackTrace();
            }
            if (ataturk) {
                break;
            }
        }
        this.queueFreeRenderBuilders.addAll(list);
    }

    public void freeRenderBuilder(RegionRenderCacheBuilder p_178512_1_)
    {
        this.queueFreeRenderBuilders.add(p_178512_1_);
    }

    public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException
    {
        return this.queueFreeRenderBuilders.take();
    }

    public ChunkCompileTaskGenerator getNextChunkUpdate() throws InterruptedException
    {
        return this.queueChunkUpdates.take();
    }

    public boolean updateTransparencyLater(RenderChunk chunkRenderer) {
        //chunkRenderer.getLockCompileTask().lock();
        boolean flag;

        try {
            final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskTransparency();

            if (chunkcompiletaskgenerator == null) {
                return true;
            }

            chunkcompiletaskgenerator.addFinishRunnable(() -> ChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkcompiletaskgenerator));
            flag = this.queueChunkUpdates.offer(chunkcompiletaskgenerator);
        } finally {
            //chunkRenderer.getLockCompileTask().unlock();
        }

        return flag;
    }
    public ListenableFuture<Object> uploadChunk(final EnumWorldBlockLayer layer, final WorldRenderer worldRenderer, final RenderChunk chunkRenderer, final CompiledChunk compiledChunkIn)
    {
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread())
        {
            if (OpenGlHelper.useVbo()) {
                this.uploadVertexBuffer(worldRenderer, chunkRenderer.getVertexBufferByLayer(layer.ordinal()));
            } else {
                this.uploadDisplayList(worldRenderer, layer, compiledChunkIn, chunkRenderer);
            }

            //worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
            return Futures.immediateFuture(null);
        }
        else
        {
            ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.create( () -> ChunkRenderDispatcher.this.uploadChunk(layer, worldRenderer, chunkRenderer, compiledChunkIn), null);

            synchronized (this.queueChunkUploads)
            {
                this.queueChunkUploads.add(listenablefuturetask);
                return listenablefuturetask;
            }
        }
    }
    private void uploadDisplayList(WorldRenderer worldRenderer, EnumWorldBlockLayer layer, CompiledChunk compiledChunkIn, RenderChunk renderChunk) {
        if (!Config.isVBORegions()) {
            GL11.glNewList(((ListedRenderChunk) renderChunk).getDisplayList(layer, compiledChunkIn), GL11.GL_COMPILE);
        }
        GlStateManager.pushMatrix();
        //renderChunk.multModelviewMatrix();
        this.worldVertexUploader.buildDrawArrays(worldRenderer);
        GlStateManager.popMatrix();
        if (!Config.isVBORegions()) {
            GL11.glEndList();
        }
    }


    private void uploadVertexBuffer(WorldRenderer worldRenderer, VertexBuffer vb)
    {
        this.vertexUploader.setVertexBuffer(vb);
        this.vertexUploader.buildDrawArrays(worldRenderer);
    }

    public void clearChunkUpdates()
    {
        while (!this.queueChunkUpdates.isEmpty())
        {
            ChunkCompileTaskGenerator chunkcompiletaskgenerator = this.queueChunkUpdates.poll();

            if (chunkcompiletaskgenerator != null)
            {
                chunkcompiletaskgenerator.finish();
            }
        }
    }

    public boolean hasChunkUpdates()
    {
        return this.queueChunkUpdates.isEmpty() && this.queueChunkUploads.isEmpty();
    }
}
