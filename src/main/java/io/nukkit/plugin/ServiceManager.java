package io.nukkit.plugin;

import java.util.Collection;
import java.util.List;

/**
 * Manages services and service providers. Services are an interface
 * specifying a list of methods that a provider must implement. Providers are
 * implementations of these services. A provider can be queried from the
 * services manager in order to use a service (if one is available). If
 * multiple plugins register a service, then the service with the highest
 * priority takes precedence.
 */
public interface ServiceManager {

    /**
     * Register a provider of a service.
     *
     * @param <T>      Provider
     * @param serviceClass  service class
     * @param service service to register
     * @param owner   owner with the provider
     * @param priority priority of the provider
     */
    <T extends Service> void register(Class<T> serviceClass, T service, ServiceOwner owner, ServicePriority priority);

    /**
     * Unregister all the providers registered by a particular plugin.
     *
     * @param owner The service owner
     */
    void unregisterAll(ServiceOwner owner);

    /**
     * Unregister a particular provider for a particular service.
     *
     * @param serviceClass  The service interface
     * @param service The service provider implementation
     */
    <T extends Service> void unregister(Class<T> serviceClass, T service);

    /**
     * Unregister a particular provider.
     *
     * @param service The service implementation
     */
    <T extends Service> void unregister(T service);

    /**
     * Queries for a provider. This may return if no provider has been
     * registered for a service. The highest priority provider is returned.
     *
     * @param <T>     The service interface
     * @param serviceClass Class of the service interface
     * @return provider or null
     */
    <T extends Service> T load(Class<T> serviceClass);

    /**
     * Queries for a provider registration. This may return if no provider
     * has been registered for a service.
     *
     * @param <T>     The service interface
     * @param serviceClass Class of the service interface
     * @return provider registration or null
     */
    <T extends Service> RegisteredServiceProvider<T> getRegistration(Class<T> serviceClass);

    /**
     * Get registrations of providers for a plugin.
     *
     * @param owner The owner. Can be a plugin.
     * @return provider registration or null
     */
    List<RegisteredServiceProvider<?>> getRegistrations(ServiceOwner owner);

    /**
     * Get registrations of providers for a service. The returned list is
     * unmodifiable.
     *
     * @param <T>     The service interface
     * @param serviceClass Class of the service interface
     * @return list of registrations
     */
    <T extends Service> Collection<RegisteredServiceProvider<T>> getRegistrations(Class<T> serviceClass);

    /**
     * Get a list of known services. A service is known if it has registered
     * providers for it.
     *
     * @return list of known services
     */
    Collection<Class<?>> getKnownServices();

    /**
     * Returns whether a provider has been registered for a service. Do not
     * check this first only to call <code>load(service)</code> later, as that
     * would be a non-thread safe situation.
     *
     * @param <T>     service
     * @param serviceClass service class to check
     * @return whether there has been a registered provider
     */
    <T extends Service> boolean isProvidedFor(Class<T> serviceClass);

}
