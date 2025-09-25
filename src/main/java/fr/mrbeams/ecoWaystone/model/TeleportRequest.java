package fr.mrbeams.ecoWaystone.model;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class TeleportRequest {
    private final UUID playerId;
    private final UUID waystoneId;
    private final long startTime;
    private final int waitTicks;
    private BukkitTask task;
    private boolean cancelled;

    public TeleportRequest(Player player, UUID waystoneId, int waitTicks) {
        this.playerId = player.getUniqueId();
        this.waystoneId = waystoneId;
        this.startTime = System.currentTimeMillis();
        this.waitTicks = waitTicks;
        this.cancelled = false;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getWaystoneId() {
        return waystoneId;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getWaitTicks() {
        return waitTicks;
    }

    public BukkitTask getTask() {
        return task;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public int getRemainingTicks() {
        long elapsedTicks = (System.currentTimeMillis() - startTime) / 50;
        return Math.max(0, waitTicks - (int) elapsedTicks);
    }
}