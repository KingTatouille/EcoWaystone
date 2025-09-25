package fr.mrbeams.ecoWaystone.service;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AdvancementManager {
    private final Plugin plugin;
    private final ConfigManager configManager;

    public AdvancementManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void grantAdvancement(Player player, String advancementName) {
        if (!configManager.isEnableAdvancements()) {
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, advancementName);
        Advancement advancement = Bukkit.getAdvancement(key);

        if (advancement != null) {
            org.bukkit.advancement.AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (!progress.isDone()) {
                for (String criteria : progress.getRemainingCriteria()) {
                    progress.awardCriteria(criteria);
                }
            }
        }
    }

    public void grantWaystoneAdvancement(Player player) {
        grantAdvancement(player, "waystones");
    }

    public void grantSecretTunnelAdvancement(Player player) {
        grantAdvancement(player, "secret_tunnel");
    }

    public void grantGigawarpsAdvancement(Player player) {
        grantAdvancement(player, "gigawarps");
    }

    public void grantPortalSicknessAdvancement(Player player) {
        grantAdvancement(player, "portal_sickness");
    }

    public void grantHeavyArtilleryAdvancement(Player player) {
        grantAdvancement(player, "heavy_artillery");
    }

    public void grantUnlimitedPowerAdvancement(Player player) {
        grantAdvancement(player, "unlimited_power");
    }

    public void grantQuantumDomesticationAdvancement(Player player) {
        grantAdvancement(player, "quantum_domestication");
    }

    public void grantBlockedAdvancement(Player player) {
        grantAdvancement(player, "blocked");
    }

    public void grantShootMessengerAdvancement(Player player) {
        grantAdvancement(player, "shoot_messenger");
    }

    public void grantCleanEnergyAdvancement(Player player) {
        grantAdvancement(player, "clean_energy");
    }
}