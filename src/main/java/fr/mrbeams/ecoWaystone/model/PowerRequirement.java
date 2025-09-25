package fr.mrbeams.ecoWaystone.model;

public enum PowerRequirement {
    NONE,
    INTER_DIMENSION,
    ALL;

    public static PowerRequirement fromString(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INTER_DIMENSION;
        }
    }

    public boolean requiresPower(boolean isDimensionJump) {
        return switch (this) {
            case NONE -> false;
            case INTER_DIMENSION -> isDimensionJump;
            case ALL -> true;
        };
    }
}