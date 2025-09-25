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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WaystoneGUI {

    private final EcoWaystone plugin;
    private final MessageManager messageManager;

    public WaystoneGUI(EcoWaystone plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    public void openWaystoneMenu(Player player) {
        Collection<AdminWaystone> discoveredWaystones = plugin.getWaystoneManager().getDiscoveredWaystones(player);

        int size = Math.max(9, ((discoveredWaystones.size() + 8) / 9) * 9);
        if (size > 54) size = 54;

        Component title = messageManager.getMessage("gui.waystone.title", player);
        Inventory gui = Bukkit.createInventory(null, size, title);

        int slot = 0;
        for (AdminWaystone waystone : discoveredWaystones) {
            if (slot >= size - 9) break;

            ItemStack item = createWaystoneItem(waystone, player);
            gui.setItem(slot, item);
            slot++;
        }

        for (int i = size - 9; i < size; i++) {
            gui.setItem(i, createFillerItem());
        }

        ItemStack closeItem = createCloseItem();
        gui.setItem(size - 1, closeItem);

        player.openInventory(gui);
    }

    private ItemStack createWaystoneItem(AdminWaystone waystone, Player player) {
        ItemStack item;

        CustomStack customStack = CustomStack.getInstance(waystone.getIconItem());
        if (customStack != null) {
            item = customStack.getItemStack().clone();
        } else {
            item = new ItemStack(Material.LODESTONE);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component displayName = Component.text(waystone.getDisplayName())
                    .color(NamedTextColor.AQUA);
            meta.displayName(displayName);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Location: " + formatLocation(waystone))
                    .color(NamedTextColor.GRAY));

            double distance = calculateDistance(player, waystone);
            lore.add(Component.text("Distance: " + String.format("%.1f", distance) + " blocks")
                    .color(NamedTextColor.YELLOW));

            lore.add(Component.empty());
            lore.add(messageManager.getMessage("gui.waystone.click_to_teleport", player));

            meta.lore(lore);
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

    private ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component displayName = Component.text("Close")
                    .color(NamedTextColor.RED);
            meta.displayName(displayName);
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
}