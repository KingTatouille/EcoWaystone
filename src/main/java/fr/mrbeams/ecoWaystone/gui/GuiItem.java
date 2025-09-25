package fr.mrbeams.ecoWaystone.gui;

import fr.mrbeams.ecoWaystone.model.AdminWaystone;
import fr.mrbeams.ecoWaystone.model.DiscoveryZone;
import fr.mrbeams.ecoWaystone.model.ZoneSpawn;

public class GuiItem {
    private final ItemType type;
    private final AdminWaystone waystone;
    private final DiscoveryZone zone;
    private final ZoneSpawn spawn;
    private final boolean isDiscovered;

    public GuiItem(AdminWaystone waystone, boolean isDiscovered) {
        this.type = ItemType.WAYSTONE;
        this.waystone = waystone;
        this.zone = null;
        this.spawn = null;
        this.isDiscovered = isDiscovered;
    }

    public GuiItem(DiscoveryZone zone) {
        this.type = ItemType.ZONE;
        this.waystone = null;
        this.zone = zone;
        this.spawn = null;
        this.isDiscovered = true; // Les zones dans le GUI sont toujours découvertes
    }

    public GuiItem(ZoneSpawn spawn) {
        this.type = ItemType.SPAWN;
        this.waystone = null;
        this.zone = null;
        this.spawn = spawn;
        this.isDiscovered = true; // Les spawns dans le GUI sont toujours découverts
    }

    public ItemType getType() {
        return type;
    }

    public AdminWaystone getWaystone() {
        return waystone;
    }

    public DiscoveryZone getZone() {
        return zone;
    }

    public ZoneSpawn getSpawn() {
        return spawn;
    }

    public boolean isDiscovered() {
        return isDiscovered;
    }

    public enum ItemType {
        WAYSTONE,
        ZONE,
        SPAWN
    }
}