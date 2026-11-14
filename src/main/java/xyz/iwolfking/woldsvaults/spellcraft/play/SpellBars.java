package xyz.iwolfking.woldsvaults.spellcraft.play;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBarData;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The spell bar service: resolves each player's effective bar, keeps the tree's selected ability on the selected slot
 * and syncs the bar to the client. VH code that reads the tree's selection (Vein Miner, the HUD tray) keeps working
 * because the selection follows the slot.
 */
public final class SpellBars {
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static boolean dispatching;

    private SpellBars() {
    }

    private static final class State {
        EffectiveBar bar;
        String[] keys;
        String signature;
    }

    /** True while the bar is driving VH's tree itself, so the tree mixins let the call through. */
    public static boolean isDispatching() {
        return dispatching;
    }

    @Nullable
    public static EffectiveBar current(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state == null ? refresh(player) : state.bar;
    }

    static SpellBarData.PlayerBar saved(ServerPlayer player) {
        return SpellBarData.get(player.getServer()).bar(player.getUUID());
    }

    static void select(ServerPlayer player, SpellBarData.PlayerBar saved, int index) {
        saved.selected = index;
        SpellBarData.get(player.getServer()).setDirty();
    }

    /**
     * Resolves the player's effective bar from their equipped sources and brings held keys, the selection and the
     * client up to date.
     */
    public static EffectiveBar refresh(ServerPlayer player) {
        SpellBarData.PlayerBar saved = saved(player);
        AbilityTree tree = AbilityLookup.tree(player);
        if (!saved.migrated) {
            DefaultBar.migrate(player, saved, tree);
        }
        EffectiveBar bar = EffectiveBar.forPlayer(player, saved.defaults, AbilityLookup.specOf(tree));
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        String[] keys = new String[SpellcraftItemData.SLOTS];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = bar.slot(i).occupantKey();
        }
        HeldSlots.cancelChanged(player, state.keys, keys);
        state.keys = keys;
        state.bar = bar;
        if (bar.slot(saved.selected).kind() == EffectiveBar.Kind.EMPTY) {
            int first = firstFilled(bar, saved.selected, 1);
            if (first >= 0) {
                select(player, saved, first);
            }
        }
        followSelection(player, tree, bar.slot(saved.selected));
        ToggleGuard.check(player, bar, tree);
        SpellBarSyncMessage message = SpellBarSyncMessage.of(saved, bar);
        String signature = message.signature();
        if (!Objects.equals(signature, state.signature)) {
            state.signature = signature;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
        return bar;
    }

    static int firstFilled(EffectiveBar bar, int from, int direction) {
        for (int step = 1; step <= SpellcraftItemData.SLOTS; step++) {
            int index = Math.floorMod(from + step * direction, SpellcraftItemData.SLOTS);
            if (bar.slot(index).kind() != EffectiveBar.Kind.EMPTY) {
                return index;
            }
        }
        return -1;
    }

    static void followSelection(ServerPlayer player, AbilityTree tree, EffectiveBar.Slot slot) {
        if (slot.kind() == EffectiveBar.Kind.EMPTY || slot.group().isEmpty()) {
            return;
        }
        SpecializedSkill selected = tree.getSelected();
        if (selected == null || !selected.getId().equals(slot.group())) {
            Skill skill = tree.getForId(slot.group()).orElse(null);
            if (skill instanceof SpecializedSkill specialized && specialized.isUnlocked()) {
                dispatch(() -> tree.onQuickSelect(slot.group(), SkillContext.of(player)));
            }
        }
    }

    static void dispatch(Runnable action) {
        boolean outer = dispatching;
        dispatching = true;
        try {
            action.run();
        } finally {
            dispatching = outer;
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        HeldSlots.forget(event.getPlayer().getUUID());
        StatMirror.forget(event.getPlayer().getUUID());
        STATES.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HeldSlots.clear();
        StatMirror.clear();
        STATES.clear();
    }
}
