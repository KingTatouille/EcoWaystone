package fr.mrbeams.ecoWaystone.command;

import fr.mrbeams.ecoWaystone.service.ConfigManager;
import fr.mrbeams.ecoWaystone.service.ItemsAdderIntegration;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import fr.mrbeams.ecoWaystone.service.WaystoneManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaystonesCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final WaystoneManager waystoneManager;
    private final ConfigManager configManager;
    private final MessageManager messageManager;

    public WaystonesCommand(Plugin plugin, WaystoneManager waystoneManager,
                           ConfigManager configManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.waystoneManager = waystoneManager;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showPluginInfo(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "info":
                showPluginInfo(sender);
                break;

            case "getkey":
                handleGetKey(sender, args);
                break;

            case "config":
                handleConfig(sender, args);
                break;

            case "ratio":
                handleRatio(sender, args);
                break;

            case "reload":
                handleReload(sender, args);
                break;

            default:
                sender.sendMessage(messageManager.getErrorComponent("commands.invalid-syntax",
                    "/waystones [info|getkey|config|ratio|reload]"));
                break;
        }

        return true;
    }

    private void showPluginInfo(CommandSender sender) {
        Component header = messageManager.getInfoComponent("commands.info.header");
        Component version = messageManager.getInfoComponent("commands.info.version", "1.0");
        Component author = messageManager.getInfoComponent("commands.info.author", "MrBeams");
        Component description = messageManager.getInfoComponent("commands.info.description",
            "Teleportation through waystones without commands");

        sender.sendMessage(header);
        sender.sendMessage(version);
        sender.sendMessage(author);
        sender.sendMessage(description);
    }

    private void handleGetKey(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getErrorComponent("commands.player-not-found", "console"));
                return;
            }

            if (!sender.hasPermission("waystones.getkey.self")) {
                sender.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
                return;
            }

            giveWarpKey((Player) sender, 1);
            sender.sendMessage(messageManager.getSuccessComponent("commands.getkey.success-self", 1));

        } else if (args.length == 2) {
            try {
                int count = Integer.parseInt(args[1]);
                if (!(sender instanceof Player)) {
                    sender.sendMessage(messageManager.getErrorComponent("commands.player-not-found", "console"));
                    return;
                }

                if (!sender.hasPermission("waystones.getkey.self")) {
                    sender.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
                    return;
                }

                giveWarpKey((Player) sender, count);
                sender.sendMessage(messageManager.getSuccessComponent("commands.getkey.success-self", count));

            } catch (NumberFormatException e) {
                String playerName = args[1];
                Player target = Bukkit.getPlayer(playerName);

                if (target == null) {
                    sender.sendMessage(messageManager.getErrorComponent("commands.player-not-found", playerName));
                    return;
                }

                if (!sender.hasPermission("waystones.getkey.all")) {
                    sender.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
                    return;
                }

                giveWarpKey(target, 1);
                sender.sendMessage(messageManager.getSuccessComponent("commands.getkey.success-other", playerName, 1));
            }

        } else if (args.length == 3) {
            String playerName = args[1];
            Player target = Bukkit.getPlayer(playerName);

            if (target == null) {
                sender.sendMessage(messageManager.getErrorComponent("commands.player-not-found", playerName));
                return;
            }

            if (!sender.hasPermission("waystones.getkey.all")) {
                sender.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
                return;
            }

            try {
                int count = Integer.parseInt(args[2]);
                giveWarpKey(target, count);
                sender.sendMessage(messageManager.getSuccessComponent("commands.getkey.success-other", playerName, count));

            } catch (NumberFormatException e) {
                sender.sendMessage(messageManager.getErrorComponent("commands.invalid-syntax",
                    "/waystones getkey [count | player [count]]"));
            }
        }
    }

    private void handleConfig(CommandSender sender, String[] args) {
        if (!sender.hasPermission("waystones.config")) {
            sender.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
            return;
        }

        if (args.length == 1) {
            sender.sendMessage(messageManager.getErrorComponent("commands.invalid-syntax",
                "/waystones config <property> [new-value]"));
            return;
        }

        String property = args[1];

        if (!configManager.hasPath(property)) {
            sender.sendMessage(messageManager.getErrorComponent("commands.config.invalid-property", property));
            return;
        }

        if (args.length == 2) {
            Object currentValue = configManager.getValue(property);
            sender.sendMessage(messageManager.getInfoComponent("commands.config.current-value",
                property, currentValue));

        } else {
            String newValue = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            try {
                Object value = parseConfigValue(newValue);
                configManager.setValue(property, value);
                plugin.saveConfig();

                sender.sendMessage(messageManager.getSuccessComponent("commands.config.value-updated",
                    property, value));

            } catch (Exception e) {
                sender.sendMessage(messageManager.getErrorComponent("errors.config-error", e.getMessage()));
            }
        }
    }

    private void handleRatio(CommandSender sender, String[] args) {
        if (!sender.hasPermission("waystones.ratios")) {
            sender.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
            return;
        }

        sender.sendMessage(messageManager.getInfoComponent("commands.ratio.no-ratios"));
    }

    private void handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("waystones.reload")) {
            sender.sendMessage(messageManager.getErrorComponent("commands.no-permission"));
            return;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("config")) {
            plugin.reloadConfig();
            messageManager.reload();
            sender.sendMessage(messageManager.getSuccessComponent("commands.config-reloaded"));

        } else if (args[1].equalsIgnoreCase("advancements")) {
            sender.sendMessage(messageManager.getSuccessComponent("commands.advancements-reloaded"));

        } else {
            sender.sendMessage(messageManager.getErrorComponent("commands.invalid-syntax",
                "/waystones reload [config|advancements]"));
        }
    }

    private void giveWarpKey(Player player, int count) {
        for (int i = 0; i < count; i++) {
            ItemStack warpKey;

            if (configManager.isEnableKeyItems() && ItemsAdderIntegration.isItemsAdderAvailable()) {
                warpKey = ItemsAdderIntegration.getCustomWarpKey();
                if (warpKey == null) {
                    warpKey = createDefaultWarpKey();
                }
            } else {
                warpKey = createDefaultWarpKey();
            }

            player.getInventory().addItem(warpKey);
        }
    }

    private ItemStack createDefaultWarpKey() {
        ItemStack warpKey = new ItemStack(Material.COMPASS);
        ItemMeta meta = warpKey.getItemMeta();

        if (meta != null) {
            meta.displayName(messageManager.getComponent("items.warp-key.name"));

            List<Component> lore = new ArrayList<>();
            lore.add(messageManager.getComponent("items.warp-key.lore"));
            meta.lore(lore);

            warpKey.setItemMeta(meta);
        }

        return warpKey;
    }

    private Object parseConfigValue(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }

        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("info", "getkey", "config", "ratio", "reload");
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }

        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("getkey") && sender.hasPermission("waystones.getkey.all")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                List<String> reloadTypes = Arrays.asList("config", "advancements");
                for (String type : reloadTypes) {
                    if (type.startsWith(args[1].toLowerCase())) {
                        completions.add(type);
                    }
                }
            }
        }

        return completions;
    }
}