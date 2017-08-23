package io.nukkit.util.logging;

import io.nukkit.Nukkit;
import io.nukkit.NukkitServer;
import jline.console.ConsoleReader;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.logging.Level;


/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class ConsoleHandler extends Thread {

    private final NukkitServer server;

    public ConsoleHandler(NukkitServer server) {
        super("Server console handler");

        this.server = server;
    }

    @Override
    public void run() {
        if (Nukkit.useConsole) {
            ConsoleReader reader = this.server.reader;

            try {
                while (!server.isStopped() && server.isRunning()) {
                    String line;

                    if (Nukkit.useJline) {
                        line = reader.readLine("> ", null);
                    } else {
                        line = reader.readLine();
                    }

                    if (line != null && !line.trim().isEmpty()) {
                        server.issueCommand(line);
                    }
                }
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Exception handling console input", e);
            }
        }
    }
}
