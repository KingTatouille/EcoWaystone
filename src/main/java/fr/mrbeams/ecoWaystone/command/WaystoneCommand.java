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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WaystoneCommand implements CommandExecutor, TabCompleter {

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
            if (args[0].equalsIgnoreCase("admin")) {
                // Commandes admin
                return handleAdminCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("effect")) {
                // Commandes d'effets pour joueurs
                return handlePlayerEffectCommand(sender, args);
            } else {
                // Essayer d'interpréter comme un nom de joueur
                Player targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    handleOpenGuiForPlayer(sender, targetPlayer);
                    return true;
                }
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
            player.sendMessage(Component.text("Usage: /waystone create <nom_region_worldguard> <nom_zone> [description] [iditemsadder]").color(NamedTextColor.RED));
            return;
        }

        String regionName = args[1];
        String zoneName = args[2];
        String description = "";
        String customItem = null;

        // Si des arguments supplémentaires sont fournis
        if (args.length >= 4) {
            // Vérifier si le dernier argument ressemble à un ID ItemsAdder (contient ":")
            String lastArg = args[args.length - 1];
            if (lastArg.contains(":")) {
                customItem = lastArg;
                // Tout ce qui est entre args[2] et l'avant-dernier arg est la description
                if (args.length > 4) {
                    StringBuilder descBuilder = new StringBuilder();
                    for (int i = 3; i < args.length - 1; i++) {
                        if (i > 3) descBuilder.append(" ");
                        descBuilder.append(args[i]);
                    }
                    description = descBuilder.toString();
                }
            } else {
                // Tout à partir de args[3] est la description
                StringBuilder descBuilder = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    if (i > 3) descBuilder.append(" ");
                    descBuilder.append(args[i]);
                }
                description = descBuilder.toString();
            }
        }

        // Créer la zone
        plugin.getZoneManager().createZone(regionName, zoneName, description, player.getWorld(), customItem);

        Component message = Component.text("Zone de découverte '").color(NamedTextColor.GREEN)
                .append(Component.text(zoneName).color(NamedTextColor.YELLOW))
                .append(Component.text("' créée pour la région '").color(NamedTextColor.GREEN))
                .append(Component.text(regionName).color(NamedTextColor.AQUA))
                .append(Component.text("'").color(NamedTextColor.GREEN));

        if (!description.isEmpty()) {
            message = message.append(Component.text(" avec description: '").color(NamedTextColor.GREEN))
                    .append(Component.text(description).color(NamedTextColor.WHITE))
                    .append(Component.text("'").color(NamedTextColor.GREEN));
        }

        if (customItem != null && !customItem.isEmpty()) {
            message = message.append(Component.text(" avec l'item custom: ").color(NamedTextColor.GREEN))
                    .append(Component.text(customItem).color(NamedTextColor.GOLD));
        }

        player.sendMessage(message);
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
    }

    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /waystone admin <create|delete|setspawn|removespawn|discover|undiscover> [args...]").color(NamedTextColor.RED));
            return true;
        }

        // Créer un nouveau tableau d'arguments sans "admin"
        String[] adminArgs = new String[args.length - 1];
        System.arraycopy(args, 1, adminArgs, 0, args.length - 1);

        switch (adminArgs[0].toLowerCase()) {
            case "create":
                if (adminArgs.length >= 3 && sender instanceof Player) {
                    handleZoneCreation((Player) sender, adminArgs);
                    return true;
                }
                sender.sendMessage(Component.text("Usage: /waystone admin create <nom_region_worldguard> <nom_zone> [description] [iditemsadder]").color(NamedTextColor.RED));
                break;
            case "setspawn":
                if (adminArgs.length >= 2 && sender instanceof Player) {
                    handleSetSpawn((Player) sender, adminArgs);
                    return true;
                }
                sender.sendMessage(Component.text("Usage: /waystone admin setspawn <zone_id>").color(NamedTextColor.RED));
                break;
            case "removespawn":
                if (adminArgs.length >= 2 && sender instanceof Player) {
                    handleRemoveSpawn((Player) sender, adminArgs);
                    return true;
                }
                sender.sendMessage(Component.text("Usage: /waystone admin removespawn <zone_id>").color(NamedTextColor.RED));
                break;
            case "discover":
                if (adminArgs.length >= 3) {
                    handleDiscoverZone(sender, adminArgs);
                    return true;
                }
                sender.sendMessage(Component.text("Usage: /waystone admin discover <player> <zone> [all]").color(NamedTextColor.RED));
                break;
            case "undiscover":
                if (adminArgs.length >= 3) {
                    handleUndiscoverZone(sender, adminArgs);
                    return true;
                }
                sender.sendMessage(Component.text("Usage: /waystone admin undiscover <player> <zone> [all]").color(NamedTextColor.RED));
                break;
            case "delete":
                if (adminArgs.length >= 2 && sender instanceof Player) {
                    handleDeleteZone((Player) sender, adminArgs);
                    return true;
                }
                sender.sendMessage(Component.text("Usage: /waystone admin delete <zone_id> [confirm]").color(NamedTextColor.RED));
                break;
            case "description":
                if (adminArgs.length >= 3) {
                    handleSetDescription(sender, adminArgs);
                    return true;
                }
                sender.sendMessage(Component.text("Usage: /waystone admin description <zone_id> <description...>").color(NamedTextColor.RED));
                break;
            case "reload":
                handleReloadCommand(sender);
                return true;
            case "repair":
                handleRepairCommand(sender);
                return true;
            case "effect":
                if (adminArgs.length >= 2) {
                    handleEffectCommand(sender, adminArgs);
                    return true;
                }
                sender.sendMessage(Component.text("Usage: /waystone admin effect <list|set> [player] [effect]").color(NamedTextColor.RED));
                break;
            case "rename":
            case "list":
            case "info":
                sender.sendMessage(Component.text("Cette commande n'est plus disponible - le plugin est maintenant centré sur la découverte de zones").color(NamedTextColor.YELLOW));
                break;
            default:
                sender.sendMessage(Component.text("Commande admin inconnue: " + adminArgs[0]).color(NamedTextColor.RED));
                break;
        }
        return true;
    }


    private void handleDeleteZone(Player player, String[] args) {
        if (!player.hasPermission("waystones.admin.create")) {
            player.sendMessage(Component.text("Vous n'avez pas la permission de supprimer des zones").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /waystone delete <zone_id>").color(NamedTextColor.RED));
            return;
        }

        String zoneId = args[1];

        // Vérifier que la zone existe
        DiscoveryZone zone = plugin.getZoneManager().getZone(zoneId);
        if (zone == null) {
            player.sendMessage(Component.text("Zone non trouvée: " + zoneId).color(NamedTextColor.RED));
            return;
        }

        // Demander confirmation
        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            player.sendMessage(Component.text("⚠️ ATTENTION: Cette action supprimera définitivement la zone '").color(NamedTextColor.YELLOW)
                    .append(Component.text(zone.getDisplayName()).color(NamedTextColor.AQUA))
                    .append(Component.text("' et retirera sa découverte de tous les joueurs.").color(NamedTextColor.YELLOW)));
            player.sendMessage(Component.text("Pour confirmer, utilisez: /waystone delete " + zoneId + " confirm").color(NamedTextColor.GOLD));
            return;
        }

        // Supprimer la zone
        boolean success = plugin.getZoneManager().deleteZone(zoneId);

        if (success) {
            player.sendMessage(Component.text("Zone '").color(NamedTextColor.GREEN)
                    .append(Component.text(zone.getDisplayName()).color(NamedTextColor.AQUA))
                    .append(Component.text("' supprimée avec succès !").color(NamedTextColor.GREEN)));
        } else {
            player.sendMessage(Component.text("Erreur lors de la suppression de la zone").color(NamedTextColor.RED));
        }
    }

    private void handleSetDescription(CommandSender sender, String[] args) {
        if (!sender.hasPermission("waystones.admin.create")) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission de modifier les descriptions de zones").color(NamedTextColor.RED));
            return;
        }

        String zoneId = args[1];

        // Vérifier que la zone existe
        DiscoveryZone zone = plugin.getZoneManager().getZone(zoneId);
        if (zone == null) {
            sender.sendMessage(Component.text("Zone non trouvée: " + zoneId).color(NamedTextColor.RED));
            return;
        }

        // Construire la description à partir des arguments restants
        StringBuilder descBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) descBuilder.append(" ");
            descBuilder.append(args[i]);
        }
        String description = descBuilder.toString();

        // Mettre à jour la description
        boolean success = plugin.getZoneManager().setZoneDescription(zoneId, description);

        if (success) {
            sender.sendMessage(Component.text("Description mise à jour pour la zone '").color(NamedTextColor.GREEN)
                    .append(Component.text(zone.getDisplayName()).color(NamedTextColor.AQUA))
                    .append(Component.text("': '").color(NamedTextColor.GREEN))
                    .append(Component.text(description).color(NamedTextColor.WHITE))
                    .append(Component.text("'").color(NamedTextColor.GREEN)));
        } else {
            sender.sendMessage(Component.text("Erreur lors de la mise à jour de la description").color(NamedTextColor.RED));
        }
    }

    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("waystones.admin.reload")) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission de recharger les configurations").color(NamedTextColor.RED));
            return;
        }

        try {
            // Recharger la configuration principale
            plugin.reloadConfig();

            // Recharger les messages
            plugin.getMessageManager().reloadConfig();

            // Recharger les configurations GUI
            plugin.getGuiItemsManager().reloadConfig();

            // Recharger les effets de téléportation
            plugin.getTeleportEffectManager().reloadConfig();

            sender.sendMessage(Component.text("✓ Configurations rechargées avec succès !").color(NamedTextColor.GREEN));
            plugin.getLogger().info("Configurations rechargées par " + sender.getName());

        } catch (Exception e) {
            sender.sendMessage(Component.text("✗ Erreur lors du rechargement des configurations").color(NamedTextColor.RED));
            plugin.getLogger().severe("Erreur lors du rechargement des configurations: " + e.getMessage());
        }
    }

    private void handleRepairCommand(CommandSender sender) {
        if (!sender.hasPermission("waystones.admin.repair")) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission de réparer les fichiers").color(NamedTextColor.RED));
            return;
        }

        try {
            sender.sendMessage(Component.text("⚠️ Réparation des fichiers de joueurs en cours...").color(NamedTextColor.YELLOW));

            int repairedFiles = plugin.getZoneManager().repairPlayerFiles();

            if (repairedFiles > 0) {
                sender.sendMessage(Component.text("✓ " + repairedFiles + " fichier(s) de joueur réparé(s) avec succès !").color(NamedTextColor.GREEN));
                plugin.getLogger().info("Fichiers de joueurs réparés: " + repairedFiles + " par " + sender.getName());
            } else {
                sender.sendMessage(Component.text("✓ Aucun fichier à réparer trouvé").color(NamedTextColor.GREEN));
            }

        } catch (Exception e) {
            sender.sendMessage(Component.text("✗ Erreur lors de la réparation des fichiers").color(NamedTextColor.RED));
            plugin.getLogger().severe("Erreur lors de la réparation des fichiers: " + e.getMessage());
        }
    }

    private void handleEffectCommand(CommandSender sender, String[] args) {
        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                if (args.length >= 3) {
                    // Lister les effets d'un joueur spécifique
                    Player targetPlayer = Bukkit.getPlayer(args[2]);
                    if (targetPlayer == null || !targetPlayer.isOnline()) {
                        sender.sendMessage(Component.text("Joueur non trouvé ou hors ligne: " + args[2]).color(NamedTextColor.RED));
                        return;
                    }
                    handleListPlayerEffects(sender, targetPlayer);
                } else {
                    sender.sendMessage(Component.text("Usage: /waystone admin effect list <player>").color(NamedTextColor.RED));
                }
                break;
            case "set":
                if (args.length >= 4) {
                    // Forcer un effet pour un joueur
                    Player targetPlayer = Bukkit.getPlayer(args[2]);
                    if (targetPlayer == null || !targetPlayer.isOnline()) {
                        sender.sendMessage(Component.text("Joueur non trouvé ou hors ligne: " + args[2]).color(NamedTextColor.RED));
                        return;
                    }
                    String effectId = args[3];
                    handleSetPlayerEffect(sender, targetPlayer, effectId);
                } else {
                    sender.sendMessage(Component.text("Usage: /waystone admin effect set <player> <effect>").color(NamedTextColor.RED));
                }
                break;
            default:
                sender.sendMessage(Component.text("Sous-commande inconnue: " + subCommand).color(NamedTextColor.RED));
                break;
        }
    }

    private boolean handlePlayerEffectCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Cette commande ne peut être utilisée que par un joueur").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /waystone effect <list|set> [effect]").color(NamedTextColor.RED));
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                handleListPlayerEffects(player, player);
                return true;
            case "set":
                if (args.length >= 3) {
                    String effectId = args[2];
                    handleSetPlayerEffect(player, player, effectId);
                } else {
                    sender.sendMessage(Component.text("Usage: /waystone effect set <effect>").color(NamedTextColor.RED));
                }
                return true;
            default:
                sender.sendMessage(Component.text("Sous-commande inconnue: " + subCommand).color(NamedTextColor.RED));
                return true;
        }
    }

    private void handleListPlayerEffects(CommandSender sender, Player targetPlayer) {
        List<String> availableEffects = plugin.getTeleportEffectManager().getAvailableEffects(targetPlayer);
        String currentEffect = plugin.getTeleportEffectManager().getPlayerEffect(targetPlayer);

        sender.sendMessage(Component.text("=== Effets de téléportation pour " + targetPlayer.getName() + " ===").color(NamedTextColor.GOLD));

        if (availableEffects.isEmpty()) {
            sender.sendMessage(Component.text("Aucun effet disponible").color(NamedTextColor.RED));
            return;
        }

        for (String effectId : availableEffects) {
            Component displayName = plugin.getTeleportEffectManager().getEffectDisplayName(effectId);
            Component description = plugin.getTeleportEffectManager().getEffectDescription(effectId);

            Component line = Component.text("• ").color(NamedTextColor.GRAY)
                    .append(displayName);

            if (effectId.equals(currentEffect)) {
                line = line.append(Component.text(" (ACTUEL)").color(NamedTextColor.GREEN));
            }

            if (!description.equals(Component.empty())) {
                line = line.append(Component.text(" - ").color(NamedTextColor.GRAY))
                        .append(description);
            }

            sender.sendMessage(line);
        }
    }

    private void handleSetPlayerEffect(CommandSender sender, Player targetPlayer, String effectId) {
        if (!plugin.getTeleportEffectManager().hasPermissionForEffect(targetPlayer, effectId)) {
            List<String> availableEffects = plugin.getTeleportEffectManager().getAvailableEffects(targetPlayer);

            sender.sendMessage(Component.text("Le joueur " + targetPlayer.getName() + " n'a pas accès à l'effet: " + effectId).color(NamedTextColor.RED));

            if (!availableEffects.isEmpty()) {
                Component availableText = Component.text("Effets disponibles: ").color(NamedTextColor.YELLOW);
                for (int i = 0; i < availableEffects.size(); i++) {
                    if (i > 0) availableText = availableText.append(Component.text(", ").color(NamedTextColor.GRAY));
                    availableText = availableText.append(Component.text(availableEffects.get(i)).color(NamedTextColor.GREEN));
                }
                sender.sendMessage(availableText);
            }
            return;
        }

        plugin.getTeleportEffectManager().setPlayerEffect(targetPlayer, effectId);

        Component displayName = plugin.getTeleportEffectManager().getEffectDisplayName(effectId);

        sender.sendMessage(Component.text("Effet de téléportation défini pour " + targetPlayer.getName() + ": ").color(NamedTextColor.GREEN)
                .append(displayName));

        if (!sender.equals(targetPlayer)) {
            targetPlayer.sendMessage(Component.text("Votre effet de téléportation a été défini sur: ").color(NamedTextColor.GREEN)
                    .append(displayName));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Premier argument : "admin", "effect" ou nom de joueur
            if (sender.hasPermission("waystones.admin.use")) {
                completions.add("admin");
            }

            // Commande effect pour tous les joueurs
            completions.add("effect");

            // Ajouter les noms des joueurs en ligne
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            // Deuxième argument après "admin" : sous-commandes admin
            if (sender.hasPermission("waystones.admin.create")) {
                completions.add("create");
                completions.add("delete");
                completions.add("setspawn");
                completions.add("removespawn");
                completions.add("description");
            }
            if (sender.hasPermission("waystones.admin.discover")) {
                completions.add("discover");
                completions.add("undiscover");
            }
            if (sender.hasPermission("waystones.admin.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("waystones.admin.repair")) {
                completions.add("repair");
            }
            if (sender.hasPermission("waystones.admin.effects")) {
                completions.add("effect");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("effect")) {
            // Deuxième argument après "effect" : sous-commandes d'effet
            completions.add("list");
            completions.add("set");
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("admin")) {
            // Arguments pour les sous-commandes admin
            String subCommand = args[1].toLowerCase();

            switch (subCommand) {
                case "setspawn":
                case "removespawn":
                case "delete":
                case "description":
                    if (args.length == 3) {
                        // Lister les zones existantes
                        completions.addAll(plugin.getZoneManager().getAllZones().keySet());
                    } else if (args.length == 4 && "delete".equals(subCommand)) {
                        completions.add("confirm");
                    }
                    break;
                case "discover":
                case "undiscover":
                    if (args.length == 3) {
                        // Lister les joueurs en ligne
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            completions.add(player.getName());
                        }
                    } else if (args.length == 4) {
                        // Lister les zones existantes
                        completions.addAll(plugin.getZoneManager().getAllZones().keySet());
                    } else if (args.length == 5) {
                        completions.add("all");
                    }
                    break;
                case "effect":
                    if (args.length == 3) {
                        completions.add("list");
                        completions.add("set");
                    } else if (args.length == 4 && "list".equals(args[2])) {
                        // Joueurs pour liste d'effets
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            completions.add(player.getName());
                        }
                    } else if (args.length == 4 && "set".equals(args[2])) {
                        // Joueurs pour définir effet
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            completions.add(player.getName());
                        }
                    } else if (args.length == 5 && "set".equals(args[2])) {
                        // Effets disponibles
                        if (sender instanceof Player) {
                            Player targetPlayer = Bukkit.getPlayer(args[3]);
                            if (targetPlayer != null) {
                                completions.addAll(plugin.getTeleportEffectManager().getAvailableEffects(targetPlayer));
                            }
                        }
                    }
                    break;
            }
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("effect")) {
            // Tab completion pour commandes d'effet joueur
            String subCommand = args[1].toLowerCase();
            if (args.length == 3 && "set".equals(subCommand) && sender instanceof Player) {
                // Effets disponibles pour le joueur
                completions.addAll(plugin.getTeleportEffectManager().getAvailableEffects((Player) sender));
            }
        }

        // Filtrer les suggestions basées sur ce que l'utilisateur a déjà tapé
        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .sorted()
                .collect(Collectors.toList());
    }
}