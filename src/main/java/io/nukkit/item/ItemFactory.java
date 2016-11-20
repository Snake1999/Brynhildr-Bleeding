package io.nukkit.item;

import io.nukkit.Server;
import io.nukkit.configuration.serialization.ConfigurationSerialization;
import io.nukkit.item.meta.MetaItem;
import io.nukkit.material.Material;
import org.apache.commons.lang.Validate;

/**
 * An instance of the ItemFactory can be obtained with {@link
 * Server#getItemFactory()}.
 * <p>
 * The ItemFactory is solely responsible for creating item meta containers to
 * apply on item stacks.
 */
public final class ItemFactory {

    private static final ItemFactory instance;

    static {
        instance = new ItemFactory();
        ConfigurationSerialization.registerClass(MetaItem.SerializableMeta.class);
        //TODO ATTRIBUTE?
    }

    private ItemFactory() {
    }

    /**
     * This creates a new item meta for the material.
     *
     * @param material The material to consider as base for the meta
     * @return a new MetaItem that could be applied to an item stack of the
     * specified material
     */
    MetaItem getItemMeta(Material material) {
        Validate.notNull(material, "Material cannot be null");
        return getItemMeta(material, null);
    }

    private MetaItem getItemMeta(Material material, MetaItem meta) {
        return new MetaItem(meta);
        //TODO
        /*switch (material) {
            case AIR:
                return null;
            case WRITTEN_BOOK:
                return meta instanceof CraftMetaBookSigned ? meta : new CraftMetaBookSigned(meta);
            case BOOK_AND_QUILL:
                return meta != null && meta.getClass().equals(CraftMetaBook.class) ? meta : new CraftMetaBook(meta);
            case SKULL_ITEM:
                return meta instanceof CraftMetaSkull ? meta : new CraftMetaSkull(meta);
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                return meta instanceof CraftMetaLeatherArmor ? meta : new CraftMetaLeatherArmor(meta);
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
            case TIPPED_ARROW:
                return meta instanceof CraftMetaPotion ? meta : new CraftMetaPotion(meta);
            case MAP:
                return meta instanceof CraftMetaMap ? meta : new CraftMetaMap(meta);
            case FIREWORK:
                return meta instanceof CraftMetaFirework ? meta : new CraftMetaFirework(meta);
            case FIREWORK_CHARGE:
                return meta instanceof CraftMetaCharge ? meta : new CraftMetaCharge(meta);
            case ENCHANTED_BOOK:
                return meta instanceof CraftMetaEnchantedBook ? meta : new CraftMetaEnchantedBook(meta);
            case BANNER:
                return meta instanceof CraftMetaBanner ? meta : new CraftMetaBanner(meta);
            case FURNACE:
            case CHEST:
            case TRAPPED_CHEST:
            case JUKEBOX:
            case DISPENSER:
            case DROPPER:
            case SIGN:
            case MOB_SPAWNER:
            case NOTE_BLOCK:
            case PISTON_BASE:
            case BREWING_STAND_ITEM:
            case ENCHANTMENT_TABLE:
            case COMMAND:
            case COMMAND_REPEATING:
            case COMMAND_CHAIN:
            case BEACON:
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
            case HOPPER:
            case REDSTONE_COMPARATOR:
            case FLOWER_POT_ITEM:
            case SHIELD:
                return new CraftMetaBlockState(meta, material);
            default:
                return new CraftMetaItem(meta);
        }*/
    }

    /**
     * This method checks the item meta to confirm that it is applicable (no
     * data lost if applied) to the specified ItemStack.
     * <p>
     * A {@link SkullMeta} would not be valid for a sword, but a normal {@link
     * MetaItem} from an enchanted dirt block would.
     *
     * @param meta  Meta to check
     * @param stack Item that meta will be applied to
     * @return true if the meta can be applied without losing data, false
     * otherwise
     * @throws IllegalArgumentException if the meta was not created by this
     *                                  factory
     */
    boolean isApplicable(MetaItem meta, ItemStack stack) throws IllegalArgumentException {
        if (stack == null) {
            return false;
        }
        return isApplicable(meta, stack.getType());
    }

    /**
     * This method checks the item meta to confirm that it is applicable (no
     * data lost if applied) to the specified Material.
     * <p>
     * A {@link SkullMeta} would not be valid for a sword, but a normal {@link
     * MetaItem} from an enchanted dirt block would.
     *
     * @param meta     Meta to check
     * @param material Material that meta will be applied to
     * @return true if the meta can be applied without losing data, false
     * otherwise
     * @throws IllegalArgumentException if the meta was not created by this
     *                                  factory
     */
    boolean isApplicable(MetaItem meta, Material material) throws IllegalArgumentException {
        if (material == null || meta == null) {
            return false;
        }

        return meta.applicableTo(material);
    }

    /**
     * This method is used to compare two item meta data objects.
     *
     * @param meta1 First meta to compare, and may be null to indicate no data
     * @param meta2 Second meta to compare, and may be null to indicate no
     *              data
     * @return false if one of the meta has data the other does not, otherwise
     * true
     * @throws IllegalArgumentException if either meta was not created by this
     *                                  factory
     */
    public boolean equals(MetaItem meta1, MetaItem meta2) throws IllegalArgumentException {
        if (meta1 == meta2) {
            return true;
        }

        if (meta1 == null) {
            return meta2.isEmpty();
        }
        if (meta2 == null) {
            return meta1.isEmpty();
        }

        return meta1.equalsCommon(meta2) && meta1.notUncommon(meta2) && meta2.notUncommon(meta1);
    }

    public static ItemFactory instance() {
        return instance;
    }

    /**
     * Returns an appropriate item meta for the specified stack.
     * <p>
     * The item meta returned will always be a valid meta for a given
     * ItemStack of the specified material. It may be a more or less specific
     * meta, and could also be the same meta or meta type as the parameter.
     * The item meta returned will also always be the most appropriate meta.
     * <p>
     * Example, if a {@link SkullMeta} is being applied to a book, this method
     * would return a {@link BookMeta} containing all information in the
     * specified meta that is applicable to an {@link MetaItem}, the highest
     * common interface.
     *
     * @param meta  the meta to convert
     * @param stack the stack to convert the meta for
     * @return An appropriate item meta for the specified item stack. No
     * guarantees are made as to if a copy is returned. This will be null
     * for a stack of air.
     * @throws IllegalArgumentException if the specified meta was not created
     *                                  by this factory
     */
    MetaItem asMetaFor(final MetaItem meta, final ItemStack stack) throws IllegalArgumentException {
        Validate.notNull(stack, "Stack cannot be null");
        return asMetaFor(meta, stack.getType());
    }

    /**
     * Returns an appropriate item meta for the specified material.
     * <p>
     * The item meta returned will always be a valid meta for a given
     * ItemStack of the specified material. It may be a more or less specific
     * meta, and could also be the same meta or meta type as the parameter.
     * The item meta returned will also always be the most appropriate meta.
     * <p>
     * Example, if a {@link SkullMeta} is being applied to a book, this method
     * would return a {@link BookMeta} containing all information in the
     * specified meta that is applicable to an {@link MetaItem}, the highest
     * common interface.
     *
     * @param meta     the meta to convert
     * @param material the material to convert the meta for
     * @return An appropriate item meta for the specified item material. No
     * guarantees are made as to if a copy is returned. This will be null for air.
     * @throws IllegalArgumentException if the specified meta was not created
     *                                  by this factory
     */
    MetaItem asMetaFor(final MetaItem meta, final Material material) throws IllegalArgumentException {
        Validate.notNull(material, "Material cannot be null");
        if (meta == null) {
            throw new IllegalArgumentException("Meta of " + "null" + " not created by " + ItemFactory.class.getName());
        }
        return getItemMeta(material, meta);
    }
}
