package io.nukkit.util.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class RegistrySimple<K, V> implements IRegistry<K, V> {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final Map<K, V> registryObjects = this.createUnderlyingMap();
    private Object[] values;

    protected Map<K, V> createUnderlyingMap() {
        return Maps.newHashMap();
    }

    @Nullable
    public V getObject(@Nullable K name) {
        return this.registryObjects.get(name);
    }

    /**
     * Register an object on this registry.
     */
    public void putObject(K key, V value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        this.values = null;

        if (this.registryObjects.containsKey(key)) {
            LOGGER.debug("Adding duplicate key '{}' to registry", key);
        }

        this.registryObjects.put(key, value);
    }

    public Set<K> getKeys() {
        return Collections.unmodifiableSet(this.registryObjects.keySet());
    }

    @Nullable
    public V getRandomObject(Random random) {
        if (this.values == null) {
            Collection<V> collection = this.registryObjects.values();

            if (collection.isEmpty()) {
                return null;
            }

            this.values = collection.toArray(new Object[collection.size()]);
        }

        //noinspection unchecked
        return (V) this.values[random.nextInt(this.values.length)];
    }

    /**
     * Does this registry contain an entry for the given key?
     */
    public boolean containsKey(K key) {
        return this.registryObjects.containsKey(key);
    }


    public Iterator<V> iterator() {
        return this.registryObjects.values().iterator();
    }
}
