package xyz.iwolfking.woldsvaults.spellcraft.cast;

import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.ability.effect.NecromancyAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.util.calc.ManaCostHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityTrait;
import xyz.iwolfking.woldsvaults.spellcraft.ability.CastOrigin;
import xyz.iwolfking.woldsvaults.spellcraft.ability.CastResult;
import xyz.iwolfking.woldsvaults.spellcraft.ability.SpellAbilities;
import xyz.iwolfking.woldsvaults.spellcraft.ability.SpellAbility;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.buff.ToggleDiscounts;
import xyz.iwolfking.woldsvaults.spellcraft.hit.ShotgunHits;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.RailBolts;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ShotgunPattern;
import xyz.iwolfking.woldsvaults.spellcraft.scope.AimOverride;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastBilling;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastModifiers;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ShotgunShot;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Casts a spell slot. The trigger step is a real key press billed for the whole slot ({@link
 * SlotManaBilling}), and when it does not go through nothing else casts. Every later step is a free extra cast, and a
 * toggle trigger runs them only when it switches on.
 */
public final class SpellCaster {
    private SpellCaster() {
    }

    /** Casts the slot's spell. {@code chargeShare} is how full Charge got (0-1), or negative when not charged. */
    public static void castSlot(ServerPlayer player, EffectiveBar.Slot slot, float chargeShare) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        SpellModel model = slot.model();
        if (config == null || model == null || model.steps().isEmpty() || !(model.steps().get(0) instanceof SpellModel.Cast trigger)
                || trigger.spec() == null) {
            return;
        }
        AbilityTree tree = AbilityLookup.tree(player);
        float otherMana = 0.0F;
        for (String group : model.castGroups()) {
            if (group.equals(trigger.group())) {
                continue;
            }
            Ability ability = AbilityLookup.ability(tree, group);
            if (ability instanceof InstantManaAbility mana) {
                otherMana += ManaCostHelper.adjustManaCost(player, mana, mana.getManaCost());
            }
        }
        CastBilling billing = CastBilling.trigger(trigger.spec(), otherMana, model.manaFactor(),
                slot, chargeShare >= 1.0F);
        Ability triggerAbility = AbilityLookup.ability(tree, trigger.group());
        if (triggerAbility != null && !triggerAbility.isActive() && !SlotManaBilling.canPayUnbilledTrigger(player, triggerAbility, billing)) {
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
                            @Nullable CastBilling trigger, float chargeShare, @Nullable SpellModel model) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null) {
            return;
        }
        CastBilling billing = trigger;
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
                SlotManaBilling.payUnbilledTrigger(player, AbilityLookup.ability(AbilityLookup.tree(player), cast.group()), billing);
            }
            billing = null;
        }
    }

    private static CastResult castStep(SpellcraftConfig config, ServerPlayer player, SpellModel.Cast cast, @Nullable CastOrigin origin,
                                       float damageFactor, @Nullable CastBilling trigger, float chargeShare, @Nullable SpellModel model) {
        SpellAbility ability = cast.spec() == null ? null : SpellAbilities.get(config, cast.spec());
        if (ability == null) {
            SpellMessages.show(player, AbilityLookup.groupName(cast.group()) + " can't be cast by a spell");
            return CastResult.UNSUPPORTED;
        }
        CastModifiers base = CastModifiers.of(config, cast.glyphs()).withDamageFactor(damageFactor);
        CastModifiers modifiers = base;
        if (chargeShare >= 0.0F && base.chargeBonus() > 0.0F) {
            float share = chargeShare < config.tuning.chargeMinShare ? 0.0F : Math.min(1.0F, chargeShare);
            modifiers = base.withDamageFactor(1.0F + base.chargeBonus() * share);
        }
        CastResult result = castVolley(config, player, ability, modifiers, origin, trigger);
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
                SpellcraftConfig current = ModConfigs.SPELLCRAFT;
                if (current == null) {
                    return;
                }
                castVolley(current, later, ability, echoed, origin, null);
            });
        }
        return result;
    }

    private static CastResult castVolley(SpellcraftConfig config, ServerPlayer player, SpellAbility ability, CastModifiers modifiers,
                                         @Nullable CastOrigin origin, @Nullable CastBilling trigger) {
        List<Vec3> aims = ShotgunPattern.fan(baseDirection(player, origin), modifiers.shotgunCopies(), config.tuning.shotgunSpreadDegrees);
        long volley = aims.size() > 1 ? ShotgunHits.newGroup() : 0L;
        ShotgunShot nativeShot = volley > 0 ? new ShotgunShot(volley, 0) : null;
        CastResult result;
        if (trigger != null) {
            result = scoped(player, modifiers, aimedOrigin(origin, aims.get(0)), trigger, ability.id(), nativeShot, aims.get(0),
                    () -> ability.cast(player, aimedOrigin(origin, aims.get(0))));
        } else {
            result = scoped(player, modifiers, aimedOrigin(origin, aims.get(0)), CastBilling.free(false), ability.id(), nativeShot,
                    aims.get(0), () -> ability.castExtra(player, aimedOrigin(origin, aims.get(0))));
        }
        afterRail(config, player, ability, modifiers, aims.get(0), result, nativeShot);
        if (result == CastResult.SUCCESS) {
            castCopies(config, player, ability, modifiers, origin, aims, volley);
        }
        return result;
    }

    private static boolean echoesMinionHits(ServerPlayer player, SpellAbility ability) {
        return ability.findAbility(player).filter(NecromancyAbility.class::isInstance).isPresent();
    }

    private static void castCopies(SpellcraftConfig config, ServerPlayer player, SpellAbility ability, CastModifiers modifiers,
                                   @Nullable CastOrigin origin, List<Vec3> aims, long volley) {
        for (int i = 1; i < aims.size(); i++) {
            Vec3 aim = aims.get(i);
            ShotgunShot shot = new ShotgunShot(volley, i);
            CastResult copy = scoped(player, modifiers, aimedOrigin(origin, aim), CastBilling.free(true), ability.id(), shot, aim,
                    () -> ability.castExtra(player, aimedOrigin(origin, aim)));
            afterRail(config, player, ability, modifiers, aim, copy, shot);
        }
    }

    private static CastResult scoped(ServerPlayer player, CastModifiers modifiers, @Nullable CastOrigin origin, CastBilling billing,
                                     String spec, @Nullable ShotgunShot shot, Vec3 aim, Supplier<CastResult> action) {
        return AimOverride.call(player, aim, () -> CastScope.call(player, modifiers, origin, billing, spec, shot, action));
    }

    static Vec3 baseDirection(ServerPlayer player, @Nullable CastOrigin origin) {
        return origin != null && origin.direction().lengthSqr() > 1.0E-6 ? origin.direction() : player.getLookAngle();
    }

    private static void afterRail(SpellcraftConfig config, ServerPlayer player, SpellAbility ability, CastModifiers modifiers,
                                  Vec3 aim, CastResult result, @Nullable ShotgunShot shot) {
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

    private static void report(ServerPlayer player, SpellAbility ability, CastResult result) {
        if (result == CastResult.ON_COOLDOWN || result == CastResult.FAILED) {
            player.level.playSound(null, player.blockPosition(), ModSounds.ABILITY_ON_COOLDOWN, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (result.message() != null) {
            SpellMessages.show(player, String.format(result.message(), SpellAbilities.displayName(ability.id())));
        }
    }
}
