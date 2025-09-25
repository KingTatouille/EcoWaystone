package fr.mrbeams.ecoWaystone.model;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SerializableAs("PlayerZoneDiscovery")
public class PlayerZoneDiscovery implements ConfigurationSerializable {

    private final UUID playerId;
    private final Set<String> discoveredZones;

    public PlayerZoneDiscovery(UUID playerId) {
        this.playerId = playerId;
        this.discoveredZones = new HashSet<>();
    }

    public PlayerZoneDiscovery(UUID playerId, Set<String> discoveredZones) {
        this.playerId = playerId;
        this.discoveredZones = new HashSet<>(discoveredZones);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Set<String> getDiscoveredZones() {
        return new HashSet<>(discoveredZones);
    }

    public boolean hasDiscovered(String zoneId) {
        return discoveredZones.contains(zoneId);
    }

    public boolean addDiscoveredZone(String zoneId) {
        return discoveredZones.add(zoneId);
    }

    public boolean removeDiscoveredZone(String zoneId) {
        return discoveredZones.remove(zoneId);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("playerId", playerId.toString());
        map.put("discoveredZones", discoveredZones);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static PlayerZoneDiscovery deserialize(Map<String, Object> map) {
        UUID playerId = UUID.fromString((String) map.get("playerId"));
        Set<String> discoveredZones = new HashSet<>((Set<String>) map.getOrDefault("discoveredZones", new HashSet<>()));
        return new PlayerZoneDiscovery(playerId, discoveredZones);
    }
}