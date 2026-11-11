package xyz.iwolfking.woldsvaults.spellcraft.pool;

import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.CuriosGearItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModItems;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.MagnetItem;
import iskallia.vault.item.gear.IVaultUsesItem;
import iskallia.vault.item.gear.VaultUsesHelper;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.GlyphViewSyncMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A player's glyph sources: the equipped items VH itself counts for the attribute snapshot (intended slot, not broken,
 * not on item cooldown, item level within the player's, curios usable), each with the lines it grants. Arcana items
 * additionally have to be identified spellcraft carriers in a slot they are meant for, so a focus in the main hand gives
 * nothing. Cached per player until VH's snapshot revision, a spellcraft edit, the player's level or the equipment
 * changes. The candidates go to the player's client whenever they change.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GlyphSources {
    private static final int SYNC_TICKS = 4;
    private static final Map<UUID, Cached> CACHE = new HashMap<>();
    private static final Map<UUID, String> SENT = new HashMap<>();
    private static final Set<String> REPORTED = Collections.synchronizedSet(new HashSet<>());

    private GlyphSources() {
    }

    /**
     * One equipped source. {@code key} is the item id for arcana items and the slot for other gear; {@code hand} is set
     * for items held in a hand.
     */
    public record Equipped(ItemStack stack, String key, String name, String slot, @Nullable InteractionHand hand, boolean arcana) {
    }

    private record Cached(int snapshotRevision, int editRevision, long fingerprint, Snapshot snapshot) {
    }

    /** The sources at one moment and the candidates they offer. */
    public static final class Snapshot {
        private final List<Equipped> sources;
        private final List<Equipped> arcana;
        private final Map<String, List<SpellcraftItemData.Line>> own;
        private final List<GlyphCandidate> shared;
        private final List<GlyphCandidate> auto;
        private final Map<String, GlyphView> views = new HashMap<>();

        Snapshot(List<Equipped> sources, Map<String, List<SpellcraftItemData.Line>> own, List<GlyphCandidate> shared, List<GlyphCandidate> auto) {
            this.sources = List.copyOf(sources);
            this.arcana = sources.stream().filter(Equipped::arcana).toList();
            this.own = own;
            this.shared = List.copyOf(shared);
            this.auto = List.copyOf(auto);
        }

        public List<Equipped> sources() {
            return this.sources;
        }

        /** The equipped arcana items whose spells reach the bar, off hand first. */
        public List<Equipped> arcanaItems() {
            return this.arcana;
        }

        /** Every transferable line on every source. */
        public List<GlyphCandidate> shared() {
            return this.shared;
        }

        /** Auto lines: unique glyph lines on gear without arcana, and etching glyph lines on any source. */
        public List<GlyphCandidate> auto() {
            return this.auto;
        }

        @Nullable
        public Equipped arcanaItem(String key) {
            for (Equipped equipped : this.arcana) {
                if (equipped.key().equals(key)) {
                    return equipped;
                }
            }
            return null;
        }

        /**
         * The view of an arcana item, equipped or not (the editor works on items in the inventory): its own lines plus
         * the transferable lines of every equipped source other than itself.
         */
        public GlyphView view(FocusAugmentsConfig config, ItemStack stack) {
            String key = SpellcraftItemData.key(stack);
            Equipped equipped = arcanaItem(key);
            if (equipped != null && equipped.stack() == stack) {
                return this.views.computeIfAbsent(key, k -> GlyphView.of(config, k, equipped.name(), this.own.get(k), this.shared));
            }
            return GlyphView.of(config, key, stack.getHoverName().getString(), GlyphLines.own(stack), this.shared);
        }

        /** The best auto line of each type. */
        public GlyphView autoLines(FocusAugmentsConfig config) {
            return this.views.computeIfAbsent("", k -> GlyphView.best(config, this.auto));
        }
    }

    public static Snapshot get(ServerPlayer player) {
        AttributeSnapshotHelper helper = AttributeSnapshotHelper.getInstance();
        helper.getSnapshot(player);
        int snapshotRevision = helper.getSnapshotRevision(player);
        int editRevision = SpellcraftItemData.revision();
        int level = playerLevel(player);
        long fingerprint = level;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            fingerprint = fingerprint * 31 + System.identityHashCode(stack);
            fingerprint = fingerprint * 2 + (!stack.isEmpty() && player.getCooldowns().isOnCooldown(stack.getItem()) ? 1 : 0);
        }
        Cached cached = CACHE.get(player.getUUID());
        if (cached != null && cached.snapshotRevision() == snapshotRevision && cached.editRevision() == editRevision
                && cached.fingerprint() == fingerprint) {
            return cached.snapshot();
        }
        Snapshot snapshot = collect(player, level);
        CACHE.put(player.getUUID(), new Cached(snapshotRevision, editRevision, fingerprint, snapshot));
        return snapshot;
    }

    private static int playerLevel(ServerPlayer player) {
        return PlayerVaultStatsData.get(player.getLevel()).getVaultStats(player).getVaultLevel();
    }

    private static Snapshot collect(ServerPlayer player, int playerLevel) {
        List<Equipped> sources = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (EquipmentSlot slot : List.of(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!countsInSlot(player, stack, slot, playerLevel)) {
                continue;
            }
            InteractionHand hand = slot == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND
                    : slot == EquipmentSlot.OFFHAND ? InteractionHand.OFF_HAND : null;
            String slotName = slot.getName();
            if (SpellcraftRoller.canCarry(stack)) {
                if (SpellcraftItemData.has(stack) && VaultGearData.read(stack).getState() == VaultGearState.IDENTIFIED) {
                    String key = SpellcraftItemData.key(stack);
                    if (!keys.add(key)) {
                        report("duplicate:" + key, "Spellcraft: two equipped items share the spellcraft id " + key + "; the one in "
                                + slotName + " is ignored as a glyph source");
                        continue;
                    }
                    sources.add(new Equipped(stack, key, stack.getHoverName().getString(), slotName, hand, true));
                }
                continue;
            }
            sources.add(new Equipped(stack, "slot:" + slotName, stack.getHoverName().getString(), slotName, hand, false));
        }
        IntegrationCurios.getCuriosItemStacks(player).forEach((curio, stacks) -> {
            for (Tuple<ItemStack, Integer> entry : stacks) {
                ItemStack stack = entry.getA();
                if (curioCounts(player, stack, curio)) {
                    String slotName = "curio:" + curio + ":" + entry.getB();
                    sources.add(new Equipped(stack, slotName, stack.getHoverName().getString(), slotName, null, false));
                }
            }
        });
        FocusAugmentsConfig config = xyz.iwolfking.woldsvaults.augments.FocusAugments.config();
        Map<String, List<SpellcraftItemData.Line>> own = new LinkedHashMap<>();
        List<GlyphCandidate> shared = new ArrayList<>();
        List<GlyphCandidate> auto = new ArrayList<>();
        for (Equipped source : sources) {
            List<SpellcraftItemData.Line> lines = new ArrayList<>();
            for (GlyphLines.SourceLine line : GlyphLines.of(source.stack(), source.arcana())) {
                lines.add(line.line());
                boolean transferable = line.transferable() || !source.arcana();
                if (transferable) {
                    shared.add(new GlyphCandidate(line.line(), source.key(), source.name()));
                }
                if (line.auto()) {
                    if (config != null && "unique".equals(config.glyph(line.line().type()).glyphClass)) {
                        auto.add(new GlyphCandidate(line.line(), source.key(), source.name()));
                    } else if (config != null) {
                        report("auto:" + line.line().type().id(), "Spellcraft: a glyph provider offered " + line.line().type().id()
                                + " as an auto glyph, but only unique glyphs auto-apply; it is only lent to arcana items");
                    }
                }
            }
            own.put(source.key(), lines);
        }
        return new Snapshot(sources, own, shared, auto);
    }

    private static boolean countsInSlot(ServerPlayer player, ItemStack stack, EquipmentSlot slot, int playerLevel) {
        if (stack.isEmpty() || player.getCooldowns().isOnCooldown(stack.getItem())) {
            return false;
        }
        if (stack.getItem() instanceof VaultGearItem gear && (!gear.isIntendedForSlot(stack, slot) || gear.isBroken(stack))) {
            return false;
        }
        if (stack.getItem() instanceof CuriosGearItem curios && !curios.isIntendedSlot(stack, slot)) {
            return false;
        }
        return !(stack.getItem() instanceof VaultGearItem) || VaultGearData.read(stack).getItemLevel() <= playerLevel;
    }

    private static boolean curioCounts(ServerPlayer player, ItemStack stack, String curio) {
        if (stack.isEmpty() || !AttributeGearData.hasData(stack)) {
            return false;
        }
        if (stack.getItem() instanceof CuriosGearItem curios && !curios.isIntendedSlot(stack, curio)) {
            return false;
        }
        if (stack.is(ModItems.MAGNET) && MagnetItem.isLegacy(stack)) {
            return false;
        }
        return !(stack.getItem() instanceof IVaultUsesItem) || VaultUsesHelper.isUsable(stack, player);
    }

    private static void report(String key, String message) {
        if (REPORTED.add(key.toLowerCase(Locale.ROOT))) {
            WoldsVaults.LOGGER.error(message);
        }
    }

    /** Sends the player's glyph candidates to their client when they changed since the last send. */
    public static void sync(ServerPlayer player) {
        Snapshot snapshot = get(player);
        GlyphViewSyncMessage message = new GlyphViewSyncMessage(snapshot.shared(), snapshot.auto());
        String signature = message.signature();
        if (!signature.equals(SENT.get(player.getUUID()))) {
            SENT.put(player.getUUID(), signature);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player && player.tickCount % SYNC_TICKS == 0) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        CACHE.remove(event.getPlayer().getUUID());
        SENT.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CACHE.clear();
        SENT.clear();
    }
}
