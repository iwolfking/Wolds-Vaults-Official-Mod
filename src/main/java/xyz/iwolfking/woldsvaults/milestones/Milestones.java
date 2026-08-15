package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.UUID;

/**
 * Public entry point for the milestone engine. Every progress source — bus-routed or a direct
 * call from addon-owned code — funnels through here, so tier evaluation, reputation awards,
 * persistence marking and client sync all happen in exactly one place.
 */
public class Milestones {
    private static final ThreadLocal<Boolean> EVALUATING_VETERAN = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Map<UUID, Integer> LAST_LUCKY_HIT = new java.util.concurrent.ConcurrentHashMap<>();

    private Milestones() {
    }

    /**
     * Adds to an accumulating counter. This is the call addon-owned action sites should use.
     */
    public static void advance(ServerPlayer player, String milestoneId, long amount) {
        if (player == null || amount <= 0L) {
            return;
        }
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone advance for unknown id '{}' ignored", milestoneId);
            return;
        }
        MilestoneData data = MilestoneData.get(player.server);
        long current = data.getValue(player.getUUID(), milestoneId);
        if (definition.isComplete(current)) {
            return;
        }
        apply(player, data, definition, current + amount);
    }

    /**
     * Adds a fractional amount to an accumulating counter, carrying the sub-unit remainder so
     * that float-valued progress sources (alchemy) do not systematically round to zero.
     */
    public static void advanceFractional(ServerPlayer player, String milestoneId, double amount) {
        if (player == null || amount <= 0.0D) {
            return;
        }
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone advance for unknown id '{}' ignored", milestoneId);
            return;
        }
        MilestoneData data = MilestoneData.get(player.server);
        if (definition.isComplete(data.getValue(player.getUUID(), milestoneId))) {
            return;
        }
        long whole = data.addFraction(player.getUUID(), milestoneId, amount);
        if (whole > 0L) {
            advance(player, milestoneId, whole);
        }
    }

    /**
     * Raises a high-water-mark counter (god levels, Vault Veteran) to the given value.
     */
    public static void reach(ServerPlayer player, String milestoneId, long value) {
        if (player == null || value <= 0L) {
            return;
        }
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone reach for unknown id '{}' ignored", milestoneId);
            return;
        }
        MilestoneData data = MilestoneData.get(player.server);
        long current = data.getValue(player.getUUID(), milestoneId);
        if (value <= current) {
            return;
        }
        apply(player, data, definition, value);
    }

    /**
     * Records a distinct token (a vault objective key for "Seen It All") and re-derives the
     * counter from the size of the token set.
     */
    public static void addToken(ServerPlayer player, String milestoneId, String token) {
        if (player == null || token == null) {
            return;
        }
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone token for unknown id '{}' ignored", milestoneId);
            return;
        }
        MilestoneData data = MilestoneData.get(player.server);
        if (!data.addToken(player.getUUID(), milestoneId, token)) {
            return;
        }
        apply(player, data, definition, data.getTokens(player.getUUID(), milestoneId).size());
    }

    /**
     * Marks a one-shot milestone as done. Repeat calls are free.
     */
    public static void complete(ServerPlayer player, String milestoneId) {
        reach(player, milestoneId, 1L);
    }

    /**
     * Drops the transient per-player bookkeeping when a player disconnects.
     */
    public static void forget(UUID playerId) {
        LAST_LUCKY_HIT.remove(playerId);
    }

    public static long getValue(ServerPlayer player, String milestoneId) {
        return MilestoneData.get(player.server).getValue(player.getUUID(), milestoneId);
    }

    public static long getValue(MinecraftServer server, UUID playerId, String milestoneId) {
        return MilestoneData.get(server).getValue(playerId, milestoneId);
    }

    public static int getCompletedTiers(MinecraftServer server, UUID playerId, String milestoneId) {
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            return 0;
        }
        return definition.getCompletedTiers(MilestoneData.get(server).getValue(playerId, milestoneId));
    }

    /**
     * Debug seam for the {@code /wvmilestones set} command: forces a counter to an exact value,
     * awarding every tier that the jump crosses.
     */
    public static void setExact(ServerPlayer player, String milestoneId, long value) {
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            return;
        }
        MilestoneData data = MilestoneData.get(player.server);
        long current = data.getValue(player.getUUID(), milestoneId);
        if (value <= current) {
            data.setValue(player.getUUID(), milestoneId, value);
            data.flush();
            return;
        }
        apply(player, data, definition, value);
    }

    private static void apply(ServerPlayer player, MilestoneData data, MilestoneDefinition definition, long value) {
        UUID playerId = player.getUUID();
        long previous = data.getValue(playerId, definition.getId());
        if (value <= previous) {
            return;
        }
        long capped = Math.min(value, definition.getFinalThreshold());
        data.setValue(playerId, definition.getId(), capped);

        int previousTiers = definition.getCompletedTiers(previous);
        int newTiers = definition.getCompletedTiers(capped);
        if (newTiers <= previousTiers) {
            return;
        }
        int reputation = 0;
        for (int tier = previousTiers; tier < newTiers; tier++) {
            reputation += definition.getReputation(tier);
        }
        if (reputation > 0) {
            PlayerGreedTreeData.get(player.server).addGreedReputation(player, reputation);
        }
        announce(player, definition, newTiers, reputation);
        data.flush();
        evaluateVeteran(player, data);
    }

    private static void announce(ServerPlayer player, MilestoneDefinition definition, int tier, int reputation) {
        TranslatableComponent name = new TranslatableComponent(definition.getNameKey());
        TranslatableComponent message = definition.getTierCount() == 1
                ? new TranslatableComponent("milestone.woldsvaults.completed", name)
                : new TranslatableComponent("milestone.woldsvaults.tier_completed", name, tier, definition.getTierCount());
        player.displayClientMessage(message.withStyle(ChatFormatting.GOLD), false);
        if (reputation > 0) {
            player.displayClientMessage(new TranslatableComponent("milestone.woldsvaults.reputation", reputation)
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    /**
     * "Vault Veteran" is derived: its tier is the highest T for which every other milestone has
     * completed at least min(T, that milestone's tier count) tiers, so single-tier milestones only
     * ever have to be finished once.
     */
    private static void evaluateVeteran(ServerPlayer player, MilestoneData data) {
        if (EVALUATING_VETERAN.get()) {
            return;
        }
        MilestoneDefinition veteran = MilestoneRegistry.get(MilestoneIds.VAULT_VETERAN);
        if (veteran == null) {
            return;
        }
        Map<String, Long> values = data.getAllValues(player.getUUID());
        int reached = 0;
        for (int tier = 1; tier <= veteran.getTierCount(); tier++) {
            boolean satisfied = true;
            for (MilestoneDefinition other : MilestoneRegistry.getAll()) {
                if (other == veteran) {
                    continue;
                }
                int required = Math.min(tier, other.getTierCount());
                if (other.getCompletedTiers(values.getOrDefault(other.getId(), 0L)) < required) {
                    satisfied = false;
                    break;
                }
            }
            if (!satisfied) {
                break;
            }
            reached = tier;
        }
        if (reached <= 0) {
            return;
        }
        EVALUATING_VETERAN.set(Boolean.TRUE);
        try {
            long current = data.getValue(player.getUUID(), MilestoneIds.VAULT_VETERAN);
            if (reached > current) {
                apply(player, data, veteran, reached);
            }
        } finally {
            EVALUATING_VETERAN.set(Boolean.FALSE);
        }
    }

    /**
     * Counts one lucky hit, deduplicated by the damage event that produced it: a single lucky hit
     * fans out to every unlocked lucky-hit talent, and all of them route here.
     */
    public static void onLuckyHit(ServerPlayer player, Object damageEvent) {
        if (player == null || damageEvent == null) {
            return;
        }
        int token = System.identityHashCode(damageEvent);
        Integer previous = LAST_LUCKY_HIT.put(player.getUUID(), token);
        if (previous != null && previous == token) {
            return;
        }
        advance(player, MilestoneIds.FIVE_LEAF_CLOVER, 1L);
    }

    /**
     * Ancient uniques do not exist yet; this is the seam their identify path will call once they
     * ship. Registered as a counter today so saves written now stay forward compatible.
     */
    public static void onAncientUniqueIdentified(ServerPlayer player) {
        advance(player, MilestoneIds.ARCHEOLOGIST, 1L);
    }

    /**
     * No companion levelling system exists in the addon yet. The companion system should call
     * this once per companion that first reaches level 10.
     */
    public static void onCompanionReachedMaxLevel(ServerPlayer player) {
        advance(player, MilestoneIds.PAL_TRAINER, 1L);
    }

    /**
     * The vault forge exposes no reachable "fully upgraded" state from the addon today. Whatever
     * ships that state should call this once.
     */
    public static void onVaultForgeMaxed(ServerPlayer player) {
        complete(player, MilestoneIds.MASTER_SMITH);
    }
}
