package xyz.iwolfking.woldsvaults.armory;

import com.google.gson.JsonObject;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.item.gear.VaultCharmItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.builds.state.PlayerStateCodec;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeView;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A saved armory page's god standing as a read-only {@link GodTreeView}: per god the saved experience, sacrifices,
 * bonus points, reputation, stars and transfer slots, plus the god of the page's saved charm. Piety is a stand-in
 * (reputation and level only, see {@link #basePiety}); every constellation is charted so the saved stars show.
 */
public final class ArmoryGodSnapshot implements GodTreeView {
    private static boolean pietyStandInLogged;

    private final boolean saved;
    @Nullable
    private final VaultGod charmGod;
    private final Map<VaultGod, GodPage> gods;

    private record GodPage(long xp, int sacrifices, int bonusPoints, int reputation, Set<String> nodes,
                           List<String> transfers, Map<String, Integer> ledger) {
    }

    private ArmoryGodSnapshot(boolean saved, @Nullable VaultGod charmGod, Map<VaultGod, GodPage> gods) {
        this.saved = saved;
        this.charmGod = charmGod;
        this.gods = gods;
    }

    /** A page with nothing saved: every god at zero with no stars. */
    public static ArmoryGodSnapshot empty() {
        Map<VaultGod, GodPage> gods = new EnumMap<>(VaultGod.class);
        for (VaultGod god : VaultGod.values()) {
            gods.put(god, new GodPage(0L, 0, 0, 0, Set.of(), List.of(), Map.of()));
        }
        return new ArmoryGodSnapshot(false, null, gods);
    }

    /** The god half of {@code build}, with {@code charm} the page's saved charm (empty when none). */
    public static ArmoryGodSnapshot of(@Nullable SavedBuild build, ItemStack charm) {
        if (build == null) {
            return empty();
        }
        if (!pietyStandInLogged) {
            pietyStandInLogged = true;
            WoldsVaults.LOGGER.warn("Armory: saved builds do not record tree bonus piety; the Gods view shows piety from "
                    + "saved reputation and god level only");
        }
        Map<VaultGod, GodPage> gods = new EnumMap<>(VaultGod.class);
        for (VaultGod god : VaultGod.values()) {
            List<String> nodes = build.godNodes(god);
            gods.put(god, page(god, build.godXp(god), build.godSacrifices(god), bonusPoints(build, god),
                    build.reputation(god), nodes, build.godTransfers(god)));
        }
        VaultGod charmGod = charm.isEmpty() ? null : VaultCharmItem.getGod(charm).orElse(null);
        return new ArmoryGodSnapshot(true, charmGod, gods);
    }

    private static int bonusPoints(SavedBuild build, VaultGod god) {
        JsonObject state = build.state();
        if (!state.has("gods") || !state.get("gods").isJsonObject()) {
            return 0;
        }
        JsonObject gods = state.getAsJsonObject("gods");
        String key = PlayerStateCodec.godKey(god);
        if (!gods.has(key) || !gods.get(key).isJsonObject()) {
            return 0;
        }
        JsonObject json = gods.getAsJsonObject(key);
        return json.has("bonus_points") ? json.get("bonus_points").getAsInt() : 0;
    }

    /** One god's page, with its spent ledger rebuilt from the stars as the game builds it on purchase. */
    private static GodPage page(VaultGod god, long xp, int sacrifices, int bonusPoints, int reputation,
                                List<String> nodeIds, List<String> transfers) {
        Set<String> nodes = new LinkedHashSet<>(nodeIds);
        Map<String, Integer> ledger = new LinkedHashMap<>();
        GodTreeModel tree = GodNodeRegistry.tree(god).orElse(null);
        if (tree == null && !nodes.isEmpty()) {
            WoldsVaults.LOGGER.warn("Armory: the {} god tree is not loaded; its {} saved stars count no points",
                    god.getName(), nodes.size());
        }
        if (tree != null) {
            for (String id : nodes) {
                GodNode node = tree.getNode(id);
                if (node == null) {
                    WoldsVaults.LOGGER.warn("Armory: saved {} star {} does not exist in this game; shown unlearned",
                            god.getName(), id);
                    continue;
                }
                ledger.merge(node.ledgerKey(), node.cost(), Integer::sum);
            }
        }
        return new GodPage(xp, sacrifices, bonusPoints, reputation, Collections.unmodifiableSet(nodes),
                List.copyOf(transfers), Collections.unmodifiableMap(ledger));
    }

    /** Whether the page holds a saved build at all. */
    public boolean saved() {
        return this.saved;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.saved);
        buf.writeBoolean(this.charmGod != null);
        if (this.charmGod != null) {
            buf.writeEnum(this.charmGod);
        }
        for (VaultGod god : VaultGod.values()) {
            GodPage page = this.page(god);
            buf.writeVarLong(page.xp());
            buf.writeVarInt(page.sacrifices());
            buf.writeVarInt(page.bonusPoints());
            buf.writeVarInt(page.reputation());
            writeStrings(buf, new ArrayList<>(page.nodes()));
            writeStrings(buf, page.transfers());
        }
    }

    public static ArmoryGodSnapshot read(FriendlyByteBuf buf) {
        boolean saved = buf.readBoolean();
        VaultGod charmGod = buf.readBoolean() ? buf.readEnum(VaultGod.class) : null;
        Map<VaultGod, GodPage> gods = new EnumMap<>(VaultGod.class);
        for (VaultGod god : VaultGod.values()) {
            long xp = buf.readVarLong();
            int sacrifices = buf.readVarInt();
            int bonus = buf.readVarInt();
            int reputation = buf.readVarInt();
            List<String> nodes = readStrings(buf);
            List<String> transfers = readStrings(buf);
            gods.put(god, page(god, xp, sacrifices, bonus, reputation, nodes, transfers));
        }
        return new ArmoryGodSnapshot(saved, charmGod, gods);
    }

    private static void writeStrings(FriendlyByteBuf buf, List<String> values) {
        buf.writeVarInt(values.size());
        values.forEach(buf::writeUtf);
    }

    private static List<String> readStrings(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            out.add(buf.readUtf());
        }
        return out;
    }

    private GodPage page(VaultGod god) {
        return this.gods.get(god);
    }

    /** Piety from saved reputation and level, as {@code GodAlignmentData.piety} counts them; tree bonus piety is not saved. */
    private int basePiety(VaultGod god) {
        return 10 * this.page(god).reputation() + 20 * this.getLevel(god);
    }

    @Override
    public boolean isLive() {
        return false;
    }

    /** Follows the viewer's live data, so the Load tags refresh when the live tree changes. */
    @Override
    public long revision() {
        return ClientGodAlignmentData.revision();
    }

    @Override
    public long getXp(VaultGod god) {
        return this.page(god).xp();
    }

    @Override
    public int getLevel(VaultGod god) {
        GodPage page = this.page(god);
        return GodLevels.gatedLevel(page.xp(), page.sacrifices());
    }

    @Override
    public int getSacrifices(VaultGod god) {
        return this.page(god).sacrifices();
    }

    @Override
    public int getUnspentPoints(VaultGod god) {
        GodPage page = this.page(god);
        int spent = 0;
        for (int points : page.ledger().values()) {
            spent += points;
        }
        return GodLevels.totalPointsForLevel(this.getLevel(god)) + GodLevels.reputationPoints(page.reputation())
                + page.bonusPoints() - spent;
    }

    @Override
    public int getPiety(VaultGod god) {
        return this.basePiety(god);
    }

    @Override
    public int getReputation(VaultGod god) {
        return this.page(god).reputation();
    }

    @Override
    public Map<String, Integer> getSpentLedger(VaultGod god) {
        return this.page(god).ledger();
    }

    @Override
    public Set<String> getPurchasedTreeNodes(VaultGod god) {
        return this.page(god).nodes();
    }

    @Override
    public boolean isTreeNodePurchased(VaultGod god, String nodeId) {
        return this.page(god).nodes().contains(nodeId);
    }

    @Override
    public Optional<String> getMinorTransferSlot(VaultGod god, int slot) {
        List<String> transfers = this.page(god).transfers();
        if (slot < 0 || slot >= transfers.size() || transfers.get(slot).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(transfers.get(slot));
    }

    @Override
    public int findMinorTransferSlot(VaultGod god, String effectId) {
        if (effectId == null || effectId.isEmpty()) {
            return -1;
        }
        return this.page(god).transfers().indexOf(effectId);
    }

    @Override
    public Optional<VaultGod> charmGod() {
        return Optional.ofNullable(this.charmGod);
    }

    @Override
    public boolean isCharted(VaultGod god) {
        return true;
    }
}
