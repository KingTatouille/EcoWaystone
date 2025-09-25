package fr.mrbeams.ecoWaystone.service;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import fr.mrbeams.ecoWaystone.util.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class TeleportEffectManager {

    private final EcoWaystone plugin;
    private FileConfiguration config;
    private final Map<UUID, String> playerSelectedEffects;

    public TeleportEffectManager(EcoWaystone plugin) {
        this.plugin = plugin;
        this.playerSelectedEffects = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "teleport-effects.yml");
        if (!configFile.exists()) {
            plugin.saveResource("teleport-effects.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfig() {
        loadConfig();
        playerSelectedEffects.clear();
    }

    public void teleportPlayerWithEffect(Player player, Location destination) {
        String effectId = getPlayerEffect(player);
        TeleportEffect effect = loadEffect(effectId);

        if (effect == null) {
            plugin.getLogger().warning("Effet de téléportation introuvable: " + effectId);
            player.teleport(destination);
            return;
        }

        // Jouer les effets de départ
        playStartEffects(player, effect);

        // Programmer la téléportation avec délai
        int delay = config.getInt("settings.teleport-delay", 40);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Téléporter le joueur
                player.teleport(destination);

                // Jouer les effets d'arrivée
                playEndEffects(player, effect);
            }
        }.runTaskLater(plugin, delay);
    }

    private void playStartEffects(Player player, TeleportEffect effect) {
        Location location = player.getLocation();

        // Son de départ
        if (effect.startSound != null && effect.startSoundEnabled) {
            plugin.getSoundManager().playSound(player, effect.startSound,
                effect.startSoundVolume, effect.startSoundPitch);
        }

        // Particules de départ
        if (effect.startParticles != null && effect.startParticlesEnabled) {
            try {
                Particle particle = Particle.valueOf(effect.startParticles);
                for (int i = 0; i < effect.startParticleCount; i++) {
                    double x = location.getX() + (Math.random() - 0.5) * effect.startParticleSpread;
                    double y = location.getY() + Math.random() * 2;
                    double z = location.getZ() + (Math.random() - 0.5) * effect.startParticleSpread;
                    Location particleLoc = new Location(location.getWorld(), x, y, z);
                    player.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Particule invalide: " + effect.startParticles);
            }
        }

        // Message de départ
        if (effect.startMessage != null && !effect.startMessage.isEmpty()) {
            Component message = ColorUtils.parseColors(effect.startMessage);
            player.sendMessage(message);
        }
    }

    private void playEndEffects(Player player, TeleportEffect effect) {
        Location location = player.getLocation();

        // Son d'arrivée
        if (effect.endSound != null && effect.endSoundEnabled) {
            plugin.getSoundManager().playSound(player, effect.endSound,
                effect.endSoundVolume, effect.endSoundPitch);
        }

        // Particules d'arrivée
        if (effect.endParticles != null && effect.endParticlesEnabled) {
            try {
                Particle particle = Particle.valueOf(effect.endParticles);
                for (int i = 0; i < effect.endParticleCount; i++) {
                    double x = location.getX() + (Math.random() - 0.5) * effect.endParticleSpread;
                    double y = location.getY() + Math.random() * 2;
                    double z = location.getZ() + (Math.random() - 0.5) * effect.endParticleSpread;
                    Location particleLoc = new Location(location.getWorld(), x, y, z);
                    player.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Particule invalide: " + effect.endParticles);
            }
        }

        // Message d'arrivée
        if (effect.endMessage != null && !effect.endMessage.isEmpty()) {
            Component message = ColorUtils.parseColors(effect.endMessage);
            player.sendMessage(message);
        }
    }

    public String getPlayerEffect(Player player) {
        String selected = playerSelectedEffects.get(player.getUniqueId());
        if (selected != null && hasPermissionForEffect(player, selected)) {
            return selected;
        }

        // Trouver le meilleur effet auquel le joueur a accès
        return getBestAvailableEffect(player);
    }

    public void setPlayerEffect(Player player, String effectId) {
        if (hasPermissionForEffect(player, effectId)) {
            playerSelectedEffects.put(player.getUniqueId(), effectId);
        }
    }

    public boolean hasPermissionForEffect(Player player, String effectId) {
        ConfigurationSection effectSection = config.getConfigurationSection("effects." + effectId);
        if (effectSection == null) return false;

        String permission = effectSection.getString("permission", "");
        return permission.isEmpty() || player.hasPermission(permission);
    }

    public List<String> getAvailableEffects(Player player) {
        List<String> available = new ArrayList<>();
        ConfigurationSection effectsSection = config.getConfigurationSection("effects");

        if (effectsSection != null) {
            for (String effectId : effectsSection.getKeys(false)) {
                ConfigurationSection effectSection = effectsSection.getConfigurationSection(effectId);
                if (effectSection != null && effectSection.getBoolean("enabled", true)
                    && hasPermissionForEffect(player, effectId)) {
                    available.add(effectId);
                }
            }
        }

        return available;
    }

    public String getBestAvailableEffect(Player player) {
        List<String> available = getAvailableEffects(player);
        if (available.isEmpty()) {
            return config.getString("settings.default-effect", "basic");
        }

        // Priorité: dragon > tech > magic > nature > stealth > basic
        String[] priority = {"dragon", "tech", "magic", "nature", "stealth", "basic"};
        for (String effect : priority) {
            if (available.contains(effect)) {
                return effect;
            }
        }

        return available.get(0);
    }

    public Component getEffectDisplayName(String effectId) {
        ConfigurationSection effectSection = config.getConfigurationSection("effects." + effectId);
        if (effectSection == null) return Component.text(effectId);

        String name = effectSection.getString("name", effectId);
        return ColorUtils.parseColors(name);
    }

    public Component getEffectDescription(String effectId) {
        ConfigurationSection effectSection = config.getConfigurationSection("effects." + effectId);
        if (effectSection == null) return Component.empty();

        String description = effectSection.getString("description", "");
        return ColorUtils.parseColors(description);
    }

    private TeleportEffect loadEffect(String effectId) {
        ConfigurationSection effectSection = config.getConfigurationSection("effects." + effectId);
        if (effectSection == null) return null;

        TeleportEffect effect = new TeleportEffect();

        // Charger les effets de départ
        ConfigurationSection startSection = effectSection.getConfigurationSection("start");
        if (startSection != null) {
            ConfigurationSection startSound = startSection.getConfigurationSection("sound");
            if (startSound != null) {
                effect.startSoundEnabled = startSound.getBoolean("enabled", false);
                effect.startSound = startSound.getString("type");
                effect.startSoundVolume = (float) startSound.getDouble("volume", 1.0);
                effect.startSoundPitch = (float) startSound.getDouble("pitch", 1.0);
            }

            ConfigurationSection startParticles = startSection.getConfigurationSection("particles");
            if (startParticles != null) {
                effect.startParticlesEnabled = startParticles.getBoolean("enabled", false);
                effect.startParticles = startParticles.getString("type");
                effect.startParticleCount = startParticles.getInt("count", 10);
                effect.startParticleSpread = startParticles.getDouble("spread", 1.0);
            }

            effect.startMessage = startSection.getString("message", "");
        }

        // Charger les effets d'arrivée
        ConfigurationSection endSection = effectSection.getConfigurationSection("end");
        if (endSection != null) {
            ConfigurationSection endSound = endSection.getConfigurationSection("sound");
            if (endSound != null) {
                effect.endSoundEnabled = endSound.getBoolean("enabled", false);
                effect.endSound = endSound.getString("type");
                effect.endSoundVolume = (float) endSound.getDouble("volume", 1.0);
                effect.endSoundPitch = (float) endSound.getDouble("pitch", 1.0);
            }

            ConfigurationSection endParticles = endSection.getConfigurationSection("particles");
            if (endParticles != null) {
                effect.endParticlesEnabled = endParticles.getBoolean("enabled", false);
                effect.endParticles = endParticles.getString("type");
                effect.endParticleCount = endParticles.getInt("count", 10);
                effect.endParticleSpread = endParticles.getDouble("spread", 1.0);
            }

            effect.endMessage = endSection.getString("message", "");
        }

        return effect;
    }

    // Classe interne pour représenter un effet de téléportation
    private static class TeleportEffect {
        // Effets de départ
        boolean startSoundEnabled;
        String startSound;
        float startSoundVolume;
        float startSoundPitch;

        boolean startParticlesEnabled;
        String startParticles;
        int startParticleCount;
        double startParticleSpread;

        String startMessage;

        // Effets d'arrivée
        boolean endSoundEnabled;
        String endSound;
        float endSoundVolume;
        float endSoundPitch;

        boolean endParticlesEnabled;
        String endParticles;
        int endParticleCount;
        double endParticleSpread;

        String endMessage;
    }
}