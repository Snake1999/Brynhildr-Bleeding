package io.nukkit.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.nukkit.Nukkit;
import io.nukkit.event.server.ServiceRegisterEvent;
import io.nukkit.event.server.ServiceUnregisterEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages services and service providers. Services are an interface
 * specifying a list of methods that a provider must implement. Providers are
 * implementations of these services. A provider can be queried from the
 * services manager in order to use a service (if one is available). If
 * multiple plugins register a service, then the service with the highest
 * priority takes precedence.
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
    public <T extends Service> void register(Class<T> service, T provider, ServiceOwner owner, ServicePriority priority) {
        RegisteredServiceProvider<T> registeredProvider;
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(service);
            if (registered == null) {
                registered = new ArrayList<>();
                providers.put(service, registered);
            }

            registeredProvider = new RegisteredServiceProvider<>(service, provider, priority, owner);

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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
                    } catch (NoSuchElementException ignore) {
                    } // There must be at least an element

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
    public <T extends Service> void unregister(T provider) {
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

                            if (registered.getService().equals(provider)) {
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
     * {@inheritDoc}
     */
    @Override
    public <T extends Service> T load(Class<T> service) {
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(service);

            if (registered == null) {
                return null;
            }

            // This should not be null!
            return service.cast(registered.get(0).getService());
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Service> List<RegisteredServiceProvider<T>> getRegistrations(Class<T> service) {
        ImmutableList.Builder<RegisteredServiceProvider<T>> ret;
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(service);

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
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getKnownServices() {
        synchronized (providers) {
            return ImmutableSet.copyOf(providers.keySet());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Service> boolean isProvidedFor(Class<T> service) {
        synchronized (providers) {
            return providers.containsKey(service);
        }
    }
}
