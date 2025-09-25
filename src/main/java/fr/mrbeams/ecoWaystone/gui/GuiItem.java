package fr.mrbeams.ecoWaystone.gui;

import fr.mrbeams.ecoWaystone.model.DiscoveryZone;

public class GuiItem {
    private final ItemType type;
    private final DiscoveryZone zone;
    private final boolean discovered;

    public GuiItem(DiscoveryZone zone, boolean discovered) {
        this.type = ItemType.ZONE;
        this.zone = zone;
        this.discovered = discovered;
    }

    public GuiItem(DiscoveryZone zone) {
        this(zone, true);
    }

    public ItemType getType() {
        return type;
    }

    public DiscoveryZone getZone() {
        return zone;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public enum ItemType {
        ZONE
    }
}