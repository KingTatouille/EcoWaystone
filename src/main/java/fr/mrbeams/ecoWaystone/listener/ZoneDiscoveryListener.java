package fr.mrbeams.ecoWaystone.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.DiscoveryZone;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ZoneDiscoveryListener implements Listener {

    private final EcoWaystone plugin;
    private final Map<UUID, Set<String>> playerCurrentRegions;

    public ZoneDiscoveryListener(EcoWaystone plugin) {
        this.plugin = plugin;
        this.playerCurrentRegions = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Vérifier seulement si le joueur a changé de bloc
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        checkRegionEntry(player);
    }

    private void checkRegionEntry(Player player) {
        UUID playerId = player.getUniqueId();

        try {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(player.getWorld()));

            if (regionManager == null) {
                return;
            }

            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                    BukkitAdapter.asBlockVector(player.getLocation())
            );

            Set<String> currentRegions = regions.getRegions().stream()
                    .map(ProtectedRegion::getId)
                    .collect(java.util.stream.Collectors.toSet());

            Set<String> previousRegions = playerCurrentRegions.get(playerId);

            // Vérifier les nouvelles régions entrées
            if (previousRegions != null) {
                for (String regionName : currentRegions) {
                    if (!previousRegions.contains(regionName)) {
                        // Le joueur vient d'entrer dans cette région
                        onRegionEnter(player, regionName);
                    }
                }
            } else {
                // Premier passage, vérifier toutes les régions actuelles
                for (String regionName : currentRegions) {
                    onRegionEnter(player, regionName);
                }
            }

            // Mettre à jour les régions actuelles du joueur
            playerCurrentRegions.put(playerId, currentRegions);

        } catch (Exception e) {
            // Ignorer les erreurs silencieusement pour éviter le spam de logs
        }
    }

    private void onRegionEnter(Player player, String regionName) {
        // Vérifier si cette région correspond à une zone de découverte
        DiscoveryZone zone = plugin.getZoneManager().getZoneByRegion(regionName);

        if (zone != null) {
            // Tenter de découvrir la zone
            boolean discovered = plugin.getZoneManager().discoverZone(player, zone.getId());

            if (discovered) {
                plugin.getLogger().info("Le joueur " + player.getName() + " a découvert la zone: " + zone.getDisplayName());
            }
        }
    }

    public void onPlayerQuit(UUID playerId) {
        playerCurrentRegions.remove(playerId);
    }
}