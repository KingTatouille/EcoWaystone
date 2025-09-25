package fr.mrbeams.ecoWaystone.service;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessageManager {

    private final EcoWaystone plugin;
    private final Map<String, FileConfiguration> languageFiles;
    private final String defaultLanguage = "fr_FR";

    public MessageManager(EcoWaystone plugin) {
        this.plugin = plugin;
        this.languageFiles = new HashMap<>();
        loadLanguageFiles();
    }

    private void loadLanguageFiles() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        String[] defaultLanguages = {"fr_FR", "en_US"};
        for (String lang : defaultLanguages) {
            File langFile = new File(langDir, "lang-" + lang + ".yml");
            if (!langFile.exists()) {
                saveDefaultLanguageFile(lang);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
            languageFiles.put(lang, config);
        }

        plugin.getLogger().info("Loaded " + languageFiles.size() + " language files");
    }

    private void saveDefaultLanguageFile(String language) {
        String fileName = "lang-" + language + ".yml";
        File langFile = new File(plugin.getDataFolder(), "lang/" + fileName);

        try (InputStream inputStream = plugin.getResource(fileName)) {
            if (inputStream != null) {
                Files.copy(inputStream, langFile.toPath());
            } else {
                createDefaultLanguageFile(langFile, language);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save default language file: " + fileName, e);
            createDefaultLanguageFile(langFile, language);
        }
    }

    private void createDefaultLanguageFile(File langFile, String language) {
        YamlConfiguration config = new YamlConfiguration();

        config.set("gui.waystone.title", "&bWaystones");
        config.set("gui.waystone.click_to_teleport", "&7Click to teleport");

        config.set("waystone.placed", "&aWaystone placed at %location%");
        config.set("waystone.removed", "&cWaystone &e%name% &cremoved");
        config.set("waystone.discovered", "&6You discovered waystone: &e%name%");
        config.set("waystone.discovery_count", "&aTotal discovered: &e%count%");
        config.set("waystone.teleporting", "&aTeleporting to &e%name%&a...");
        config.set("waystone.teleport_success", "&aSuccessfully teleported to &e%name%");

        config.set("error.no_permission_place", "&cYou don't have permission to place waystones");
        config.set("error.no_permission_break", "&cYou don't have permission to break waystones");
        config.set("error.waystone_not_found", "&cWaystone not found");
        config.set("error.waystone_not_discovered", "&cYou haven't discovered this waystone yet");

        try {
            config.save(langFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create default language file: " + langFile.getName(), e);
        }
    }

    public Component getMessage(String key, Player player) {
        return getMessage(key, getPlayerLanguage(player));
    }

    public Component getMessage(String key, String language) {
        FileConfiguration config = languageFiles.get(language);
        if (config == null) {
            config = languageFiles.get(defaultLanguage);
        }

        String message = config.getString(key, "&cMissing message: " + key);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public String getPlayerLanguage(Player player) {
        return defaultLanguage;
    }

    public void reloadConfig() {
        languageFiles.clear();
        loadLanguageFiles();
    }
}