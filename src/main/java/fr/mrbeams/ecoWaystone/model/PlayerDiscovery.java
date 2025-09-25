package fr.mrbeams.ecoWaystone.model;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SerializableAs("PlayerDiscovery")
public class PlayerDiscovery implements ConfigurationSerializable {

    private final UUID playerId;
    private final Set<String> discoveredWaystones;

    public PlayerDiscovery(UUID playerId) {
        this.playerId = playerId;
        this.discoveredWaystones = new HashSet<>();
    }

    public PlayerDiscovery(UUID playerId, Set<String> discoveredWaystones) {
        this.playerId = playerId;
        this.discoveredWaystones = new HashSet<>(discoveredWaystones);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Set<String> getDiscoveredWaystones() {
        return new HashSet<>(discoveredWaystones);
    }

    public boolean hasDiscovered(String waystoneId) {
        return discoveredWaystones.contains(waystoneId);
    }

    public boolean addDiscovery(String waystoneId) {
        return discoveredWaystones.add(waystoneId);
    }

    public boolean removeDiscovery(String waystoneId) {
        return discoveredWaystones.remove(waystoneId);
    }

    public int getDiscoveryCount() {
        return discoveredWaystones.size();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("playerId", playerId.toString());
        result.put("discoveredWaystones", discoveredWaystones);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static PlayerDiscovery deserialize(Map<String, Object> args) {
        UUID playerId = UUID.fromString((String) args.get("playerId"));
        Set<String> discovered = (Set<String>) args.getOrDefault("discoveredWaystones", new HashSet<String>());
        return new PlayerDiscovery(playerId, discovered);
    }
}