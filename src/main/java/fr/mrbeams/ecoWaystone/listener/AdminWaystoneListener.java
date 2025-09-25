package fr.mrbeams.ecoWaystone.listener;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.AdminWaystone;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AdminWaystoneListener implements Listener {

    private final EcoWaystone plugin;
    private final MessageManager messageManager;

    public AdminWaystoneListener(EcoWaystone plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        CustomStack customStack = CustomStack.byItemStack(item);
        if (customStack == null || !customStack.getNamespacedID().equals("ecowaystone:waystone")) {
            return;
        }

        if (!player.hasPermission("waystones.admin.place")) {
            event.setCancelled(true);
            Component message = messageManager.getMessage("error.no_permission_place", player);
            player.sendMessage(message);
            return;
        }

        Location location = event.getBlock().getLocation();
        String waystoneId = generateWaystoneId(location);

        plugin.getWaystoneManager().createWaystone(
                waystoneId,
                "waystone_" + waystoneId,
                "Waystone at " + formatLocation(location),
                location
        );

        Component message = messageManager.getMessage("waystone.placed", player)
                .replaceText(builder -> builder.matchLiteral("%location%")
                        .replacement(formatLocation(location)));
        player.sendMessage(message);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock == null || !customBlock.getNamespacedID().equals("ecowaystone:waystone")) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("waystones.admin.break")) {
            event.setCancelled(true);
            Component message = messageManager.getMessage("error.no_permission_break", player);
            player.sendMessage(message);
            return;
        }

        AdminWaystone waystone = plugin.getWaystoneManager().getWaystoneAt(block.getLocation());
        if (waystone != null) {
            plugin.getWaystoneManager().deleteWaystone(waystone.getId());
            Component message = messageManager.getMessage("waystone.removed", player)
                    .replaceText(builder -> builder.matchLiteral("%name%")
                            .replacement(waystone.getDisplayName()));
            player.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);

        if (customBlock == null || !customBlock.getNamespacedID().equals("ecowaystone:waystone")) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        AdminWaystone waystone = plugin.getWaystoneManager().getWaystoneAt(block.getLocation());
        if (waystone == null) {
            Component message = messageManager.getMessage("error.waystone_not_found", player);
            player.sendMessage(message);
            return;
        }

        boolean wasNewDiscovery = plugin.getWaystoneManager().discoverWaystone(player, waystone.getId());

        if (wasNewDiscovery) {
            Component discoveryMessage = messageManager.getMessage("waystone.discovered", player)
                    .replaceText(builder -> builder.matchLiteral("%name%")
                            .replacement(waystone.getDisplayName()));
            player.sendMessage(discoveryMessage);

            int totalDiscovered = plugin.getWaystoneManager().getDiscoveryCount(player);
            Component countMessage = messageManager.getMessage("waystone.discovery_count", player)
                    .replaceText(builder -> builder.matchLiteral("%count%")
                            .replacement(String.valueOf(totalDiscovered)));
            player.sendMessage(countMessage);
        }

        plugin.getWaystoneGUI().openWaystoneMenu(player);
    }

    private String generateWaystoneId(Location location) {
        return String.format("%s_%d_%d_%d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    private String formatLocation(Location location) {
        return String.format("%s: %d, %d, %d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }
}