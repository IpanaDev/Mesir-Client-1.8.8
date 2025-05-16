package ipana.utils.async;

import ipana.events.EventPacketReceive;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Async {
    private static final ExecutorService mainThread = Executors.newFixedThreadPool(16);

    public static void runInThread(Runnable runnable) {
        mainThread.submit(runnable);
    }

    public static ExecutorService mainThread() {
        return mainThread;
    }

    public static void runInMainThread(Runnable runnable) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            System.out.println("A RUNNABLE MOVED TO MINECRAFT'S THREAD");
            Minecraft.getMinecraft().addScheduledTask(runnable);
        } else {
            runnable.run();
        }
    }
}
