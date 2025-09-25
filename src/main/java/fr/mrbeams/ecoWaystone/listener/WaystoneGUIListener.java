package fr.mrbeams.ecoWaystone.listener;

import dev.lone.itemsadder.api.CustomStack;
import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.AdminWaystone;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public class WaystoneGUIListener implements Listener {

    private final EcoWaystone plugin;
    private final MessageManager messageManager;

    public WaystoneGUIListener(EcoWaystone plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = messageManager.getMessage("gui.waystone.title", player);
        if (!event.getView().title().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        AdminWaystone targetWaystone = findWaystoneByItem(player, clickedItem, event.getSlot());
        if (targetWaystone == null) {
            Component message = messageManager.getMessage("error.waystone_not_found", player);
            player.sendMessage(message);
            return;
        }

        if (!plugin.getWaystoneManager().hasDiscovered(player, targetWaystone.getId())) {
            Component message = messageManager.getMessage("error.waystone_not_discovered", player);
            player.sendMessage(message);
            return;
        }

        player.closeInventory();

        Component teleportMessage = messageManager.getMessage("waystone.teleporting", player)
                .replaceText(builder -> builder.matchLiteral("%name%")
                        .replacement(targetWaystone.getDisplayName()));
        player.sendMessage(teleportMessage);

        player.teleport(targetWaystone.getLocation().add(0.5, 1, 0.5));

        Component successMessage = messageManager.getMessage("waystone.teleport_success", player)
                .replaceText(builder -> builder.matchLiteral("%name%")
                        .replacement(targetWaystone.getDisplayName()));
        player.sendMessage(successMessage);
    }

    private AdminWaystone findWaystoneByItem(Player player, ItemStack item, int slot) {
        Collection<AdminWaystone> discoveredWaystones = plugin.getWaystoneManager().getDiscoveredWaystones(player);
        List<AdminWaystone> waystoneList = discoveredWaystones.stream().toList();

        if (slot >= waystoneList.size()) {
            return null;
        }

        return waystoneList.get(slot);
    }
}