package fr.mrbeams.ecoWaystone.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.DiscoveryZone;
import fr.mrbeams.ecoWaystone.model.PlayerZoneDiscovery;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ZoneManager {

    private final EcoWaystone plugin;
    private final Map<String, DiscoveryZone> zones;
    private final Map<UUID, PlayerZoneDiscovery> playerDiscoveries;
    private final File zonesDirectory;
    private final File playerDataFile;

    public ZoneManager(EcoWaystone plugin) {
        this.plugin = plugin;
        this.zones = new HashMap<>();
        this.playerDiscoveries = new HashMap<>();
        this.zonesDirectory = new File(plugin.getDataFolder(), "zones");
        this.playerDataFile = new File(plugin.getDataFolder(), "zone_discoveries.yml");

        if (!zonesDirectory.exists()) {
            zonesDirectory.mkdirs();
        }

        loadData();
    }

    public void createZone(String regionName, String displayName, World world) {
        // Vérifier que la région existe dans WorldGuard
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            plugin.getLogger().warning("Impossible de trouver le RegionManager pour le monde: " + world.getName());
            return;
        }

        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) {
            plugin.getLogger().warning("Région WorldGuard non trouvée: " + regionName);
            return;
        }

        // Générer l'ID (sans accents, sans espaces, en minuscules)
        String zoneId = generateZoneId(displayName);

        // Obtenir les messages de titre depuis la config
        String titleMessage = plugin.getConfig().getString("zones.discovery.title", "Région découverte !")
                .replace("%zone%", displayName);
        String subtitleMessage = plugin.getConfig().getString("zones.discovery.subtitle", "Vous avez découvert %zone%")
                .replace("%zone%", displayName);

        // Créer la zone
        DiscoveryZone zone = new DiscoveryZone(zoneId, regionName, displayName, titleMessage, subtitleMessage);
        zones.put(zoneId, zone);

        // Créer le fichier .yml pour cette zone
        saveZoneFile(zone);

        plugin.getLogger().info("Zone de découverte créée: " + displayName + " (ID: " + zoneId + ", Région: " + regionName + ")");
    }

    public boolean discoverZone(Player player, String zoneId) {
        UUID playerId = player.getUniqueId();

        PlayerZoneDiscovery discovery = playerDiscoveries.computeIfAbsent(playerId, PlayerZoneDiscovery::new);

        if (discovery.hasDiscovered(zoneId)) {
            return false; // Déjà découvert
        }

        discovery.addDiscoveredZone(zoneId);
        savePlayerData();

        // Afficher le titre si la zone existe
        DiscoveryZone zone = zones.get(zoneId);
        if (zone != null) {
            showDiscoveryTitle(player, zone);
        }

        return true;
    }

    public boolean undiscoverZone(Player player, String zoneId) {
        UUID playerId = player.getUniqueId();

        PlayerZoneDiscovery discovery = playerDiscoveries.get(playerId);
        if (discovery == null || !discovery.hasDiscovered(zoneId)) {
            return false; // Pas découvert
        }

        discovery.removeDiscoveredZone(zoneId);
        savePlayerData();

        return true;
    }

    public boolean hasDiscovered(Player player, String zoneId) {
        PlayerZoneDiscovery discovery = playerDiscoveries.get(player.getUniqueId());
        return discovery != null && discovery.hasDiscovered(zoneId);
    }

    public Set<String> getDiscoveredZones(Player player) {
        PlayerZoneDiscovery discovery = playerDiscoveries.get(player.getUniqueId());
        return discovery != null ? discovery.getDiscoveredZones() : Set.of();
    }

    public DiscoveryZone getZone(String zoneId) {
        return zones.get(zoneId);
    }

    public Map<String, DiscoveryZone> getAllZones() {
        return new HashMap<>(zones);
    }

    public DiscoveryZone getZoneByRegion(String regionName) {
        return zones.values().stream()
                .filter(zone -> zone.getRegionName().equals(regionName))
                .findFirst()
                .orElse(null);
    }

    private void showDiscoveryTitle(Player player, DiscoveryZone zone) {
        Component titleComponent = Component.text(zone.getTitle()).color(NamedTextColor.GOLD);
        Component subtitleComponent = Component.text(zone.getSubtitle()).color(NamedTextColor.YELLOW);

        Title title = Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
        );

        player.showTitle(title);
    }

    private String generateZoneId(String displayName) {
        return displayName.toLowerCase()
                .replace(" ", "_")
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ýÿ]", "y")
                .replaceAll("[ç]", "c")
                .replaceAll("[ñ]", "n")
                .replaceAll("[^a-z0-9_]", "");
    }

    private void saveZoneFile(DiscoveryZone zone) {
        File zoneFile = new File(zonesDirectory, zone.getId() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("zone", zone.serialize());

        try {
            config.save(zoneFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de la sauvegarde du fichier de zone: " + zoneFile.getName());
            e.printStackTrace();
        }
    }

    private void loadData() {
        loadZones();
        loadPlayerData();
    }

    private void loadZones() {
        if (!zonesDirectory.exists()) {
            return;
        }

        File[] zoneFiles = zonesDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (zoneFiles == null) {
            return;
        }

        for (File zoneFile : zoneFiles) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(zoneFile);
                Map<String, Object> zoneData = config.getConfigurationSection("zone").getValues(false);
                DiscoveryZone zone = DiscoveryZone.deserialize(zoneData);
                zones.put(zone.getId(), zone);
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur lors du chargement du fichier de zone: " + zoneFile.getName());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("Chargé " + zones.size() + " zone(s) de découverte");
    }

    private void loadPlayerData() {
        if (!playerDataFile.exists()) {
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerDataFile);

            if (config.contains("discoveries")) {
                for (String key : config.getConfigurationSection("discoveries").getKeys(false)) {
                    Map<String, Object> data = config.getConfigurationSection("discoveries." + key).getValues(false);
                    PlayerZoneDiscovery discovery = PlayerZoneDiscovery.deserialize(data);
                    playerDiscoveries.put(discovery.getPlayerId(), discovery);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors du chargement des données de découverte des joueurs");
            e.printStackTrace();
        }
    }

    private void savePlayerData() {
        try {
            FileConfiguration config = new YamlConfiguration();

            for (Map.Entry<UUID, PlayerZoneDiscovery> entry : playerDiscoveries.entrySet()) {
                String key = entry.getKey().toString();
                config.set("discoveries." + key, entry.getValue().serialize());
            }

            config.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de la sauvegarde des données de découverte des joueurs");
            e.printStackTrace();
        }
    }

    public boolean setZoneSpawn(String zoneId, Location location) {
        DiscoveryZone zone = zones.get(zoneId);
        if (zone == null) {
            return false;
        }

        zone.setSpawnLocation(location);

        // Sauvegarder le fichier de zone mis à jour
        saveZoneFile(zone);

        plugin.getLogger().info("Spawn défini pour la zone '" + zone.getDisplayName() + "' à " + formatLocation(location));
        return true;
    }

    public boolean removeZoneSpawn(String zoneId) {
        DiscoveryZone zone = zones.get(zoneId);
        if (zone == null) {
            return false;
        }

        zone.setSpawnLocation(null);

        // Sauvegarder le fichier de zone mis à jour
        saveZoneFile(zone);

        plugin.getLogger().info("Spawn supprimé de la zone '" + zone.getDisplayName() + "'");
        return true;
    }

    private String formatLocation(Location location) {
        return String.format("%s: %d, %d, %d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    public void shutdown() {
        savePlayerData();
    }
}