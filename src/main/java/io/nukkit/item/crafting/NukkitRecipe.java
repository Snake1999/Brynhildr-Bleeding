package io.nukkit.item.crafting;

import org.bukkit.inventory.Recipe;

public interface NukkitRecipe extends Recipe {
    void addToCraftingManager();
}
