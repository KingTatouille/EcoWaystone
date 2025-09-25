package fr.mrbeams.ecoWaystone.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public class WarpKey {
    private final ItemStack itemStack;
    private final NamespacedKey waystoneIdKey;
    private final NamespacedKey warpKeyKey;

    public WarpKey(ItemStack itemStack, NamespacedKey waystoneIdKey, NamespacedKey warpKeyKey) {
        this.itemStack = itemStack;
        this.waystoneIdKey = waystoneIdKey;
        this.warpKeyKey = warpKeyKey;
    }

    public boolean isWarpKey() {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(warpKeyKey, PersistentDataType.BYTE);
    }

    public boolean isLinked() {
        if (!isWarpKey()) {
            return false;
        }

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(waystoneIdKey, PersistentDataType.STRING);
    }

    public UUID getLinkedWaystoneId() {
        if (!isLinked()) {
            return null;
        }

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String uuidString = container.get(waystoneIdKey, PersistentDataType.STRING);

        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void linkToWaystone(UUID waystoneId) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(waystoneIdKey, PersistentDataType.STRING, waystoneId.toString());
        itemStack.setItemMeta(meta);
    }

    public void unlink() {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(waystoneIdKey);
        itemStack.setItemMeta(meta);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static void markAsWarpKey(ItemStack itemStack, NamespacedKey warpKeyKey) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(warpKeyKey, PersistentDataType.BYTE, (byte) 1);
        itemStack.setItemMeta(meta);
    }
}