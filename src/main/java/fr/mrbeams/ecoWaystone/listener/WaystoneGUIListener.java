package fr.mrbeams.ecoWaystone.listener;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.gui.GuiItem;
import fr.mrbeams.ecoWaystone.model.DiscoveryZone;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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

        String titleText = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!titleText.startsWith("Zones (")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int slot = event.getSlot();

        // Handle navigation
        if (slot == 45 && clickedItem.getType() == Material.ARROW) {
            plugin.getWaystoneGUI().previousPage(player);
            return;
        }

        if (slot == 53 && clickedItem.getType() == Material.ARROW) {
            plugin.getWaystoneGUI().nextPage(player);
            return;
        }

        if (slot == 49 && clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Handle item interaction (waystone, zone, or spawn)
        if (slot < 45) {
            GuiItem guiItem = plugin.getWaystoneGUI().getGuiItemAtSlot(player, slot);
            if (guiItem == null) {
                return;
            }

            if (guiItem.getType() == GuiItem.ItemType.ZONE) {
                if (guiItem.isDiscovered()) {
                    handleZoneClick(player, guiItem.getZone());
                } else {
                    Component message = Component.text("Vous devez d'abord découvrir cette zone pour vous y téléporter")
                            .color(NamedTextColor.RED);
                    player.sendMessage(message);
                }
            }
        }
    }


    private void handleZoneClick(Player player, DiscoveryZone zone) {
        if (!zone.hasSpawn()) {
            Component message = Component.text("Cette zone n'a pas de spawn défini")
                    .color(NamedTextColor.RED);
            player.sendMessage(message);
            return;
        }

        // Fermer l'inventaire
        player.closeInventory();

        // Téléporter avec les effets personnalisés
        plugin.getTeleportEffectManager().teleportPlayerWithEffect(player, zone.getSpawnLocation());
    }

}