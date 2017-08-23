package io.nukkit.scheduler;

import org.bukkit.plugin.Plugin;


class NukkitAsyncDebugger {
    private final int expiry;
    private final Plugin plugin;
    private final Class<? extends Runnable> clazz;
    private NukkitAsyncDebugger next = null;

    NukkitAsyncDebugger(final int expiry, final Plugin plugin, final Class<? extends Runnable> clazz) {
        this.expiry = expiry;
        this.plugin = plugin;
        this.clazz = clazz;

    }

    final NukkitAsyncDebugger getNextHead(final int time) {
        NukkitAsyncDebugger next, current = this;
        while (time > current.expiry && (next = current.next) != null) {
            current = next;
        }
        return current;
    }

    final NukkitAsyncDebugger setNext(final NukkitAsyncDebugger next) {
        return this.next = next;
    }

    StringBuilder debugTo(final StringBuilder string) {
        for (NukkitAsyncDebugger next = this; next != null; next = next.next) {
            string.append(next.plugin.getDescription().getName()).append(':').append(next.clazz.getName()).append('@').append(next.expiry).append(',');
        }
        return string;
    }
}
