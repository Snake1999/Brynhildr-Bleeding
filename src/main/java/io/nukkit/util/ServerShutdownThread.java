package io.nukkit.util;

import io.nukkit.NukkitServer;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class ServerShutdownThread extends Thread {

    private final NukkitServer server;

    public ServerShutdownThread(NukkitServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            this.server.stop();
        } finally {
            try {
                this.server.reader.getTerminal().restore();
            } catch (Exception ignored) {

            }
        }
    }
}
