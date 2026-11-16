package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultTrueDamage;
import xyz.iwolfking.woldsvaults.medallions.assassins.GreedAssassinBehaviors;
import xyz.iwolfking.woldsvaults.medallions.assassins.GreedAssassinFury;
import xyz.iwolfking.woldsvaults.medallions.assassins.GreedAssassinRegistry;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionAura;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionKills;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionManager;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionRage;

/**
 * Every Forge and VH event the greed medallion feature listens to, medallions, assassins and the Vault Champion
 * alike, one handler per event; each hands off to the class that owns the behaviour.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GreedMedallionEvents {
    private static final Object OWNER = new Object();
    /** VH listener priority that runs after the vault's own priority-0 loot listener has stamped gear roll types. */
    private static final int AFTER_VAULT_LOOT = -1;

    private GreedMedallionEvents() {
    }

    /** Registers the VH listeners and the Champion damage pool once common setup runs. */
    @Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(GreedMedallionEvents::register);
        }
    }

    private static void register() {
        VaultTrueDamage.register();
        VaultChampionRage.register();
        VaultChampionKills.registerDamagePool();
        CommonEvents.VAULT_END.register(OWNER, data -> {
            Vault vault = data.getVault();
            if (vault != null && vault.has(Vault.ID)) {
                GreedAssassinFury.release(vault.get(Vault.ID));
            }
            VaultChampionManager.releaseVault(vault, ServerLifecycleHooks.getCurrentServer());
        });
        CommonEvents.CHEST_LOOT_GENERATION.post().register(OWNER, data ->
                GreedMedallionGearRolls.upgradeOmegaPlus(data.getTileEntity().getLevel(), data.getLoot()), AFTER_VAULT_LOOT);
        CommonEvents.LOOTABLE_BLOCK_GENERATION_EVENT.post().register(OWNER, data ->
                GreedMedallionGearRolls.upgradeOmegaPlus(data.getTileEntity().getLevel(), data.getLoot()), AFTER_VAULT_LOOT);
        CommonEvents.PLAYER_STAT.of(PlayerStat.TRAP_DISARM_CHANCE).register(OWNER, data -> {
            if (!(data.getEntity() instanceof ServerPlayer player)) {
                return;
            }
            float penalty = GreedMedallionEffects.trapDisarmPenalty(player);
            if (penalty > 0.0F) {
                data.setValue(Math.max(0.0F, data.getValue() - penalty));
            }
        });
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
        GreedAssassinFury.tick(server);
        GreedAssassinBehaviors.pulseAuras(server);
        VaultChampionManager.tick(server);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        GreedAssassinBehaviors.onHit(event);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving().level instanceof ServerLevel level) {
            VaultChampionRage.onKill(event.getEntityLiving(), event, level);
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        VaultChampionKills.tick(event.getEntityLiving());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        VaultChampionAura.pierceEffectAvoidance(event);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        GreedAssassinRegistry.clearAll();
        GreedAssassinFury.clearAll();
        VaultChampionManager.clearAll();
    }
}
