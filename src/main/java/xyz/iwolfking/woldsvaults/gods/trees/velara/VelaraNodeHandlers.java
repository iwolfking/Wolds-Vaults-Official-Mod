package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectAvoidanceListGearAttribute;
import iskallia.vault.init.ModGearAttributes;

import net.minecraft.world.effect.MobEffect;

import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.gods.node.ListenerBoundHandler;
import xyz.iwolfking.woldsvaults.gods.node.StatContributor;

import java.util.List;

/**
 * Every handler type the Velara tree owns, and the typed parameters each one reads.
 *
 * <p>The twelve plain stat effects bind the shared {@code gear_attribute_scaled} type and Pious
 * Devotion binds the shared {@code piety} type, so neither is named here. What is left is one
 * type per behavioural effect: the two that are pure stat contributions implement
 * {@link StatContributor}, and the rest bind {@link ListenerBoundHandler}, meaning their numbers
 * live in config and are validated at load while their behaviour is still reached through Velara's own
 * ordered listeners - the damage stage, the player-stat bus, the downed hooks - because the
 * capability seams those effects would need do not exist yet.
 *
 * <p>Params component names are the config keys verbatim, including their underscores. The codec
 * binds by component name, so renaming one here silently means renaming it in every god's config
 * file too.
 */
public final class VelaraNodeHandlers {
    /** Shared by both Immune ranks: one type, two effects, each with its own chance table. */
    public static final String EFFECT_AVOIDANCE = "velara_effect_avoidance";

    private VelaraNodeHandlers() {
    }

    /**
     * Registers every Velara handler type. Called from {@code GodNodeHandlerTypes.bootstrap()},
     * which is the only point guaranteed to run before the god tree configs are validated.
     */
    public static void register() {
        GodNodeHandlers.register(EFFECT_AVOIDANCE, EffectAvoidanceHandler::new);
        GodNodeHandlers.register(VelaraNodes.UTILIZED, UtilizedParams.class, UtilizedHandler::new);
        GodNodeHandlers.register(VelaraNodes.COUNTERSTRIKE, CounterstrikeParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.MAGIC_ARMOR, MagicArmorParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.DEFENDER_OF_THE_FAITH, DefenderOfTheFaithParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.SACRIFICE, SacrificeParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.PERSERVERENCE, PerserverenceParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.ADAPTIVE_ARMOR, AdaptiveArmorParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.BOUNCE_BACK, BounceBackParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.INDOMITABLE, IndomitableParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.FIELD_MEDIC, FieldMedicParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.THE_STONEWALL, StonewallParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.CACTUS, CactusParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.MALEDICTION, MaledictionParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.IMMORTAL, ImmortalParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.FLEETING_PHYSICALITY, FleetingPhysicalityParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.STEADFAST, SteadfastParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.SANITATION, SanitationParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.PRESENCE, PresenceParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(VelaraNodes.HEALING_FLOW, HealingFlowParams.class, ListenerBoundHandler::new);
        GodNodePreviews.register(VelaraNodes.MALEDICTION, GodNodePreviews.MALEDICTION_FORMULA,
                VelaraStatBus::previewMalediction);
    }

    /**
     * The loaded parameters of one Velara effect. Absence is a programming error rather than a
     * config error - load-time validation has already asserted that every placed effect exists
     * and resolves its handler - so it fails loudly naming the effect instead of degrading to a
     * zero that would read as a balance change.
     */
    public static <T extends GodEffectParams> T params(String effectId, Class<T> type) {
        return GodNodeRegistry.params(VaultGod.VELARA, effectId, type);
    }

    /** Immune: a chance to avoid every effect in {@link VelaraBadEffects}, linear in stars owned. */
    public record EffectAvoidanceHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            float chance = 0.0F;
            for (int point = 0; point < context.points(); point++) {
                chance += context.value(point);
            }
            if (chance <= 0.0F) {
                return;
            }
            List<MobEffect> effects = VelaraBadEffects.resolve();
            if (effects.isEmpty()) {
                return;
            }
            sink.add(ModGearAttributes.EFFECT_LIST_AVOIDANCE,
                    new EffectAvoidanceListGearAttribute(effects, VelaraBadEffects.KEY, chance));
        }
    }

    /** Utilized: added levels to every utility ability, at a flat amount rather than per star. */
    public record UtilizedHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            int levels = this.effect.params(UtilizedParams.class).ability_levels();
            if (levels <= 0) {
                return;
            }
            sink.add(ModGearAttributes.ABILITY_LEVEL, new AbilityLevelAttribute("UTILITY", levels));
        }
    }

    public record UtilizedParams(int ability_levels) implements GodEffectParams {
    }

    public record CounterstrikeParams(float chance) implements GodEffectParams {
    }

    public record MagicArmorParams(float efficiency) implements GodEffectParams {
    }

    public record DefenderOfTheFaithParams(float per_charm) implements GodEffectParams {
    }

    public record SacrificeParams(float syphon, float resistance) implements GodEffectParams {
    }

    public record PerserverenceParams(float timer_bonus) implements GodEffectParams {
    }

    public record AdaptiveArmorParams(float per_stack, int max_stacks) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.max_stacks < 1) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has max_stacks " + this.max_stacks
                        + "; it must be at least 1");
            }
        }
    }

    public record BounceBackParams(float multiplier, float health_threshold) implements GodEffectParams {
    }

    public record IndomitableParams(int regeneration_levels) implements GodEffectParams {
    }

    public record FieldMedicParams(float multiplier) implements GodEffectParams {
    }

    public record StonewallParams(float speed_multiplier, float armor_multiplier) implements GodEffectParams {
    }

    public record CactusParams(float armor_multiplier, float thorns_multiplier) implements GodEffectParams {
    }

    public record MaledictionParams(float forced_healing) implements GodEffectParams {
    }

    public record ImmortalParams(float health_multiplier, float armor_multiplier, float healing_multiplier,
                                 float flat_health, float flat_armor, int regeneration_levels,
                                 float damage_multiplier, int revive_cooldown_ticks) implements GodEffectParams {
    }

    public record FleetingPhysicalityParams(int immune_ticks, int vulnerable_ticks,
                                            float damage_multiplier) implements GodEffectParams {
        /** One full immune-then-vulnerable rotation, so the two halves can never drift apart. */
        public int cycleTicks() {
            return this.immune_ticks + this.vulnerable_ticks;
        }

        @Override
        public void validate(String effectId) {
            if (this.cycleTicks() < 1) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has an immune plus vulnerable cycle "
                        + "of " + this.cycleTicks() + " ticks; it must be at least 1");
            }
        }
    }

    public record SteadfastParams(float knockback_floor, float armor_per_excess) implements GodEffectParams {
    }

    public record SanitationParams(float radius, int duration_divisor) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.duration_divisor < 1) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has duration_divisor "
                        + this.duration_divisor + "; it must be at least 1");
            }
        }
    }

    public record PresenceParams(float radius, float resistance, float healing,
                                 int regeneration_levels) implements GodEffectParams {
    }

    public record HealingFlowParams(float per_mana_regen) implements GodEffectParams {
    }
}
