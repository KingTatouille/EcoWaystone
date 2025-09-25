package fr.mrbeams.ecoWaystone.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

public class ColorUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    // Pattern pour détecter les codes couleur HEX (#ffffff ou &#ffffff)
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_MESSAGE_PATTERN = Pattern.compile("<[^>]+>");

    /**
     * Parse une chaîne avec support pour:
     * - Codes couleur legacy (&a, &c, etc.)
     * - Codes couleur HEX (&#ffffff)
     * - Format MiniMessage (<red>, <#ff0000>, etc.)
     */
    public static Component parseColors(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Détecter le format utilisé et parser en conséquence
        if (MINI_MESSAGE_PATTERN.matcher(text).find()) {
            // Format MiniMessage
            return miniMessage.deserialize(text);
        } else if (HEX_PATTERN.matcher(text).find()) {
            // Format HEX personnalisé (&#ffffff)
            String converted = convertHexToMiniMessage(text);
            return miniMessage.deserialize(converted);
        } else {
            // Format legacy (&a, &c, etc.)
            return legacySerializer.deserialize(text);
        }
    }

    /**
     * Convertit les codes HEX personnalisés (&#ffffff) en format MiniMessage (<#ffffff>)
     */
    private static String convertHexToMiniMessage(String text) {
        return HEX_PATTERN.matcher(text).replaceAll("<#$1>");
    }

    /**
     * Parse une liste de strings avec support couleur
     */
    public static java.util.List<Component> parseColorList(java.util.List<String> lines) {
        return lines.stream()
                .map(ColorUtils::parseColors)
                .toList();
    }
}