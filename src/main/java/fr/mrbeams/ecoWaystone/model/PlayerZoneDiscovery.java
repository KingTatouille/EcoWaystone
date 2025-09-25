package fr.mrbeams.ecoWaystone.model;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        // Convertir le Set en List pour éviter les problèmes de sérialisation YAML
        map.put("discoveredZones", discoveredZones.stream().toList());
        return map;
    }

    @SuppressWarnings("unchecked")
    public static PlayerZoneDiscovery deserialize(Map<String, Object> map) {
        UUID playerId = UUID.fromString((String) map.get("playerId"));

        // Gérer différents types de désérialisation pour discoveredZones
        Set<String> discoveredZones = new HashSet<>();
        Object zonesObj = map.getOrDefault("discoveredZones", new HashSet<>());

        if (zonesObj instanceof List) {
            // Format préféré : List de String
            discoveredZones.addAll((List<String>) zonesObj);
        } else if (zonesObj instanceof Set) {
            // Ancien format : Set de String
            discoveredZones.addAll((Set<String>) zonesObj);
        } else if (zonesObj instanceof MemorySection) {
            // Format YAML avec clés-valeurs (problématique)
            MemorySection section = (MemorySection) zonesObj;
            for (String key : section.getKeys(false)) {
                // Ajouter seulement si la valeur n'est pas null
                if (section.get(key) != null) {
                    discoveredZones.add(key);
                }
            }
        }

        return new PlayerZoneDiscovery(playerId, discoveredZones);
    }
}