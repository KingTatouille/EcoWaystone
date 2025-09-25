package fr.mrbeams.ecoWaystone.model;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Bukkit;

@SerializableAs("DiscoveryZone")
public class DiscoveryZone implements ConfigurationSerializable {

    private final String id;
    private final String regionName;
    private final String displayName;
    private final String title;
    private final String subtitle;
    private Location spawnLocation;

    public DiscoveryZone(String id, String regionName, String displayName, String title, String subtitle) {
        this.id = id;
        this.regionName = regionName;
        this.displayName = displayName;
        this.title = title;
        this.subtitle = subtitle;
        this.spawnLocation = null;
    }

    public DiscoveryZone(String id, String regionName, String displayName, String title, String subtitle, Location spawnLocation) {
        this.id = id;
        this.regionName = regionName;
        this.displayName = displayName;
        this.title = title;
        this.subtitle = subtitle;
        this.spawnLocation = spawnLocation;
    }

    public String getId() {
        return id;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public boolean hasSpawn() {
        return spawnLocation != null;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("regionName", regionName);
        map.put("displayName", displayName);
        map.put("title", title);
        map.put("subtitle", subtitle);

        if (spawnLocation != null) {
            map.put("spawnWorld", spawnLocation.getWorld().getName());
            map.put("spawnX", spawnLocation.getX());
            map.put("spawnY", spawnLocation.getY());
            map.put("spawnZ", spawnLocation.getZ());
            map.put("spawnYaw", spawnLocation.getYaw());
            map.put("spawnPitch", spawnLocation.getPitch());
        }

        return map;
    }

    public static DiscoveryZone deserialize(Map<String, Object> map) {
        String id = (String) map.get("id");
        String regionName = (String) map.get("regionName");
        String displayName = (String) map.get("displayName");
        String title = (String) map.get("title");
        String subtitle = (String) map.get("subtitle");

        Location spawnLocation = null;
        if (map.containsKey("spawnWorld")) {
            try {
                String worldName = (String) map.get("spawnWorld");
                org.bukkit.World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    double x = (Double) map.get("spawnX");
                    double y = (Double) map.get("spawnY");
                    double z = (Double) map.get("spawnZ");
                    float yaw = ((Double) map.get("spawnYaw")).floatValue();
                    float pitch = ((Double) map.get("spawnPitch")).floatValue();
                    spawnLocation = new Location(world, x, y, z, yaw, pitch);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du spawn pour la zone " + id + ": " + e.getMessage());
            }
        }

        return new DiscoveryZone(id, regionName, displayName, title, subtitle, spawnLocation);
    }
}