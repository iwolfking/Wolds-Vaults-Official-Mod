package xyz.iwolfking.woldsvaults.gear.ancient;

import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/** Builds unidentified uniques and ancients, and marks gear ancient. */
public final class AncientItems {
    private AncientItems() {
    }

    /**
     * Writes the ancient flag and, when the unique has one, its ancient name. Returns whether the name was written;
     * without it the item keeps its unique name.
     */
    public static boolean markAncient(VaultGearData data, @Nullable ResourceLocation uniqueKey) {
        data.createOrReplaceAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE, true);
        String name = uniqueKey == null ? null : Ancients.getAncientName(uniqueKey).orElse(null);
        if (name == null) {
            return false;
        }
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_NAME, name);
        return true;
    }

    /**
     * An unidentified piece of the unique's own gear item, locked to that unique; it rolls into that unique when
     * identified. The caller runs it through vault loot initialisation. {@link ItemStack#EMPTY} if the unique cannot
     * be built; failures log.
     */
    public static ItemStack createUnidentifiedUnique(ResourceLocation uniqueKey) {
        UniqueGearConfig.Entry entry = ModConfigs.UNIQUE_GEAR.getEntry(uniqueKey).orElse(null);
        if (entry == null) {
            WoldLogger.error("Unique {} has no entry in the unique registry; skipping.", uniqueKey);
            return ItemStack.EMPTY;
        }
        Item item = entry.getItem();
        ItemStack stack = new ItemStack(item);
        if (stack.isEmpty()) {
            WoldLogger.error("Unique {} resolved to an empty base item; skipping.", uniqueKey);
            return ItemStack.EMPTY;
        }
        VaultGearData data = VaultGearData.read(stack);
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, WoldConstants.UNIQUE_ROLL_TYPE);
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_UNIQUE_POOL, ModConfigs.UNIQUE_GEAR.findPoolForUnique(uniqueKey).orElse(UniqueGearConfig.DEFAULT_POOL));
        data.createOrReplaceAttributeValue(ModGearAttributes.UNIQUE_ITEM_KEY, uniqueKey);
        data.write(stack);
        return stack;
    }

    /**
     * An unidentified unique flagged ancient, so identification never rolls the chance. The caller runs it through
     * vault loot initialisation so it takes an item level like any other loot. {@link ItemStack#EMPTY} if the unique
     * cannot be built; failures log.
     */
    public static ItemStack createUnidentifiedAncient(ResourceLocation uniqueKey) {
        ItemStack stack = createUnidentifiedUnique(uniqueKey);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        VaultGearData data = VaultGearData.read(stack);
        data.createOrReplaceAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE, true);
        data.write(stack);
        return stack;
    }

    /** An unidentified ancient of a unique drawn uniformly from every ancient-capable unique, set pieces included. */
    public static ItemStack rollUnidentifiedAncient(Random random) {
        List<ResourceLocation> candidates = Ancients.getAncientCapableUniques();
        if (candidates.isEmpty()) {
            WoldLogger.error("No loaded unique has an ancient name; cannot roll an unidentified ancient unique.");
            return ItemStack.EMPTY;
        }
        return createUnidentifiedAncient(candidates.get(random.nextInt(candidates.size())));
    }

    /** Builds and identifies one named ancient at the given item level; used by the give command. Failures log. */
    public static ItemStack createAncientUnique(ResourceLocation uniqueKey, Player player, int itemLevel) {
        ItemStack stack = createUnidentifiedAncient(uniqueKey);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        VaultGearData data = VaultGearData.read(stack);
        data.setItemLevel(itemLevel);
        data.setRarity(VaultGearRarity.UNIQUE);
        data.write(stack);

        GearRollHelper.initializeGear(stack, player);

        VaultGearData rolled = VaultGearData.read(stack);
        if (!Ancients.isAncient(rolled)) {
            WoldLogger.error("Ancient marker was lost while rolling {}; the item will roll as a normal unique.", uniqueKey);
        }
        if (!rolled.getFirstValue(ModGearAttributes.GEAR_NAME).map(name -> name.startsWith("Ancient")).orElse(false)) {
            WoldLogger.error("Ancient {} identified without its ancient name; writing it after the roll.", uniqueKey);
            markAncient(rolled, uniqueKey);
        }
        rolled.createOrReplaceAttributeValue(ModGearAttributes.IS_LOOT, true);
        rolled.write(stack);
        return stack;
    }
}
