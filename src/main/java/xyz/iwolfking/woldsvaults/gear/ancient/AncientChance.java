package xyz.iwolfking.woldsvaults.gear.ancient;

import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.util.PrestigePowerHelper;
import xyz.iwolfking.woldsvaults.config.AncientsConfig;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.prestige.RelicHunterPrestigePower;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * The identify roll's chance: the rank the gear rolls under (the identifying player's greed tier for black market
 * gear, otherwise the stamped loot rank), the table's breakpoints for that rank, and the player's Relic Hunter
 * multiplier. Gear with neither rank source never rolls.
 */
public final class AncientChance {
    private AncientChance() {
    }

    /** The breakpoint chance of a rank, 0 below the first breakpoint or when the table is not installed. */
    public static float chanceForRank(int rank) {
        AncientsConfig config = Ancients.installed();
        if (config == null) {
            return 0.0F;
        }
        float chance = 0.0F;
        for (AncientsConfig.Breakpoint breakpoint : config.breakpoints) {
            if (rank >= breakpoint.minRank) {
                chance = breakpoint.chance;
            }
        }
        return chance;
    }

    public static boolean isBlackMarketOrigin(VaultGearData data) {
        return data.getFirstValue(ModGearAttributes.BLACK_MARKET_ORIGIN).orElse(false);
    }

    /** Whether the gear has a rank to roll under: black market origin or a stamped loot rank. */
    public static boolean hasRankSource(VaultGearData data) {
        return isBlackMarketOrigin(data) || AncientLootStamp.isStamped(data);
    }

    /** The rank the gear rolls under, 0 when it has no rank source. */
    public static int rankFor(VaultGearData data, ServerPlayer player) {
        if (isBlackMarketOrigin(data)) {
            return PlayerGreedTreeData.get(player.getLevel()).getGreedTier(player);
        }
        return AncientLootStamp.read(data);
    }

    /** Rolls whether an identifying ancient-capable unique becomes ancient; false unless identified by a server player. */
    public static boolean rollIdentify(VaultGearData data, @Nullable Player player, Random random) {
        ResourceLocation uniqueKey = data.getFirstValue(iskallia.vault.init.ModGearAttributes.UNIQUE_ITEM_KEY).orElse(null);
        if (uniqueKey == null || !Ancients.isAncientCapable(uniqueKey)) {
            return false;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            WoldLogger.debug("Skipping ancient roll for {}: identified with no server player, failing closed.", uniqueKey);
            return false;
        }
        int rank = rankFor(data, serverPlayer);
        if (rank <= 0) {
            return false;
        }
        float chance = chanceForRank(rank) * getRelicHunterMultiplier(serverPlayer);
        return chance > 0.0F && random.nextFloat() < chance;
    }

    public static float getRelicHunterMultiplier(ServerPlayer player) {
        if (player.getServer() == null) {
            WoldLogger.error("Relic Hunter multiplier requested for {} with no server; treating it as no bonus.",
                    player.getGameProfile().getName());
            return 1.0F;
        }
        float bonus = 0.0F;
        for (RelicHunterPrestigePower power : PrestigePowerHelper.getPrestigePowersOfType(player, RelicHunterPrestigePower.class)) {
            bonus += power.getChanceMultiplier();
        }
        return 1.0F + bonus;
    }
}
