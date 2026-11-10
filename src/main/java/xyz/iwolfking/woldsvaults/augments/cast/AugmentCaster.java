package xyz.iwolfking.woldsvaults.augments.cast;

import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.ability.effect.NecromancyAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.util.calc.ManaCostHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.augments.AbilityTrait;
import xyz.iwolfking.woldsvaults.augments.AugmentAbilities;
import xyz.iwolfking.woldsvaults.augments.AugmentAbility;
import xyz.iwolfking.woldsvaults.augments.CastResult;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.bundle.BundleLauncher;
import xyz.iwolfking.woldsvaults.augments.effect.RailBolts;
import xyz.iwolfking.woldsvaults.augments.effect.ToggleDiscounts;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SlotBilling;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellCooldowns;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Casts an augmented spell slot. The trigger step is a real key press on the player's ability, charged for the whole
 * slot (every ability's mana times the spell's mana factor, and the slot cooldown); if it doesn't go through, nothing
 * else casts. A toggle trigger runs the rest of the spell only when it switches on. Every later step is free: chained abilities, Delays, Bundled Spell payloads where the orb lands, Echo
 * (on-cast effects fire) and Shotgun copies (they don't). The table is re-read at every step.
 */
public final class AugmentCaster {
    private AugmentCaster() {
    }

    /** Casts the slot's spell. {@code chargeShare} is how full Charge got (0-1), or negative when not charged. */
    public static void castSlot(ServerPlayer player, EffectiveBar.Slot slot, float chargeShare) {
        FocusAugmentsConfig config = FocusAugments.config();
        SpellModel model = slot.model();
        if (config == null || model == null || model.steps().isEmpty() || !(model.steps().get(0) instanceof SpellModel.Cast trigger)
                || trigger.spec() == null) {
            return;
        }
        AbilityTree tree = SpellCooldowns.tree(player);
        float otherMana = 0.0F;
        for (String group : model.castGroups()) {
            if (group.equals(trigger.group())) {
                continue;
            }
            Ability ability = SpellCooldowns.ability(tree, group);
            if (ability instanceof InstantManaAbility mana) {
                otherMana += ManaCostHelper.adjustManaCost(player, mana, mana.getManaCost());
            }
        }
        CastScope.Billing billing = CastScope.Billing.trigger(trigger.spec(), otherMana, model.manaFactor(), slot, chargeShare >= 1.0F);
        Ability triggerAbility = SpellCooldowns.ability(tree, trigger.group());
        if (triggerAbility != null && !triggerAbility.isActive() && !SlotBilling.canPayUnbilledTrigger(player, triggerAbility, billing)) {
            player.level.playSound(null, player.blockPosition(), ModSounds.ABILITY_OUT_OF_MANA, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        run(player, model.steps(), 0, null, 1.0F, billing, chargeShare, model);
    }

    /** A Bundled Spell landed: run its payload at the landing point, free. */
    public static void runPayload(ServerPlayer player, List<SpellModel.Step> payload, CastOrigin origin, float damageFactor) {
        run(player, payload, 0, origin, damageFactor, null, -1.0F, null);
    }

    private static void run(ServerPlayer player, List<SpellModel.Step> steps, int from, @Nullable CastOrigin origin, float damageFactor,
                            @Nullable CastScope.Billing trigger, float chargeShare, @Nullable SpellModel model) {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            return;
        }
        CastScope.Billing billing = trigger;
        for (int i = from; i < steps.size(); i++) {
            SpellModel.Step step = steps.get(i);
            if (step instanceof SpellModel.Pause pause && !pause.glyph().active()) {
                continue;
            }
            if (step instanceof SpellModel.Pause pause) {
                int next = i + 1;
                int ticks = Math.max(1, Math.round(pause.glyph().potency() * 20.0F));
                SpellScheduler.schedule(player, ticks, later -> run(later, steps, next, origin, damageFactor, null, chargeShare, model));
                return;
            }
            if (step instanceof SpellModel.Bundle bundle) {
                if (bundle.glyph().active()) {
                    BundleLauncher.launch(config, player, bundle, origin, damageFactor);
                }
                return;
            }
            SpellModel.Cast cast = (SpellModel.Cast) step;
            CastResult result = castStep(config, player, cast, origin, damageFactor, billing, chargeShare, model);
            if (billing != null) {
                if (!result.went() || result == CastResult.STOPPED) {
                    return;
                }
                SlotBilling.payUnbilledTrigger(player, SpellCooldowns.ability(SpellCooldowns.tree(player), cast.group()), billing);
            }
            billing = null;
        }
    }

    private static CastResult castStep(FocusAugmentsConfig config, ServerPlayer player, SpellModel.Cast cast, @Nullable CastOrigin origin,
                                       float damageFactor, @Nullable CastScope.Billing trigger, float chargeShare, @Nullable SpellModel model) {
        AugmentAbility ability = cast.spec() == null ? null : AugmentAbilities.get(config, cast.spec());
        if (ability == null) {
            showMessage(player, cast.group().replace('_', ' ') + " can't be cast by a spell");
            return CastResult.UNSUPPORTED;
        }
        CastModifiers base = CastModifiers.of(config, cast.glyphs()).withDamageFactor(damageFactor);
        CastModifiers modifiers = base;
        if (chargeShare >= 0.0F && base.chargeBonus() > 0.0F) {
            float share = chargeShare < config.tuning.chargeMinShare ? 0.0F : Math.min(1.0F, chargeShare);
            modifiers = base.withDamageFactor(1.0F + base.chargeBonus() * share);
        }
        List<Vec3> aims = ShotgunPattern.fan(baseDirection(player, origin), modifiers.shotgunCopies(), config.tuning.shotgunSpreadDegrees);
        CastModifiers applied = modifiers;
        long volley = aims.size() > 1 ? ShotgunHits.newGroup() : 0L;
        ShotgunHits.Shot nativeShot = volley > 0 ? new ShotgunHits.Shot(volley, 0) : null;
        CastResult result;
        if (trigger != null) {
            result = scoped(player, applied, aimedOrigin(origin, aims.get(0)), trigger, ability.id(), nativeShot, aims.get(0),
                    () -> ability.cast(player, aimedOrigin(origin, aims.get(0))));
        } else {
            result = scoped(player, applied, aimedOrigin(origin, aims.get(0)), CastScope.Billing.free(false), ability.id(), nativeShot,
                    aims.get(0), () -> ability.castExtra(player, aimedOrigin(origin, aims.get(0))));
        }
        afterRail(config, player, ability, applied, aims.get(0), result, nativeShot);
        if (result == CastResult.SUCCESS) {
            castCopies(config, player, ability, applied, origin, aims, volley);
        }
        report(player, ability, result);
        if (!result.went()) {
            return result;
        }
        if (trigger != null && model != null && ability.has(AbilityTrait.TOGGLE) && model.manaFactor() < 1.0F
                && ability.findAbility(player).filter(Ability::isActive).isPresent()) {
            ToggleDiscounts.start(player, ability.id(), 1.0F - model.manaFactor());
        }
        if (base.echoPower() > 0.0F && ability.has(AbilityTrait.INSTANT) && !echoesMinionHits(player, ability)) {
            CastModifiers echoed = base.withDamageFactor(base.echoPower());
            int delay = Math.max(1, Math.round(config.tuning.echoDelaySeconds * 20.0F));
            SpellScheduler.schedule(player, delay, later -> {
                FocusAugmentsConfig current = FocusAugments.config();
                if (current == null) {
                    return;
                }
                List<Vec3> echoAims = ShotgunPattern.fan(baseDirection(later, origin), echoed.shotgunCopies(), current.tuning.shotgunSpreadDegrees);
                long echoVolley = echoAims.size() > 1 ? ShotgunHits.newGroup() : 0L;
                ShotgunHits.Shot echoShot = echoVolley > 0 ? new ShotgunHits.Shot(echoVolley, 0) : null;
                CastResult echo = scoped(later, echoed, aimedOrigin(origin, echoAims.get(0)), CastScope.Billing.free(false), ability.id(),
                        echoShot, echoAims.get(0), () -> ability.castExtra(later, aimedOrigin(origin, echoAims.get(0))));
                afterRail(current, later, ability, echoed, echoAims.get(0), echo, echoShot);
                if (echo == CastResult.SUCCESS) {
                    castCopies(current, later, ability, echoed, origin, echoAims, echoVolley);
                }
            });
        }
        return result;
    }

    /** Echo on Necromancy follows each minion hit instead of recasting the curse. */
    private static boolean echoesMinionHits(ServerPlayer player, AugmentAbility ability) {
        return ability.findAbility(player).filter(NecromancyAbility.class::isInstance).isPresent();
    }

    private static void castCopies(FocusAugmentsConfig config, ServerPlayer player, AugmentAbility ability, CastModifiers modifiers,
                                   @Nullable CastOrigin origin, List<Vec3> aims, long volley) {
        for (int i = 1; i < aims.size(); i++) {
            Vec3 aim = aims.get(i);
            ShotgunHits.Shot shot = new ShotgunHits.Shot(volley, i);
            CastResult copy = scoped(player, modifiers, aimedOrigin(origin, aim), CastScope.Billing.free(true), ability.id(), shot, aim,
                    () -> ability.castExtra(player, aimedOrigin(origin, aim)));
            afterRail(config, player, ability, modifiers, aim, copy, shot);
        }
    }

    private static CastResult scoped(ServerPlayer player, CastModifiers modifiers, @Nullable CastOrigin origin, CastScope.Billing billing,
                                     String spec, @Nullable ShotgunHits.Shot shot, Vec3 aim, Supplier<CastResult> action) {
        return AimOverride.call(player, aim, () -> CastScope.call(player, modifiers, origin, billing, spec, shot, action));
    }

    private static Vec3 baseDirection(ServerPlayer player, @Nullable CastOrigin origin) {
        return origin != null && origin.direction().lengthSqr() > 1.0E-6 ? origin.direction() : player.getLookAngle();
    }

    private static void afterRail(FocusAugmentsConfig config, ServerPlayer player, AugmentAbility ability, CastModifiers modifiers,
                                  Vec3 aim, CastResult result, @Nullable ShotgunHits.Shot shot) {
        if (result != CastResult.SUCCESS || !ability.has(AbilityTrait.BOLT)) {
            return;
        }
        double extraRadius = RailBolts.extraRadius(modifiers);
        if (extraRadius > 0) {
            RailBolts.widen(player, ability, modifiers, aim, extraRadius, config.tuning.railRange, shot);
        }
    }

    @Nullable
    private static CastOrigin aimedOrigin(@Nullable CastOrigin origin, Vec3 aim) {
        return origin == null ? null : new CastOrigin(origin.position(), aim);
    }

    private static void report(ServerPlayer player, AugmentAbility ability, CastResult result) {
        if (result == CastResult.ON_COOLDOWN) {
            player.level.playSound(null, player.blockPosition(), ModSounds.ABILITY_ON_COOLDOWN, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (result.message() != null) {
            showMessage(player, String.format(result.message(), AugmentAbilities.displayName(ability.id())));
        }
    }

    public static void showMessage(ServerPlayer player, String message) {
        player.displayClientMessage(new TextComponent(message).withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }
}
