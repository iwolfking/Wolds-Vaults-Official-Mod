package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.vault.Vault;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Where every greed medallion vault effect resolves its multiplier: {@code 1 + percent/100}, multiplicative with
 * every other source, and 1.0 with no medallion. The vault's medallion comes from {@link GreedMedallionVaultState}.
 */
public final class GreedMedallionEffects {
    private GreedMedallionEffects() {
    }

    public static double multiplier(int percent) {
        return 1.0D + percent / 100.0D;
    }

    public static double mobStatMultiplier(GreedMedallionTier tier) {
        return multiplier(tier.getMobHealthDamageBonus());
    }

    public static double mobStatMultiplier(Vault vault) {
        return GreedMedallionVaultState.get(vault).map(GreedMedallionEffects::mobStatMultiplier).orElse(1.0D);
    }

    public static double mobStatMultiplier(Entity entity) {
        return GreedMedallionVaultState.get(entity).map(GreedMedallionEffects::mobStatMultiplier).orElse(1.0D);
    }

    public static double crateLootMultiplier(Vault vault) {
        return GreedMedallionVaultState.get(vault).map(tier -> multiplier(tier.getCrateLootBonus())).orElse(1.0D);
    }

    public static double chestRollMultiplier(Entity opener) {
        return GreedMedallionVaultState.get(opener).map(tier -> multiplier(tier.getChestRollsBonus())).orElse(1.0D);
    }

    /** The factor on a room spawner's attempts in this level's vault, applied after the vault's own spawn affixes. */
    public static double mobSpawnMultiplier(Level level) {
        return GreedMedallionVaultState.get(level).map(tier -> multiplier(tier.getMobSpawnBonus())).orElse(1.0D);
    }

    /**
     * The strongbox tier of this level's vault as {@code StrongboxTierScaling} counts it (the displayed tier minus one),
     * or -1 when its strongboxes are untiered.
     */
    public static int strongboxTier(Level level) {
        return GreedMedallionVaultState.get(level).map(tier -> tier.getStrongboxTier() - 1).orElse(-1);
    }

    public static double objectiveDifficultyMultiplier(Vault vault) {
        return GreedMedallionVaultState.get(vault).map(tier -> multiplier(tier.getObjectiveDifficultyBonus())).orElse(1.0D);
    }

    /**
     * The trap disarm chance a medallion removes, as a fraction (a -350% medallion returns 3.5); it is subtracted,
     * not multiplied.
     */
    public static float trapDisarmPenalty(Entity entity) {
        return GreedMedallionVaultState.get(entity).map(tier -> tier.getTrapDisarmPenalty() / 100.0F).orElse(0.0F);
    }
}
