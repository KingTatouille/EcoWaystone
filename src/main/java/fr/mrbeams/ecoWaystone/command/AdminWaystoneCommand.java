package fr.mrbeams.ecoWaystone.command;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.model.AdminWaystone;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminWaystoneCommand implements CommandExecutor, TabCompleter {

    private final EcoWaystone plugin;
    private final MessageManager messageManager;

    public AdminWaystoneCommand(EcoWaystone plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
            case "delete", "remove" -> handleDelete(player, args);
            case "list" -> handleList(player);
            case "info" -> handleInfo(player, args);
            case "reload" -> handleReload(player);
            default -> sendHelpMessage(player);
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("waystones.admin.create")) {
            player.sendMessage(Component.text("You don't have permission to create waystones").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /waystones create <id> <displayName>").color(NamedTextColor.RED));
            return;
        }

        String id = args[1];
        StringBuilder displayNameBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) displayNameBuilder.append(" ");
            displayNameBuilder.append(args[i]);
        }
        String displayName = displayNameBuilder.toString();

        if (plugin.getWaystoneManager().getWaystone(id) != null) {
            player.sendMessage(Component.text("A waystone with ID '" + id + "' already exists").color(NamedTextColor.RED));
            return;
        }

        Location location = player.getLocation();
        plugin.getWaystoneManager().createWaystone(id, id, displayName, location);

        player.sendMessage(Component.text("Waystone '" + displayName + "' created at your location").color(NamedTextColor.GREEN));
    }

    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("waystones.admin.delete")) {
            player.sendMessage(Component.text("You don't have permission to delete waystones").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /waystones delete <id>").color(NamedTextColor.RED));
            return;
        }

        String id = args[1];
        AdminWaystone waystone = plugin.getWaystoneManager().getWaystone(id);

        if (waystone == null) {
            player.sendMessage(Component.text("Waystone with ID '" + id + "' not found").color(NamedTextColor.RED));
            return;
        }

        plugin.getWaystoneManager().deleteWaystone(id);
        player.sendMessage(Component.text("Waystone '" + waystone.getDisplayName() + "' deleted").color(NamedTextColor.GREEN));
    }

    private void handleList(Player player) {
        if (!player.hasPermission("waystones.admin.list")) {
            player.sendMessage(Component.text("You don't have permission to list waystones").color(NamedTextColor.RED));
            return;
        }

        var waystones = plugin.getWaystoneManager().getAllWaystones();

        if (waystones.isEmpty()) {
            player.sendMessage(Component.text("No waystones found").color(NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("=== Waystones ===").color(NamedTextColor.GOLD));
        for (AdminWaystone waystone : waystones) {
            Component message = Component.text("- " + waystone.getId() + ": " + waystone.getDisplayName())
                    .color(NamedTextColor.AQUA)
                    .append(Component.text(" (" + formatLocation(waystone.getLocation()) + ")")
                            .color(NamedTextColor.GRAY));
            player.sendMessage(message);
        }
    }

    private void handleInfo(Player player, String[] args) {
        if (!player.hasPermission("waystones.admin.info")) {
            player.sendMessage(Component.text("You don't have permission to view waystone info").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /waystones info <id>").color(NamedTextColor.RED));
            return;
        }

        String id = args[1];
        AdminWaystone waystone = plugin.getWaystoneManager().getWaystone(id);

        if (waystone == null) {
            player.sendMessage(Component.text("Waystone with ID '" + id + "' not found").color(NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("=== Waystone Info ===").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("ID: " + waystone.getId()).color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("Name: " + waystone.getName()).color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("Display Name: " + waystone.getDisplayName()).color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("Location: " + formatLocation(waystone.getLocation())).color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("Icon: " + waystone.getIconItem()).color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("Enabled: " + waystone.isEnabled()).color(NamedTextColor.AQUA));
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("waystones.admin.reload")) {
            player.sendMessage(Component.text("You don't have permission to reload the plugin").color(NamedTextColor.RED));
            return;
        }

        plugin.getWaystoneManager().loadData();
        messageManager.reloadMessages();

        player.sendMessage(Component.text("EcoWaystone reloaded successfully").color(NamedTextColor.GREEN));
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(Component.text("=== EcoWaystone Commands ===").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("/waystones create <id> <displayName> - Create a waystone").color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("/waystones delete <id> - Delete a waystone").color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("/waystones list - List all waystones").color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("/waystones info <id> - Show waystone info").color(NamedTextColor.AQUA));
        player.sendMessage(Component.text("/waystones reload - Reload the plugin").color(NamedTextColor.AQUA));
    }

    private String formatLocation(Location location) {
        return String.format("%s: %d, %d, %d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("delete");
            completions.add("list");
            completions.add("info");
            completions.add("reload");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("info"))) {
            for (AdminWaystone waystone : plugin.getWaystoneManager().getAllWaystones()) {
                completions.add(waystone.getId());
            }
        }

        return completions;
    }
}