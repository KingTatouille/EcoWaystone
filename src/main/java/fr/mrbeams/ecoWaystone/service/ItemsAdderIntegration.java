package fr.mrbeams.ecoWaystone.service;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import dev.lone.itemsadder.api.ItemsAdder;
import fr.mrbeams.ecoWaystone.EcoWaystone;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ItemsAdderIntegration implements Listener {

    private final EcoWaystone plugin;
    private boolean itemsAdderReady = false;

    public ItemsAdderIntegration(EcoWaystone plugin) {
        this.plugin = plugin;
        checkItemsAdderStatus();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemsAdderLoadData(ItemsAdderLoadDataEvent event) {
        plugin.getLogger().info("ItemsAdder data loaded/reloaded");
        itemsAdderReady = true;
    }

    private void checkItemsAdderStatus() {
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("ItemsAdder")) {
                itemsAdderReady = ItemsAdder.areItemsLoaded();
                if (itemsAdderReady) {
                    plugin.getLogger().info("ItemsAdder is ready and items are loaded");
                } else {
                    plugin.getLogger().info("ItemsAdder found but items not loaded yet, waiting for ItemsAdderLoadDataEvent...");
                }
            } else {
                itemsAdderReady = false;
                plugin.getLogger().info("ItemsAdder not found or not enabled yet, waiting for plugin to load...");
            }
        } catch (Exception e) {
            itemsAdderReady = false;
            plugin.getLogger().warning("Error checking ItemsAdder status: " + e.getMessage());
        }
    }

    public boolean isItemsAdderReady() {
        return itemsAdderReady && ItemsAdder.areItemsLoaded();
    }

    public boolean isWaystoneItem(String namespace) {
        if (!isItemsAdderReady()) {
            return false;
        }
        return CustomStack.isInRegistry(namespace);
    }

    public boolean isWaystoneBlock(String namespace) {
        if (!isItemsAdderReady()) {
            return false;
        }
        return CustomBlock.isInRegistry(namespace);
    }

    public CustomStack getWaystoneItem(String namespace) {
        if (!isItemsAdderReady()) {
            plugin.getLogger().warning("Attempted to get waystone item before ItemsAdder is ready: " + namespace);
            return null;
        }

        CustomStack stack = CustomStack.getInstance(namespace);
        if (stack == null) {
            // Log au niveau debug seulement pour éviter le spam dans les logs
            plugin.getLogger().fine("Waystone item not found in ItemsAdder registry: " + namespace);
        }
        return stack;
    }

    public CustomBlock getWaystoneBlock(String namespace) {
        if (!isItemsAdderReady()) {
            plugin.getLogger().warning("Attempted to get waystone block before ItemsAdder is ready: " + namespace);
            return null;
        }

        CustomBlock block = CustomBlock.getInstance(namespace);
        if (block == null) {
            // Log au niveau debug seulement pour éviter le spam dans les logs
            plugin.getLogger().fine("Waystone block not found in ItemsAdder registry: " + namespace);
        }
        return block;
    }

    public String getWaystoneNamespace() {
        return plugin.getConfig().getString("admin-waystones.waystone-item", "ecowaystone:waystone");
    }

    public String getDefaultIconNamespace() {
        return plugin.getConfig().getString("admin-waystones.default-icon", "ecowaystone:waystone");
    }
}