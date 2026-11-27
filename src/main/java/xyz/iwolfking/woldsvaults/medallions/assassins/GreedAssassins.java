package xyz.iwolfking.woldsvaults.medallions.assassins;

import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;

import java.util.Optional;

/**
 * Identity and tier lookup for greed assassins. The {@code greed_assassin} tag is the only authoritative
 * marker, since the assassin entity types also spawn as ordinary vault mobs, and the medallion rank an
 * assassin was spawned under is stamped into its persistent data.
 */
public final class GreedAssassins {
    public static final String GREED_ASSASSIN_TAG = "greed_assassin";
    private static final String RANK_KEY = "woldsvaults:greed_assassin_rank";

    private GreedAssassins() {
    }

    public static boolean isAssassin(Entity entity) {
        return entity != null && entity.getTags().contains(GREED_ASSASSIN_TAG);
    }

    public static void setTier(Entity entity, GreedMedallionTier tier) {
        entity.getPersistentData().putInt(RANK_KEY, tier.getRankIndex());
    }

    /**
     * The tier stamped on the assassin at spawn, falling back to its vault's medallion when there is no stamp; the
     * fallback logs an error.
     */
    public static Optional<GreedMedallionTier> getTier(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        CompoundTag data = entity.getPersistentData();
        if (data.contains(RANK_KEY)) {
            return GreedMedallionTier.byRankIndex(data.getInt(RANK_KEY));
        }
        WoldLogger.errorOnce("greed-assassin-unstamped", "A greed assassin carries no medallion rank stamp; using its "
                + "vault's medallion instead.");
        return GreedMedallionVaultState.get(entity);
    }

    /**
     * Hostile vault mobs the buffing aura may pick up: alive, not a player, not a player-allied summon, not
     * another assassin.
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
