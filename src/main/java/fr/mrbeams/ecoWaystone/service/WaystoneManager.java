package fr.mrbeams.ecoWaystone.service;

import fr.mrbeams.ecoWaystone.model.Waystone;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WaystoneManager {
    private final Map<UUID, Waystone> waystones;
    private final Map<Location, UUID> locationIndex;
    private final File dataFile;
    private final Map<Material, Double> boostBlocks;

    public WaystoneManager(File dataFolder, Map<Material, Double> boostBlocks) {
        this.waystones = new ConcurrentHashMap<>();
        this.locationIndex = new ConcurrentHashMap<>();
        this.dataFile = new File(dataFolder, "waystones.yml");
        this.boostBlocks = new HashMap<>(boostBlocks);
        loadWaystones();
    }

    public Waystone createWaystone(Location location, UUID creator) {
        if (hasWaystoneAt(location)) {
            return null;
        }

        UUID id = UUID.randomUUID();
        Waystone waystone = new Waystone(id, location, creator);

        waystones.put(id, waystone);
        locationIndex.put(location, id);

        saveWaystones();
        return waystone;
    }

    public void removeWaystone(UUID id) {
        Waystone waystone = waystones.remove(id);
        if (waystone != null) {
            locationIndex.remove(waystone.getLocation());
            saveWaystones();
        }
    }

    public void removeWaystone(Location location) {
        UUID id = locationIndex.remove(location);
        if (id != null) {
            waystones.remove(id);
            saveWaystones();
        }
    }

    public Waystone getWaystone(UUID id) {
        return waystones.get(id);
    }

    public Waystone getWaystoneAt(Location location) {
        UUID id = locationIndex.get(location);
        return id != null ? waystones.get(id) : null;
    }

    public boolean hasWaystoneAt(Location location) {
        return locationIndex.containsKey(location);
    }

    public Collection<Waystone> getAllWaystones() {
        return new ArrayList<>(waystones.values());
    }

    public void setWaystoneName(UUID id, String name) {
        Waystone waystone = waystones.get(id);
        if (waystone != null) {
            waystone.setName(name);
            saveWaystones();
        }
    }

    public boolean isWaystoneBlocked(Location location) {
        Block below = location.getBlock().getRelative(0, -1, 0);
        return below.getType() == Material.OBSIDIAN;
    }

    public boolean hasCommandBlockBelow(Location location) {
        Block below = location.getBlock().getRelative(0, -1, 0);
        return below.getType() == Material.COMMAND_BLOCK;
    }

    public boolean hasRespawnAnchorBelow(Location location) {
        Block below = location.getBlock().getRelative(0, -1, 0);
        return below.getType() == Material.RESPAWN_ANCHOR;
    }

    public int getRespawnAnchorCharges(Location location) {
        Block below = location.getBlock().getRelative(0, -1, 0);
        if (below.getType() == Material.RESPAWN_ANCHOR) {
            return below.getBlockData().getAsString().contains("charges=4") ? 4 :
                   below.getBlockData().getAsString().contains("charges=3") ? 3 :
                   below.getBlockData().getAsString().contains("charges=2") ? 2 :
                   below.getBlockData().getAsString().contains("charges=1") ? 1 : 0;
        }
        return 0;
    }

    public int calculateWaystoneRange(Location location, int baseDistance, int maxBoost, int maxWarpSize) {
        if (hasCommandBlockBelow(location)) {
            return Integer.MAX_VALUE;
        }

        int totalBoost = 0;
        int blocksChecked = 0;

        for (int x = -maxWarpSize; x <= maxWarpSize && blocksChecked < maxWarpSize; x++) {
            for (int y = -maxWarpSize; y <= maxWarpSize && blocksChecked < maxWarpSize; y++) {
                for (int z = -maxWarpSize; z <= maxWarpSize && blocksChecked < maxWarpSize; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block block = location.getBlock().getRelative(x, y, z);
                    Material material = block.getType();

                    if (boostBlocks.containsKey(material)) {
                        double boostMultiplier = boostBlocks.get(material);
                        int boost = (int) (maxBoost * boostMultiplier);
                        totalBoost += boost;
                        blocksChecked++;
                    }
                }
            }
        }

        return baseDistance + totalBoost;
    }

    private void loadWaystones() {
        if (!dataFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection waystoneSection = config.getConfigurationSection("waystones");

        if (waystoneSection == null) {
            return;
        }

        for (String idString : waystoneSection.getKeys(false)) {
            try {
                UUID id = UUID.fromString(idString);
                ConfigurationSection section = waystoneSection.getConfigurationSection(idString);

                if (section == null) continue;

                String worldName = section.getString("world");
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                String creatorString = section.getString("creator");
                String name = section.getString("name");

                if (worldName == null || creatorString == null) continue;

                Location location = new Location(
                    org.bukkit.Bukkit.getWorld(worldName),
                    x, y, z
                );

                if (location.getWorld() == null) continue;

                UUID creator = UUID.fromString(creatorString);
                Waystone waystone = new Waystone(id, location, creator);

                if (name != null && !name.isEmpty()) {
                    waystone.setName(name);
                }

                waystones.put(id, waystone);
                locationIndex.put(location, id);

            } catch (IllegalArgumentException e) {
                System.err.println("Invalid waystone data for ID: " + idString);
            }
        }
    }

    private void saveWaystones() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Waystone> entry : waystones.entrySet()) {
            UUID id = entry.getKey();
            Waystone waystone = entry.getValue();
            Location location = waystone.getLocation();

            String path = "waystones." + id.toString();
            config.set(path + ".world", location.getWorld().getName());
            config.set(path + ".x", location.getX());
            config.set(path + ".y", location.getY());
            config.set(path + ".z", location.getZ());
            config.set(path + ".creator", waystone.getCreator().toString());

            if (waystone.getName() != null) {
                config.set(path + ".name", waystone.getName());
            }
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            System.err.println("Could not save waystones: " + e.getMessage());
        }
    }
}