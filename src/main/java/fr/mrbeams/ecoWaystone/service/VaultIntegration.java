package fr.mrbeams.ecoWaystone.service;

import fr.mrbeams.ecoWaystone.EcoWaystone;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {

    private final EcoWaystone plugin;
    private Economy economy = null;
    private boolean vaultEnabled = false;

    public VaultIntegration(EcoWaystone plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault non trouvé - fonctionnalités d'économie désactivées");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Aucun plugin d'économie trouvé - fonctionnalités d'achat désactivées");
            return;
        }

        economy = rsp.getProvider();
        vaultEnabled = true;
        plugin.getLogger().info("Intégration Vault activée avec " + economy.getName());
    }

    public boolean isVaultEnabled() {
        return vaultEnabled && economy != null;
    }

    public double getBalance(Player player) {
        if (!isVaultEnabled()) return 0.0;
        return economy.getBalance(player);
    }

    public boolean hasEnough(Player player, double amount) {
        if (!isVaultEnabled()) return false;
        return economy.getBalance(player) >= amount;
    }

    public boolean withdraw(Player player, double amount) {
        if (!isVaultEnabled()) return false;

        if (!hasEnough(player, amount)) {
            return false;
        }

        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public String formatMoney(double amount) {
        if (!isVaultEnabled()) return String.valueOf(amount);
        return economy.format(amount);
    }

    public String getCurrencyName() {
        if (!isVaultEnabled()) return "pièces";
        return economy.currencyNameSingular();
    }

    public String getCurrencyNamePlural() {
        if (!isVaultEnabled()) return "pièces";
        return economy.currencyNamePlural();
    }
}