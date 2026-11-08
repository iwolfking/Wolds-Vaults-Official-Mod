package xyz.iwolfking.woldsvaults.augments.cast;

import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.augments.AbilityTrait;
import xyz.iwolfking.woldsvaults.augments.AugmentAbilities;
import xyz.iwolfking.woldsvaults.augments.AugmentAbility;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.CastResult;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.bundle.BundleLauncher;
import xyz.iwolfking.woldsvaults.augments.effect.ManaRefunds;
import xyz.iwolfking.woldsvaults.augments.effect.RailBolts;
import xyz.iwolfking.woldsvaults.augments.effect.ToggleDiscounts;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Runs a recipe's timeline: each ability step with its augments in order, pausing at Delay, winding up Charge, and
 * throwing Bundled Spells whose payload runs where they land. A step that cannot cast is skipped with an action-bar
 * message and the rest still cast. The table is re-read at every step, so a reload mid-recipe uses the new values,
 * and a recipe whose table disappeared stops.
 */
public final class AugmentCaster {
    private AugmentCaster() {
    }

    /** Casts a parsed, validated recipe from the player. */
    public static void cast(ServerPlayer player, SpellPlan plan) {
        run(player, plan.steps(), 0, CastContext.DIRECT, false);
    }

    /** A Bundled Spell landed: run its payload at the landing point. */
    public static void runPayload(ServerPlayer player, List<SpellPlan.Step> payload, CastContext context) {
        run(player, payload, 0, context, false);
    }

    private static void run(ServerPlayer player, List<SpellPlan.Step> steps, int from, CastContext context, boolean charged) {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            return;
        }
        for (int i = from; i < steps.size(); i++) {
            SpellPlan.Step step = steps.get(i);
            if (step instanceof SpellPlan.Pause pause) {
                int next = i + 1;
                SpellScheduler.schedule(player, pause.ticks(), later -> run(later, steps, next, context, false));
                return;
            }
            if (step instanceof SpellPlan.Bundle bundle) {
                if (SpellPlan.containsCast(bundle.payload())) {
                    BundleLauncher.launch(config, player, bundle, context);
                } else {
                    showMessage(player, "The Bundled Spell carries nothing to cast");
                }
                return;
            }
            SpellPlan.Cast cast = (SpellPlan.Cast) step;
            boolean woundUp = charged && i == from;
            if (!woundUp && cast.count(AugmentType.CHARGE) > 0 && startCharge(player, cast)) {
                int index = i;
                int ticks = Math.max(1, Math.round(config.tuning.chargeSecondsPerGlyph * 20 * cast.count(AugmentType.CHARGE)));
                SpellScheduler.schedule(player, ticks, later -> run(later, steps, index, context, true));
                return;
            }
            castWithAugments(config, player, cast, context);
        }
    }

    private static boolean startCharge(ServerPlayer player, SpellPlan.Cast cast) {
        AugmentAbility ability = cast.ability();
        if (!ability.isUnlocked(AugmentAbility.serverTree(player)) || ability.remainingCooldownTicks(AugmentAbility.serverTree(player)) > 0) {
            return false;
        }
        player.level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.6F, 1.6F);
        showMessage(player, "Charging " + AugmentAbilities.displayName(ability.id()) + "...");
        return true;
    }

    private static void castWithAugments(FocusAugmentsConfig config, ServerPlayer player, SpellPlan.Cast cast, CastContext context) {
        AugmentAbility ability = cast.ability();
        if (config.isDisabled(ability.id())) {
            report(player, ability, CastResult.DISABLED);
            return;
        }
        CastModifiers base = CastModifiers.of(config, cast);
        float splitFactor = (float) Math.pow(config.strength(AugmentType.SHOTGUN), base.shotgunSplits());
        CastModifiers modifiers = base.withDamageFactor(context.damageMultiplier() * splitFactor);
        CastOrigin origin = context.origin();
        CastContext.Claim claim = context.claim(ability);
        if (claim == CastContext.Claim.SKIP) {
            return;
        }
        if (claim == CastContext.Claim.EXTRA) {
            castExtra(config, player, ability, modifiers, origin);
            return;
        }
        List<Vec3> aims = shotgunAims(config, player, origin, modifiers);
        Vec3 aim = aims.get(0);
        CastResult result = ManaRefunds.withRefund(player, modifiers.discountFraction(),
                () -> aimed(player, aim, () -> CastScope.call(player, modifiers, aimedOrigin(origin, aim),
                        () -> ability.cast(player, aimedOrigin(origin, aim)))));
        context.record(ability, result.went());
        afterRail(config, player, ability, modifiers, aim, result);
        if (result == CastResult.SUCCESS) {
            castShotgunCopies(config, player, ability, modifiers, origin, aims);
        }
        report(player, ability, result);
        if (!result.went()) {
            return;
        }
        if (modifiers.discountFraction() > 0 && ability.has(AbilityTrait.TOGGLE)
                && ability.findAbility(player).filter(Ability::isActive).isPresent()) {
            ToggleDiscounts.start(player, ability.id(), modifiers.discountFraction());
        }
        if (modifiers.echo()) {
            CastModifiers echoed = config.tuning.echoKeepsCharge ? modifiers : modifiers.withoutCharge();
            int echoDelay = Math.max(1, Math.round(config.tuning.echoDelaySeconds * 20));
            SpellScheduler.schedule(player, echoDelay, later -> {
                FocusAugmentsConfig current = FocusAugments.config();
                if (current != null) {
                    castExtra(current, later, ability, echoed, origin);
                }
            });
        }
    }

    private static void castExtra(FocusAugmentsConfig config, ServerPlayer player, AugmentAbility ability, CastModifiers modifiers,
                                  @Nullable CastOrigin origin) {
        List<Vec3> aims = shotgunAims(config, player, origin, modifiers);
        CastResult result = castExtraAimed(config, player, ability, modifiers, origin, aims.get(0), modifiers.discountFraction());
        if (result == CastResult.SUCCESS) {
            castShotgunCopies(config, player, ability, modifiers, origin, aims);
        } else if (result != CastResult.UNSUPPORTED) {
            report(player, ability, result);
        }
    }

    private static void castShotgunCopies(FocusAugmentsConfig config, ServerPlayer player, AugmentAbility ability,
                                          CastModifiers modifiers, @Nullable CastOrigin origin, List<Vec3> aims) {
        float refund = 1.0F - config.tuning.shotgunCopyManaShare;
        for (int i = 1; i < aims.size(); i++) {
            castExtraAimed(config, player, ability, modifiers, origin, aims.get(i), refund);
        }
    }

    private static CastResult castExtraAimed(FocusAugmentsConfig config, ServerPlayer player, AugmentAbility ability,
                                             CastModifiers modifiers, @Nullable CastOrigin origin, Vec3 aim, float refundFraction) {
        CastOrigin aimedOrigin = aimedOrigin(origin, aim);
        CastResult result = ManaRefunds.withRefund(player, refundFraction, () -> aimed(player, aim,
                () -> CastScope.call(player, modifiers, aimedOrigin, () -> ability.castExtra(player, aimedOrigin))));
        afterRail(config, player, ability, modifiers, aim, result);
        return result;
    }

    private static void afterRail(FocusAugmentsConfig config, ServerPlayer player, AugmentAbility ability, CastModifiers modifiers,
                                  Vec3 aim, CastResult result) {
        if (result != CastResult.SUCCESS || !ability.has(AbilityTrait.BOLT)) {
            return;
        }
        double extraRadius = RailBolts.extraRadius(modifiers);
        if (extraRadius > 0) {
            RailBolts.widen(player, ability, modifiers, aim, extraRadius, config.tuning.railRange);
        }
    }

    private static List<Vec3> shotgunAims(FocusAugmentsConfig config, ServerPlayer player, @Nullable CastOrigin origin, CastModifiers modifiers) {
        Vec3 base = origin != null && origin.direction().lengthSqr() > 1.0E-6 ? origin.direction() : player.getLookAngle();
        return ShotgunPattern.directions(base, modifiers.shotgunSplits(), config.tuning.shotgunSpreadDegrees);
    }

    private static <T> T aimed(ServerPlayer player, Vec3 aim, Supplier<T> action) {
        return AimOverride.call(player, aim, action);
    }

    @Nullable
    private static CastOrigin aimedOrigin(@Nullable CastOrigin origin, Vec3 aim) {
        return origin == null ? null : new CastOrigin(origin.position(), aim);
    }

    private static void report(ServerPlayer player, AugmentAbility ability, CastResult result) {
        if (result == CastResult.ON_COOLDOWN) {
            int seconds = (int) Math.ceil(ability.remainingCooldownTicks(AugmentAbility.serverTree(player)) / 20.0);
            showMessage(player, AugmentAbilities.displayName(ability.id()) + " is on cooldown (" + seconds + "s)");
            player.level.playSound(null, player.blockPosition(), ModSounds.ABILITY_ON_COOLDOWN, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (result.message() != null) {
            showMessage(player, String.format(result.message(), AugmentAbilities.displayName(ability.id())));
        }
    }

    public static void showMessage(ServerPlayer player, String message) {
        player.displayClientMessage(new TextComponent(message).withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }
}
