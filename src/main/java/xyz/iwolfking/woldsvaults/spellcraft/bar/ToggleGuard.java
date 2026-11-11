package xyz.iwolfking.woldsvaults.spellcraft.bar;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.ToggleAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Switches a toggle off when the spell that switched it on changes. Each activation records the bar slot that holds
 * the toggle's group: its occupant key, the spell's tokens and every glyph's potency and state. When the bar resolves
 * again and that slot differs (another item's spell, a moved or removed spell, an edit, a different borrowed potency,
 * a locked slot), the toggle goes through VH's own off path, which starts its cooldown from the slot now holding it.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ToggleGuard {
    private static final Map<UUID, Map<String, String>> ACTIVE = new ConcurrentHashMap<>();

    private ToggleGuard() {
    }

    /** A toggle switched: on, it records the slot signature of its group; off, it forgets it. */
    public static void onToggle(ServerPlayer player, ToggleAbility ability) {
        String group = groupOf(ability);
        if (group == null) {
            return;
        }
        if (!ability.isActive()) {
            forget(player.getUUID(), group);
            return;
        }
        EffectiveBar bar = SpellBars.current(player);
        ACTIVE.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>()).put(group, signature(bar, group));
    }

    /** Called after every bar resolution: switches off each active toggle whose slot changed. */
    static void check(ServerPlayer player, EffectiveBar bar, AbilityTree tree) {
        Map<String, String> active = ACTIVE.get(player.getUUID());
        if (active == null || active.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : new ArrayList<>(active.entrySet())) {
            String group = entry.getKey();
            Ability ability = SpellCooldowns.ability(tree, group);
            if (!(ability instanceof ToggleAbility toggle) || !toggle.isActive()) {
                active.remove(group);
                continue;
            }
            String now = signature(bar, group);
            if (now.equals(entry.getValue())) {
                continue;
            }
            active.remove(group);
            switchOff(player, tree, toggle, group);
        }
        if (active.isEmpty()) {
            ACTIVE.remove(player.getUUID());
        }
    }

    private static void switchOff(ServerPlayer player, AbilityTree tree, ToggleAbility toggle, String group) {
        SpellBars.dispatch(() -> {
            SkillContext context = SkillContext.of(player);
            toggle.setActive(false);
            Ability.ActionResult result = toggle.onAction(context);
            if (result.startCooldown()) {
                toggle.putOnCooldown(result.getCooldownDelayTicks(), context);
            }
            tree.sync(context);
        });
        WoldsVaults.LOGGER.debug("Spellcraft: switched off {} for {}: its spell changed", group, player.getGameProfile().getName());
    }

    private static String signature(@Nullable EffectiveBar bar, String group) {
        int index = bar == null ? -1 : bar.slotOf(group);
        if (index < 0) {
            return "-";
        }
        EffectiveBar.Slot slot = bar.slot(index);
        StringBuilder out = new StringBuilder().append(index).append('|').append(slot.key());
        if (slot.spell() != null) {
            out.append('|').append(String.join(",", slot.spell().tokens()));
        }
        SpellModel model = slot.model();
        if (model != null) {
            for (SpellModel.Glyph glyph : model.glyphs()) {
                out.append('|').append(glyph.token()).append(':').append(glyph.type().id()).append(':')
                        .append(String.format(Locale.ROOT, "%.4f", glyph.potency())).append(':').append(glyph.state());
            }
        }
        return out.toString();
    }

    private static void forget(UUID player, String group) {
        Map<String, String> active = ACTIVE.get(player);
        if (active != null) {
            active.remove(group);
        }
    }

    @Nullable
    private static String groupOf(Ability ability) {
        Skill parent = ability.getParent();
        Skill group = parent == null ? null : parent.getParent();
        return group == null ? null : group.getId();
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }
}
