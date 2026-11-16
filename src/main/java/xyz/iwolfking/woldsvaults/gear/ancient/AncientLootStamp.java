package xyz.iwolfking.woldsvaults.gear.ancient;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The loot rank: the medallion rank of the vault a unique was generated in, written onto unidentified unique gear
 * when vault loot is initialised and read when it is identified.
 */
public final class AncientLootStamp {
    private AncientLootStamp() {
    }

    /**
     * Writes the vault's medallion rank onto unique roll type gear. Nothing is written outside a vault, without a
     * medallion, on other gear, or on gear that already carries a loot rank. Returns the same stack.
     */
    public static ItemStack stamp(ItemStack stack, @Nullable Vault vault) {
        if (vault == null || stack.isEmpty() || !(stack.getItem() instanceof VaultGearItem)) {
            return stack;
        }
        int rank = GreedMedallionVaultState.get(vault).map(GreedMedallionTier::getRankIndex).orElse(0);
        if (rank < 1) {
            return stack;
        }
        VaultGearData data = VaultGearData.read(stack);
        boolean unique = data.getFirstValue(iskallia.vault.init.ModGearAttributes.GEAR_ROLL_TYPE)
                .map(WoldConstants.UNIQUE_ROLL_TYPE::equals).orElse(false);
        if (!unique || data.hasAttribute(ModGearAttributes.LOOT_RANK)) {
            return stack;
        }
        data.createOrReplaceAttributeValue(ModGearAttributes.LOOT_RANK, rank);
        data.write(stack);
        return stack;
    }

    /** Stamps every stack of a loot list in place; returns the same list. */
    public static List<ItemStack> stampAll(List<ItemStack> stacks, @Nullable Vault vault) {
        if (vault != null) {
            stacks.forEach(stack -> stamp(stack, vault));
        }
        return stacks;
    }

    /** The stamped loot rank, 0 when the gear carries none. */
    public static int read(VaultGearData data) {
        return data.getFirstValue(ModGearAttributes.LOOT_RANK).orElse(0);
    }

    public static boolean isStamped(VaultGearData data) {
        return read(data) > 0;
    }
}
