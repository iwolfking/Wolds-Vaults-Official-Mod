package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModGearAttributes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;
import xyz.iwolfking.woldsvaults.gods.combat.UltraRampagingConfig;
import xyz.iwolfking.woldsvaults.gods.node.CombatContributor;
import xyz.iwolfking.woldsvaults.gods.node.DeferredHandler;
import xyz.iwolfking.woldsvaults.gods.node.GodDamageContext;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.gods.node.ListenerBoundHandler;
import xyz.iwolfking.woldsvaults.gods.node.StatContributor;

/** Idona handler types and their params, whose component names are the config keys verbatim. */
public final class IdonaNodeHandlers {
    private IdonaNodeHandlers() {
    }

    /** Registers every Idona handler type. Must run before the god tree configs are validated. */
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
        GodNodeHandlers.register(IdonaNodes.SOULSTEALER, SoulstealerParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(IdonaNodes.THWACK, ThwackParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(IdonaNodes.LUCKIEST_HIT, LuckiestHitParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(IdonaNodes.OVERCRIT, ListenerBoundHandler::new);
        GodNodeHandlers.register(IdonaNodes.SUPER_STACKER, SuperStackerParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(IdonaNodes.STACK_HOARDER, StackHoarderParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(IdonaNodes.POWER_DUMP, PowerDumpParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(IdonaNodes.ULTRA_RAMPAGING, UltraRampagingConfig.class,
                IdonaTickHandlers.UltraRampagingHandler::new);
    }

    /** The loaded parameters of one Idona effect. Throws naming the effect if it is absent. */
    public static <T extends GodEffectParams> T params(String effectId, Class<T> type) {
        return GodNodeRegistry.params(VaultGod.IDONA, effectId, type);
    }

    /** Stack Stack Stack: added maximum stacks for every stacking talent, rounded to whole stacks. */
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

    /** Grand Archmage's mana pool, mana regen and cooldown-reduction cap, granted flat. */
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

    /** Weaponmaster: attack speed while dual-wielding, damage on normal two-handed attacks. */
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

    /** The per-point table summed over the points held. */
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
