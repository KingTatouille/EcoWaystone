package fr.mrbeams.ecoWaystone.listener;

import fr.mrbeams.ecoWaystone.service.WaystoneManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEventListener implements Listener {
    private final WaystoneManager waystoneManager;

    public BlockEventListener(WaystoneManager waystoneManager) {
        this.waystoneManager = waystoneManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.LODESTONE) {
            if (waystoneManager.hasWaystoneAt(event.getBlock().getLocation())) {
                waystoneManager.removeWaystone(event.getBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.OBSIDIAN) {
            if (waystoneManager.hasWaystoneAt(event.getBlock().getLocation().add(0, 1, 0))) {
                // Obsidian placed under waystone - this will block it
            }
        } else if (event.getBlock().getType() == Material.COMMAND_BLOCK) {
            if (waystoneManager.hasWaystoneAt(event.getBlock().getLocation().add(0, 1, 0))) {
                // Command block placed under waystone - this will give unlimited range
            }
        }
    }
}