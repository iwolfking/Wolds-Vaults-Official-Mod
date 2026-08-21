package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.init.ModGearAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;
import xyz.iwolfking.woldsvaults.gods.node.CombatContributor;
import xyz.iwolfking.woldsvaults.gods.node.GodDamageContext;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandler;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.gods.node.StatContributor;

/**
 * Every handler type the Idona tree owns, and the typed parameters each one reads.
 *
 * <p>Seven plain stat effects bind the shared {@code gear_attribute_scaled} type and Pious
 * Devotion binds the shared {@code piety} type, so none of those is named here. What is left is
 * one type per effect Java has to know about: the stat effects that pay into more than one
 * attribute or into a non-float one, the damage effects that reach the game through the shared
 * {@link CombatContributor} pipeline in {@link IdonaHitHandlers}, the slowly-varying damage
 * factors on the shared ticker in {@link IdonaTickHandlers}, and the rest as
 * {@link ListenerBound} - numbers in config, validated at load, behaviour still reached through
 * the base mod events and mixins that own the ordering.
 *
 * <p>Params component names are the config keys verbatim, including their underscores. The codec
 * binds by component name, so renaming one here silently means renaming it in the config file
 * too.
 */
public final class IdonaNodeHandlers {
    private IdonaNodeHandlers() {
    }

    /**
     * Registers every Idona handler type. Called from {@code GodNodeHandlerTypes.bootstrap()},
     * which is the only point guaranteed to run before the god tree configs are validated.
     */
    public static void register() {
        GodNodeHandlers.register(IdonaNodes.STACK_STACK_STACK, StackStackStackHandler::new);
        GodNodeHandlers.register(IdonaNodes.KING_HUNTER, KingHunterHandler::new);
        GodNodeHandlers.register(IdonaNodes.ENFORCER, EnforcerHandler::new);
        GodNodeHandlers.register(IdonaNodes.GRAND_ARCHMAGE, GrandArchmageParams.class, GrandArchmageHandler::new);
        GodNodeHandlers.register(IdonaNodes.WEAPONMASTER, WeaponmasterParams.class, WeaponmasterHandler::new);
        GodNodeHandlers.register(IdonaNodes.PINCUSHION, PincushionParams.class,
                IdonaHitHandlers.PincushionHandler::new);
        GodNodeHandlers.register(IdonaNodes.SNEAKY_ADVANTAGE, SneakyAdvantageParams.class,
                IdonaHitHandlers.SneakyAdvantageHandler::new);
        GodNodeHandlers.register(IdonaNodes.GREEDBANE, GreedbaneParams.class,
                IdonaHitHandlers.GreedbaneHandler::new);
        GodNodeHandlers.register(IdonaNodes.PRISON_WARDEN, PrisonWardenParams.class,
                IdonaHitHandlers.PrisonWardenHandler::new);
        GodNodeHandlers.register(IdonaNodes.TRUE_RAGE, TrueRageParams.class,
                IdonaHitHandlers.TrueRageHandler::new);
        GodNodeHandlers.register(IdonaNodes.CLEAVE_EXPERT, CleaveExpertParams.class,
                IdonaHitHandlers.CleaveExpertHandler::new);
        GodNodeHandlers.register(IdonaNodes.KINETIC_IMPACT, KineticImpactParams.class,
                IdonaTickHandlers.KineticImpactHandler::new);
        GodNodeHandlers.register(IdonaNodes.SURROUNDED, SurroundedParams.class,
                IdonaTickHandlers.SurroundedHandler::new);
        GodNodeHandlers.register(IdonaNodes.UNDER_PRESSURE, UnderPressureParams.class,
                IdonaTickHandlers.UnderPressureHandler::new);
        GodNodeHandlers.register(IdonaNodes.BANKED_ANGER, BankedAngerParams.class,
                IdonaTickHandlers.BankedAngerHandler::new);
        GodNodeHandlers.register(IdonaNodes.CRUSHING_BLOWS, CrushingBlowsParams.class,
                IdonaTickHandlers.CrushingBlowsHandler::new);
        GodNodeHandlers.register(IdonaNodes.SOULSTEALER, SoulstealerParams.class, ListenerBound::new);
        GodNodeHandlers.register(IdonaNodes.THWACK, ThwackParams.class, ListenerBound::new);
        GodNodeHandlers.register(IdonaNodes.LUCKIEST_HIT, LuckiestHitParams.class, ListenerBound::new);
        GodNodeHandlers.register(IdonaNodes.OVERCRIT, ListenerBound::new);
        GodNodeHandlers.register(IdonaNodes.SUPER_STACKER, SuperStackerParams.class, ListenerBound::new);
        GodNodeHandlers.register(IdonaNodes.STACK_HOARDER, StackHoarderParams.class, ListenerBound::new);
        GodNodeHandlers.register(IdonaNodes.POWER_DUMP, PowerDumpParams.class, ListenerBound::new);
        GodNodeHandlers.register(IdonaNodes.ULTRA_RAMPAGING, Deferred::new);
    }

    /**
     * The loaded parameters of one Idona effect. Absence is a programming error rather than a
     * config error - load-time validation has already asserted that every placed effect exists
     * and resolves its handler - so it fails loudly naming the effect instead of degrading to a
     * zero that would read as a balance change.
     */
    public static <T extends GodEffectParams> T params(String effectId, Class<T> type) {
        GodEffect effect = GodNodeRegistry.effect(effectId).orElse(null);
        if (effect == null) {
            throw GodTreeConfigException.fail("Idona effect '" + effectId + "' was read before the god node "
                    + "registry finished loading, or is missing from god_node_effects_idona.json");
        }
        return effect.params(type);
    }

    /**
     * An effect whose numbers are config and whose behaviour is reached through the base mod's own
     * events or through a mixin. It implements no capability on purpose: a handler that claimed
     * one would be dispatched twice, once by the capability driver and once by the listener that
     * actually owns the ordering.
     */
    public record ListenerBound(GodEffect effect) implements GodNodeHandler {
    }

    /**
     * A node the tree places but has no behaviour for yet. Binding it to a type of its own rather
     * than to the catch-all is what lets load-time validation tell a deliberately deferred node
     * from an unported one.
     */
    public record Deferred(GodEffect effect) implements GodNodeHandler {
    }

    /**
     * Stack Stack Stack: added maximum stacks for every stacking talent, one whole stack per
     * point. The count is an integer gear attribute rather than a float one, which is why it does
     * not bind the shared scaled type.
     */
    public record StackStackStackHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            int stacks = Math.round(total(context));
            if (stacks == 0) {
                return;
            }
            sink.add(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ADDITIONAL_STACKING_STACKS, stacks);
        }
    }

    /** King Hunter: one table paying the same value into both single-target damage attributes. */
    public record KingHunterHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            float value = total(context);
            if (value == 0.0F) {
                return;
            }
            sink.add(ModGearAttributes.DAMAGE_CHAMPION, value);
            sink.add(ModGearAttributes.DAMAGE_TANK, value);
        }
    }

    /** Enforcer: one table paying the same value into both crowd damage attributes. */
    public record EnforcerHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            float value = total(context);
            if (value == 0.0F) {
                return;
            }
            sink.add(ModGearAttributes.DAMAGE_HORDE, value);
            sink.add(ModGearAttributes.DAMAGE_ASSASSIN, value);
        }
    }

    /**
     * Grand Archmage's mana pool, mana regen and cooldown-reduction cap. Flat grants rather than
     * per-point ones: it is a single major placement, so a point count never enters the numbers.
     * Its other two halves - the ability-damage multiplier and the weapon-swing suppression - are
     * listener-bound, because a player stat and a cancelled hurt event are neither of them a
     * capability seam.
     */
    public record GrandArchmageHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            GrandArchmageParams params = this.effect.params(GrandArchmageParams.class);
            sink.add(ModGearAttributes.MANA_ADDITIVE_PERCENTILE, params.mana_percentile());
            sink.add(ModGearAttributes.MANA_ADDITIVE, params.mana_flat());
            sink.add(ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, params.mana_regen());
            sink.add(ModGearAttributes.COOLDOWN_REDUCTION_CAP, params.cooldown_reduction_cap());
        }
    }

    /**
     * Weaponmaster's two branches, which are mutually exclusive by weapon. The dual-wield branch
     * is attack speed and is a stat contribution, re-evaluated whenever the snapshot is rebuilt;
     * the two-handed branch is a per-hit damage multiplier and runs pre-mitigation on the shared
     * combat pipeline, where every other Idona damage multiplier already composes.
     */
    public record WeaponmasterHandler(GodEffect effect) implements StatContributor, CombatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            if (!IdonaTargeting.isDualWielding(context.player())) {
                return;
            }
            sink.add(ModGearAttributes.ATTACK_SPEED_PERCENT,
                    this.effect.params(WeaponmasterParams.class).dual_wield_attack_speed() * context.points());
        }

        @Override
        public void onOutgoing(GodNodeContext context, GodDamageContext damage) {
            if (damage.isPercentageBased() || !WoldEventHelper.isNormalAttack()) {
                return;
            }
            ServerPlayer player = context.player();
            if (!IdonaTargeting.isTwoHanded(player.getItemBySlot(EquipmentSlot.MAINHAND))) {
                return;
            }
            damage.multiply((float) Math.pow(this.effect.params(WeaponmasterParams.class).two_handed(),
                    context.points()));
        }
    }

    /**
     * The per-point table summed over the points held. A one-entry table is simply linear and
     * uncapped in the star count, which is what every shipped Idona stat node does.
     */
    private static float total(GodNodeContext context) {
        float total = 0.0F;
        for (int point = 0; point < context.points(); point++) {
            total += context.value(point);
        }
        return total;
    }

    public record GrandArchmageParams(float mana_percentile, int mana_flat, float mana_regen, float ability_damage,
                                      float cooldown_reduction_cap) implements GodEffectParams {
    }

    public record WeaponmasterParams(float two_handed, double dual_wield_attack_speed) implements GodEffectParams {
    }

    public record KineticImpactParams(float per_percent) implements GodEffectParams {
    }

    public record SurroundedParams(float per_mob, double radius) implements GodEffectParams {
    }

    public record TrueRageParams(float efficiency) implements GodEffectParams {
    }

    public record CleaveExpertParams(float efficiency) implements GodEffectParams {
    }

    public record CrushingBlowsParams(float multiplier) implements GodEffectParams {
    }

    public record UnderPressureParams(int window_ticks, float max) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.window_ticks < 1) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has window_ticks "
                        + this.window_ticks + "; it must be at least 1");
            }
        }
    }

    public record PincushionParams(float per_hit) implements GodEffectParams {
    }

    public record BankedAngerParams(double base) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.base <= 1.0D) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has base " + this.base
                        + "; it is the base of a logarithm and must be greater than 1");
            }
        }
    }

    public record SoulstealerParams(float multiplier) implements GodEffectParams {
    }

    public record LuckiestHitParams(float chance_scale) implements GodEffectParams {
    }

    public record ThwackParams(float multiplier) implements GodEffectParams {
    }

    public record SneakyAdvantageParams(float per_effect) implements GodEffectParams {
    }

    public record SuperStackerParams(float multiplier) implements GodEffectParams {
    }

    public record StackHoarderParams(float multiplier) implements GodEffectParams {
    }

    public record PrisonWardenParams(float multiplier, int duration_ticks) implements GodEffectParams {
    }

    public record GreedbaneParams(float multiplier) implements GodEffectParams {
    }

    public record PowerDumpParams(float per_mana, int surplus_ttl_ticks,
                                  int continuous_grace_ticks) implements GodEffectParams {
    }
}
