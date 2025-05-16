package ipana.utils.chunk;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncChunkBuilder {
    private static final ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat("Chunk Batcher %d").setDaemon(true).build();
    public final BlockingQueue<ChunkTypePair> queueChunkUpdates = Queues.newArrayBlockingQueue(16 * 16 * 16);
    public final Queue<Runnable> queueChunkUploads = Queues.newArrayDeque();
    public final ReentrantLock uploadLock = new ReentrantLock();
    public AtomicInteger chunkUpdates = new AtomicInteger();

    public AsyncChunkBuilder() {
        for (int i = 0; i < threadCount(); i++) {
            Thread thread = threadFactory.newThread(new ChunkBuilder(this));
            thread.start();
        }
    }

    public void updateChunk(ChunkBuildType type, RenderChunk renderChunk) {
        try {
            this.queueChunkUpdates.put(new ChunkTypePair(type, renderChunk));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean runChunkUploads() {
        boolean flag = false;
        while (!this.queueChunkUploads.isEmpty()) {
            this.queueChunkUploads.poll().run();
            flag = true;
        }
        return flag;
    }

    public String getDebugInfo() {
        return String.format("pC: %03d, pL: %1d, pU: %1d", chunkUpdates.get(), this.queueChunkUploads.size(), this.queueChunkUpdates.size());
    }

    private int threadCount() {
        //return Math.max(Runtime.getRuntime().availableProcessors() - 2, 2);
        return 1;
    }

    public void stopChunkUpdates() {
        while (!this.queueChunkUpdates.isEmpty()) {
            this.queueChunkUpdates.poll();
        }

        chunkUpdates.set(0);
        runChunkUploads();
    }
}
