package xyz.iwolfking.woldsvaults.augments.item;

import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.gear.FocusItem;
import iskallia.vault.item.gear.WandItem;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Rolls augment sockets onto a wand or focus when it is identified. The socket count comes from the table tier for
 * the item level; each socket draws one augment by weight from those whose minimum level the item reaches, with
 * 1 to that augment's {@code maxTokensPerRoll} tokens. Sockets that draw the same augment add up.
 */
public final class AugmentRoller {
    private AugmentRoller() {
    }

    public static boolean canCarry(ItemStack stack) {
        return stack.getItem() instanceof FocusItem || stack.getItem() instanceof WandItem;
    }

    /** Rolls the item's augments once, the first time it is identified with a table installed. */
    public static void rollIfMissing(ItemStack stack, Random random) {
        if (!canCarry(stack) || AugmentItemData.hasRolled(stack)) {
            return;
        }
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            WoldsVaults.LOGGER.error("Focus augments: no table installed; {} was identified without augments", stack.getItem().getRegistryName());
            return;
        }
        AugmentItemData.setRolled(stack, roll(config, VaultGearData.read(stack).getItemLevel(), random));
    }

    public static Map<AugmentType, Integer> roll(FocusAugmentsConfig config, int itemLevel, Random random) {
        Map<AugmentType, Integer> rolled = new EnumMap<>(AugmentType.class);
        FocusAugmentsConfig.SocketTier tier = config.socketTier(itemLevel);
        if (tier == null) {
            return rolled;
        }
        int sockets = tier.minSockets + (tier.maxSockets > tier.minSockets ? random.nextInt(tier.maxSockets - tier.minSockets + 1) : 0);
        for (int i = 0; i < sockets; i++) {
            AugmentType type = draw(config, itemLevel, random);
            if (type == null) {
                WoldsVaults.LOGGER.error("Focus augments: no augment can roll at item level {}; socket left empty", itemLevel);
                break;
            }
            int max = Math.max(1, config.entry(type).maxTokensPerRoll);
            rolled.merge(type, 1 + random.nextInt(max), Integer::sum);
        }
        return rolled;
    }

    private static AugmentType draw(FocusAugmentsConfig config, int itemLevel, Random random) {
        int total = 0;
        for (AugmentType type : AugmentType.values()) {
            FocusAugmentsConfig.AugmentEntry entry = config.entry(type);
            if (entry.minLevel <= itemLevel) {
                total += entry.rollWeight;
            }
        }
        if (total <= 0) {
            return null;
        }
        int pick = random.nextInt(total);
        for (AugmentType type : AugmentType.values()) {
            FocusAugmentsConfig.AugmentEntry entry = config.entry(type);
            if (entry.minLevel > itemLevel) {
                continue;
            }
            pick -= entry.rollWeight;
            if (pick < 0) {
                return type;
            }
        }
        return null;
    }
}
