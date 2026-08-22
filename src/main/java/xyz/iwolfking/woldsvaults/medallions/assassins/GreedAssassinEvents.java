package xyz.iwolfking.woldsvaults.medallions.assassins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultTrueDamage;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;

/**
 * Server-side hooks for the two greed assassin behaviours that act after the spawn: the on-hit
 * debuff and the buffing aura pulse. Both read the tier stamped on the assassin itself rather than
 * the vault, so an assassin always behaves as the medallion that produced it.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public class GreedAssassinEvents {
    private static final int AURA_PULSE_TICKS = 100;

    /**
     * Everything an assassin does on a landed melee hit: the flat true-damage follow-up every
     * assassin lands, and the Hunter 1+ debuff. Only direct hits count - the attacker and the
     * damage's direct entity have to be the same assassin - so projectiles, thorns and ability
     * damage attributed to the assassin apply neither.
     *
     * <p>The true-damage hit fires its own {@code LivingHurtEvent} with the assassin as the attacker,
     * which would land straight back here; the flag the helper sets for the duration of that hit is
     * what stops it recursing and what stops one swing applying two debuffs.</p>
     */
    @SubscribeEvent
    public static void onAssassinHit(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player) || WoldActiveFlags.IS_TRUE_DAMAGE.isSet()) {
            return;
        }
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (attacker == null || source.getDirectEntity() != attacker || !GreedAssassins.isAssassin(attacker)) {
            return;
        }
        if (attacker instanceof LivingEntity assassin) {
            VaultTrueDamage.deal(assassin, player, VaultChampion.config().getTrueDamage().assassin);
        }
        GreedAssassins.getTier(attacker)
                .filter(GreedMedallionTier::assassinsInflictNegativeEffects)
                .ifPresent(tier -> GreedAssassinBehaviors.applyRandomNegativeEffect(player, player.level.random));
    }

    /**
     * Hunter 3+ assassins pulse their aura every five seconds. The sweep also prunes assassins that
     * no longer resolve, so the tracked set stays bounded without a death or unload listener of its
     * own beyond the kill hook.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % AURA_PULSE_TICKS != 0) {
            return;
        }
        GreedAssassinRegistry.forEach(server, (level, assassin) ->
                GreedAssassins.getTier(assassin)
                        .filter(GreedMedallionTier::assassinsGainBuffingAuras)
                        .ifPresent(tier -> GreedAssassinBehaviors.pulseBuffingAura(level, assassin)));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        GreedAssassinRegistry.clearAll();
        GreedAssassinSpawner.clearDecayTrackers();
    }
}
