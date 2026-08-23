package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Where every greed medallion vault effect resolves its multiplier: {@code 1 + percent/100}, multiplicative with
 * every other source, and 1.0 with no medallion.
 */
public final class GreedMedallionEffects {
    private GreedMedallionEffects() {
    }

    public static Optional<GreedMedallionTier> of(Vault vault) {
        return GreedMedallionVaultState.get(vault);
    }

    public static Optional<GreedMedallionTier> of(Entity entity) {
        return entity == null ? Optional.empty() : of(entity.getLevel());
    }

    public static Optional<GreedMedallionTier> of(Level level) {
        if (level == null) {
            return Optional.empty();
        }
        return ServerVaults.get(level).flatMap(GreedMedallionVaultState::get);
    }

    public static double multiplier(int percent) {
        return 1.0D + percent / 100.0D;
    }

    public static double mobStatMultiplier(Vault vault) {
        return of(vault).map(tier -> multiplier(tier.getMobHealthDamageBonus())).orElse(1.0D);
    }

    public static double mobStatMultiplier(Entity entity) {
        return of(entity).map(tier -> multiplier(tier.getMobHealthDamageBonus())).orElse(1.0D);
    }

    public static double crateLootMultiplier(Vault vault) {
        return of(vault).map(tier -> multiplier(tier.getCrateLootBonus())).orElse(1.0D);
    }

    public static double chestRollMultiplier(Entity opener) {
        return of(opener).map(tier -> multiplier(tier.getChestRollsBonus())).orElse(1.0D);
    }

    public static double mobSpawnMultiplier(Vault vault) {
        return of(vault).map(tier -> multiplier(tier.getMobSpawnBonus())).orElse(1.0D);
    }

    public static double objectiveDifficultyMultiplier(Vault vault) {
        return of(vault).map(tier -> multiplier(tier.getObjectiveDifficultyBonus())).orElse(1.0D);
    }

    /**
     * The trap disarm chance a medallion removes, as a fraction (a -350% medallion returns 3.5); it is subtracted,
     * not multiplied.
     */
    public static float trapDisarmPenalty(Entity entity) {
        return of(entity).map(tier -> tier.getTrapDisarmPenalty() / 100.0F).orElse(0.0F);
    }
}
