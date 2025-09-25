package fr.mrbeams.ecoWaystone.gui;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.DiscoveryZone;
import fr.mrbeams.ecoWaystone.service.GuiItemsManager;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WaystoneGUI {

    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private static final int ITEMS_PER_PAGE = 45; // 5 rows of items, 1 row for navigation

    private final EcoWaystone plugin;
    private final MessageManager messageManager;
    private final GuiItemsManager guiItemsManager;

    public WaystoneGUI(EcoWaystone plugin, MessageManager messageManager, GuiItemsManager guiItemsManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.guiItemsManager = guiItemsManager;
    }

    public void openWaystoneMenu(Player player) {
        openWaystoneMenu(player, 0);
    }

    public void openWaystoneMenu(Player player, int page) {
        Set<String> discoveredZones = plugin.getZoneManager().getDiscoveredZones(player);
        Map<String, DiscoveryZone> allZones = plugin.getZoneManager().getAllZones();

        int totalZones = allZones.size();
        int discoveredZonesCount = discoveredZones.size();

        if (allZones.isEmpty()) {
            Component message = Component.text("Aucune zone n'existe encore ! ")
                    .color(NamedTextColor.YELLOW);
            player.sendMessage(message);
            return;
        }

        // Créer la liste des éléments GUI
        List<GuiItem> allItems = new ArrayList<>();

        // Ajouter les zones découvertes
        for (String zoneId : discoveredZones) {
            DiscoveryZone zone = allZones.get(zoneId);
            if (zone != null) {
                allItems.add(new GuiItem(zone, true)); // true = découverte
            }
        }

        // Ajouter les zones non découvertes
        for (DiscoveryZone zone : allZones.values()) {
            if (!discoveredZones.contains(zone.getId())) {
                allItems.add(new GuiItem(zone, false)); // false = non découverte
            }
        }

        int maxPages = (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);
        if (maxPages == 0) maxPages = 1;

        page = Math.max(0, Math.min(page, maxPages - 1));
        playerPages.put(player.getUniqueId(), page);

        Component title = Component.text("Zones (" + discoveredZonesCount + "/" + totalZones + ") - Page " + (page + 1) + "/" + maxPages)
                .color(NamedTextColor.BLUE);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Ajouter les éléments
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            GuiItem guiItem = allItems.get(i);
            ItemStack item = createGuiItem(guiItem, player);
            gui.setItem(i - startIndex, item);
        }

        // Navigation items
        if (page > 0) {
            gui.setItem(45, guiItemsManager.createNavigationItem("previous-page", page));
        }

        if (page < maxPages - 1) {
            gui.setItem(53, guiItemsManager.createNavigationItem("next-page", page + 2));
        }

        gui.setItem(49, guiItemsManager.createNavigationItem("close", 0));

        // Fill empty navigation slots
        for (int i = 45; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, guiItemsManager.createNavigationItem("filler", 0));
            }
        }

        player.openInventory(gui);
    }

    public void nextPage(Player player) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        openWaystoneMenu(player, currentPage + 1);
    }

    public void previousPage(Player player) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        openWaystoneMenu(player, currentPage - 1);
    }


    private ItemStack createGuiItem(GuiItem guiItem, Player player) {
        if (guiItem.getType() == GuiItem.ItemType.ZONE) {
            return createZoneItem(guiItem.getZone(), player, guiItem.isDiscovered());
        }
        return new ItemStack(Material.BARRIER);
    }


    private ItemStack createZoneItem(DiscoveryZone zone, Player player, boolean discovered) {
        String location = formatZoneSpawnLocation(zone);
        String distance = null;

        if (discovered && zone.hasSpawn()) {
            double distanceValue = calculateZoneSpawnDistance(player, zone);
            if (distanceValue >= 0) {
                distance = String.format("%.1f blocs", distanceValue);
            } else {
                distance = "Dimension différente";
            }
        }

        return guiItemsManager.createZoneItem(
            zone.getId(),
            zone.getDisplayName(),
            zone.getDescription(),
            zone.getRegionName(),
            location,
            distance,
            discovered
        );
    }





    private String formatZoneSpawnLocation(DiscoveryZone zone) {
        if (!zone.hasSpawn()) return "Aucun spawn";
        return String.format("%s: %d, %d, %d",
                zone.getSpawnLocation().getWorld().getName(),
                zone.getSpawnLocation().getBlockX(),
                zone.getSpawnLocation().getBlockY(),
                zone.getSpawnLocation().getBlockZ());
    }

    private double calculateZoneSpawnDistance(Player player, DiscoveryZone zone) {
        if (!zone.hasSpawn() || !player.getWorld().equals(zone.getSpawnLocation().getWorld())) {
            return -1;
        }
        return player.getLocation().distance(zone.getSpawnLocation());
    }

    public GuiItem getGuiItemAtSlot(Player player, int slot) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);

        Set<String> discoveredZones = plugin.getZoneManager().getDiscoveredZones(player);
        Map<String, DiscoveryZone> allZones = plugin.getZoneManager().getAllZones();

        // Créer la liste des éléments GUI (même logique que dans openWaystoneMenu)
        List<GuiItem> allItems = new ArrayList<>();

        // Ajouter les zones découvertes
        for (String zoneId : discoveredZones) {
            DiscoveryZone zone = allZones.get(zoneId);
            if (zone != null) {
                allItems.add(new GuiItem(zone, true));
            }
        }

        // Ajouter les zones non découvertes
        for (DiscoveryZone zone : allZones.values()) {
            if (!discoveredZones.contains(zone.getId())) {
                allItems.add(new GuiItem(zone, false));
            }
        }

        int actualIndex = currentPage * ITEMS_PER_PAGE + slot;
        if (actualIndex >= 0 && actualIndex < allItems.size()) {
            return allItems.get(actualIndex);
        }
        return null;
    }
}