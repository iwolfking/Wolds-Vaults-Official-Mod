package xyz.iwolfking.woldsvaults.modifiers.gear.ancient;

import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.EntityHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import xyz.iwolfking.woldsvaults.config.AncientsConfig;
import xyz.iwolfking.woldsvaults.gear.ancient.Ancients;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

/**
 * Irradiates mobs near a player wearing the ancient Plague Steppers. Exposure is cumulative - Slowness I
 * on entry, a level per further five seconds, capped at III - and decays at the same rate on leaving.
 */
public class RadiationWaveTracker {
    private static final Map<Integer, Integer> exposureTicks = new HashMap<>();

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        exposureTicks.clear();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        AncientsConfig.Effects effects = Ancients.effects();
        if (effects == null) {
            return;
        }
        int scanInterval = effects.radiationScanIntervalTicks;
        if (server.getTickCount() % scanInterval != 0) {
            return;
        }

        Set<Integer> exposedThisScan = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!AttributeSnapshotHelper.canHaveSnapshot(player)) {
                continue;
            }
            AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
            float radius = snapshot.getAttributeValue(ModGearAttributes.RADIATION_WAVE, VaultGearAttributeTypeMerger.floatSum());
            if (radius <= 0.0F) {
                continue;
            }

            List<Mob> nearby = EntityHelper.getNearby(player.level, player.blockPosition(), radius, Mob.class);
            for (Mob mob : nearby) {
                if (!mob.isAlive() || mob instanceof EternalEntity || mob instanceof PetEntity) {
                    continue;
                }
                exposedThisScan.add(mob.getId());
                int exposure = exposureTicks.merge(mob.getId(), scanInterval, Integer::sum);
                int amplifier = Math.min(effects.radiationMaxAmplifier, exposure / effects.radiationEscalationTicks);
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, effects.radiationSlowDurationTicks, amplifier, false, true));
            }
        }

        exposureTicks.entrySet().removeIf(entry -> {
            if (exposedThisScan.contains(entry.getKey())) {
                return false;
            }
            int remaining = entry.getValue() - scanInterval;
            entry.setValue(remaining);
            return remaining <= 0;
        });
    }
}
