package io.nukkit.item.meta;


import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import io.nukkit.configuration.serialization.ConfigurationSerializable;
import io.nukkit.configuration.serialization.DelegateDeserialization;
import io.nukkit.configuration.serialization.SerializableAs;
import io.nukkit.item.ItemFactory;
import io.nukkit.item.enchantments.Enchantment;
import io.nukkit.item.meta.MetaItem.ItemMetaKey.Specific;
import io.nukkit.material.Material;
import io.nukkit.nbt.CompressedStreamTools;
import io.nukkit.nbt.NBTTag;
import io.nukkit.nbt.NBTTagCompound;
import io.nukkit.nbt.NBTTagList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.Validate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Children must include the following:
 * <p>
 * <li> Constructor(CraftMetaItem meta)
 * <li> Constructor(NBTTagCompound tag)
 * <li> Constructor(Map<String, Object> map)
 * <br><br>
 * <li> void applyToItem(NBTTagCompound tag)
 * <li> boolean applicableTo(Material type)
 * <br><br>
 * <li> boolean equalsCommon(CraftMetaItem meta)
 * <li> boolean notUncommon(CraftMetaItem meta)
 * <br><br>
 * <li> boolean isEmpty()
 * <li> boolean is{Type}Empty()
 * <br><br>
 * <li> int applyHash()
 * <li> public Class clone()
 * <br><br>
 * <li> Builder<String, Object> serialize(Builder<String, Object> builder)
 * <li> SerializableMeta.Deserializers deserializer()
 */
@DelegateDeserialization(MetaItem.SerializableMeta.class)
public class MetaItem implements Cloneable, ConfigurationSerializable {

    static class ItemMetaKey {
        @Retention(RetentionPolicy.SOURCE)
        @Target({ElementType.FIELD})
        @interface Specific {
            MetaItem.ItemMetaKey.Specific.To value();

            enum To {
                BUKKIT,
                NBT
            }
        }

        final String BUKKIT;
        final String NBT;

        ItemMetaKey(String both) {
            this(both, both);
        }

        ItemMetaKey(String nbt, String bukkit) {
            this.NBT = nbt;
            this.BUKKIT = bukkit;
        }
    }

    static final ItemMetaKey NAME = new ItemMetaKey("Name", "display-name");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey DISPLAY = new ItemMetaKey("display");
    static final ItemMetaKey LORE = new ItemMetaKey("Lore", "lore");
    static final ItemMetaKey ENCHANTMENTS = new ItemMetaKey("ench", "enchants");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ENCHANTMENTS_ID = new ItemMetaKey("id");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ENCHANTMENTS_LVL = new ItemMetaKey("lvl");
    static final ItemMetaKey REPAIR = new ItemMetaKey("RepairCost", "repair-cost");

    private String displayName;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments;
    private int repairCost;
    private int hideFlag;

    private static final Set<String> HANDLED_TAGS = Sets.newHashSet();

    private final Map<String, NBTTag> unhandledTags = new HashMap<>();

    public MetaItem(MetaItem meta) {
        if (meta == null) {
            return;
        }

        this.displayName = meta.displayName;

        if (meta.hasLore()) {
            this.lore = new ArrayList<String>(meta.lore);
        }

        if (meta.hasEnchants()) {
            this.enchantments = new HashMap<>(meta.enchantments);
        }

        this.repairCost = meta.repairCost;
        this.hideFlag = meta.hideFlag;
        this.unhandledTags.putAll(meta.unhandledTags);
    }

    public MetaItem(NBTTagCompound tag) {
        if (tag.hasKey(DISPLAY.NBT)) {
            NBTTagCompound display = tag.getCompoundTag(DISPLAY.NBT);

            if (display.hasKey(NAME.NBT)) {
                displayName = display.getString(NAME.NBT);
            }

            if (display.hasKey(LORE.NBT)) {
                NBTTagList list = display.getTagList(LORE.NBT, 8);
                lore = new ArrayList<String>(list.tagCount());

                for (int index = 0; index < list.tagCount(); index++) {
                    String line = list.getStringTagAt(index);
                    lore.add(line);
                }
            }
        }

        this.enchantments = buildEnchantments(tag, ENCHANTMENTS);

        if (tag.hasKey(REPAIR.NBT)) {
            repairCost = tag.getInteger(REPAIR.NBT);
        }

        //TODO: SUPPORT MORE TAG
        /*if (tag.hasKey(HIDEFLAGS.NBT)) {
            hideFlag = tag.getInt(HIDEFLAGS.NBT);
        }

        if (tag.get(ATTRIBUTES.NBT) instanceof NBTTagList) {
            NBTTagList save = null;
            NBTTagList nbttaglist = tag.getList(ATTRIBUTES.NBT, 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                if (!(nbttaglist.get(i) instanceof NBTTagCompound)) {
                    continue;
                }
                NBTTagCompound nbttagcompound = (NBTTagCompound) nbttaglist.get(i);

                if (!nbttagcompound.hasKeyOfType(ATTRIBUTES_UUID_HIGH.NBT, 99)) {
                    continue;
                }
                if (!nbttagcompound.hasKeyOfType(ATTRIBUTES_UUID_LOW.NBT, 99)) {
                    continue;
                }
                if (!(nbttagcompound.get(ATTRIBUTES_IDENTIFIER.NBT) instanceof NBTTagString) || !CraftItemFactory.KNOWN_NBT_ATTRIBUTE_NAMES.contains(nbttagcompound.getString(ATTRIBUTES_IDENTIFIER.NBT))) {
                    continue;
                }
                if (!(nbttagcompound.get(ATTRIBUTES_NAME.NBT) instanceof NBTTagString) || nbttagcompound.getString(ATTRIBUTES_NAME.NBT).isEmpty()) {
                    continue;
                }
                if (!nbttagcompound.hasKeyOfType(ATTRIBUTES_VALUE.NBT, 99)) {
                    continue;
                }
                if (!nbttagcompound.hasKeyOfType(ATTRIBUTES_TYPE.NBT, 99) || nbttagcompound.getInt(ATTRIBUTES_TYPE.NBT) < 0 || nbttagcompound.getInt(ATTRIBUTES_TYPE.NBT) > 2) {
                    continue;
                }

                if (save == null) {
                    save = new NBTTagList();
                }

                NBTTagCompound entry = new NBTTagCompound();
                entry.set(ATTRIBUTES_UUID_HIGH.NBT, nbttagcompound.get(ATTRIBUTES_UUID_HIGH.NBT));
                entry.set(ATTRIBUTES_UUID_LOW.NBT, nbttagcompound.get(ATTRIBUTES_UUID_LOW.NBT));
                entry.set(ATTRIBUTES_IDENTIFIER.NBT, nbttagcompound.get(ATTRIBUTES_IDENTIFIER.NBT));
                entry.set(ATTRIBUTES_NAME.NBT, nbttagcompound.get(ATTRIBUTES_NAME.NBT));
                entry.set(ATTRIBUTES_VALUE.NBT, nbttagcompound.get(ATTRIBUTES_VALUE.NBT));
                entry.set(ATTRIBUTES_TYPE.NBT, nbttagcompound.get(ATTRIBUTES_TYPE.NBT));
                save.add(entry);
            }

            unhandledTags.put(ATTRIBUTES.NBT, save);
        }*/

        Set<String> keys = tag.getKeySet();
        for (String key : keys) {
            if (!getHandledTags().contains(key)) {
                unhandledTags.put(key, tag.getTag(key));
            }
        }
    }

    static Map<Enchantment, Integer> buildEnchantments(NBTTagCompound tag, ItemMetaKey key) {
        if (!tag.hasKey(key.NBT)) {
            return null;
        }

        NBTTagList ench = tag.getTagList(key.NBT, NBTTag.TAG_COMPOUND);
        Map<Enchantment, Integer> enchantments = new HashMap<>(ench.tagCount());

        for (int i = 0; i < ench.tagCount(); i++) {
            NBTTagCompound compound = ench.getCompoundTagAt(i);
            int id = 0xffff & compound.getShort(ENCHANTMENTS_ID.NBT);
            int level = 0xffff & compound.getShort(ENCHANTMENTS_LVL.NBT);

            enchantments.put(Enchantment.getById(id), level);
        }

        return enchantments;
    }

    /**
     * Checks for existence of a display name.
     *
     * @return true if this has a display name
     */
    boolean hasDisplayName() {
        return !Strings.isNullOrEmpty(this.displayName);
    }

    /**
     * Gets the display name that is set.
     * <p>
     * Plugins should check that hasDisplayName() returns <code>true</code>
     * before calling this method.
     *
     * @return the display name that is set
     */
    String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     *
     * @param name the name to set
     */
    void setDisplayName(String name) {
        this.displayName = name;
    }

    /**
     * Checks for existence of lore.
     *
     * @return true if this has lore
     */
    boolean hasLore() {
        return this.lore != null && !this.lore.isEmpty();
    }

    /**
     * Gets the lore that is set.
     * <p>
     * Plugins should check if hasLore() returns <code>true</code> before
     * calling this method.
     *
     * @return a list of lore that is set
     */
    List<String> getLore() {
        return this.lore == null ? null : new ArrayList<>(this.lore);
    }

    /**
     * Sets the lore for this item.
     * Removes lore when given null.
     *
     * @param lore the lore that will be set
     */
    void setLore(List<String> lore) {
        if (lore == null) {
            this.lore = null;
        } else if (this.lore == null) {
            safelyAdd(lore, this.lore = new ArrayList<>(lore.size()), Integer.MAX_VALUE);
        } else {
            this.lore.clear();
            safelyAdd(lore, this.lore, Integer.MAX_VALUE);
        }
    }

    /**
     * Checks for the existence of any enchantments.
     *
     * @return true if an enchantment exists on this meta
     */
    boolean hasEnchants() {
        return !(enchantments == null || enchantments.isEmpty());
    }

    /**
     * Checks for existence of the specified enchantment.
     *
     * @param ench enchantment to check
     * @return true if this enchantment exists for this meta
     */
    public boolean hasEnchant(Enchantment ench) {
        return hasEnchants() && enchantments.containsKey(ench);
    }

    /**
     * Checks for the level of the specified enchantment.
     *
     * @param ench enchantment to check
     * @return The level that the specified enchantment has, or 0 if none
     */
    public int getEnchantLevel(Enchantment ench) {
        Integer level = hasEnchants() ? enchantments.get(ench) : null;
        if (level == null) {
            return 0;
        }
        return level;
    }

    /**
     * Returns a copy the enchantments in this MetaItem. <br>
     * Returns an empty map if none.
     *
     * @return An immutable copy of the enchantments
     */
    public Map<Enchantment, Integer> getEnchants() {
        return hasEnchants() ? ImmutableMap.copyOf(enchantments) : ImmutableMap.of();
    }

    /**
     * Adds the specified enchantment to this item meta.
     *
     * @param ench                   Enchantment to add
     * @param level                  Level for the enchantment
     * @param ignoreLevelRestriction this indicates the enchantment should be
     *                               applied, ignoring the level limit
     * @return true if the item meta changed as a result of this call, false
     * otherwise
     */
    public boolean addEnchant(Enchantment ench, int level, boolean ignoreLevelRestriction) {
        if (enchantments == null) {
            enchantments = new HashMap<Enchantment, Integer>(4);
        }

        if (ignoreLevelRestriction || level >= ench.getStartLevel() && level <= ench.getMaxLevel()) {
            Integer old = enchantments.put(ench, level);
            return old == null || old != level;
        }
        return false;
    }

    /**
     * Removes the specified enchantment from this item meta.
     *
     * @param ench Enchantment to remove
     * @return true if the item meta changed as a result of this call, false
     * otherwise
     */
    public boolean removeEnchant(Enchantment ench) {
        return hasEnchants() && enchantments.remove(ench) != null;
    }

    /**
     * Checks if the specified enchantment conflicts with any enchantments in
     * this MetaItem.
     *
     * @param ench enchantment to test
     * @return true if the enchantment conflicts, false otherwise
     */
    boolean hasConflictingEnchant(Enchantment ench) {
        return checkConflictingEnchants(enchantments, ench);
    }

    /**
     * Checks to see if this has a repair penalty
     *
     * @return true if this has a repair penalty
     */
    public boolean hasRepairCost() {
        return repairCost > 0;
    }

    /**
     * Gets the repair penalty
     *
     * @return the repair penalty
     */
    public int getRepairCost() {
        return repairCost;
    }

    /**
     * Sets the repair penalty
     *
     * @param cost repair penalty
     */
    public void setRepairCost(int cost) {
        repairCost = cost;
    }

    public boolean applicableTo(Material type) {
        return type != Material.AIR;
    }

    public boolean isEmpty() {
        return !(hasDisplayName() || hasEnchants() || hasLore() || hasRepairCost() || !unhandledTags.isEmpty() || hideFlag != 0);
    }

    @Override
    public final boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof MetaItem)) {
            return false;
        }
        return ItemFactory.instance().equals(this, (MetaItem) object);
    }

    /**
     * This method is almost as weird as notUncommon.
     * Only return false if your common internals are unequal.
     * Checking your own internals is redundant if you are not common, as notUncommon is meant for checking those 'not common' variables.
     */
    public boolean equalsCommon(MetaItem what) {
        return ((this.hasDisplayName() ? what.hasDisplayName() && this.displayName.equals(what.displayName) : !what.hasDisplayName()))
                && (this.hasEnchants() ? what.hasEnchants() && this.enchantments.equals(what.enchantments) : !what.hasEnchants())
                && (this.hasLore() ? what.hasLore() && this.lore.equals(what.lore) : !what.hasLore())
                && (this.hasRepairCost() ? what.hasRepairCost() && this.repairCost == what.repairCost : !what.hasRepairCost())
                && (this.unhandledTags.equals(what.unhandledTags))
                && (this.hideFlag == what.hideFlag);
    }

    /**
     * This method is a bit weird...
     * Return true if you are a common class OR your uncommon parts are empty.
     * Empty uncommon parts implies the NBT data would be equivalent if both were applied to an item
     */
    public boolean notUncommon(MetaItem meta) {
        return true;
    }

    @Override
    public final int hashCode() {
        return applyHash();
    }

    int applyHash() {
        int hash = 3;
        hash = 61 * hash + (hasDisplayName() ? this.displayName.hashCode() : 0);
        hash = 61 * hash + (hasLore() ? this.lore.hashCode() : 0);
        hash = 61 * hash + (hasEnchants() ? this.enchantments.hashCode() : 0);
        hash = 61 * hash + (hasRepairCost() ? this.repairCost : 0);
        hash = 61 * hash + unhandledTags.hashCode();
        hash = 61 * hash + hideFlag;
        return hash;
    }

    @Override
    public MetaItem clone() {
        try {
            MetaItem clone = (MetaItem) super.clone();
            if (this.lore != null) {
                clone.lore = new ArrayList<>(this.lore);
            }
            if (this.enchantments != null) {
                clone.enchantments = new HashMap<>(this.enchantments);
            }
            clone.hideFlag = this.hideFlag;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public final Map<String, Object> serialize() {
        ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
        map.put(SerializableMeta.TYPE_FIELD, SerializableMeta.classMap.get(getClass()));
        serialize(map);
        return map.build();
    }

    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        if (hasDisplayName()) {
            builder.put(NAME.BUKKIT, displayName);
        }

        if (hasLore()) {
            builder.put(LORE.BUKKIT, ImmutableList.copyOf(lore));
        }

        serializeEnchantments(enchantments, builder, ENCHANTMENTS);

        if (hasRepairCost()) {
            builder.put(REPAIR.BUKKIT, repairCost);
        }

        //TODO: SUPPORT HIDE FLAGS
        /*Set<String> hideFlags = new HashSet<String>();
        for (ItemFlag hideFlagEnum : getItemFlags()) {
            hideFlags.add(hideFlagEnum.name());
        }
        if (!hideFlags.isEmpty()) {
            builder.put(HIDEFLAGS.BUKKIT, hideFlags);
        }*/

        final Map<String, NBTTag> internalTags = new HashMap<>(unhandledTags);
        serializeInternal(internalTags);
        if (!internalTags.isEmpty()) {
            NBTTagCompound internal = new NBTTagCompound();
            for (Map.Entry<String, NBTTag> e : internalTags.entrySet()) {
                internal.setTag(e.getKey(), e.getValue());
            }
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                CompressedStreamTools.writeCompressed(internal, buf);
                builder.put("internal", Base64.encodeBase64String(buf.toByteArray()));
            } catch (IOException ex) {
                Logger.getLogger(MetaItem.class.getName()).log(Level.SEVERE, null, ex);
                //TODO: CHECK JUL
            }
        }

        return builder;
    }

    void serializeInternal(final Map<String, NBTTag> unhandledTags) {
    }

    static void serializeEnchantments(Map<Enchantment, Integer> enchantments, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (enchantments == null || enchantments.isEmpty()) {
            return;
        }

        ImmutableMap.Builder<String, Integer> enchants = ImmutableMap.builder();
        for (Map.Entry<? extends Enchantment, Integer> enchant : enchantments.entrySet()) {
            enchants.put(enchant.getKey().getName(), enchant.getValue());
        }

        builder.put(key.BUKKIT, enchants.build());
    }

    static void safelyAdd(Iterable addFrom, Collection addTo, int maxItemLength) {
        if (addFrom != null) {
            Iterator iterator = addFrom.iterator();

            while (iterator.hasNext()) {
                Object object = iterator.next();
                if (!(object instanceof String)) {
                    if (object != null) {
                        throw new IllegalArgumentException(addFrom + " cannot contain non-string " + object.getClass().getName());
                    }

                    addTo.add("");
                } else {
                    String page = object.toString();
                    if (page.length() > maxItemLength) {
                        page = page.substring(0, maxItemLength);
                    }

                    addTo.add(page);
                }
            }

        }
    }

    static boolean checkConflictingEnchants(Map<Enchantment, Integer> enchantments, Enchantment ench) {
        if (enchantments == null || enchantments.isEmpty()) {
            return false;
        }

        for (Enchantment enchant : enchantments.keySet()) {
            if (enchant.conflictsWith(ench)) {
                return true;
            }
        }

        return false;
    }

    public static Set<String> getHandledTags() {
        synchronized (HANDLED_TAGS) {
            if (HANDLED_TAGS.isEmpty()) {
                HANDLED_TAGS.addAll(Arrays.asList(
                        DISPLAY.NBT,
                        REPAIR.NBT,
                        ENCHANTMENTS.NBT
                        //TODO: ADD MORE
                ));
            }
            return HANDLED_TAGS;
        }
    }

    @SerializableAs("ItemMeta")
    public static class SerializableMeta implements ConfigurationSerializable {
        static final String TYPE_FIELD = "meta-type";

        static final ImmutableMap<Class<? extends MetaItem>, String> classMap;
        static final ImmutableMap<String, Constructor<? extends MetaItem>> constructorMap;

        static {
            classMap = ImmutableMap.<Class<? extends MetaItem>, String>builder()
                    /*TODO
                    .put(CraftMetaBlockState.class, "TILE_ENTITY")
                    .put(BookMeta.class, "BOOK")
                    .put(CraftMetaBookSigned.class, "BOOK_SIGNED")
                    .put(CraftMetaSkull.class, "SKULL")
                    .put(CraftMetaLeatherArmor.class, "LEATHER_ARMOR")
                    .put(CraftMetaMap.class, "MAP")
                    .put(CraftMetaPotion.class, "POTION")
                    .put(CraftMetaEnchantedBook.class, "ENCHANTED")
                    .put(CraftMetaFirework.class, "FIREWORK")
                    .put(CraftMetaCharge.class, "FIREWORK_EFFECT")
                    .put(CraftMetaItem.class, "UNSPECIFIC")*/
                    .build();

            final ImmutableMap.Builder<String, Constructor<? extends MetaItem>> classConstructorBuilder = ImmutableMap.builder();
            for (Map.Entry<Class<? extends MetaItem>, String> mapping : classMap.entrySet()) {
                try {
                    classConstructorBuilder.put(mapping.getValue(), mapping.getKey().getDeclaredConstructor(Map.class));
                } catch (NoSuchMethodException e) {
                    throw new AssertionError(e);
                }
            }
            constructorMap = classConstructorBuilder.build();
        }

        private SerializableMeta() {
        }

        public static MetaItem deserialize(Map<String, Object> map) throws Throwable {
            Validate.notNull(map, "Cannot deserialize null map");

            String type = getString(map, TYPE_FIELD, false);
            Constructor<? extends MetaItem> constructor = constructorMap.get(type);

            if (constructor == null) {
                throw new IllegalArgumentException(type + " is not a valid " + TYPE_FIELD);
            }

            try {
                return constructor.newInstance(map);
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
        }

        public Map<String, Object> serialize() {
            throw new AssertionError();
        }

        static String getString(Map<?, ?> map, Object field, boolean nullable) {
            return getObject(String.class, map, field, nullable);
        }

        static boolean getBoolean(Map<?, ?> map, Object field) {
            Boolean value = getObject(Boolean.class, map, field, true);
            return value != null && value;
        }

        static <T> T getObject(Class<T> clazz, Map<?, ?> map, Object field, boolean nullable) {
            final Object object = map.get(field);

            if (clazz.isInstance(object)) {
                return clazz.cast(object);
            }
            if (object == null) {
                if (!nullable) {
                    throw new NoSuchElementException(map + " does not contain " + field);
                }
                return null;
            }
            throw new IllegalArgumentException(field + "(" + object + ") is not a valid " + clazz);
        }
    }

}
