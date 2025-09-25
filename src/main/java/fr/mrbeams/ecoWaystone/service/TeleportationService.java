package fr.mrbeams.ecoWaystone.service;

import fr.mrbeams.ecoWaystone.model.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportationService {
    private final Plugin plugin;
    private final WaystoneManager waystoneManager;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final Map<UUID, TeleportRequest> activeRequests;
    private final Map<UUID, Long> portalSicknessMap;
    private final Random random;

    public TeleportationService(Plugin plugin, WaystoneManager waystoneManager,
                              ConfigManager configManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.waystoneManager = waystoneManager;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.activeRequests = new ConcurrentHashMap<>();
        this.portalSicknessMap = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    public boolean initiateWarp(Player player, UUID waystoneId) {
        if (activeRequests.containsKey(player.getUniqueId())) {
            return false;
        }

        Waystone waystone = waystoneManager.getWaystone(waystoneId);
        if (waystone == null) {
            player.sendMessage(messageManager.getErrorComponent("errors.no-waystone"));
            return false;
        }

        if (waystoneManager.isWaystoneBlocked(waystone.getLocation())) {
            player.sendMessage(messageManager.getErrorComponent("waystone.blocked"));
            return false;
        }

        if (!validateWarpConditions(player, waystone)) {
            return false;
        }

        int waitTime = configManager.getWaitTime();
        TeleportRequest request = new TeleportRequest(player, waystoneId, waitTime);
        activeRequests.put(player.getUniqueId(), request);

        startWarpSequence(player, request);
        return true;
    }

    private boolean validateWarpConditions(Player player, Waystone waystone) {
        Location playerLocation = player.getLocation();
        Location waystoneLocation = waystone.getLocation();

        if (isPlayerSick(player)) {
            PortalSicknessAction action = configManager.getPortalSicknessWarping();
            if (action == PortalSicknessAction.PREVENT_TELEPORT) {
                player.sendMessage(messageManager.getErrorComponent("portal-sickness.prevented"));
                return false;
            }
        }

        if (configManager.isLimitDistance()) {
            int maxRange = waystoneManager.calculateWaystoneRange(
                waystoneLocation,
                configManager.getBaseDistance(),
                configManager.getMaxBoost(),
                configManager.getMaxWarpSize()
            );

            boolean isDimensionJump = !waystone.isInSameWorld(playerLocation);
            if (isDimensionJump && configManager.isJumpWorlds()) {
                maxRange /= configManager.getWorldRatio();
            } else if (isDimensionJump && !configManager.isJumpWorlds()) {
                player.sendMessage(messageManager.getErrorComponent("waystone.out-of-range",
                    "∞", maxRange));
                return false;
            }

            double distance = waystone.getDistanceTo(playerLocation);
            if (distance > maxRange) {
                player.sendMessage(messageManager.getErrorComponent("waystone.out-of-range",
                    Math.round(distance), maxRange));
                return false;
            }
        }

        boolean isDimensionJump = !waystone.isInSameWorld(playerLocation);
        PowerRequirement powerReq = configManager.getRequirePower();

        if (powerReq.requiresPower(isDimensionJump)) {
            if (!waystoneManager.hasRespawnAnchorBelow(waystoneLocation)) {
                player.sendMessage(messageManager.getErrorComponent("waystone.no-power"));
                return false;
            }

            int charges = waystoneManager.getRespawnAnchorCharges(waystoneLocation);
            int powerCost = configManager.getPowerCost();

            if (charges < powerCost) {
                player.sendMessage(messageManager.getErrorComponent("waystone.insufficient-power"));
                return false;
            }
        }

        return true;
    }

    private void startWarpSequence(Player player, TeleportRequest request) {
        int waitTicks = request.getWaitTicks();
        int waitSeconds = waitTicks / 20;

        player.sendMessage(messageManager.getInfoComponent("teleport.preparing", waitSeconds));

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            executeWarp(player, request);
        }, waitTicks);

        request.setTask(task);

        if (configManager.isWarpAnimations()) {
            startWarpAnimation(player, waitTicks);
        }
    }

    private void startWarpAnimation(Player player, int totalTicks) {
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticksRemaining = totalTicks;

            @Override
            public void run() {
                TeleportRequest request = activeRequests.get(player.getUniqueId());
                if (request == null || request.isCancelled()) {
                    return;
                }

                if (ticksRemaining <= 0) {
                    return;
                }

                Location loc = player.getLocation();
                player.getWorld().spawnParticle(Particle.PORTAL, loc, 10, 0.5, 1, 0.5, 0.1);

                ticksRemaining--;
            }
        }, 0L, 5L);
    }

    private void executeWarp(Player player, TeleportRequest request) {
        UUID playerId = player.getUniqueId();
        activeRequests.remove(playerId);

        if (request.isCancelled()) {
            return;
        }

        Waystone waystone = waystoneManager.getWaystone(request.getWaystoneId());
        if (waystone == null) {
            player.sendMessage(messageManager.getErrorComponent("errors.no-waystone"));
            return;
        }

        if (!validateWarpConditions(player, waystone)) {
            return;
        }

        if (isPlayerSick(player)) {
            PortalSicknessAction action = configManager.getPortalSicknessWarping();
            if (action == PortalSicknessAction.DAMAGE_ON_TELEPORT) {
                double damage = configManager.getPortalSicknessDamage();
                player.damage(damage);
                player.sendMessage(messageManager.getWarningComponent("portal-sickness.damage"));
            }
        }

        Location destination = waystone.getLocation().clone().add(0.5, 1, 0.5);
        player.teleport(destination);
        player.sendMessage(messageManager.getSuccessComponent("teleport.success"));

        handlePortalSickness(player);
        consumePower(waystone);

        if (configManager.isSingleUse()) {
            removeSingleUseKey(player);
        }
    }

    private void handlePortalSickness(Player player) {
        if (!configManager.isEnablePortalSickness()) {
            return;
        }

        double chance = configManager.getPortalSicknessChance();
        if (random.nextDouble() < chance) {
            applySickness(player);
            player.sendMessage(messageManager.getWarningComponent("portal-sickness.contracted"));
        }
    }

    private void applySickness(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 600, 9)); // 30 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0)); // 5 seconds

        portalSicknessMap.put(player.getUniqueId(), System.currentTimeMillis() + 30000);
    }

    private boolean isPlayerSick(Player player) {
        Long sickUntil = portalSicknessMap.get(player.getUniqueId());
        if (sickUntil == null) {
            return false;
        }

        if (System.currentTimeMillis() > sickUntil) {
            portalSicknessMap.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private void consumePower(Waystone waystone) {
        // This would need to interact with the respawn anchor block data
        // For now, we'll leave this as a placeholder
    }

    private void removeSingleUseKey(Player player) {
        // This would remove the warp key from the player's inventory
        // Implementation depends on how warp keys are identified
    }

    public void cancelWarp(Player player, String reason) {
        TeleportRequest request = activeRequests.remove(player.getUniqueId());
        if (request != null) {
            request.cancel();
            player.sendMessage(messageManager.getWarningComponent(reason));
        }
    }

    public boolean hasActiveWarp(Player player) {
        return activeRequests.containsKey(player.getUniqueId());
    }

    public void clearSickness(Player player) {
        portalSicknessMap.remove(player.getUniqueId());
    }
}