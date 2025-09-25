package fr.mrbeams.ecoWaystone.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("ZoneSpawn")
public class ZoneSpawn implements ConfigurationSerializable {

    private final String id;
    private final String displayName;
    private final Location location;
    private final String iconItem;

    public ZoneSpawn(String id, String displayName, Location location, String iconItem) {
        this.id = id;
        this.displayName = displayName;
        this.location = location;
        this.iconItem = iconItem;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Location getLocation() {
        return location;
    }

    public String getIconItem() {
        return iconItem;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("displayName", displayName);
        map.put("world", location.getWorld().getName());
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", location.getYaw());
        map.put("pitch", location.getPitch());
        map.put("iconItem", iconItem);
        return map;
    }

    public static ZoneSpawn deserialize(Map<String, Object> map) {
        String id = (String) map.get("id");
        String displayName = (String) map.get("displayName");
        String worldName = (String) map.get("world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw new IllegalStateException("Le monde " + worldName + " n'existe pas pour le spawn " + id);
        }

        double x = (Double) map.get("x");
        double y = (Double) map.get("y");
        double z = (Double) map.get("z");
        float yaw = ((Double) map.get("yaw")).floatValue();
        float pitch = ((Double) map.get("pitch")).floatValue();
        String iconItem = (String) map.get("iconItem");

        Location location = new Location(world, x, y, z, yaw, pitch);

        return new ZoneSpawn(id, displayName, location, iconItem);
    }
}