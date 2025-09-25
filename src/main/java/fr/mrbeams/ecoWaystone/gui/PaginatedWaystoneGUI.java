package fr.mrbeams.ecoWaystone.gui;

import dev.lone.itemsadder.api.CustomStack;
import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.AdminWaystone;
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

public class PaginatedWaystoneGUI {

    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private static final int ITEMS_PER_PAGE = 45; // 5 rows of items, 1 row for navigation

    private final EcoWaystone plugin;
    private final MessageManager messageManager;

    public PaginatedWaystoneGUI(EcoWaystone plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    public void openWaystoneMenu(Player player) {
        openWaystoneMenu(player, 0);
    }

    public void openWaystoneMenu(Player player, int page) {
        Collection<AdminWaystone> discoveredWaystones = plugin.getWaystoneManager().getDiscoveredWaystones(player);
        Collection<AdminWaystone> allWaystones = plugin.getWaystoneManager().getAllWaystones();

        int totalWaystones = allWaystones.size();
        int discoveredCount = discoveredWaystones.size();

        if (allWaystones.isEmpty()) {
            Component message = Component.text("Aucune waystone n'existe encore ! ")
                    .color(NamedTextColor.YELLOW);
            player.sendMessage(message);
            return;
        }

        // Include undiscovered waystones for purchase
        List<AdminWaystone> availableWaystones = new ArrayList<>(discoveredWaystones);
        List<AdminWaystone> undiscoveredWaystones = new ArrayList<>();

        for (AdminWaystone waystone : allWaystones) {
            if (!plugin.getWaystoneManager().hasDiscovered(player, waystone.getId())) {
                undiscoveredWaystones.add(waystone);
            }
        }

        List<AdminWaystone> allAvailable = new ArrayList<>(availableWaystones);
        allAvailable.addAll(undiscoveredWaystones);

        int maxPages = (int) Math.ceil((double) allAvailable.size() / ITEMS_PER_PAGE);
        if (maxPages == 0) maxPages = 1;

        page = Math.max(0, Math.min(page, maxPages - 1));
        playerPages.put(player.getUniqueId(), page);

        Component title = Component.text("Waystones (" + discoveredCount + "/" + totalWaystones + ") - Page " + (page + 1) + "/" + maxPages)
                .color(NamedTextColor.BLUE);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Add waystone items
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allAvailable.size());

        for (int i = startIndex; i < endIndex; i++) {
            AdminWaystone waystone = allAvailable.get(i);
            boolean isDiscovered = plugin.getWaystoneManager().hasDiscovered(player, waystone.getId());
            ItemStack item = createWaystoneItem(waystone, player, isDiscovered);
            gui.setItem(i - startIndex, item);
        }

        // Navigation items
        if (page > 0) {
            gui.setItem(45, createNavigationItem(Material.ARROW, "§ePage précédente", "§7Cliquez pour aller à la page " + page));
        }

        if (page < maxPages - 1) {
            gui.setItem(53, createNavigationItem(Material.ARROW, "§ePage suivante", "§7Cliquez pour aller à la page " + (page + 2)));
        }

        gui.setItem(49, createNavigationItem(Material.BARRIER, "§cFermer", "§7Cliquez pour fermer"));

        // Fill empty navigation slots
        for (int i = 45; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, createFillerItem());
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

    private ItemStack createNavigationItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name));
            meta.lore(List.of(Component.text(lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createWaystoneItem(AdminWaystone waystone, Player player, boolean isDiscovered) {
        ItemStack item;

        if (isDiscovered) {
            // Discovered waystone - use normal icon
            if (plugin.getItemsAdderIntegration().isItemsAdderReady()) {
                CustomStack customStack = plugin.getItemsAdderIntegration().getWaystoneItem(waystone.getIconItem());
                if (customStack != null) {
                    item = customStack.getItemStack().clone();
                } else {
                    CustomStack defaultStack = plugin.getItemsAdderIntegration().getWaystoneItem(
                        plugin.getItemsAdderIntegration().getDefaultIconNamespace()
                    );
                    if (defaultStack != null) {
                        item = defaultStack.getItemStack().clone();
                    } else {
                        item = new ItemStack(Material.LODESTONE);
                    }
                }
            } else {
                item = new ItemStack(Material.LODESTONE);
            }
        } else {
            // Undiscovered waystone - use different material
            item = new ItemStack(Material.GRAY_DYE);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (isDiscovered) {
                Component displayName = Component.text(waystone.getDisplayName())
                        .color(NamedTextColor.AQUA);
                meta.displayName(displayName);

                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Localisation: " + formatLocation(waystone))
                        .color(NamedTextColor.GRAY));

                double distance = calculateDistance(player, waystone);
                if (distance >= 0) {
                    lore.add(Component.text("Distance: " + String.format("%.1f", distance) + " blocs")
                            .color(NamedTextColor.YELLOW));
                } else {
                    lore.add(Component.text("Dimension différente")
                            .color(NamedTextColor.DARK_PURPLE));
                }

                lore.add(Component.empty());
                lore.add(Component.text("§aCliquez pour vous téléporter"));

                meta.lore(lore);
            } else {
                // Undiscovered waystone
                Component displayName = Component.text("Waystone inconnue")
                        .color(NamedTextColor.GRAY);
                meta.displayName(displayName);

                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Cette waystone n'a pas encore été découverte")
                        .color(NamedTextColor.GRAY));

                if (plugin.getVaultIntegration().isVaultEnabled()) {
                    double cost = plugin.getConfig().getDouble("admin-waystones.discovery-cost", 1000.0);
                    lore.add(Component.empty());
                    lore.add(Component.text("Coût d'achat: " + plugin.getVaultIntegration().formatMoney(cost))
                            .color(NamedTextColor.GOLD));
                    lore.add(Component.text("§eCliquez pour acheter la découverte"));
                } else {
                    lore.add(Component.text("§7Explorez pour la découvrir"));
                }

                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createFillerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatLocation(AdminWaystone waystone) {
        return String.format("%s: %d, %d, %d",
                waystone.getLocation().getWorld().getName(),
                waystone.getLocation().getBlockX(),
                waystone.getLocation().getBlockY(),
                waystone.getLocation().getBlockZ());
    }

    private double calculateDistance(Player player, AdminWaystone waystone) {
        if (!player.getWorld().equals(waystone.getLocation().getWorld())) {
            return -1;
        }
        return player.getLocation().distance(waystone.getLocation());
    }

    public AdminWaystone getWaystoneAtSlot(Player player, int slot) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);

        Collection<AdminWaystone> discoveredWaystones = plugin.getWaystoneManager().getDiscoveredWaystones(player);
        Collection<AdminWaystone> allWaystones = plugin.getWaystoneManager().getAllWaystones();

        List<AdminWaystone> availableWaystones = new ArrayList<>(discoveredWaystones);
        for (AdminWaystone waystone : allWaystones) {
            if (!plugin.getWaystoneManager().hasDiscovered(player, waystone.getId())) {
                availableWaystones.add(waystone);
            }
        }

        int actualIndex = currentPage * ITEMS_PER_PAGE + slot;
        if (actualIndex >= 0 && actualIndex < availableWaystones.size()) {
            return availableWaystones.get(actualIndex);
        }
        return null;
    }
}