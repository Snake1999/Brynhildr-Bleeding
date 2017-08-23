package io.nukkit.util;

import io.nukkit.Nukkit;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Versioning {
    public static String getNukkitVersion() {
        String result = Nukkit.VERSION;
        InputStream stream = Nukkit.class.getClassLoader().getResourceAsStream("META-INF/maven/io.nukkit/nukkit/pom.properties");
        Properties properties = new Properties();
        if (stream != null) {
            try {
                properties.load(stream);
                result = properties.getProperty("version");
            } catch (IOException e) {
                Logger.getLogger(Versioning.class.getName()).log(Level.SEVERE, "Could not get Nukkit version!", e);
            }
        }

        return result;
    }

    public static String getBukkitVersion() {
        String result = "Unknown-Version";

        InputStream stream = Bukkit.class.getClassLoader().getResourceAsStream("META-INF/maven/org.spigotmc/spigot-api/pom.properties");
        Properties properties = new Properties();

        if (stream != null) {
            try {
                properties.load(stream);

                result = properties.getProperty("version");
            } catch (IOException ex) {
                Logger.getLogger(Versioning.class.getName()).log(Level.SEVERE, "Could not get Bukkit version!", ex);
            }
        }

        return result;
    }
}
