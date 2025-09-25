package fr.mrbeams.ecoWaystone.service;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    private final EcoWaystone plugin;

    public SoundManager(EcoWaystone plugin) {
        this.plugin = plugin;
    }

    /**
     * Joue un son pour un joueur
     * Supporte à la fois les sons Minecraft vanilla et les sons custom du resource pack
     */
    public void playSound(Player player, String soundName, float volume, float pitch) {
        if (soundName == null || soundName.isEmpty()) {
            return;
        }

        try {
            if (soundName.contains(":")) {
                // Son custom du resource pack (format: namespace:sound_name)
                playCustomSound(player, soundName, volume, pitch);
            } else {
                // Son vanilla Minecraft
                playVanillaSound(player, soundName, volume, pitch);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Impossible de jouer le son: " + soundName + " - " + e.getMessage());
        }
    }

    /**
     * Joue un son pour tous les joueurs dans un rayon autour d'une location
     */
    public void playSoundInRadius(Location location, String soundName, float volume, float pitch, double radius) {
        if (soundName == null || soundName.isEmpty() || location.getWorld() == null) {
            return;
        }

        location.getWorld().getNearbyEntities(location, radius, radius, radius)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .forEach(player -> playSound(player, soundName, volume, pitch));
    }

    /**
     * Joue un son vanilla Minecraft
     */
    private void playVanillaSound(Player player, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Son vanilla introuvable: " + soundName);
        }
    }

    /**
     * Joue un son custom du resource pack
     * Format attendu: namespace:sound_name
     */
    private void playCustomSound(Player player, String soundName, float volume, float pitch) {
        try {
            // Créer une NamespacedKey pour le son custom
            String[] parts = soundName.split(":", 2);
            if (parts.length != 2) {
                plugin.getLogger().warning("Format de son custom invalide: " + soundName + " (attendu: namespace:sound_name)");
                return;
            }

            String namespace = parts[0];
            String key = parts[1];

            // Utiliser la méthode moderne pour jouer les sons custom
            NamespacedKey soundKey = new NamespacedKey(namespace, key);
            player.playSound(player.getLocation(), soundKey.toString(), volume, pitch);

        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de la lecture du son custom: " + soundName + " - " + e.getMessage());
        }
    }

    /**
     * Méthodes de convenance pour des sons prédéfinis
     */
    public void playDiscoverySound(Player player) {
        String soundName = plugin.getConfig().getString("zones.discovery.sound.name", "ENTITY_PLAYER_LEVELUP");
        float volume = (float) plugin.getConfig().getDouble("zones.discovery.sound.volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("zones.discovery.sound.pitch", 1.0);

        playSound(player, soundName, volume, pitch);
    }

    public void playTeleportSound(Player player, String soundName, float volume, float pitch) {
        playSound(player, soundName, volume, pitch);
    }

    /**
     * Teste si un son peut être joué (utile pour la validation)
     */
    public boolean isSoundValid(String soundName) {
        if (soundName == null || soundName.isEmpty()) {
            return false;
        }

        if (soundName.contains(":")) {
            // Sons custom - on assume qu'ils sont valides car on ne peut pas les vérifier facilement
            return soundName.split(":").length == 2;
        } else {
            // Sons vanilla
            try {
                Sound.valueOf(soundName.toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}