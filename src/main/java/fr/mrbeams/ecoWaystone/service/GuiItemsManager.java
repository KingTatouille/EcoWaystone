package fr.mrbeams.ecoWaystone.service;

import dev.lone.itemsadder.api.CustomStack;
import fr.mrbeams.ecoWaystone.EcoWaystone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class GuiItemsManager {

    private final EcoWaystone plugin;
    private FileConfiguration config;

    public GuiItemsManager(EcoWaystone plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "gui-items.yml");
        if (!configFile.exists()) {
            plugin.saveResource("gui-items.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfig() {
        loadConfig();
    }

    public ItemStack createZoneItem(String zoneName, String regionName, String location, String distance, boolean discovered) {
        return createZoneItem(zoneName, zoneName, regionName, location, distance, discovered);
    }

    public ItemStack createZoneItem(String zoneId, String zoneName, String regionName, String location, String distance, boolean discovered) {
        ConfigurationSection section = getZoneItemSection(zoneId, discovered);

        ItemStack item = createItemFromSection(section);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String displayName = section.getString("name", discovered ? "&a%zone_name%" : "&7Zone inconnue");
            displayName = displayName.replace("%zone_name%", zoneName);
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize(displayName));

            List<String> configLore = section.getStringList("lore");
            List<Component> lore = new ArrayList<>();

            for (String line : configLore) {
                line = line.replace("%zone_name%", zoneName)
                          .replace("%region%", regionName)
                          .replace("%location%", location != null ? location : "")
                          .replace("%distance%", distance != null ? distance : "");
                lore.add(LegacyComponentSerializer.legacySection().deserialize(line));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack createWaystoneItem(String waystoneName, String location, String distance, boolean discovered, String cost) {
        ConfigurationSection section = discovered ?
            config.getConfigurationSection("default-waystone-items.discovered") :
            config.getConfigurationSection("default-waystone-items.undiscovered");

        if (section == null) {
            return new ItemStack(Material.BARRIER);
        }

        ItemStack item = createItemFromSection(section);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String displayName = section.getString("name", discovered ? "&b%waystone_name%" : "&7Waystone inconnue");
            displayName = displayName.replace("%waystone_name%", waystoneName);
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize(displayName));

            List<String> configLore = section.getStringList("lore");
            List<Component> lore = new ArrayList<>();

            for (String line : configLore) {
                line = line.replace("%waystone_name%", waystoneName)
                          .replace("%location%", location != null ? location : "")
                          .replace("%distance%", distance != null ? distance : "")
                          .replace("%cost%", cost != null ? cost : "");
                lore.add(LegacyComponentSerializer.legacySection().deserialize(line));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack createNavigationItem(String type, int pageNumber) {
        ConfigurationSection section = config.getConfigurationSection("navigation-items." + type);
        if (section == null) {
            return getDefaultNavigationItem(type);
        }

        ItemStack item = createItemFromSection(section);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String displayName = section.getString("name", "&eNavigation");
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize(displayName));

            List<String> configLore = section.getStringList("lore");
            List<Component> lore = new ArrayList<>();

            for (String line : configLore) {
                line = line.replace("%page%", String.valueOf(pageNumber));
                lore.add(LegacyComponentSerializer.legacySection().deserialize(line));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ConfigurationSection getZoneItemSection(String zoneId, boolean discovered) {
        String state = discovered ? "discovered" : "undiscovered";

        ConfigurationSection customSection = config.getConfigurationSection("custom-zone-items." + zoneId + "." + state);
        if (customSection != null) {
            return customSection;
        }

        ConfigurationSection defaultSection = config.getConfigurationSection("default-zone-items." + state);
        if (defaultSection != null) {
            return defaultSection;
        }

        plugin.getLogger().warning("Section GUI non trouvée pour zone " + zoneId + " état " + state);
        return config.createSection("temp");
    }

    private ItemStack createItemFromSection(ConfigurationSection section) {
        String itemsAdderID = section.getString("itemsadder", "");
        String materialName = section.getString("material", "BARRIER");

        if (!itemsAdderID.isEmpty()) {
            try {
                CustomStack customStack = CustomStack.getInstance(itemsAdderID);
                if (customStack != null) {
                    return customStack.getItemStack().clone();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Erreur lors du chargement de l'item ItemsAdder: " + itemsAdderID, e);
            }
        }

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            return new ItemStack(material);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Matériau invalide: " + materialName + ", utilisation de BARRIER");
            return new ItemStack(Material.BARRIER);
        }
    }

    private ItemStack getDefaultNavigationItem(String type) {
        switch (type) {
            case "previous-page":
            case "next-page":
                return new ItemStack(Material.ARROW);
            case "close":
                return new ItemStack(Material.BARRIER);
            case "filler":
                return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            default:
                return new ItemStack(Material.BARRIER);
        }
    }
}