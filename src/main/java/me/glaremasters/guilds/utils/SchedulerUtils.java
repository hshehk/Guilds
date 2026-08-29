package me.glaremasters.guilds.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Small facade around Paper/Folia schedulers.
 *
 * Keeping scheduler access in one place prevents individual classes from
 * depending on BukkitScheduler/BukkitRunnable, which are not Folia-safe.
 */
public final class SchedulerUtils {

    private SchedulerUtils() {
    }

    public static ScheduledTask runEntity(Plugin plugin, Entity entity, Runnable task) {
        return entity.getScheduler().run(plugin, ignored -> task.run(), () -> {
        });
    }

    public static ScheduledTask runEntityLater(Plugin plugin, Entity entity, long delayTicks, Runnable task) {
        return entity.getScheduler().runDelayed(plugin, ignored -> task.run(), () -> {
        }, delayTicks);
    }

    public static ScheduledTask runRegionLater(Plugin plugin, Location location, long delayTicks, Runnable task) {
        return plugin.getServer().getRegionScheduler()
                .runDelayed(plugin, location, ignored -> task.run(), delayTicks);
    }

    public static ScheduledTask runGlobal(Plugin plugin, Runnable task) {
        return plugin.getServer().getGlobalRegionScheduler()
                .run(plugin, ignored -> task.run());
    }

    public static ScheduledTask runGlobalLater(Plugin plugin, long delayTicks, Runnable task) {
        return plugin.getServer().getGlobalRegionScheduler()
                .runDelayed(plugin, ignored -> task.run(), delayTicks);
    }

    public static ScheduledTask runGlobalRepeating(
            Plugin plugin, long initialDelayTicks, long periodTicks, Runnable task) {
        return plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, ignored -> task.run(), initialDelayTicks, periodTicks);
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        plugin.getServer().getAsyncScheduler()
                .runNow(plugin, ignored -> task.run());
    }

    public static ScheduledTask runAsyncLater(
            Plugin plugin, long delay, TimeUnit unit, Runnable task) {
        return plugin.getServer().getAsyncScheduler()
                .runDelayed(plugin, ignored -> task.run(), delay, unit);
    }
}
