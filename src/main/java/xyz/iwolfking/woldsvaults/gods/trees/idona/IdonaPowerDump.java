package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.mana.Mana;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.skill.ability.effect.ImplodeAbility;
import iskallia.vault.skill.ability.effect.spi.core.HoldManaAbility;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona.ManaCostHelperAccessor;

import java.util.function.BooleanSupplier;

/**
 * Power Dump: an instant ability cast while sneaking consumes the whole mana bar and multiplies the
 * damage of its own hits by {@code 1 + B x (bar spent above its cost) / max mana}, where the largest
 * bonus is {@code B = per_mana x sqrt(sqrt_ref x max mana)}. Held channels cast while sneaking drain
 * {@code hold_drain_fraction} of the bar per tick and score the drain above their cost. Every Implode
 * takes the bonus for the mana it consumes, sneaking or not. The multiplier lives on the cast's
 * {@link CastTag} and is applied at hurt time.
 */
public final class IdonaPowerDump {
    private IdonaPowerDump() {
    }

    private static boolean active(ServerPlayer player) {
        return IdonaNodes.points(player, IdonaNodes.POWER_DUMP) > 0;
    }

    private static IdonaNodeHandlers.PowerDumpParams params() {
        return IdonaNodeHandlers.params(IdonaNodes.POWER_DUMP, IdonaNodeHandlers.PowerDumpParams.class);
    }

    /** The largest bonus a cast can reach, set by the player's max mana. */
    public static float largestBonus(ServerPlayer player) {
        IdonaNodeHandlers.PowerDumpParams params = params();
        return params.per_mana() * (float) Math.sqrt(params.sqrt_ref() * Math.max(0.0F, Mana.getMax(player)));
    }

    private static float holdDrainFraction() {
        return params().hold_drain_fraction();
    }

    private static float multiplier(ServerPlayer player, float manaAboveCost) {
        float max = Mana.getMax(player);
        if (max <= 0.0F) {
            return 1.0F;
        }
        float share = Math.max(0.0F, Math.min(1.0F, manaAboveCost / max));
        return 1.0F + largestBonus(player) * share;
    }

    /**
     * The mana cost an ability should be checked against. Resolves the running cast's multiplier the
     * first time it is asked; a held channel cast while sneaking costs its drain instead.
     */
    public static float adjustCost(ServerPlayer player, Skill skill, float cost) {
        if (!active(player)) {
            return cost;
        }
        CastTag tag = CastContext.current();
        boolean ownCast = tag != null && tag.isCastOf(player, skill) && !tag.powerDumpResolved();
        if (skill instanceof HoldManaAbility) {
            if (!player.isShiftKeyDown()) {
                return cost;
            }
            float drain = Math.max(cost, holdDrainFraction() * Mana.getMax(player));
            if (ownCast) {
                tag.resolvePowerDump(false, multiplier(player, drain - cost));
            }
            return drain;
        }
        if (!(skill instanceof InstantManaAbility) || !ownCast) {
            return cost;
        }
        float bar = Mana.get(player);
        if (skill instanceof ImplodeAbility) {
            tag.resolvePowerDump(false, multiplier(player, bar));
            tag.setManaConsumed(bar);
            return cost;
        }
        if (!player.isShiftKeyDown()) {
            tag.resolvePowerDump(false, 1.0F);
            return cost;
        }
        if (bar < cost) {
            return cost;
        }
        boolean free = ManaCostHelperAccessor.woldsvaults$rollsEthereal(player);
        tag.resolvePowerDump(true, multiplier(player, free ? bar : bar - cost));
        return cost;
    }

    /** The mana a cast actually pays: a sneaking Power Dump cast pays the whole bar, free casts included. */
    public static float commit(ServerPlayer player, Skill skill, float paid) {
        CastTag tag = CastContext.current();
        if (tag == null || !tag.isCastOf(player, skill) || !tag.dumpsBar()) {
            return paid;
        }
        float bar = Mana.get(player);
        tag.setManaConsumed(bar);
        IdonaState.markSneakDrain(player);
        return Math.max(bar, paid);
    }

    /**
     * Runs a Starforge nova. While sneaking with Power Dump it takes the bonus for the whole bar and
     * then spends it, like a free sneaking cast.
     */
    public static void triggerNova(ServerPlayer player, Runnable nova) {
        if (!active(player) || !player.isShiftKeyDown()) {
            nova.run();
            return;
        }
        float bar = Mana.get(player);
        CastTag tag = new CastTag(player, null);
        tag.resolvePowerDump(true, multiplier(player, bar));
        tag.setManaConsumed(bar);
        CastContext.runTask(tag, nova);
        if (!player.isCreative() && bar > 0.0F) {
            Mana.decrease(player, ManaAction.PLAYER_ACTION, bar);
            IdonaState.markSneakDrain(player);
        }
    }

    /** Whether a toggle that just ran out of mana should stay on, because a sneaking dump emptied the bar. */
    public static boolean inToggleGrace(ServerPlayer player) {
        if (!active(player)) {
            return false;
        }
        return IdonaState.ticksSinceSneakDrain(player) <= params().toggle_grace_ticks();
    }

    /** The Power Dump multiplier for a hit, from the cast it belongs to. */
    public static float damageMultiplier(ServerPlayer player, DamageSource source, BooleanSupplier abilityHit) {
        CastTag tag = CastContext.forHit(player, source);
        if (tag == null) {
            if (active(player) && abilityHit.getAsBoolean()) {
                CastContext.warnUntagged(source);
            }
            return 1.0F;
        }
        return tag.damageMultiplier();
    }
}
