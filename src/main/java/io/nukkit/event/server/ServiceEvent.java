package io.nukkit.event.server;

import io.nukkit.plugin.RegisteredServiceProvider;
import io.nukkit.plugin.ServiceManager;

/**
 * An event relating to a registered service. This is called in a {@link
 * ServiceManager}
 */
public abstract class ServiceEvent extends ServerEvent {
    private final RegisteredServiceProvider<?> provider;

    public ServiceEvent(final RegisteredServiceProvider<?> provider) {
        this.provider = provider;
    }

    public RegisteredServiceProvider<?> getProvider() {
        return provider;
    }
}