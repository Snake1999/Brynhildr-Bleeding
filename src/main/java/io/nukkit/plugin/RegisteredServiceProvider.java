package io.nukkit.plugin;

/**
 * A registered service provider.
 *
 * @param <T> Service
 */
public class RegisteredServiceProvider<T extends Service> implements Comparable<RegisteredServiceProvider<T>> {

    private Class<T> serviceClass;
    private ServiceOwner owner;
    private T service;
    private ServicePriority priority;

    public RegisteredServiceProvider(Class<T> serviceClass, T service, ServicePriority priority, ServiceOwner owner) {

        this.serviceClass = serviceClass;
        this.owner = owner;
        this.service = service;
        this.priority = priority;
    }

    public Class<T> getServiceClass() {
        return serviceClass;
    }

    public ServiceOwner getOwner() {
        return owner;
    }

    public T getService() {
        return service;
    }

    public ServicePriority getPriority() {
        return priority;
    }

    @Override
    public int compareTo(RegisteredServiceProvider<T> other) {
        if (priority.getValue() == other.getPriority().getValue()) {
            return 0;
        } else {
            return priority.getValue() < other.getPriority().getValue() ? 1 : -1;
        }
    }
}
