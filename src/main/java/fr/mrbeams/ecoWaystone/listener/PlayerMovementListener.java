package fr.mrbeams.ecoWaystone.listener;

import fr.mrbeams.ecoWaystone.service.TeleportationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementListener implements Listener {
    private final TeleportationService teleportationService;

    public PlayerMovementListener(TeleportationService teleportationService) {
        this.teleportationService = teleportationService;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!teleportationService.hasActiveWarp(player)) {
            return;
        }

        if (event.getFrom().distanceSquared(event.getTo()) > 0.1) {
            teleportationService.cancelWarp(player, "teleport.cancelled-movement");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!teleportationService.hasActiveWarp(player)) {
            return;
        }

        teleportationService.cancelWarp(player, "teleport.cancelled-damage");
    }
}