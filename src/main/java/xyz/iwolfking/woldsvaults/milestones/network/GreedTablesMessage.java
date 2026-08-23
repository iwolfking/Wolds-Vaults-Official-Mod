package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.config.GreedRanksConfig;
import xyz.iwolfking.woldsvaults.milestones.MilestoneDefinition;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The greed balance tables, sent once per login alongside the full milestone sync.
 *
 * <p>The client's screens read tier thresholds, per-tier reputation, challenge rank tags and the
 * whole rank ladder to draw claim chips, progress bars, rank badges and the trial plate. All of it
 * is pack data ({@code greed_milestones.json}, {@code gods/greed_ranks.json}), and until this
 * message existed the client read its own copy - so a player whose pack was a version behind, or
 * who edited either file, saw bars and unlock ranks that did not match what the server would
 * actually pay or gate. Nothing here changes who decides anything: the server still judges every
 * claim, rank-up and crystal unlock against its own tables. This only stops the screen lying about
 * them.</p>
 *
 * <p>Structure is not sent. Which milestones exist, what each counts and which crystal each tracks
 * are code, identical on both sides by construction; only the numbers can differ.</p>
 */
public class GreedTablesMessage extends Message<GreedTablesMessage> {
    private static final int MAX_DECODED_MILESTONES = 1024;
    private static final int MAX_DECODED_TIERS = 64;
    private static final int MAX_DECODED_BANDS = 64;
    private static final int MAX_DECODED_RANKS = 256;

    private final Map<String, long[]> thresholds;
    private final Map<String, int[]> reputation;
    private final Map<String, Integer> requiredRanks;
    private final GreedRanksConfig ladder;

    public GreedTablesMessage() {
        this(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), GreedRanksConfig.defaults());
    }

    public GreedTablesMessage(Map<String, long[]> thresholds, Map<String, int[]> reputation,
                              Map<String, Integer> requiredRanks, GreedRanksConfig ladder) {
        this.thresholds = thresholds;
        this.reputation = reputation;
        this.requiredRanks = requiredRanks;
        this.ladder = ladder;
    }

    /**
     * Snapshots the server's live tables. Read straight off the registry and the ladder rather than
     * off the config objects, so what goes on the wire is what the server is actually using -
     * including any milestone that fell back to its shipped numbers because its config entry was
     * refused.
     */
    public static GreedTablesMessage build() {
        Map<String, long[]> thresholds = new LinkedHashMap<>();
        Map<String, int[]> reputation = new LinkedHashMap<>();
        Map<String, Integer> requiredRanks = new LinkedHashMap<>();
        for (MilestoneDefinition definition : MilestoneRegistry.getAll()) {
            long[] tiers = new long[definition.getTierCount()];
            int[] payouts = new int[definition.getTierCount()];
            for (int tier = 0; tier < tiers.length; tier++) {
                tiers[tier] = definition.getThreshold(tier);
                payouts[tier] = definition.getReputation(tier);
            }
            thresholds.put(definition.getId(), tiers);
            reputation.put(definition.getId(), payouts);
            requiredRanks.put(definition.getId(), definition.getRequiredRank());
        }
        return new GreedTablesMessage(thresholds, reputation, requiredRanks, MilestoneRankLadder.snapshot());
    }

    /**
     * Bounds a decoded element count. The count is attacker-controlled and every one of them is
     * followed by that many reads, so it cannot simply be clamped - a clamp would leave the rest of
     * the frame unread and desynchronise the stream. An impossible count is a broken or hostile
     * peer, so the decode is failed and the connection with it.
     */
    private static int bounded(int count, int max, String what) {
        if (count < 0 || count > max) {
            throw new DecoderException("Greed table packet declares " + count + " " + what + "; the limit is " + max);
        }
        return count;
    }

    @Override
    public GreedTablesMessage read(FriendlyByteBuf buffer) {
        int count = bounded(buffer.readVarInt(), MAX_DECODED_MILESTONES, "milestones");
        Map<String, long[]> thresholds = new HashMap<>();
        Map<String, int[]> reputation = new HashMap<>();
        Map<String, Integer> requiredRanks = new HashMap<>();
        for (int index = 0; index < count; index++) {
            String id = buffer.readUtf();
            int tiers = bounded(buffer.readVarInt(), MAX_DECODED_TIERS, "milestone tiers");
            long[] tierThresholds = new long[tiers];
            int[] tierReputation = new int[tiers];
            for (int tier = 0; tier < tiers; tier++) {
                tierThresholds[tier] = buffer.readVarLong();
            }
            for (int tier = 0; tier < tiers; tier++) {
                tierReputation[tier] = buffer.readVarInt();
            }
            thresholds.put(id, tierThresholds);
            reputation.put(id, tierReputation);
            requiredRanks.put(id, buffer.readVarInt());
        }
        int bandSize = buffer.readVarInt();
        int bandCount = bounded(buffer.readVarInt(), MAX_DECODED_BANDS, "rank bands");
        List<String> bandNames = new ArrayList<>(bandCount);
        for (int index = 0; index < bandCount; index++) {
            bandNames.add(buffer.readUtf());
        }
        int rankCount = bounded(buffer.readVarInt(), MAX_DECODED_RANKS, "ranks");
        List<Integer> rankThresholds = new ArrayList<>(rankCount);
        for (int index = 0; index < rankCount; index++) {
            rankThresholds.add(buffer.readVarInt());
        }
        int gateCount = bounded(buffer.readVarInt(), MAX_DECODED_RANKS, "god level gates");
        Map<String, Integer> gates = new LinkedHashMap<>();
        for (int index = 0; index < gateCount; index++) {
            gates.put(buffer.readUtf(), buffer.readVarInt());
        }
        return new GreedTablesMessage(thresholds, reputation, requiredRanks,
                GreedRanksConfig.of(bandSize, bandNames, rankThresholds, gates));
    }

    @Override
    public void write(GreedTablesMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.thresholds.size());
        message.thresholds.forEach((id, tiers) -> {
            buffer.writeUtf(id);
            buffer.writeVarInt(tiers.length);
            for (long threshold : tiers) {
                buffer.writeVarLong(threshold);
            }
            for (int payout : message.reputation.get(id)) {
                buffer.writeVarInt(payout);
            }
            buffer.writeVarInt(message.requiredRanks.getOrDefault(id, 0));
        });
        buffer.writeVarInt(message.ladder.getBandSize());
        buffer.writeVarInt(message.ladder.getBandNames().size());
        message.ladder.getBandNames().forEach(buffer::writeUtf);
        buffer.writeVarInt(message.ladder.getThresholds().size());
        message.ladder.getThresholds().forEach(buffer::writeVarInt);
        buffer.writeVarInt(message.ladder.getGodLevelGates().size());
        message.ladder.getGodLevelGates().forEach((rank, level) -> {
            buffer.writeUtf(rank);
            buffer.writeVarInt(level);
        });
    }

    /**
     * Installs both tables on the client. No client-only class is touched - the registry and the
     * ladder are common code that the server side simply never receives this message on - so there
     * is nothing here to guard with {@code DistExecutor}.
     */
    @Override
    public void onMessage(GreedTablesMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            MilestoneRankLadder.applyServerLadder(message.ladder);
            MilestoneRegistry.applyServerTables(message.thresholds, message.reputation, message.requiredRanks);
        });
        context.get().setPacketHandled(true);
    }
}
