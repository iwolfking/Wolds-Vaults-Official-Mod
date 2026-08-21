package xyz.iwolfking.woldsvaults.gods.node;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The one dispatcher for {@link CombatContributor}. Every god node that takes part in damage math
 * runs here, inside a single {@link LivingHurtEvent} handler, in one deterministic order - no node
 * registers a Forge listener of its own, so the outcome of a hit never depends on event priority,
 * registration order or class-loading order.
 *
 * <p>Order within the pipeline is each contributor's {@link CombatContributor#order()} ascending,
 * ties broken by effect id. The outgoing leg runs before the incoming leg, so a hit between two
 * players is the attacker's nodes composed with the defender's, in that order, over one running
 * amount that is written back to the event exactly once.
 *
 * <p>This is the pre-mitigation stage, at {@link EventPriority#LOW} on {@link LivingHurtEvent} -
 * the same seam the god core's other damage listeners occupy, after the base mod's {@code NORMAL}
 * damage pipeline and before {@code LuckyHitTalent} at {@code LOWEST}, so contributions still pass
 * through the target's armour and resistance like every other damage bonus in the game. It does not
 * compete with {@link FinalDamageStage}: that is the post-mitigation stage on
 * {@link net.minecraftforge.event.entity.living.LivingDamageEvent}, and a node that must have the
 * last word on a hit after armour registers a sub-stage there instead of contributing here.
 *
 * <p>{@code percentageBased} is resolved once, here, through
 * {@link GlobalDamageMultiplierRegistry#isPercentageBased(DamageSource)} - the single
 * implementation of that test - and handed to every contributor on the context. A contributor that
 * means "more damage" must consult it; a contributor that acts on the hit some other way need not.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GodCombatPipeline {
    private record Stage(GodEffect effect, CombatContributor handler) {
    }

    private static volatile List<GodEffect> loaded = List.of();
    private static volatile List<Stage> ordered = List.of();

    private GodCombatPipeline() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void runPipeline(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        List<Stage> stages = stages();
        if (stages.isEmpty()) {
            return;
        }
        LivingEntity target = event.getEntityLiving();
        DamageSource damageSource = event.getSource();
        Entity dealer = damageSource.getEntity();
        ServerPlayer attacker = dealer instanceof ServerPlayer player ? player : null;
        ServerPlayer defender = target instanceof ServerPlayer player ? player : null;
        if (attacker == null && defender == null) {
            return;
        }
        float before = event.getAmount();
        GodDamageContext damage = new GodDamageContext(dealer instanceof LivingEntity living ? living : null,
                target, damageSource, before, GlobalDamageMultiplierRegistry.isPercentageBased(damageSource));
        if (attacker != null) {
            dispatch(stages, attacker, damage, true);
        }
        if (defender != null) {
            dispatch(stages, defender, damage, false);
        }
        if (damage.getAmount() != before) {
            event.setAmount(damage.getAmount());
        }
    }

    /** The pipeline as it currently stands, for a debug command or a load-time sanity check. */
    public static List<String> listStages() {
        List<String> ids = new ArrayList<>();
        stages().forEach(stage -> ids.add(stage.effect().id()));
        return ids;
    }

    private static void dispatch(List<Stage> stages, ServerPlayer player, GodDamageContext damage, boolean outgoing) {
        for (Stage stage : stages) {
            GodNodeContext context = GodNodeGate.context(player, stage.effect()).orElse(null);
            if (context == null) {
                continue;
            }
            try {
                if (outgoing) {
                    stage.handler().onOutgoing(context, damage);
                } else {
                    stage.handler().onIncoming(context, damage);
                }
            } catch (RuntimeException e) {
                WoldsVaults.LOGGER.error("God combat contributor {} threw on the {} leg; its contribution to this "
                        + "hit was skipped.", stage.effect().id(), outgoing ? "outgoing" : "incoming", e);
            }
        }
    }

    /**
     * The ordered pipeline, rebuilt whenever the registry has published a new set of contributors.
     * {@code GodNodeRegistry} hands out a fresh list instance on every config load, so comparing
     * identity is enough to notice a reload without the registry having to announce one.
     */
    private static List<Stage> stages() {
        List<GodEffect> current = GodNodeRegistry.effectsWith(CombatContributor.class);
        if (current != loaded) {
            rebuild(current);
        }
        return ordered;
    }

    private static synchronized void rebuild(List<GodEffect> current) {
        if (current == loaded) {
            return;
        }
        List<Stage> stages = new ArrayList<>();
        for (GodEffect effect : current) {
            CombatContributor handler = GodNodeRegistry.handler(effect.id(), CombatContributor.class);
            if (handler == null) {
                WoldsVaults.LOGGER.error("God effect {} is registered as a combat contributor but its handler is not "
                        + "one; it will take no part in damage math.", effect.id());
                continue;
            }
            stages.add(new Stage(effect, handler));
        }
        stages.sort(Comparator.comparingInt((Stage stage) -> stage.handler().order())
                .thenComparing(stage -> stage.effect().id()));
        ordered = List.copyOf(stages);
        loaded = current;
    }
}
