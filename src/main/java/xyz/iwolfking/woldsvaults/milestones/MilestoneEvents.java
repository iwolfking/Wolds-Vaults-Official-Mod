package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.world.data.PlayerGreedData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.event.GodLevelUpEvent;

import java.util.UUID;

/** Forge-bus half of the milestone dispatcher. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public class MilestoneEvents {
    private MilestoneEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            MilestoneFlusher.tick(server);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            healUnrankedHeraldWinner(player);
            healGodMilestones(player);
            MilestoneFlusher.syncAll(player);
        }
    }

    /** Puts a Herald winner still on tier 0 onto the ladder's first rank. Idempotent. */
    private static void healUnrankedHeraldWinner(ServerPlayer player) {
        PlayerGreedTreeData treeData = PlayerGreedTreeData.get(player.server);
        if (treeData.getGreedTier(player) != 0
                || !PlayerGreedData.get(player.server).get(player.getUUID()).hasCompletedHerald()) {
            return;
        }
        treeData.setGreedTier(player, MilestoneRankLadder.FIRST_RANK);
        WoldsVaults.LOGGER.info("Healed greed rank for {}: the Herald was already beaten but the rank was still 0, set to {}",
                player.getGameProfile().getName(), MilestoneRankLadder.FIRST_RANK);
    }

    /** Re-reaches the four god milestones from the player's live levels. Idempotent. */
    private static void healGodMilestones(ServerPlayer player) {
        GodAlignmentData alignment = GodAlignmentData.get(player.server);
        for (VaultGod god : VaultGod.values()) {
            String milestone = milestoneFor(god);
            int level = alignment.getLevel(player.getUUID(), god);
            long recorded = Milestones.getValue(player, milestone);
            if (level <= recorded) {
                continue;
            }
            WoldsVaults.LOGGER.info("Healed god milestone '{}' for {}: counter was {} but {} is level {}",
                    milestone, player.getGameProfile().getName(), recorded, god.getName(), level);
            Milestones.reach(player, milestone, level);
        }
    }

    private static String milestoneFor(VaultGod god) {
        return switch (god) {
            case IDONA -> MilestoneIds.IDONAS_CHAMPION;
            case VELARA -> MilestoneIds.PRIEST_OF_VELARA;
            case TENOS -> MilestoneIds.TENOS_RIGHT_HAND;
            case WENDARR -> MilestoneIds.WENDARRS_TIMEKEEPER;
        };
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            MilestoneData.get(player.server).flush();
            MilestoneVaultState.unregisterPlayer(player.getUUID());
            Milestones.forget(player.getUUID());
            MilestoneFlusher.forget(player.getUUID());
        }
    }

    /** Drops any per-vault scratch left behind by an earlier world. */
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MilestoneVaultState.reset();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MilestoneData.get(event.getServer()).flush();
        MilestoneData.invalidate();
        MilestoneGroups.invalidate();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        MilestoneCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onGodLevelUp(GodLevelUpEvent event) {
        Milestones.reach(event.getPlayer(), milestoneFor(event.getGod()), event.getNewLevel());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            MilestoneDispatcher.onBlockMined(player, event.getState());
        }
    }

    /** Runs at LOWEST, so the amount seen is the final post-mitigation damage. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntityLiving();
        if (victim.level.isClientSide) {
            return;
        }
        float amount = event.getAmount();
        if (amount > 0.0F) {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof ServerPlayer player && !(victim instanceof Player)) {
                boolean magic = ActiveFlags.IS_AP_ATTACKING.isSet() || ActiveFlags.IS_POISON_NOVA_ATTACKING.isSet();
                Milestones.advanceFractional(player, magic ? MilestoneIds.ARCHMAGE : MilestoneIds.HACK_N_SLASH, amount);
            }
            if (victim instanceof ServerPlayer hurt) {
                MilestoneVaultState state = MilestoneDispatcher.stateFor(hurt);
                if (state != null) {
                    state.onDamaged();
                }
            }
        }
    }

    /** Fans one mob death out to every kill-counting milestone. Only counts inside a vault. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntityLiving();
        if (victim.level.isClientSide || victim instanceof Player) {
            return;
        }
        DamageSource source = event.getSource();
        ServerPlayer killer = resolveKiller(victim, source);
        if (killer == null) {
            return;
        }
        MilestoneVaultState vaultState = MilestoneVaultState.current(killer.getUUID());
        if (vaultState == null) {
            return;
        }
        Milestones.advance(killer, MilestoneIds.SLAYERRR, 1L);
        vaultState.onMobKill();

        if (source.isExplosion()) {
            Milestones.advance(killer, MilestoneIds.BOOM, 1L);
        }
        if (isLightning(source)) {
            Milestones.advance(killer, MilestoneIds.ELECTRIC_CONDUIT, 1L);
        }
        if (ActiveFlags.IS_CHAINING_ATTACKING.isSet() || hasChainTag(victim, killer)) {
            Milestones.advance(killer, MilestoneIds.MASTER_OF_CHAINS, 1L);
        }
        if (MilestoneDispatcher.isDungeonBoss(victim)) {
            Milestones.advance(killer, MilestoneIds.DUNGEONEER, 1L);
        }
        if (MilestoneDispatcher.getCockroachId().equals(EntityType.getKey(victim.getType()))) {
            Milestones.advance(killer, MilestoneIds.VILLAIN, 1L);
        }
    }

    private static ServerPlayer resolveKiller(LivingEntity victim, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof ServerPlayer player) {
            return player;
        }
        if (victim.getPersistentData().hasUUID(MilestoneDispatcher.CHAIN_ATTACKER_TAG)) {
            UUID id = victim.getPersistentData().getUUID(MilestoneDispatcher.CHAIN_ATTACKER_TAG);
            MinecraftServer server = victim.getServer();
            return server == null ? null : server.getPlayerList().getPlayer(id);
        }
        return null;
    }

    private static boolean hasChainTag(LivingEntity victim, ServerPlayer killer) {
        return victim.getPersistentData().hasUUID(MilestoneDispatcher.CHAIN_ATTACKER_TAG)
                && killer.getUUID().equals(victim.getPersistentData().getUUID(MilestoneDispatcher.CHAIN_ATTACKER_TAG));
    }

    private static boolean isLightning(DamageSource source) {
        return DamageSource.LIGHTNING_BOLT.equals(source)
                || source.getDirectEntity() instanceof LightningBolt
                || ActiveFlags.IS_LIGHTNING_ORB_ATTACKING.isSet();
    }
}
