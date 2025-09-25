package fr.mrbeams.ecoWaystone.model;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Waystone {
    private final UUID id;
    private final Location location;
    private String name;
    private final UUID creator;
    private final long createdAt;

    public Waystone(UUID id, Location location, UUID creator) {
        this.id = id;
        this.location = location.clone();
        this.creator = creator;
        this.createdAt = System.currentTimeMillis();
        this.name = null;
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location.clone();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getCreator() {
        return creator;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public World getWorld() {
        return location.getWorld();
    }

    public boolean isInSameWorld(Location other) {
        return location.getWorld().equals(other.getWorld());
    }

    public double getDistanceTo(Location other) {
        if (!isInSameWorld(other)) {
            return Double.MAX_VALUE;
        }
        return location.distance(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Waystone waystone = (Waystone) obj;
        return id.equals(waystone.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Waystone{" +
                "id=" + id +
                ", location=" + location +
                ", name='" + name + '\'' +
                ", creator=" + creator +
                '}';
    }
}