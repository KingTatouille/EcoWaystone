package fr.mrbeams.ecoWaystone.model;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("AdminWaystone")
public class AdminWaystone implements ConfigurationSerializable {

    private final String id;
    private final String name;
    private final String displayName;
    private final Location location;
    private final String iconItem;
    private final String rewardCommand;
    private final boolean enabled;

    public AdminWaystone(String id, String name, String displayName, Location location,
                        String iconItem, String rewardCommand, boolean enabled) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.location = location;
        this.iconItem = iconItem;
        this.rewardCommand = rewardCommand;
        this.enabled = enabled;
    }

    public AdminWaystone(String id, String name, String displayName, Location location) {
        this(id, name, displayName, location, "waystone", "", true);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public String getRewardCommand() {
        return rewardCommand;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("displayName", displayName);
        result.put("location", location);
        result.put("iconItem", iconItem);
        result.put("rewardCommand", rewardCommand);
        result.put("enabled", enabled);
        return result;
    }

    public static AdminWaystone deserialize(Map<String, Object> args) {
        return new AdminWaystone(
            (String) args.get("id"),
            (String) args.get("name"),
            (String) args.get("displayName"),
            (Location) args.get("location"),
            (String) args.getOrDefault("iconItem", "waystone"),
            (String) args.getOrDefault("rewardCommand", ""),
            (Boolean) args.getOrDefault("enabled", true)
        );
    }
}