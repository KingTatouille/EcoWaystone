package fr.mrbeams.ecoWaystone.listener;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.AdminWaystone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.Collection;

public class PlayerChunkListener implements Listener {

    private final EcoWaystone plugin;

    public PlayerChunkListener(EcoWaystone plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player moved to a different chunk
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }

        Player player = event.getPlayer();
        Chunk newChunk = event.getTo().getChunk();

        // Check if this chunk contains any waystones
        Collection<AdminWaystone> allWaystones = plugin.getWaystoneManager().getAllWaystones();

        for (AdminWaystone waystone : allWaystones) {
            if (isInSameChunk(waystone.getLocation(), newChunk)) {
                // Check if player already discovered this waystone
                if (!plugin.getWaystoneManager().hasDiscovered(player, waystone.getId())) {
                    // Discover the waystone
                    boolean discovered = plugin.getWaystoneManager().discoverWaystone(player, waystone.getId());

                    if (discovered) {
                        showDiscoveryTitle(player, waystone);

                        // Optional: Send chat message too
                        Component chatMessage = Component.text("You discovered: ")
                                .color(NamedTextColor.GOLD)
                                .append(Component.text(waystone.getDisplayName())
                                        .color(NamedTextColor.YELLOW));
                        player.sendMessage(chatMessage);

                        // Play discovery sound if configured
                        String soundName = plugin.getConfig().getString("admin-waystones.discovery-sound", "ENTITY_PLAYER_LEVELUP");
                        if (!soundName.isEmpty()) {
                            try {
                                player.playSound(player.getLocation(),
                                    org.bukkit.Sound.valueOf(soundName), 1.0f, 1.0f);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid discovery sound: " + soundName);
                            }
                        }
                    }
                }
                break; // Only discover one waystone per chunk entry
            }
        }
    }

    private boolean isInSameChunk(Location waystoneLocation, Chunk playerChunk) {
        if (!waystoneLocation.getWorld().equals(playerChunk.getWorld())) {
            return false;
        }

        int waystoneChunkX = waystoneLocation.getBlockX() >> 4;
        int waystoneChunkZ = waystoneLocation.getBlockZ() >> 4;

        return waystoneChunkX == playerChunk.getX() && waystoneChunkZ == playerChunk.getZ();
    }

    private void showDiscoveryTitle(Player player, AdminWaystone waystone) {
        Component mainTitle = Component.text("Waystone Discovered!")
                .color(NamedTextColor.GOLD);

        Component subtitle = Component.text(waystone.getDisplayName())
                .color(NamedTextColor.YELLOW);

        Title title = Title.title(
            mainTitle,
            subtitle,
            Title.Times.times(
                Duration.ofMillis(500),  // fade in
                Duration.ofMillis(3000), // stay
                Duration.ofMillis(1000)  // fade out
            )
        );

        player.showTitle(title);
    }
}