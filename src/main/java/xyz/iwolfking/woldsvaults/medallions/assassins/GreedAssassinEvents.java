package xyz.iwolfking.woldsvaults.medallions.assassins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultTrueDamage;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;

/**
 * Server-side hooks for the assassin behaviours that act after the spawn: the on-hit debuff and the aura pulse.
 * Both read the tier stamped on the assassin, not the vault's.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public class GreedAssassinEvents {
    private static final int AURA_PULSE_TICKS = 100;

    /**
     * The flat true-damage follow-up and the Hunter 1+ debuff on a landed melee hit; only direct hits count, and
     * the true-damage flag stops the follow-up landing back here.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
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

    /** Pulses Hunter 3+ auras every five seconds; the sweep also prunes assassins that no longer resolve. */
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
