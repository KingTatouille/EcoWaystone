package fr.mrbeams.ecoWaystone.service;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderIntegration {
    private static final String WAYSTONE_ITEM_ID = "ecowaystone:waystone";
    private static final String WARP_KEY_ITEM_ID = "ecowaystone:warp_key";

    public static boolean isCustomWaystone(ItemStack item) {
        if (item == null) {
            return false;
        }

        CustomStack customStack = CustomStack.byItemStack(item);
        return customStack != null && WAYSTONE_ITEM_ID.equals(customStack.getNamespacedID());
    }

    public static boolean isCustomWarpKey(ItemStack item) {
        if (item == null) {
            return false;
        }

        CustomStack customStack = CustomStack.byItemStack(item);
        return customStack != null && WARP_KEY_ITEM_ID.equals(customStack.getNamespacedID());
    }

    public static ItemStack getCustomWaystone() {
        CustomStack customStack = CustomStack.getInstance(WAYSTONE_ITEM_ID);
        return customStack != null ? customStack.getItemStack() : null;
    }

    public static ItemStack getCustomWarpKey() {
        CustomStack customStack = CustomStack.getInstance(WARP_KEY_ITEM_ID);
        return customStack != null ? customStack.getItemStack() : null;
    }

    public static boolean isItemsAdderAvailable() {
        try {
            Class.forName("dev.lone.itemsadder.api.CustomStack");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String getCustomItemId(ItemStack item) {
        if (item == null) {
            return null;
        }

        CustomStack customStack = CustomStack.byItemStack(item);
        return customStack != null ? customStack.getNamespacedID() : null;
    }
}