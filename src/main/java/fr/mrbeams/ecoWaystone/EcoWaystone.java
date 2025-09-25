package fr.mrbeams.ecoWaystone;

import fr.mrbeams.ecoWaystone.command.WaystoneCommand;
import fr.mrbeams.ecoWaystone.gui.WaystoneGUI;
import fr.mrbeams.ecoWaystone.listener.WaystoneGUIListener;
import fr.mrbeams.ecoWaystone.listener.ZoneDiscoveryListener;
import fr.mrbeams.ecoWaystone.model.DiscoveryZone;
import fr.mrbeams.ecoWaystone.model.PlayerZoneDiscovery;
import fr.mrbeams.ecoWaystone.service.GuiItemsManager;
import fr.mrbeams.ecoWaystone.service.ItemsAdderIntegration;
import fr.mrbeams.ecoWaystone.service.MessageManager;
import fr.mrbeams.ecoWaystone.service.SoundManager;
import fr.mrbeams.ecoWaystone.service.TeleportEffectManager;
import fr.mrbeams.ecoWaystone.service.VaultIntegration;
import fr.mrbeams.ecoWaystone.service.ZoneManager;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public final class EcoWaystone extends JavaPlugin {

    private ItemsAdderIntegration itemsAdderIntegration;
    private VaultIntegration vaultIntegration;
    private ZoneManager zoneManager;
    private MessageManager messageManager;
    private GuiItemsManager guiItemsManager;
    private SoundManager soundManager;
    private TeleportEffectManager teleportEffectManager;
    private WaystoneGUI waystoneGUI;
    private ZoneDiscoveryListener zoneDiscoveryListener;

    @Override
    public void onLoad() {
        ConfigurationSerialization.registerClass(DiscoveryZone.class);
        ConfigurationSerialization.registerClass(PlayerZoneDiscovery.class);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("gui-items.yml", false);
        saveResource("teleport-effects.yml", false);

        // Initialize components regardless of ItemsAdder status
        messageManager = new MessageManager(this);
        itemsAdderIntegration = new ItemsAdderIntegration(this);
        vaultIntegration = new VaultIntegration(this);
        soundManager = new SoundManager(this);
        zoneManager = new ZoneManager(this);
        guiItemsManager = new GuiItemsManager(this);
        teleportEffectManager = new TeleportEffectManager(this);
        waystoneGUI = new WaystoneGUI(this, messageManager, guiItemsManager);
        zoneDiscoveryListener = new ZoneDiscoveryListener(this);

        registerEvents();
        registerCommands();

        // Check ItemsAdder after initialization
        if (!getServer().getPluginManager().isPluginEnabled("ItemsAdder")) {
            getLogger().warning("ItemsAdder is not enabled yet. Waystone features will be limited until ItemsAdder loads.");
        } else {
            getLogger().info("ItemsAdder found and ready!");
        }

        getLogger().info("EcoWaystone enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (zoneManager != null) {
            zoneManager.shutdown();
        }
        getLogger().info("EcoWaystone disabled.");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(itemsAdderIntegration, this);
        getServer().getPluginManager().registerEvents(new WaystoneGUIListener(this, messageManager), this);
        getServer().getPluginManager().registerEvents(zoneDiscoveryListener, this);
    }

    private void registerCommands() {
        WaystoneCommand waystoneCommand = new WaystoneCommand(this, messageManager);
        getCommand("waystone").setExecutor(waystoneCommand);
        getCommand("waystone").setTabCompleter(waystoneCommand);
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public WaystoneGUI getWaystoneGUI() {
        return waystoneGUI;
    }

    public ItemsAdderIntegration getItemsAdderIntegration() {
        return itemsAdderIntegration;
    }

    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public GuiItemsManager getGuiItemsManager() {
        return guiItemsManager;
    }

    public TeleportEffectManager getTeleportEffectManager() {
        return teleportEffectManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }
}
