package io.nukkit.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import io.nukkit.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;

public class IntIdentityHashBiMap<K> implements IObjectIntIterable<K> {
    private static final Object EMPTY = null;
    private K[] values;
    private int[] intKeys;
    private K[] byId;
    private int nextFreeIndex;
    private int mapSize;

    @SuppressWarnings("unchecked")
    public IntIdentityHashBiMap(int initialCapacity) {
        initialCapacity = (int) ((float) initialCapacity / 0.8F);
        this.values = (K[]) (new Object[initialCapacity]);
        this.intKeys = new int[initialCapacity];
        this.byId = (K[]) (new Object[initialCapacity]);
    }

    public int getId(@Nullable K object) {
        return this.getValue(this.getIndex(object, this.hashObject(object)));
    }

    @Nullable
    public K get(int id) {
        return id >= 0 && id < this.byId.length ? this.byId[id] : null;
    }

    private int getValue(int intKey) {
        return intKey == -1 ? -1 : this.intKeys[intKey];
    }

    /**
     * Adds the given object while expanding this map
     */
    public int add(K objectIn) {
        int i = this.nextId();
        this.put(objectIn, i);
        return i;
    }

    private int nextId() {
        while (this.nextFreeIndex < this.byId.length && this.byId[this.nextFreeIndex] != null) {
            ++this.nextFreeIndex;
        }

        return this.nextFreeIndex;
    }

    /**
     * Rehashes the map to the new capacity
     */
    @SuppressWarnings("unchecked")
    private void grow(int capacity) {
        K[] ak = this.values;
        int[] aint = this.intKeys;
        this.values = (K[]) (new Object[capacity]);
        this.intKeys = new int[capacity];
        this.byId = (K[]) (new Object[capacity]);
        this.nextFreeIndex = 0;
        this.mapSize = 0;

        for (int i = 0; i < ak.length; ++i) {
            if (ak[i] != null) {
                this.put(ak[i], aint[i]);
            }
        }
    }

    /**
     * Puts the provided object value with the integer key.
     */
    public void put(K object, int intKey) {
        int i = Math.max(intKey, this.mapSize + 1);

        if ((float) i >= (float) this.values.length * 0.8F) {
            int j;

            //noinspection StatementWithEmptyBody
            for (j = this.values.length << 1; j < intKey; j <<= 1) {
            }

            this.grow(j);
        }

        int k = this.findEmpty(this.hashObject(object));
        this.values[k] = object;
        this.intKeys[k] = intKey;
        this.byId[intKey] = object;
        ++this.mapSize;

        if (intKey == this.nextFreeIndex) {
            ++this.nextFreeIndex;
        }
    }

    private int hashObject(@Nullable K object) {
        return (MathHelper.hash(System.identityHashCode(object)) & Integer.MAX_VALUE) % this.values.length;
    }

    private int getIndex(@Nullable K object, int hash) {
        for (int i = hash; i < this.values.length; ++i) {
            if (this.values[i] == object) {
                return i;
            }

            if (this.values[i] == EMPTY) {
                return -1;
            }
        }

        for (int j = 0; j < hash; ++j) {
            if (this.values[j] == object) {
                return j;
            }

            if (this.values[j] == EMPTY) {
                return -1;
            }
        }

        return -1;
    }

    private int findEmpty(int hash) {
        for (int i = hash; i < this.values.length; ++i) {
            if (this.values[i] == EMPTY) {
                return i;
            }
        }

        for (int j = 0; j < hash; ++j) {
            if (this.values[j] == EMPTY) {
                return j;
            }
        }

        throw new RuntimeException("Overflowed :(");
    }

    public Iterator<K> iterator() {
        return Iterators.filter(Iterators.forArray(this.byId), Predicates.notNull());
    }

    public void clear() {
        Arrays.fill(this.values, null);
        Arrays.fill(this.byId, null);
        this.nextFreeIndex = 0;
        this.mapSize = 0;
    }

    public int size() {
        return this.mapSize;
    }
}
