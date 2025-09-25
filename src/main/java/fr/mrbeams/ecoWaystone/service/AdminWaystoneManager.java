package fr.mrbeams.ecoWaystone.service;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.AdminWaystone;
import fr.mrbeams.ecoWaystone.model.PlayerDiscovery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class AdminWaystoneManager {

    private final EcoWaystone plugin;
    private final Map<String, AdminWaystone> waystones;
    private final Map<UUID, PlayerDiscovery> playerDiscoveries;
    private File dataFile;
    private FileConfiguration dataConfig;

    public AdminWaystoneManager(EcoWaystone plugin) {
        this.plugin = plugin;
        this.waystones = new HashMap<>();
        this.playerDiscoveries = new HashMap<>();
        setupDataFile();
        loadData();
    }

    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "waystones.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create waystones.yml", e);
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadData() {
        waystones.clear();
        playerDiscoveries.clear();

        if (dataConfig.contains("waystones")) {
            for (String key : dataConfig.getConfigurationSection("waystones").getKeys(false)) {
                try {
                    AdminWaystone waystone = (AdminWaystone) dataConfig.get("waystones." + key);
                    if (waystone != null) {
                        waystones.put(waystone.getId(), waystone);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load waystone: " + key, e);
                }
            }
        }

        if (dataConfig.contains("discoveries")) {
            for (String key : dataConfig.getConfigurationSection("discoveries").getKeys(false)) {
                try {
                    PlayerDiscovery discovery = (PlayerDiscovery) dataConfig.get("discoveries." + key);
                    if (discovery != null) {
                        playerDiscoveries.put(discovery.getPlayerId(), discovery);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load player discovery: " + key, e);
                }
            }
        }

        plugin.getLogger().info("Loaded " + waystones.size() + " waystones and " + playerDiscoveries.size() + " player discoveries");
    }

    public void saveData() {
        for (AdminWaystone waystone : waystones.values()) {
            dataConfig.set("waystones." + waystone.getId(), waystone);
        }

        for (PlayerDiscovery discovery : playerDiscoveries.values()) {
            dataConfig.set("discoveries." + discovery.getPlayerId().toString(), discovery);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save waystones.yml", e);
        }
    }

    public void createWaystone(String id, String name, String displayName, Location location) {
        AdminWaystone waystone = new AdminWaystone(id, name, displayName, location);
        waystones.put(id, waystone);
        saveData();
    }

    public boolean deleteWaystone(String id) {
        AdminWaystone removed = waystones.remove(id);
        if (removed != null) {
            for (PlayerDiscovery discovery : playerDiscoveries.values()) {
                discovery.removeDiscovery(id);
            }
            saveData();
            return true;
        }
        return false;
    }

    public AdminWaystone getWaystone(String id) {
        return waystones.get(id);
    }

    public AdminWaystone getWaystoneAt(Location location) {
        for (AdminWaystone waystone : waystones.values()) {
            if (waystone.getLocation().equals(location)) {
                return waystone;
            }
        }
        return null;
    }

    public Collection<AdminWaystone> getAllWaystones() {
        return new ArrayList<>(waystones.values());
    }

    public Collection<AdminWaystone> getDiscoveredWaystones(Player player) {
        PlayerDiscovery discovery = getPlayerDiscovery(player);
        List<AdminWaystone> discovered = new ArrayList<>();

        for (String waystoneId : discovery.getDiscoveredWaystones()) {
            AdminWaystone waystone = waystones.get(waystoneId);
            if (waystone != null && waystone.isEnabled()) {
                discovered.add(waystone);
            }
        }

        return discovered;
    }

    public PlayerDiscovery getPlayerDiscovery(Player player) {
        return playerDiscoveries.computeIfAbsent(player.getUniqueId(), k -> new PlayerDiscovery(player.getUniqueId()));
    }

    public boolean discoverWaystone(Player player, String waystoneId) {
        PlayerDiscovery discovery = getPlayerDiscovery(player);
        boolean wasNew = discovery.addDiscovery(waystoneId);

        if (wasNew) {
            playerDiscoveries.put(player.getUniqueId(), discovery);
            saveData();

            AdminWaystone waystone = waystones.get(waystoneId);
            if (waystone != null && !waystone.getRewardCommand().isEmpty()) {
                String command = waystone.getRewardCommand().replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }

        return wasNew;
    }

    public boolean hasDiscovered(Player player, String waystoneId) {
        return getPlayerDiscovery(player).hasDiscovered(waystoneId);
    }

    public int getDiscoveryCount(Player player) {
        return getPlayerDiscovery(player).getDiscoveryCount();
    }

    public void shutdown() {
        saveData();
    }
}