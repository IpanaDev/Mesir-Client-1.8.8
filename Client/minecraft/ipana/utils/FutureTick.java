package ipana.utils;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class FutureTick {
    private static final List<TickRunnable> FUTURE = new ArrayList<>();

    public static void addFuture(int tick, Runnable runnable) {
        if (tick <= 0) {
            return;
        }
        FUTURE.add(new TickRunnable(Minecraft.getRunTick() + tick, runnable));
    }

    public static void updateFutures() {
        try {
            for (int i = 0; i < FUTURE.size(); i++) {
                TickRunnable ticker = FUTURE.get(i);
                if (Minecraft.getRunTick() == ticker.tick) {
                    ticker.runnable.run();
                    FUTURE.remove(i--);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class TickRunnable {
        int tick;
        Runnable runnable;

        public TickRunnable(int tick, Runnable runnable) {
            this.tick = tick;
            this.runnable = runnable;
        }
    }
}
