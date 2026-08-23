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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Irradiates mobs near a player wearing the ancient Plague Steppers. Exposure is cumulative - Slowness I
 * on entry, a level per further five seconds, capped at III - and decays at the same rate on leaving.
 */
@Mod.EventBusSubscriber
public class RadiationWaveTracker {
    private static final Map<Integer, Integer> exposureTicks = new HashMap<>();
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final int ESCALATION_TICKS = 100;
    private static final int MAX_AMPLIFIER = 2;
    private static final int SLOW_DURATION_TICKS = 60;

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
        if (server == null || server.getTickCount() % SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        Set<Integer> exposedThisScan = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!AttributeSnapshotHelper.canHaveSnapshot(player)) {
                continue;
            }
            AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
            float radius = snapshot.getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.RADIATION_WAVE, VaultGearAttributeTypeMerger.floatSum());
            if (radius <= 0.0F) {
                continue;
            }

            List<Mob> nearby = EntityHelper.getNearby(player.level, player.blockPosition(), radius, Mob.class);
            for (Mob mob : nearby) {
                if (!mob.isAlive() || mob instanceof EternalEntity || mob instanceof PetEntity) {
                    continue;
                }
                exposedThisScan.add(mob.getId());
                int exposure = exposureTicks.merge(mob.getId(), SCAN_INTERVAL_TICKS, Integer::sum);
                int amplifier = Math.min(MAX_AMPLIFIER, exposure / ESCALATION_TICKS);
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_DURATION_TICKS, amplifier, false, true));
            }
        }

        exposureTicks.entrySet().removeIf(entry -> {
            if (exposedThisScan.contains(entry.getKey())) {
                return false;
            }
            int remaining = entry.getValue() - SCAN_INTERVAL_TICKS;
            entry.setValue(remaining);
            return remaining <= 0;
        });
    }
}
