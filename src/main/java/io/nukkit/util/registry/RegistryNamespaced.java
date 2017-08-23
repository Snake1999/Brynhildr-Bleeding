package io.nukkit.util.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.nukkit.util.IObjectIntIterable;
import io.nukkit.util.IntIdentityHashBiMap;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

public class RegistryNamespaced<K, V> extends RegistrySimple<K, V> implements IObjectIntIterable<V> {
    protected final IntIdentityHashBiMap<V> underlyingIntegerMap = new IntIdentityHashBiMap<>(256);
    protected final Map<V, K> inverseObjectRegistry;

    public RegistryNamespaced() {
        //noinspection unchecked
        this.inverseObjectRegistry = ((BiMap) this.registryObjects).inverse();
    }

    public void register(int id, K key, V value) {
        this.underlyingIntegerMap.put(value, id);
        this.putObject(key, value);
    }

    @Override
    protected BiMap<K, V> createUnderlyingMap() {
        return HashBiMap.create();
    }

    @Nullable
    public V getObject(@Nullable K name) {
        return super.getObject(name);
    }


    /**
     * Gets the name we use to identify the given object.
     */
    @Nullable
    public K getNameForObject(V value) {
        return this.inverseObjectRegistry.get(value);
    }

    /**
     * Does this registry contain an entry for the given key?
     */
    public boolean containsKey(K key) {
        return super.containsKey(key);
    }

    /**
     * Gets the integer ID we use to identify the given object.
     */
    public int getIDForObject(@Nullable V value) {
        return this.underlyingIntegerMap.getId(value);
    }


    /**
     * Gets the object identified by the given ID.
     */
    @Nullable
    public V getObjectById(int id) {
        return this.underlyingIntegerMap.get(id);
    }

    public Iterator<V> iterator() {
        return this.underlyingIntegerMap.iterator();
    }
}
