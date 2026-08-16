package xyz.iwolfking.woldsvaults.medallions.assassins;

import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;

import java.util.Optional;

/**
 * Identity and tier lookup for greed assassins under the medallion rework.
 *
 * <p>The {@code greed_assassin} entity tag is the only authoritative marker: the six assassin
 * entity types also spawn as ordinary vault mobs, so type or entity-group checks would catch
 * bystanders. The medallion rank the assassin was spawned under is stamped into the entity's
 * persistent data, which survives save/load and outlives the in-memory vault state, so every later
 * behaviour gate reads the tier the assassin was actually born with.</p>
 */
public final class GreedAssassins {
    public static final String GREED_ASSASSIN_TAG = "greed_assassin";
    private static final String RANK_KEY = "woldsvaults:greed_assassin_rank";

    private GreedAssassins() {
    }

    public static boolean isAssassin(Entity entity) {
        return entity != null && entity.getTags().contains(GREED_ASSASSIN_TAG);
    }

    /**
     * The medallion applied to the vault this level belongs to, or empty for any level that is not
     * a medallion-bearing vault. This is the single spawn gate: no medallion, no assassins.
     */
    public static Optional<GreedMedallionTier> medallionOf(Level level) {
        return VaultUtils.getVault(level).flatMap(GreedMedallionVaultState::get);
    }

    public static void setTier(Entity entity, GreedMedallionTier tier) {
        entity.getPersistentData().putInt(RANK_KEY, tier.getRankIndex());
    }

    /**
     * The tier stamped on the assassin at spawn, falling back to the vault's live medallion state
     * for assassins that predate the stamp or were spawned by other means.
     */
    public static Optional<GreedMedallionTier> getTier(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        CompoundTag data = entity.getPersistentData();
        if (data.contains(RANK_KEY)) {
            return GreedMedallionTier.byRankIndex(data.getInt(RANK_KEY));
        }
        return medallionOf(entity.level);
    }

    /**
     * Hostile vault mobs the buffing aura is allowed to pick up: living, alive, not a player, not a
     * player-allied summon, and not another assassin.
     */
    public static boolean isAuraTarget(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }
        if (living instanceof Player || living instanceof EternalEntity || living instanceof PetEntity) {
            return false;
        }
        return !isAssassin(living);
    }
}
