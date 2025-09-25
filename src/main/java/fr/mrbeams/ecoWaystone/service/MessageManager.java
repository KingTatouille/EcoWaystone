package fr.mrbeams.ecoWaystone.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;

public class MessageManager {
    private final Plugin plugin;
    private FileConfiguration messages;
    private String locale;

    public MessageManager(Plugin plugin, String locale) {
        this.plugin = plugin;
        this.locale = locale;
        loadMessages();
    }

    public void setLocale(String locale) {
        this.locale = locale;
        loadMessages();
    }

    public String getMessage(String key, Object... args) {
        String message = messages.getString(key);
        if (message == null) {
            return "Missing message: " + key;
        }

        if (args.length > 0) {
            message = MessageFormat.format(message, args);
        }

        return message;
    }

    public Component getComponent(String key, Object... args) {
        return Component.text(getMessage(key, args));
    }

    public Component getErrorComponent(String key, Object... args) {
        return Component.text(getMessage(key, args)).color(NamedTextColor.RED);
    }

    public Component getSuccessComponent(String key, Object... args) {
        return Component.text(getMessage(key, args)).color(NamedTextColor.GREEN);
    }

    public Component getWarningComponent(String key, Object... args) {
        return Component.text(getMessage(key, args)).color(NamedTextColor.YELLOW);
    }

    public Component getInfoComponent(String key, Object... args) {
        return Component.text(getMessage(key, args)).color(NamedTextColor.AQUA);
    }

    private void loadMessages() {
        String fileName = "lang-" + locale + ".yml";
        File messageFile = new File(plugin.getDataFolder(), fileName);

        if (!messageFile.exists()) {
            plugin.saveResource(fileName, false);
        }

        if (messageFile.exists()) {
            messages = YamlConfiguration.loadConfiguration(messageFile);
        } else {
            InputStream defaultStream = plugin.getResource("lang-en_US.yml");
            if (defaultStream != null) {
                messages = YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(defaultStream)
                );
            } else {
                messages = new YamlConfiguration();
            }
        }
    }

    public void reload() {
        loadMessages();
    }
}