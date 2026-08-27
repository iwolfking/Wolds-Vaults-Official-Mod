package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.VaultMod;
import iskallia.vault.container.GreedTraderContainer;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Public entry point for the milestone engine: tier evaluation, reputation awards, persistence
 * marking and client sync. Progress made outside a vault is refused unless the milestone is on
 * {@link #OUTSIDE_VAULT_MILESTONES}.
 */
public class Milestones {
    private static final ThreadLocal<Boolean> EVALUATING_VETERAN = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Map<UUID, Integer> LAST_LUCKY_HIT = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_GATE_FALLBACK_LOG = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_UNRANKED_LOG = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long GATE_FALLBACK_LOG_INTERVAL_MS = 30_000L;

    /** Milestones the in-vault gate skips; every challenge crystal milestone is exempt as well. */
    private static final Set<String> OUTSIDE_VAULT_MILESTONES = Set.of(
            MilestoneIds.WANTED_CRIMINAL,
            MilestoneIds.MASTER_SMITH,
            MilestoneIds.PAL_TRAINER,
            MilestoneIds.ARCHEOLOGIST,
            MilestoneIds.VAULT_VETERAN,
            MilestoneIds.IDONAS_CHAMPION,
            MilestoneIds.PRIEST_OF_VELARA,
            MilestoneIds.TENOS_RIGHT_HAND,
            MilestoneIds.WENDARRS_TIMEKEEPER);

    private Milestones() {
    }

    /** Whether the gate lets progress through: a tracked vault listener, else a dimension lookup. */
    public static boolean isInVault(ServerPlayer player) {
        if (MilestoneVaultState.isTracked(player.getUUID())) {
            return true;
        }
        if (!VaultMod.MOD_ID.equals(player.level.dimension().location().getNamespace())) {
            return false;
        }
        if (ServerVaults.get(player.level).isEmpty()) {
            return false;
        }
        logGateFallback(player);
        return true;
    }

    /** Whether a milestone is one of the out-of-vault exemptions. */
    public static boolean isExemptFromVaultGate(MilestoneDefinition definition) {
        return definition.getChallengeCrystalId() != null || OUTSIDE_VAULT_MILESTONES.contains(definition.getId());
    }

    /** Whether the player has beaten the Herald and joined the greed ladder. Nothing is tracked below it. */
    public static boolean hasJoinedLadder(ServerPlayer player) {
        return PlayerGreedTreeData.get(player.server).getGreedTier(player) >= MilestoneRankLadder.FIRST_RANK;
    }

    private static boolean isGated(ServerPlayer player, MilestoneDefinition definition) {
        if (!isExemptFromVaultGate(definition) && !isInVault(player)) {
            return true;
        }
        if (!hasJoinedLadder(player)) {
            logUnrankedRefusal(player);
            return true;
        }
        return false;
    }

    /** Whether the declared {@link MilestoneCounter} matches the calling mutator; else refused. */
    private static boolean counterIs(MilestoneDefinition definition, MilestoneCounter expected, String operation) {
        if (definition.getCounter() == expected) {
            return true;
        }
        WoldsVaults.LOGGER.error("Milestone '{}' declares counter {} but {}() is a {} operation; the call is ignored",
                definition.getId(), definition.getCounter(), operation, expected);
        return false;
    }

    /** Logs progress being dropped for an unranked player, at most once per player per 30 seconds. */
    private static void logUnrankedRefusal(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Long last = LAST_UNRANKED_LOG.get(player.getUUID());
        if (last != null && now - last < GATE_FALLBACK_LOG_INTERVAL_MS) {
            return;
        }
        LAST_UNRANKED_LOG.put(player.getUUID(), now);
        WoldsVaults.LOGGER.info("Dropping milestone progress for {}: greed rank is 0, so the Herald has not been beaten and the ladder has not been joined.",
                player.getGameProfile().getName());
    }

    /** Logs the dimension fallback firing, at most once per player per 30 seconds. */
    private static void logGateFallback(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Long last = LAST_GATE_FALLBACK_LOG.get(player.getUUID());
        if (last != null && now - last < GATE_FALLBACK_LOG_INTERVAL_MS) {
            return;
        }
        LAST_GATE_FALLBACK_LOG.put(player.getUUID(), now);
        WoldsVaults.LOGGER.info("Milestone vault gate fell back to a dimension lookup for {}: no tracked vault listener, but the player is in vault dimension {}. Progress is being allowed.",
                player.getGameProfile().getName(), player.level.dimension().location());
    }

    /** Adds to an accumulating counter. Progress outside a vault is dropped unless exempt. */
    public static void advance(ServerPlayer player, String milestoneId, long amount) {
        if (player == null || amount <= 0L) {
            return;
        }
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone advance for unknown id '{}' ignored", milestoneId);
            return;
        }
        if (!counterIs(definition, MilestoneCounter.ACCUMULATE, "advance")) {
            return;
        }
        if (isGated(player, definition)) {
            return;
        }
        MilestoneData data = MilestoneData.get(player.server);
        long current = data.getValue(player.getUUID(), milestoneId);
        if (definition.isComplete(current)) {
            return;
        }
        apply(player, data, definition, current + amount);
    }

    /** Adds a fractional amount, carrying the sub-unit remainder; gated before it is banked. */
    public static void advanceFractional(ServerPlayer player, String milestoneId, double amount) {
        if (player == null || amount <= 0.0D) {
            return;
        }
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone advance for unknown id '{}' ignored", milestoneId);
            return;
        }
        if (!counterIs(definition, MilestoneCounter.ACCUMULATE, "advanceFractional")) {
            return;
        }
        if (isGated(player, definition)) {
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

    /** Raises a high-water-mark counter to the given value; a lower value is ignored. */
    public static void reach(ServerPlayer player, String milestoneId, long value) {
        if (player == null || value <= 0L) {
            return;
        }
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone reach for unknown id '{}' ignored", milestoneId);
            return;
        }
        if (!counterIs(definition, MilestoneCounter.HIGHEST, "reach")) {
            return;
        }
        if (isGated(player, definition)) {
            return;
        }
        MilestoneData data = MilestoneData.get(player.server);
        long current = data.getValue(player.getUUID(), milestoneId);
        if (value <= current) {
            return;
        }
        apply(player, data, definition, value);
    }

    /** Records a distinct token and re-derives the counter from the size of the token set. */
    public static void addToken(ServerPlayer player, String milestoneId, String token) {
        if (player == null || token == null) {
            return;
        }
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            WoldsVaults.LOGGER.warn("Milestone token for unknown id '{}' ignored", milestoneId);
            return;
        }
        if (!counterIs(definition, MilestoneCounter.DISTINCT, "addToken")) {
            return;
        }
        if (isGated(player, definition)) {
            return;
        }
        MilestoneData data = MilestoneData.get(player.server);
        if (!data.addToken(player.getUUID(), milestoneId, token)) {
            return;
        }
        apply(player, data, definition, data.getTokens(player.getUUID(), milestoneId).size());
    }

    /** Marks a one-shot milestone as done. Repeat calls are free. */
    public static void complete(ServerPlayer player, String milestoneId) {
        reach(player, milestoneId, 1L);
    }

    /** Drops the transient per-player bookkeeping when a player disconnects. */
    public static void forget(UUID playerId) {
        LAST_LUCKY_HIT.remove(playerId);
        LAST_GATE_FALLBACK_LOG.remove(playerId);
        LAST_UNRANKED_LOG.remove(playerId);
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

    /** Forces a counter to an exact value, awarding every tier crossed. Not vault-gated. */
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

    /** Reputation of every tier of a milestone that is completed but not yet claimed. */
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

    /** Reputation a milestone has already paid out: every tier collected at Mr. Greedy. */
    public static int getClaimedRep(MinecraftServer server, UUID playerId, String milestoneId) {
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            return 0;
        }
        MilestoneData data = MilestoneData.get(server);
        int completed = definition.getCompletedTiers(data.getValue(playerId, milestoneId));
        int claimed = Math.min(data.getClaimedTiers(playerId, milestoneId), completed);
        int reputation = 0;
        for (int tier = 0; tier < claimed; tier++) {
            reputation += definition.getReputation(tier);
        }
        return reputation;
    }

    /**
     * Every point of reputation the player has ever been paid, across all milestones. Milestone
     * claims are the only path that grants reputation, so this is the balance the player would be
     * holding had they never spent any of it on a rank-up.
     */
    public static int getClaimedRep(MinecraftServer server, UUID playerId) {
        int total = 0;
        for (MilestoneDefinition definition : MilestoneRegistry.getAll()) {
            total += getClaimedRep(server, playerId, definition.getId());
        }
        return total;
    }

    /** Total unclaimed reputation across every milestone. */
    public static int getUnclaimedRep(MinecraftServer server, UUID playerId) {
        int total = 0;
        for (MilestoneDefinition definition : MilestoneRegistry.getAll()) {
            total += getUnclaimedRep(server, playerId, definition.getId());
        }
        return total;
    }

    /** Collects one milestone's banked reputation, or 0. Refused unless the trader is open. */
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

    /** Claims every milestone with banked reputation. Trader-gated like {@link #claim}. */
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

    /** The claim itself, without the trader gate and without a client sync. */
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

    /** Lowers the claim mark to {@code tiers}, making those tiers claimable again. */
    public static void clampClaimedTiers(ServerPlayer player, String milestoneId, int tiers) {
        MilestoneData data = MilestoneData.get(player.server);
        if (data.getClaimedTiers(player.getUUID(), milestoneId) > tiers) {
            data.setClaimedTiers(player.getUUID(), milestoneId, tiers);
            data.flush();
        }
    }

    public static Set<String> getPinned(MinecraftServer server, UUID playerId) {
        return MilestoneData.get(server).getPinned(playerId);
    }

    /** Pins a milestone An unknown id returns false. */
    public static boolean pin(ServerPlayer player, String milestoneId) {
        if (milestoneId != null && !MilestoneRegistry.contains(milestoneId)) {
            WoldsVaults.LOGGER.warn("Milestone pin for unknown id '{}' ignored", milestoneId);
            return false;
        }
        MilestoneData data = MilestoneData.get(player.server);
        data.pin(player.getUUID(), milestoneId);
        data.flush();
        MilestoneFlusher.syncAll(player);
        return true;
    }

    /** Unpins a milestone, or clears the pin when the id is null. An unknown id returns false. */
    public static boolean unpin(ServerPlayer player, String milestoneId) {
        if (milestoneId != null && !MilestoneRegistry.contains(milestoneId)) {
            WoldsVaults.LOGGER.warn("Milestone pin for unknown id '{}' ignored", milestoneId);
            return false;
        }
        MilestoneData data = MilestoneData.get(player.server);
        data.unpin(player.getUUID(), milestoneId);
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

    /** "Vault Veteran" tier is the highest T where every other milestone has min(T, its tiers) done. */
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

    /** Counts one lucky hit, deduplicated by the damage event that produced it. */
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

    /** Counts one ancient unique the player has identified. */
    public static void onAncientUniqueIdentified(ServerPlayer player) {
        advance(player, MilestoneIds.ARCHEOLOGIST, 1L);
    }

    /** Counts one max-level companion, keyed on its UUID so repeat calls cannot inflate it. */
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

    /** Marks the vault forge as maxed: proficiency has reached the cap for the player's level. */
    public static void onVaultForgeMaxed(ServerPlayer player) {
        complete(player, MilestoneIds.MASTER_SMITH);
    }

    /** Completes the milestone tracking a greed challenge crystal; one without a milestone is ignored. */
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
