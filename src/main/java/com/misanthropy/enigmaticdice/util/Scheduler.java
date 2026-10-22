package com.misanthropy.enigmaticdice.util;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID)
public final class Scheduler {
    private static final List<Task> TASKS = new ArrayList<>();
    private static final List<Task> PENDING = new ArrayList<>();

    private Scheduler() {
    }

    public static void schedule(int delay, Runnable action) {
        repeat(delay, 1, 1, action);
    }

    public static void repeat(int delay, int period, int times, Runnable action) {
        if (times <= 0) return;
        PENDING.add(new Task(Math.max(delay, 1), Math.max(period, 1), times, () -> {
            action.run();
            return true;
        }));
    }

    public static void poll(int timeout, BooleanSupplier done, Runnable onTimeout) {
        PENDING.add(new Task(1, 1, Math.max(timeout, 1), () -> !done.getAsBoolean(), onTimeout));
    }

    @SubscribeEvent
    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!PENDING.isEmpty()) {
            TASKS.addAll(PENDING);
            PENDING.clear();
        }
        Iterator<Task> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (--task.wait > 0) continue;
            boolean keep;
            try {
                keep = task.step.getAsBoolean() && --task.remaining > 0;
                if (keep) {
                    task.wait = task.period;
                } else if (task.remaining <= 0 && task.onExhausted != null) {
                    task.onExhausted.run();
                }
            } catch (RuntimeException exception) {
                EnigmaticDice.LOGGER.error("Scheduled Enigmatic Dice task failed", exception);
                keep = false;
            }
            if (!keep) iterator.remove();
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        TASKS.clear();
        PENDING.clear();
    }

    private static final class Task {
        private final int period;
        private final BooleanSupplier step;
        private final Runnable onExhausted;
        private int wait;
        private int remaining;

        private Task(int delay, int period, int times, BooleanSupplier step) {
            this(delay, period, times, step, null);
        }

        private Task(int delay, int period, int times, BooleanSupplier step, Runnable onExhausted) {
            this.wait = delay;
            this.period = period;
            this.remaining = times;
            this.step = step;
            this.onExhausted = onExhausted;
        }
    }
}
