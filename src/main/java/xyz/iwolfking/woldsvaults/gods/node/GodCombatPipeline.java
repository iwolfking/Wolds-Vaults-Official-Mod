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
 * The one dispatcher for {@link CombatContributor}: a single pre-mitigation {@link LivingHurtEvent}
 * handler running every contributor by {@link CombatContributor#order()}, outgoing leg first. A node
 * needing the last word after armour registers a {@link FinalDamageStage} sub-stage instead.
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

    /** The ordered pipeline, rebuilt whenever the registry publishes a new contributor list. */
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
