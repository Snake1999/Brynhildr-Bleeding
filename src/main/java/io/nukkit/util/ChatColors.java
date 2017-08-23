package io.nukkit.util;

import static org.bukkit.ChatColor.COLOR_CHAR;

/**
 * Brynhildr Project
 * Author: MagicDroidX
 */
public class ChatColors {

    public static String clean(String message) {
        return clean(message, true);
    }

    public static String clean(String message, boolean removeFormat) {
        message = message.replaceAll((char) 0x1b + "[0-9;\\[\\(]+[Bm]", "");
        return removeFormat ? message.replaceAll(COLOR_CHAR + "[0123456789abcdefklmnor]", "") : message;
    }
}
