package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.VaultMod;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.boss.TheVesselEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultTrueDamage;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;
import xyz.iwolfking.woldsvaults.gods.combat.FuryIncome;

/**
 * Startup and lifecycle for the Vault Champion. Self-contained in the same way the medallion and god
 * systems are: the feature registers its own listeners rather than being wired into the entrypoint.
 *
 * <p>Two of these hooks are worth explaining. The damage pool is accumulated from a final-stage
 * sub-stage rather than a plain event listener so that it always sees the number the player actually
 * dealt, after every reduction, cap and floor in the pack has run - a listener at the same priority
 * as the final stage would be a coin flip. And the defeat itself is deferred by one tick to the
 * entity's own update, because discarding an entity from inside its own damage event is the kind of
 * thing that works until the day it does not.</p>
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VaultChampionEvents {
    private static final Object OWNER = new Object();

    private VaultChampionEvents() {
    }

    @Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                VaultTrueDamage.register();
                VaultChampionRage.register();
                registerDamagePool();
                CommonEvents.VAULT_END.register(OWNER, data ->
                        VaultChampionManager.releaseVault(data.getVault(), ServerLifecycleHooks.getCurrentServer()));
            });
        }
    }

    private static void registerDamagePool() {
        FinalDamageStage.register(VaultMod.id("greed_champion_pool"), FinalDamageStage.ORDER_FLOOR + 100,
                (event, amount) -> {
                    if (amount > 0.0F && VaultChampion.isChampion(event.getEntityLiving())
                            && VaultChampionKills.accumulate(event.getEntityLiving(), amount)) {
                        VaultChampionKills.markDefeated(event.getEntityLiving());
                        if (event.getSource().getEntity() instanceof ServerPlayer slayer) {
                            FuryIncome.awardKill(slayer, event.getEntityLiving());
                        }
                    }
                    return amount;
                });
    }

    /**
     * The Champion's own per-tick business: waking from its arrival pause and dying once its pool is
     * empty. Gated on the tag before anything else so the cost for every other entity in the world is
     * one class check.
     */
    @SubscribeEvent
    public static void onChampionTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof TheVesselEntity champion)
                || champion.level.isClientSide
                || !VaultChampion.isChampion(champion)) {
            return;
        }
        if (VaultChampionKills.isDefeated(champion)) {
            Vault vault = VaultUtils.getVault(champion.level).orElse(null);
            if (champion.level instanceof ServerLevel level) {
                VaultChampionKills.defeat(vault, level, champion);
            }
            return;
        }
        VaultChampionKills.tickWake(champion);
    }

    /**
     * Rage for a kill. Its own listener rather than a call inside the assassin handler, because most
     * of what feeds it is not an assassin and the assassin path is already cancelled upstream.
     */
    @SubscribeEvent
    public static void onKill(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntityLiving().level instanceof ServerLevel level) {
            VaultChampionRage.onKill(event.getEntityLiving(), event, level);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            VaultChampionManager.tick(server);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        VaultChampionState.clearAll();
        VaultChampionAura.clear();
        VaultChampionManager.resetSessionState();
    }
}
