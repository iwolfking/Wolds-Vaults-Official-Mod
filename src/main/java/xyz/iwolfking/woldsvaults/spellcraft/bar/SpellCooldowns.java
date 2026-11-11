package xyz.iwolfking.woldsvaults.spellcraft.bar;

import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.util.calc.CooldownHelper;
import iskallia.vault.util.calc.CooldownRefundHelper;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AbilityCooldownInvoker;
import xyz.iwolfking.woldsvaults.spellcraft.haste.SpellHaste;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Slot cooldowns. A cast writes its cooldown on the tier objects of every ability group in it, which keeps VH's gating,
 * sync, HUD, cooldown reductions and vault-exit reset working unchanged. A group's remaining time is absolute: it
 * follows the group whatever spell or item casts it next, and a spell waits for its slowest group. Swapping items
 * writes nothing. An augmented spell's cooldown is the longest cooldown of its abilities plus a share of the others,
 * times its glyph cooldown factor, with its Quicken/Discount haste multiplier applied through spell haste. Plain casts
 * keep VH's own cooldown path.
 */
public final class SpellCooldowns {
    private SpellCooldowns() {
    }

    /**
     * VH is about to start an ability's cooldown. Returns true when the spell bar handles it instead: inside a slot
     * cast (the trigger starts the slot cooldown, every other step starts none), or for an ability that sits in an
     * augmented slot (deferred cooldowns such as a toggle switching off).
     */
    public static boolean onPutOnCooldown(Ability ability, int delayTicks, SkillContext context) {
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player == null) {
            return false;
        }
        CastScope.Billing billing = CastScope.billing(player);
        if (billing != null) {
            if (billing.mode() == CastScope.Mode.TRIGGER && billing.slot() != null && billing.triggerSpec() != null
                    && billing.triggerSpec().equals(specId(ability))) {
                start(player, billing.slot(), delayTicks, true);
            }
            return true;
        }
        String group = groupId(ability);
        if (group == null) {
            return false;
        }
        EffectiveBar bar = SpellBars.current(player);
        int index = bar == null ? -1 : bar.slotOf(group);
        if (index < 0 || bar.slot(index).kind() != EffectiveBar.Kind.SPELL) {
            return false;
        }
        start(player, bar.slot(index), delayTicks, true);
        tree(player).sync(context);
        return true;
    }

    /**
     * Starts a spell slot's cooldown on every ability group it holds, computed from the groups that cast. With
     * {@code roll}, skip chance and Quickening apply; a skip skips every group of the cast.
     */
    public static void start(ServerPlayer player, EffectiveBar.Slot slot, int delayTicks, boolean roll) {
        FocusAugmentsConfig config = FocusAugments.config();
        SpellModel model = slot.model();
        if (config == null || model == null) {
            return;
        }
        AbilityTree tree = tree(player);
        Ability trigger = ability(tree, slot.group());
        if (roll && trigger != null && ((AbilityCooldownInvoker) trigger).woldsvaults$trySkipCooldown(player)) {
            player.level.playSound(null, player.blockPosition(), ModSounds.COOLDOWN_RESET, SoundSource.MASTER, 0.5F, 1.0F);
            return;
        }
        int ticks = spellCooldownTicks(config, player, tree, model.castGroups(), model.cooldownBase(), model.glyphHaste());
        int remaining = roll ? CooldownRefundHelper.adjustInitialCooldown(player, ticks) : ticks;
        for (String group : model.groups()) {
            Ability ability = ability(tree, group);
            if (ability != null) {
                ((AbilityCooldownInvoker) ability).woldsvaults$setCooldown(ticks, remaining, delayTicks);
            }
        }
    }

    /**
     * An augmented spell's cooldown in ticks at the player's current haste: the chain (already divided by the player's
     * capped haste) times the base-cooldown cut, times the player's factor over the spell's factor. At least one tick.
     */
    public static int spellCooldownTicks(FocusAugmentsConfig config, ServerPlayer player, AbilityTree tree, Collection<String> groups,
                                         float cooldownBase, float glyphHaste) {
        List<Integer> adjusted = new ArrayList<>();
        for (String group : groups) {
            Ability ability = ability(tree, group);
            if (ability != null) {
                adjusted.add(CooldownHelper.adjustCooldown(player, ability, ability.getCooldownTicks()));
            }
        }
        if (adjusted.isEmpty()) {
            return 0;
        }
        adjusted.sort(Comparator.reverseOrder());
        double chain = adjusted.get(0);
        for (int i = 1; i < adjusted.size(); i++) {
            chain += config.rules.chainCooldownShare * adjusted.get(i);
        }
        return glyphedTicks(chain, cooldownBase, SpellHaste.factor(player, 1.0F),
                SpellHaste.spellFactor(player, glyphHaste, config.rules.glyphHasteHeadroom), glyphHaste);
    }

    /**
     * The chain with the spell's glyphs applied; shared with the client screens. Without the haste table the glyph haste
     * divides the chain directly.
     */
    public static int glyphedTicks(double chain, float cooldownBase, @Nullable Float playerFactor, @Nullable Float spellFactor, float glyphHaste) {
        double ratio = playerFactor != null && spellFactor != null ? playerFactor / spellFactor : 1.0 / Math.max(0.01F, glyphHaste);
        return Math.max(1, (int) Math.round(chain * cooldownBase * ratio));
    }

    /** A group holding up a cast and its remaining ticks (delay included). */
    public record Blocking(String group, int ticks) {
    }

    /**
     * The group with the longest remaining cooldown among {@code groups}, or null when all are ready. Works on the
     * client's synced tree too.
     */
    @Nullable
    public static Blocking blocking(AbilityTree tree, Collection<String> groups) {
        Blocking longest = null;
        for (String group : groups) {
            Ability ability = ability(tree, group);
            if (ability == null || !ability.isTreeOnCooldown()) {
                continue;
            }
            int ticks = ability.getCooldown().map(cooldown -> cooldown.getRemainingTicks() + cooldown.getRemainingDelayTicks()).orElse(0);
            if (longest == null || ticks > longest.ticks()) {
                longest = new Blocking(group, Math.max(1, ticks));
            }
        }
        return longest;
    }

    public static AbilityTree tree(ServerPlayer player) {
        return PlayerAbilitiesData.get(player.getLevel()).getAbilities(player);
    }

    /** The current tier object of an ability group, or null when it isn't learned. */
    @Nullable
    public static Ability ability(AbilityTree tree, String group) {
        Skill skill = tree.getForId(group).orElse(null);
        if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() instanceof TieredSkill tiered
                && tiered.isUnlocked() && tiered.getChild() instanceof Ability ability) {
            return ability;
        }
        return null;
    }

    @Nullable
    private static String specId(Ability ability) {
        Skill parent = ability.getParent();
        return parent == null ? null : parent.getId();
    }

    @Nullable
    private static String groupId(Ability ability) {
        Skill parent = ability.getParent();
        Skill group = parent == null ? null : parent.getParent();
        return group == null ? null : group.getId();
    }
}
