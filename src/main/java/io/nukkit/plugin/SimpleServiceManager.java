package io.nukkit.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.nukkit.Nukkit;
import io.nukkit.event.server.ServiceRegisterEvent;
import io.nukkit.event.server.ServiceUnregisterEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple services manager.
 */
public class SimpleServiceManager implements ServiceManager {

    /**
     * Map of providers.
     */
    private final Map<Class<?>, List<RegisteredServiceProvider<?>>> providers = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Service> void register(Class<T> serviceClass, T provider, ServiceOwner owner, ServicePriority priority) {
        RegisteredServiceProvider<T> registeredProvider;
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(serviceClass);
            if (registered == null) {
                registered = new ArrayList<>();
                providers.put(serviceClass, registered);
            }

            registeredProvider = new RegisteredServiceProvider<>(serviceClass, provider, priority, owner);

            // Insert the provider into the collection, much more efficient big O than sort
            int position = Collections.binarySearch(registered, registeredProvider);
            if (position < 0) {
                registered.add(-(position + 1), registeredProvider);
            } else {
                registered.add(position, registeredProvider);
            }

        }
        Nukkit.getServer().getPluginManager().callEvent(new ServiceRegisterEvent(registeredProvider));
    }

    /**
     * Unregister all the providers registered by a particular owner.
     *
     * @param owner The owner
     */
    @Override
    public void unregisterAll(ServiceOwner owner) {
        List<ServiceUnregisterEvent> unregisteredEvents = new ArrayList<>();
        synchronized (providers) {
            Iterator<Map.Entry<Class<?>, List<RegisteredServiceProvider<?>>>> it = providers.entrySet().iterator();

            try {
                while (it.hasNext()) {
                    Map.Entry<Class<?>, List<RegisteredServiceProvider<?>>> entry = it.next();
                    Iterator<RegisteredServiceProvider<?>> it2 = entry.getValue().iterator();

                    try {
                        // Removed entries that are from this owner

                        while (it2.hasNext()) {
                            RegisteredServiceProvider<?> registered = it2.next();

                            if (registered.getOwner().equals(owner)) {
                                it2.remove();
                                unregisteredEvents.add(new ServiceUnregisterEvent(registered));
                            }
                        }
                    } catch (NoSuchElementException e) { // Why does Java suck
                    }

                    // Get rid of the empty list
                    if (entry.getValue().size() == 0) {
                        it.remove();
                    }
                }
            } catch (NoSuchElementException ignored) {
            }
        }
        for (ServiceUnregisterEvent event : unregisteredEvents) {
            Nukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    /**
     * Unregister a particular provider for a particular service.
     *
     * @param service  The service interface
     * @param provider The service provider implementation
     */
    @Override
    public <T extends Service> void unregister(Class<T> service, T provider) {
        List<ServiceUnregisterEvent> unregisteredEvents = new ArrayList<>();
        synchronized (providers) {
            Iterator<Map.Entry<Class<?>, List<RegisteredServiceProvider<?>>>> it = providers.entrySet().iterator();

            try {
                while (it.hasNext()) {
                    Map.Entry<Class<?>, List<RegisteredServiceProvider<?>>> entry = it.next();

                    // We want a particular service
                    if (entry.getKey() != service) {
                        continue;
                    }

                    Iterator<RegisteredServiceProvider<?>> it2 = entry.getValue().iterator();

                    try {
                        // Removed entries that are from this owner

                        while (it2.hasNext()) {
                            RegisteredServiceProvider<?> registered = it2.next();

                            if (registered.getService() == provider) {
                                it2.remove();
                                unregisteredEvents.add(new ServiceUnregisterEvent(registered));
                            }
                        }
                    } catch (NoSuchElementException ignore) {} // There must be at least an element

                    // Get rid of the empty list
                    if (entry.getValue().size() == 0) {
                        it.remove();
                    }
                }
            } catch (NoSuchElementException ignored) {
            }
        }
        for (ServiceUnregisterEvent event : unregisteredEvents) {
            Nukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Service> void unregister(T service) {
        ArrayList<ServiceUnregisterEvent> unregisteredEvents = new ArrayList<>();
        synchronized (providers) {
            Iterator<Map.Entry<Class<?>, List<RegisteredServiceProvider<?>>>> it = providers.entrySet().iterator();

            try {
                while (it.hasNext()) {
                    Map.Entry<Class<?>, List<RegisteredServiceProvider<?>>> entry = it.next();
                    Iterator<RegisteredServiceProvider<?>> it2 = entry.getValue().iterator();

                    try {
                        // Removed entries that are from this owner

                        while (it2.hasNext()) {
                            RegisteredServiceProvider<?> registered = it2.next();

                            if (registered.getService().equals(service)) {
                                it2.remove();
                                unregisteredEvents.add(new ServiceUnregisterEvent(registered));
                            }
                        }
                    } catch (NoSuchElementException e) { // Why does Java suck
                    }

                    // Get rid of the empty list
                    if (entry.getValue().size() == 0) {
                        it.remove();
                    }
                }
            } catch (NoSuchElementException ignored) {
            }
        }
        for (ServiceUnregisterEvent event : unregisteredEvents) {
            Nukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    /**
     * Queries for a provider. This may return if no provider has been
     * registered for a service. The highest priority provider is returned.
     *
     * @param <T>     The service interface
     * @param serviceClass Class of service interface
     * @return provider or null
     */
    @Override
    public <T extends Service> T load(Class<T> serviceClass) {
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(serviceClass);

            if (registered == null) {
                return null;
            }

            // This should not be null!
            return serviceClass.cast(registered.get(0).getService());
        }
    }

    /**
     * Queries for a provider registration. This may return if no provider
     * has been registered for a service.
     *
     * @param <T>     The service interface
     * @param serviceClass Class of the service interface
     * @return provider registration or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Service> RegisteredServiceProvider<T> getRegistration(Class<T> serviceClass) {
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(serviceClass);

            if (registered == null) {
                return null;
            }

            // This should not be null!
            return (RegisteredServiceProvider<T>) registered.get(0);
        }
    }

    /**
     * Get registrations of providers for a owner.
     *
     * @param owner The owner
     * @return provider registration or null
     */
    @Override
    public List<RegisteredServiceProvider<?>> getRegistrations(ServiceOwner owner) {
        ImmutableList.Builder<RegisteredServiceProvider<?>> ret = ImmutableList.builder();
        synchronized (providers) {
            for (List<RegisteredServiceProvider<?>> registered : providers.values()) {
                registered.stream().filter(provider -> provider.getOwner().equals(owner)).forEach(ret::add);
            }
        }
        return ret.build();
    }

    /**
     * Get registrations of providers for a service. The returned list is
     * an unmodifiable copy.
     *
     * @param <T>     The service interface
     * @param serviceClass Class of the service interface
     * @return a copy of the list of registrations
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Service> List<RegisteredServiceProvider<T>> getRegistrations(Class<T> serviceClass) {
        ImmutableList.Builder<RegisteredServiceProvider<T>> ret;
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(serviceClass);

            if (registered == null) {
                return ImmutableList.of();
            }

            ret = ImmutableList.builder();

            for (RegisteredServiceProvider<?> provider : registered) {
                ret.add((RegisteredServiceProvider<T>) provider);
            }

        }
        return ret.build();
    }

    /**
     * Get a list of known services. A service is known if it has registered
     * providers for it.
     *
     * @return a copy of the set of known services
     */
    @Override
    public Set<Class<?>> getKnownServices() {
        synchronized (providers) {
            return ImmutableSet.copyOf(providers.keySet());
        }
    }

    /**
     * Returns whether a provider has been registered for a service.
     *
     * @param <T>     service
     * @param serviceClass Class of the service interface
     * @return true if and only if there are registered providers
     */
    @Override
    public <T extends Service> boolean isProvidedFor(Class<T> serviceClass) {
        synchronized (providers) {
            return providers.containsKey(serviceClass);
        }
    }
}
