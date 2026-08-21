package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.event.ActiveFlags;
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

/**
 * Forge-bus half of the milestone dispatcher. The events routed here are the ones the vault's
 * own {@code CommonEvents} bus only wraps, so subscribing directly avoids the wrapper's
 * five-priority dispatch amplification.
 */
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
            healReputationBelowRankFloor(player);
            healGodMilestones(player);
            MilestoneFlusher.syncAll(player);
        }
    }

    /**
     * Raises a saved reputation that sits below its own rank's band floor back up to that floor.
     *
     * <p>The coherence hook on {@code setGreedTier} only fixes force-sets made after it existed;
     * a save whose rank was set before it shipped still carries the zero base wrote, so the bar,
     * which reads {@code (reputation - floor) / band}, is pinned empty forever no matter how much
     * reputation is claimed. This runs once per login, writes through the data class's own setter
     * so the greed sync fires, and is idempotent: a healed save meets the floor and is skipped.</p>
     *
     * <p>Rank 0 has a floor of 0, so players who have never joined the greed ladder are untouched.
     * The write happens before {@link MilestoneFlusher#syncAll(ServerPlayer)} so the status packet
     * that follows already carries the healed number.</p>
     */
    private static void healReputationBelowRankFloor(ServerPlayer player) {
        PlayerGreedTreeData treeData = PlayerGreedTreeData.get(player.server);
        int rank = treeData.getGreedTier(player);
        int floor = MilestoneRankLadder.getThreshold(rank);
        int reputation = treeData.getGreedReputation(player);
        if (reputation >= floor) {
            return;
        }
        treeData.setGreedReputation(player, floor);
        WoldsVaults.LOGGER.info("Healed greed reputation for {}: {} was below the rank {} floor of {}",
                player.getGameProfile().getName(), reputation, rank, floor);
    }

    /**
     * Re-reaches the four god milestones from the player's live god levels, so any historic drift
     * between the two self-heals on the next login.
     *
     * <p>These four are set to a level rather than incremented, and their only live feed is
     * {@link GodLevelUpEvent}, which fires exactly once per level gained. A level-up that is
     * missed - a save that predates the milestone, a level gained while the milestone engine was
     * absent, or a level forced by command - would otherwise leave the counter permanently short
     * with no second chance to catch up. Reading the authoritative value out of
     * {@link GodAlignmentData} makes that unrecoverable case recoverable, and it is idempotent:
     * {@link Milestones#reach} ignores anything at or below the stored value, so a healthy save
     * writes nothing. Structured like {@link #healReputationBelowRankFloor} and run before the
     * login sync, so the packet the client receives already carries the healed counters.</p>
     */
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

    /**
     * Drops any per-vault scratch left behind by an earlier world. The scratch is static, so on an
     * integrated server it outlives the world it belongs to; clearing it here rather than on
     * shutdown keeps it intact for the save that a stopping server has not written yet.
     */
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

    /**
     * Runs at LOWEST so the amount seen is the final, post-mitigation damage, matching the
     * addon's own damage instrumentation. Splits the total on {@code ActiveFlags.IS_AP_ATTACKING}
     * to separate ability power from attack damage.
     */
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

    /**
     * Fans one mob death out to every kill-counting milestone. The vault state lookup is
     * belt-and-braces against the engine's own gate now, but it is still load-bearing here: the
     * per-vault mob counter that "Complete an Impressive Vault" reads needs the state object.
     */
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
