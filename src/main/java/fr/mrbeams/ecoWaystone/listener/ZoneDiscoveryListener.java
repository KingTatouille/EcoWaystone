package fr.mrbeams.ecoWaystone.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.DiscoveryZone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneDiscoveryListener implements Listener {

    private final EcoWaystone plugin;
    private final Map<UUID, Set<String>> playerCurrentRegions;
    private final Map<UUID, Long> lastChecked;
    private static final long CHECK_COOLDOWN = 1000; // 1 seconde entre les vérifications

    public ZoneDiscoveryListener(EcoWaystone plugin) {
        this.plugin = plugin;
        this.playerCurrentRegions = new ConcurrentHashMap<>();
        this.lastChecked = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Vérifier seulement si le joueur a changé de bloc
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Vérifier le cooldown pour éviter les vérifications trop fréquentes
        long currentTime = System.currentTimeMillis();
        Long lastCheck = lastChecked.get(playerId);
        if (lastCheck != null && (currentTime - lastCheck) < CHECK_COOLDOWN) {
            return;
        }

        lastChecked.put(playerId, currentTime);
        checkRegionEntry(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Vérifier immédiatement après une téléportation
        Player player = event.getPlayer();

        // Utiliser un délai pour s'assurer que la téléportation est complète
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    checkRegionEntry(player);
                }
            }
        }.runTaskLater(plugin, 2L); // 2 ticks de délai
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Vérifier les régions quelques secondes après la connexion
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    checkRegionEntry(player);
                }
            }
        }.runTaskLater(plugin, 40L); // 2 secondes de délai
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        onPlayerQuit(playerId);
    }

    private void checkRegionEntry(Player player) {
        UUID playerId = player.getUniqueId();

        try {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(player.getWorld()));

            if (regionManager == null) {
                plugin.getLogger().warning("RegionManager est null pour le monde: " + player.getWorld().getName());
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
            plugin.getLogger().severe("Erreur lors de la vérification des régions pour " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
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
        lastChecked.remove(playerId);
    }
}