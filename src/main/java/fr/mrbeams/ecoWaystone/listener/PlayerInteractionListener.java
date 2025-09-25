package fr.mrbeams.ecoWaystone.listener;

import fr.mrbeams.ecoWaystone.model.WarpKey;
import fr.mrbeams.ecoWaystone.model.Waystone;
import fr.mrbeams.ecoWaystone.service.ConfigManager;
import fr.mrbeams.ecoWaystone.service.ItemsAdderIntegration;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import fr.mrbeams.ecoWaystone.service.TeleportationService;
import fr.mrbeams.ecoWaystone.service.WaystoneManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class PlayerInteractionListener implements Listener {
    private final Plugin plugin;
    private final WaystoneManager waystoneManager;
    private final TeleportationService teleportationService;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final NamespacedKey waystoneIdKey;
    private final NamespacedKey warpKeyKey;

    public PlayerInteractionListener(Plugin plugin, WaystoneManager waystoneManager,
                                   TeleportationService teleportationService,
                                   ConfigManager configManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.waystoneManager = waystoneManager;
        this.teleportationService = teleportationService;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.waystoneIdKey = new NamespacedKey(plugin, "waystone_id");
        this.warpKeyKey = new NamespacedKey(plugin, "warp_key");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockInteraction(event, player, item);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            handleAirInteraction(event, player, item);
        }
    }

    private void handleBlockInteraction(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Material blockType = event.getClickedBlock().getType();
        Location blockLocation = event.getClickedBlock().getLocation();

        // Check if placing a custom waystone
        if (ItemsAdderIntegration.isCustomWaystone(item)) {
            handleWaystoneItemPlacement(event, player, item, blockLocation);
        } else if (blockType == Material.LODESTONE) {
            handleLodestoneInteraction(event, player, item, blockLocation);
        } else if (blockType == Material.NAME_TAG && item.getType() == Material.NAME_TAG) {
            handleNameTagInteraction(event, player, item, blockLocation);
        }
    }

    private void handleWaystoneItemPlacement(PlayerInteractEvent event, Player player, ItemStack item, Location location) {
        if (!player.hasPermission("waystones.place")) {
            player.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
            return;
        }

        // Get the location where the waystone will be placed
        Location placeLocation = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();

        if (waystoneManager.hasWaystoneAt(placeLocation)) {
            player.sendMessage(messageManager.getErrorComponent("waystone.already-exists"));
            return;
        }

        // Create the waystone
        Waystone waystone = waystoneManager.createWaystone(placeLocation, player.getUniqueId());
        if (waystone != null) {
            player.sendMessage(messageManager.getSuccessComponent("waystone.created"));

            // Remove the item from player's hand
            item.setAmount(item.getAmount() - 1);
            event.setCancelled(true);
        }
    }

    private void handleLodestoneInteraction(PlayerInteractEvent event, Player player, ItemStack item, Location location) {
        if (!player.hasPermission("waystones.link")) {
            player.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
            return;
        }

        Material itemType = item.getType();
        boolean isCompass = itemType == Material.COMPASS;
        boolean isCustomKey = ItemsAdderIntegration.isCustomWarpKey(item);

        if (!isCompass && !isCustomKey) {
            return;
        }

        WarpKey warpKey = new WarpKey(item, waystoneIdKey, warpKeyKey);

        if (configManager.isEnableKeyItems() && !isCustomKey && !warpKey.isWarpKey()) {
            return;
        }

        if (warpKey.isLinked() && !configManager.isRelinkableKeys()) {
            player.sendMessage(messageManager.getWarningComponent("waystone.already-linked"));
            return;
        }

        Waystone waystone = waystoneManager.getWaystoneAt(location);
        if (waystone == null) {
            waystone = waystoneManager.createWaystone(location, player.getUniqueId());
            if (waystone == null) {
                player.sendMessage(messageManager.getErrorComponent("errors.teleport-failed"));
                return;
            }
            player.sendMessage(messageManager.getSuccessComponent("waystone.created"));
        }

        if (waystoneManager.isWaystoneBlocked(location)) {
            player.sendMessage(messageManager.getErrorComponent("waystone.blocked"));
            return;
        }

        warpKey.linkToWaystone(waystone.getId());
        if (isCustomKey || configManager.isEnableKeyItems()) {
            WarpKey.markAsWarpKey(item, warpKeyKey);
        }

        player.sendMessage(messageManager.getSuccessComponent("waystone.linked"));
        event.setCancelled(true);
    }

    private void handleNameTagInteraction(PlayerInteractEvent event, Player player, ItemStack item, Location location) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        Waystone waystone = waystoneManager.getWaystoneAt(location);
        if (waystone == null) {
            return;
        }

        String name = item.getItemMeta().getDisplayName();
        waystoneManager.setWaystoneName(waystone.getId(), name);

        player.sendMessage(messageManager.getSuccessComponent("waystone.named", name));

        item.setAmount(item.getAmount() - 1);
        event.setCancelled(true);
    }

    private void handleAirInteraction(PlayerInteractEvent event, Player player, ItemStack item) {
        Material itemType = item.getType();
        boolean isCompass = itemType == Material.COMPASS;
        boolean isCustomKey = ItemsAdderIntegration.isCustomWarpKey(item);

        if (!isCompass && !isCustomKey) {
            return;
        }

        WarpKey warpKey = new WarpKey(item, waystoneIdKey, warpKeyKey);

        if (configManager.isEnableKeyItems() && !isCustomKey && !warpKey.isWarpKey()) {
            return;
        }

        if (!warpKey.isLinked() && !isCustomKey) {
            player.sendMessage(messageManager.getWarningComponent("waystone.not-linked"));
            return;
        }

        // For custom keys, check for linked waystone in NBT or use a different method
        UUID waystoneId = warpKey.getLinkedWaystoneId();
        if (waystoneId == null && isCustomKey) {
            // Custom keys might store waystone data differently
            player.sendMessage(messageManager.getWarningComponent("waystone.not-linked"));
            return;
        }

        if (waystoneId == null) {
            player.sendMessage(messageManager.getErrorComponent("errors.no-waystone"));
            return;
        }

        if (teleportationService.hasActiveWarp(player)) {
            player.sendMessage(messageManager.getWarningComponent("teleport.already-warping"));
            return;
        }

        boolean success = teleportationService.initiateWarp(player, waystoneId);
        if (success) {
            event.setCancelled(true);
        }
    }
}