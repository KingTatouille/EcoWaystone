package fr.mrbeams.ecoWaystone.command;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.DiscoveryZone;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WaystoneCommand implements CommandExecutor {

    private final EcoWaystone plugin;
    private final MessageManager messageManager;

    public WaystoneCommand(EcoWaystone plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Si il y a des arguments, vérifier les sous-commandes
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length >= 3 && sender instanceof Player) {
                        handleZoneCreation((Player) sender, args);
                        return true;
                    }
                    break;
                case "setspawn":
                    if (args.length >= 2 && sender instanceof Player) {
                        handleSetSpawn((Player) sender, args);
                        return true;
                    }
                    break;
                case "removespawn":
                    if (args.length >= 2 && sender instanceof Player) {
                        handleRemoveSpawn((Player) sender, args);
                        return true;
                    }
                    break;
                case "discover":
                    if (args.length >= 3) {
                        handleDiscoverZone(sender, args);
                        return true;
                    }
                    break;
                case "undiscover":
                    if (args.length >= 3) {
                        handleUndiscoverZone(sender, args);
                        return true;
                    }
                    break;
                default:
                    // Si l'argument n'est pas une sous-commande, essayer de l'interpréter comme un nom de joueur
                    Player targetPlayer = Bukkit.getPlayer(args[0]);
                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        handleOpenGuiForPlayer(sender, targetPlayer);
                        return true;
                    }
                    break;
            }
        }

        // Si pas d'arguments ou si sender est un joueur, ouvrir son GUI
        if (sender instanceof Player player) {
            if (!player.hasPermission("waystones.use")) {
                Component message = messageManager.getMessage("error.no_permission", player);
                player.sendMessage(message);
                return true;
            }

            // Ouvrir le GUI des waystones
            plugin.getWaystoneGUI().openWaystoneMenu(player);
        } else {
            sender.sendMessage(Component.text("Usage: /waystone [player] ou /waystone <sous-commande>").color(NamedTextColor.RED));
        }

        return true;
    }

    private void handleZoneCreation(Player player, String[] args) {
        if (!player.hasPermission("waystones.admin.create")) {
            player.sendMessage(Component.text("Vous n'avez pas la permission de créer des zones").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /waystone create <nom_region_worldguard> <nom_zone>").color(NamedTextColor.RED));
            return;
        }

        String regionName = args[1];

        // Construire le nom de la zone à partir des arguments restants
        StringBuilder displayNameBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) displayNameBuilder.append(" ");
            displayNameBuilder.append(args[i]);
        }
        String displayName = displayNameBuilder.toString();

        // Créer la zone
        plugin.getZoneManager().createZone(regionName, displayName, player.getWorld());

        player.sendMessage(Component.text("Zone de découverte '").color(NamedTextColor.GREEN)
                .append(Component.text(displayName).color(NamedTextColor.YELLOW))
                .append(Component.text("' créée pour la région '").color(NamedTextColor.GREEN))
                .append(Component.text(regionName).color(NamedTextColor.AQUA))
                .append(Component.text("'").color(NamedTextColor.GREEN)));
    }

    private void handleSetSpawn(Player player, String[] args) {
        if (!player.hasPermission("waystones.admin.create")) {
            player.sendMessage(Component.text("Vous n'avez pas la permission de gérer les spawns").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /waystone setspawn <zone_id>").color(NamedTextColor.RED));
            return;
        }

        String zoneId = args[1];

        // Vérifier que la zone existe
        DiscoveryZone zone = plugin.getZoneManager().getZone(zoneId);
        if (zone == null) {
            player.sendMessage(Component.text("Zone non trouvée: " + zoneId).color(NamedTextColor.RED));
            return;
        }

        // Utiliser la position actuelle du joueur
        boolean success = plugin.getZoneManager().setZoneSpawn(zoneId, player.getLocation().clone());

        if (success) {
            player.sendMessage(Component.text("Spawn défini pour la zone '").color(NamedTextColor.GREEN)
                    .append(Component.text(zone.getDisplayName()).color(NamedTextColor.AQUA))
                    .append(Component.text("' à votre position actuelle").color(NamedTextColor.GREEN)));
        } else {
            player.sendMessage(Component.text("Erreur lors de la définition du spawn").color(NamedTextColor.RED));
        }
    }

    private void handleRemoveSpawn(Player player, String[] args) {
        if (!player.hasPermission("waystones.admin.create")) {
            player.sendMessage(Component.text("Vous n'avez pas la permission de gérer les spawns").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /waystone removespawn <zone_id>").color(NamedTextColor.RED));
            return;
        }

        String zoneId = args[1];

        // Vérifier que la zone existe
        DiscoveryZone zone = plugin.getZoneManager().getZone(zoneId);
        if (zone == null) {
            player.sendMessage(Component.text("Zone non trouvée: " + zoneId).color(NamedTextColor.RED));
            return;
        }

        if (!zone.hasSpawn()) {
            player.sendMessage(Component.text("Cette zone n'a pas de spawn défini").color(NamedTextColor.YELLOW));
            return;
        }

        boolean success = plugin.getZoneManager().removeZoneSpawn(zoneId);

        if (success) {
            player.sendMessage(Component.text("Spawn supprimé de la zone '").color(NamedTextColor.GREEN)
                    .append(Component.text(zone.getDisplayName()).color(NamedTextColor.AQUA))
                    .append(Component.text("'").color(NamedTextColor.GREEN)));
        } else {
            player.sendMessage(Component.text("Erreur lors de la suppression du spawn").color(NamedTextColor.RED));
        }
    }


    private String formatLocation(org.bukkit.Location location) {
        return String.format("%s: %d, %d, %d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    private void handleDiscoverZone(CommandSender sender, String[] args) {
        if (!sender.hasPermission("waystones.admin.discover")) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission de forcer la découverte de zones").color(NamedTextColor.RED));
            return;
        }

        String playerName = args[1];
        String zoneName = args[2];
        boolean all = args.length > 3 && args[3].equalsIgnoreCase("all");

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(Component.text("Joueur non trouvé ou hors ligne: " + playerName).color(NamedTextColor.RED));
            return;
        }

        if (all) {
            // Découvrir toutes les zones
            int count = 0;
            for (DiscoveryZone zone : plugin.getZoneManager().getAllZones().values()) {
                if (plugin.getZoneManager().discoverZone(targetPlayer, zone.getId())) {
                    count++;
                }
            }

            sender.sendMessage(Component.text("Forcé la découverte de " + count + " zone(s) pour ").color(NamedTextColor.GREEN)
                    .append(Component.text(targetPlayer.getName()).color(NamedTextColor.YELLOW)));
        } else {
            // Découvrir une zone spécifique
            DiscoveryZone zone = plugin.getZoneManager().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage(Component.text("Zone non trouvée: " + zoneName).color(NamedTextColor.RED));
                return;
            }

            boolean discovered = plugin.getZoneManager().discoverZone(targetPlayer, zone.getId());
            if (discovered) {
                sender.sendMessage(Component.text("Forcé la découverte de la zone '").color(NamedTextColor.GREEN)
                        .append(Component.text(zone.getDisplayName()).color(NamedTextColor.AQUA))
                        .append(Component.text("' pour ").color(NamedTextColor.GREEN))
                        .append(Component.text(targetPlayer.getName()).color(NamedTextColor.YELLOW)));
            } else {
                sender.sendMessage(Component.text("Zone déjà découverte par ce joueur").color(NamedTextColor.YELLOW));
            }
        }
    }

    private void handleUndiscoverZone(CommandSender sender, String[] args) {
        if (!sender.hasPermission("waystones.admin.discover")) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission de retirer la découverte de zones").color(NamedTextColor.RED));
            return;
        }

        String playerName = args[1];
        String zoneName = args[2];
        boolean all = args.length > 3 && args[3].equalsIgnoreCase("all");

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(Component.text("Joueur non trouvé ou hors ligne: " + playerName).color(NamedTextColor.RED));
            return;
        }

        if (all) {
            // Annuler la découverte de toutes les zones
            int count = 0;
            for (DiscoveryZone zone : plugin.getZoneManager().getAllZones().values()) {
                if (plugin.getZoneManager().undiscoverZone(targetPlayer, zone.getId())) {
                    count++;
                }
            }

            sender.sendMessage(Component.text("Annulé la découverte de " + count + " zone(s) pour ").color(NamedTextColor.GREEN)
                    .append(Component.text(targetPlayer.getName()).color(NamedTextColor.YELLOW)));
        } else {
            // Annuler la découverte d'une zone spécifique
            DiscoveryZone zone = plugin.getZoneManager().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage(Component.text("Zone non trouvée: " + zoneName).color(NamedTextColor.RED));
                return;
            }

            boolean undiscovered = plugin.getZoneManager().undiscoverZone(targetPlayer, zone.getId());
            if (undiscovered) {
                sender.sendMessage(Component.text("Annulé la découverte de la zone '").color(NamedTextColor.GREEN)
                        .append(Component.text(zone.getDisplayName()).color(NamedTextColor.AQUA))
                        .append(Component.text("' pour ").color(NamedTextColor.GREEN))
                        .append(Component.text(targetPlayer.getName()).color(NamedTextColor.YELLOW)));
            } else {
                sender.sendMessage(Component.text("Cette zone n'est pas découverte par ce joueur").color(NamedTextColor.YELLOW));
            }
        }
    }

    private void handleOpenGuiForPlayer(CommandSender sender, Player targetPlayer) {
        if (!sender.hasPermission("waystones.admin.opengui")) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission d'ouvrir le GUI pour d'autres joueurs").color(NamedTextColor.RED));
            return;
        }

        if (!targetPlayer.hasPermission("waystones.use")) {
            sender.sendMessage(Component.text("Le joueur ").color(NamedTextColor.RED)
                    .append(Component.text(targetPlayer.getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" n'a pas la permission d'utiliser les waystones").color(NamedTextColor.RED)));
            return;
        }

        plugin.getWaystoneGUI().openWaystoneMenu(targetPlayer);

        sender.sendMessage(Component.text("GUI des waystones ouvert pour ").color(NamedTextColor.GREEN)
                .append(Component.text(targetPlayer.getName()).color(NamedTextColor.YELLOW)));

        targetPlayer.sendMessage(Component.text("Un administrateur a ouvert votre GUI des waystones").color(NamedTextColor.GRAY));
    }
}