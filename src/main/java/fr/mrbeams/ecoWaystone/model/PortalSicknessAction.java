package fr.mrbeams.ecoWaystone.model;

public enum PortalSicknessAction {
    ALLOW,
    PREVENT_TELEPORT,
    DAMAGE_ON_TELEPORT;

    public static PortalSicknessAction fromString(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DAMAGE_ON_TELEPORT;
        }
    }
}