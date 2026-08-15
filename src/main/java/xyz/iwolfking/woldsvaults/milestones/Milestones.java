package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.container.GreedTraderContainer;
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
        announce(player, definition, newTiers, getUnclaimedRep(player.server, playerId, definition.getId()));
        data.flush();
        evaluateVeteran(player, data);
    }

    /**
     * Reputation the player has banked but not yet collected for a milestone: the sum over every
     * tier that is completed but sits above the claimed high-water mark.
     */
    public static int getUnclaimedRep(MinecraftServer server, UUID playerId, String milestoneId) {
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            return 0;
        }
        MilestoneData data = MilestoneData.get(server);
        int completed = definition.getCompletedTiers(data.getValue(playerId, milestoneId));
        int claimed = Math.min(data.getClaimedTiers(playerId, milestoneId), completed);
        int reputation = 0;
        for (int tier = claimed; tier < completed; tier++) {
            reputation += definition.getReputation(tier);
        }
        return reputation;
    }

    /**
     * Total unclaimed reputation across every milestone.
     */
    public static int getUnclaimedRep(MinecraftServer server, UUID playerId) {
        int total = 0;
        for (MilestoneDefinition definition : MilestoneRegistry.getAll()) {
            total += getUnclaimedRep(server, playerId, definition.getId());
        }
        return total;
    }

    /**
     * Collects the banked reputation for one milestone at Mr. Greedy and returns the amount paid
     * out, or 0 when there was nothing to collect. Only legal while the greed trader container is
     * open: the claim is a trader interaction, so a client that sends the message with no trader
     * screen up is refused and logged.
     */
    public static int claim(ServerPlayer player, String milestoneId) {
        if (!(player.containerMenu instanceof GreedTraderContainer)) {
            WoldsVaults.LOGGER.warn("Refused milestone claim '{}' from {}: the greed trader container is not open (menu was {})",
                    milestoneId, player.getGameProfile().getName(), player.containerMenu.getClass().getName());
            return 0;
        }
        int reputation = claimUnchecked(player, milestoneId);
        MilestoneFlusher.syncAll(player);
        return reputation;
    }

    /**
     * Claims every milestone with banked reputation, returning the total paid out. Gated on the
     * trader container exactly like {@link #claim(ServerPlayer, String)}.
     */
    public static int claimAll(ServerPlayer player) {
        if (!(player.containerMenu instanceof GreedTraderContainer)) {
            WoldsVaults.LOGGER.warn("Refused milestone claim-all from {}: the greed trader container is not open (menu was {})",
                    player.getGameProfile().getName(), player.containerMenu.getClass().getName());
            return 0;
        }
        int total = 0;
        for (MilestoneDefinition definition : MilestoneRegistry.getAll()) {
            total += claimUnchecked(player, definition.getId());
        }
        MilestoneFlusher.syncAll(player);
        return total;
    }

    /**
     * The claim itself without the trader gate and without a client sync. Callers are responsible
     * for syncing afterwards; only the debug command may call this directly.
     */
    public static int claimUnchecked(ServerPlayer player, String milestoneId) {
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone claim for unknown id '{}' ignored", milestoneId);
            return 0;
        }
        MilestoneData data = MilestoneData.get(player.server);
        UUID playerId = player.getUUID();
        int completed = definition.getCompletedTiers(data.getValue(playerId, milestoneId));
        int claimed = Math.min(data.getClaimedTiers(playerId, milestoneId), completed);
        if (completed <= claimed) {
            return 0;
        }
        int reputation = 0;
        for (int tier = claimed; tier < completed; tier++) {
            reputation += definition.getReputation(tier);
        }
        data.setClaimedTiers(playerId, milestoneId, completed);
        data.flush();
        if (reputation > 0) {
            PlayerGreedTreeData.get(player.server).addGreedReputation(player, reputation);
            player.displayClientMessage(new TranslatableComponent("milestone.woldsvaults.claimed",
                    new TranslatableComponent(definition.getNameKey()), reputation).withStyle(ChatFormatting.YELLOW), false);
        }
        return reputation;
    }

    /**
     * Debug seam for {@code /wvmilestones set-tier}: moves the claim mark down with the counter so
     * that forcing a milestone back to a lower tier makes those tiers claimable again.
     */
    public static void clampClaimedTiers(ServerPlayer player, String milestoneId, int tiers) {
        MilestoneData data = MilestoneData.get(player.server);
        if (data.getClaimedTiers(player.getUUID(), milestoneId) > tiers) {
            data.setClaimedTiers(player.getUUID(), milestoneId, tiers);
            data.flush();
        }
    }

    public static String getPinned(MinecraftServer server, UUID playerId) {
        return MilestoneData.get(server).getPinned(playerId);
    }

    /**
     * Pins one milestone to the greed screen, or clears the pin when the id is null. Unknown ids
     * are refused so a bad client cannot park an unresolvable id in the save.
     */
    public static boolean setPinned(ServerPlayer player, String milestoneId) {
        if (milestoneId != null && !MilestoneRegistry.contains(milestoneId)) {
            WoldsVaults.LOGGER.warn("Milestone pin for unknown id '{}' ignored", milestoneId);
            return false;
        }
        MilestoneData data = MilestoneData.get(player.server);
        data.setPinned(player.getUUID(), milestoneId);
        data.flush();
        MilestoneFlusher.syncAll(player);
        return true;
    }

    private static void announce(ServerPlayer player, MilestoneDefinition definition, int tier, int unclaimed) {
        TranslatableComponent name = new TranslatableComponent(definition.getNameKey());
        TranslatableComponent message = definition.getTierCount() == 1
                ? new TranslatableComponent("milestone.woldsvaults.completed", name)
                : new TranslatableComponent("milestone.woldsvaults.tier_completed", name, tier, definition.getTierCount());
        player.displayClientMessage(message.withStyle(ChatFormatting.GOLD), false);
        if (unclaimed > 0) {
            player.displayClientMessage(new TranslatableComponent("milestone.woldsvaults.reputation", unclaimed)
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
     * Counts one companion that has reached the max companion level, the level at which its
     * ancient relic slot unlocks. Keyed on the companion's own UUID rather than counted, so that
     * repeat calls for the same companion - re-equips, further experience gains once it is
     * already capped - cannot inflate the counter.
     */
    public static void onCompanionReachedMaxLevel(ServerPlayer player, UUID companionId) {
        if (player == null) {
            return;
        }
        if (companionId == null) {
            WoldsVaults.LOGGER.warn("Companion max level reached for {} but the stack carries no CompanionUUID; not counted",
                    player.getGameProfile().getName());
            return;
        }
        addToken(player, MilestoneIds.PAL_TRAINER, companionId.toString());
    }

    /**
     * Marks the vault forge as maxed: the player's absolute proficiency has reached the cap for
     * their vault level, which is the Grandmaster step of the gear crafting config.
     */
    public static void onVaultForgeMaxed(ServerPlayer player) {
        complete(player, MilestoneIds.MASTER_SMITH);
    }

    /**
     * Completes the milestone that tracks a greed challenge crystal. This is the only reward a
     * finished challenge grants now; the reputation is banked on the milestone and collected at
     * Mr. Greedy. {@code ultra_hard} has no milestone and is silently ignored.
     */
    public static void onChallengeCrystalCompleted(ServerPlayer player, String challengeCrystalId) {
        MilestoneDefinition definition = MilestoneRegistry.getByChallengeCrystal(challengeCrystalId);
        if (definition == null) {
            WoldsVaults.LOGGER.info("Greed challenge crystal '{}' completed by {} but no milestone tracks it",
                    challengeCrystalId, player.getGameProfile().getName());
            return;
        }
        complete(player, definition.getId());
    }
}
