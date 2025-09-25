package fr.mrbeams.ecoWaystone.service;

import fr.mrbeams.ecoWaystone.model.PowerRequirement;
import fr.mrbeams.ecoWaystone.model.PortalSicknessAction;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public String getLocale() {
        return config.getString("locale", "en_US");
    }

    public int getWaitTime() {
        return config.getInt("wait-time", 60);
    }

    public boolean isDamageStopsWarping() {
        return config.getBoolean("damage-stops-warping", true);
    }

    public boolean isLimitDistance() {
        return config.getBoolean("limit-distance", true);
    }

    public int getBaseDistance() {
        return config.getInt("base-distance", 100);
    }

    public int getMaxBoost() {
        return config.getInt("max-boost", 150);
    }

    public int getMaxWarpSize() {
        return config.getInt("max-warp-size", 50);
    }

    public boolean isJumpWorlds() {
        return config.getBoolean("jump-worlds", true);
    }

    public int getWorldRatio() {
        return config.getInt("world-ratio", 8);
    }

    public boolean isWarpAnimations() {
        return config.getBoolean("warp-animations", true);
    }

    public boolean isSingleUse() {
        return config.getBoolean("single-use", false);
    }

    public boolean isRelinkableKeys() {
        return config.getBoolean("relinkable-keys", true);
    }

    public boolean isEnableKeyItems() {
        return config.getBoolean("enable-key-items", true);
    }

    public PowerRequirement getRequirePower() {
        String value = config.getString("require-power", "INTER_DIMENSION");
        return PowerRequirement.fromString(value);
    }

    public int getPowerCost() {
        return config.getInt("power-cost", 1);
    }

    public boolean isEnablePortalSickness() {
        return config.getBoolean("enable-portal-sickness", true);
    }

    public double getPortalSicknessChance() {
        return config.getDouble("portal-sickness-chance", 0.05);
    }

    public PortalSicknessAction getPortalSicknessWarping() {
        String value = config.getString("portal-sickness-warping", "DAMAGE_ON_TELEPORT");
        return PortalSicknessAction.fromString(value);
    }

    public double getPortalSicknessDamage() {
        return config.getDouble("portal-sickness-damage", 5.0);
    }

    public boolean isEnableAdvancements() {
        return config.getBoolean("enable-advancements", true);
    }

    public Map<Material, Double> getBoostBlocks() {
        Map<Material, Double> boostBlocks = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("boost-blocks");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    double boost = section.getDouble(key);
                    boostBlocks.put(material, boost);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid material in boost-blocks: " + key);
                }
            }
        }

        if (boostBlocks.isEmpty()) {
            boostBlocks.put(Material.NETHERITE_BLOCK, 1.0);
            boostBlocks.put(Material.EMERALD_BLOCK, 0.75);
            boostBlocks.put(Material.DIAMOND_BLOCK, 0.5);
            boostBlocks.put(Material.GOLD_BLOCK, 0.33);
            boostBlocks.put(Material.IRON_BLOCK, 0.2);
        }

        return boostBlocks;
    }

    public void setValue(String path, Object value) {
        config.set(path, value);
    }

    public Object getValue(String path) {
        return config.get(path);
    }

    public boolean hasPath(String path) {
        return config.contains(path);
    }
}