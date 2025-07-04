package mods.flammpfeil.slashblade.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;

public class Scheduler {
    public static final TimerCallbacks<LivingEntity> SB_CALLBACKS = (new TimerCallbacks<>());

    private final TimerQueue<LivingEntity> queue = new TimerQueue<>(SB_CALLBACKS);

    public Scheduler() {
    }

    public void onTick(LivingEntity entity) {
        queue.tick(entity, entity.level().getGameTime());
    }

    public void schedule(String key, long time, TimerCallback<LivingEntity> callback) {
        queue.schedule(key, time, callback);
    }
}
