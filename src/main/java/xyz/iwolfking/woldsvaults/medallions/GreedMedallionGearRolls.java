package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.config.gear.VaultGearTypeConfig;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * The roll type Omega+ gear carries in a medallion vault: loot stamped {@code Omega+} takes the rank's
 * {@code omegaPlusRollType} instead, which decides its Mythic chance when it is identified.
 */
public final class GreedMedallionGearRolls {
    private GreedMedallionGearRolls() {
    }

    /** Restamps every gear stack rolled {@code Omega+} in freshly generated loot with the roll type of the level's medallion. */
    public static void upgradeOmegaPlus(Level level, List<ItemStack> loot) {
        Optional<GreedMedallionTier> tier = GreedMedallionVaultState.get(level);
        if (tier.isEmpty()) {
            return;
        }
        String rollType = tier.get().getOmegaPlusRollType();
        if (rollType == null || rollType.equals(WoldConstants.OMEGA_PLUS_ROLL_TYPE)) {
            return;
        }
        if (ModConfigs.VAULT_GEAR_TYPE_CONFIG.getRollPool(rollType).isEmpty()) {
            WoldLogger.errorOnce("greed-medallion-roll-type-" + rollType, "Greed medallion {} names gear roll type '{}', "
                    + "which the gear roll type config does not define; Omega+ gear keeps Omega+.", tier.get(), rollType);
            return;
        }
        for (ItemStack stack : loot) {
            if (!(stack.getItem() instanceof VaultGearItem)) {
                continue;
            }
            VaultGearData data = VaultGearData.read(stack);
            boolean omegaPlus = data.getFirstValue(ModGearAttributes.GEAR_ROLL_TYPE)
                    .map(WoldConstants.OMEGA_PLUS_ROLL_TYPE::equals)
                    .orElse(false);
            if (omegaPlus) {
                data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, rollType);
                data.write(stack);
            }
        }
    }

    /** The Mythic chance, as a fraction, of gear identified under this rank's Omega+ roll type; empty if that type is undefined. */
    public static OptionalDouble mythicChance(GreedMedallionTier tier) {
        String rollType = tier.getOmegaPlusRollType();
        Optional<VaultGearTypeConfig.RollType> pool = rollType == null || ModConfigs.VAULT_GEAR_TYPE_CONFIG == null
                ? Optional.empty()
                : ModConfigs.VAULT_GEAR_TYPE_CONFIG.getRollPool(rollType);
        if (pool.isEmpty() || pool.get().pool == null || pool.get().pool.getTotalWeight() <= 0) {
            WoldLogger.errorOnce("greed-medallion-roll-type-chance-" + rollType, "Greed medallion {} names gear roll type '{}', "
                    + "which the gear roll type config does not define; its tooltip leaves out the Mythic chance.", tier, rollType);
            return OptionalDouble.empty();
        }
        VaultGearRarity mythic = VaultGearRarity.valueOf("MYTHIC");
        double[] mythicWeight = {0.0D};
        pool.get().pool.forEach((rarity, weight) -> {
            if (rarity == mythic) {
                mythicWeight[0] += weight.doubleValue();
            }
        });
        return OptionalDouble.of(mythicWeight[0] / pool.get().pool.getTotalWeight());
    }
}
