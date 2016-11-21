package io.nukkit.plugin;

/**
 * Represents various priorities of a provider.
 */
public enum ServicePriority {
    LOWEST(1),
    LOW(2),
    NORMAL(3),
    HIGH(4),
    HIGHEST(5);

    public int getValue() {
        return value;
    }

    /* --- Internal Part ---*/

    int value;

    ServicePriority(int value) {
        this.value = value;
    }

}
