package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import iskallia.vault.entity.boss.TheVesselEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The damage pool, the boss bar that draws it, and what happens when it empties.
 *
 * <p>The Vessel cannot be killed. Its {@code hurt} restores any health that would drop below one and
 * both {@code die} and {@code kill} are overridden to do the same, which is what makes the endless
 * trial endless. A Champion is therefore defeated by absorbing a fixed amount of damage rather than
 * by dying: every hit is accumulated here, and when the running total reaches the pool the entity is
 * discarded in a burst of smoke and pays out.
 *
 * <p>The bar reads the pool, not the entity's health. Health is pinned near its maximum for the whole
 * fight and would never move.</p>
 */
public final class VaultChampionKills {
    private static final String POOL_KEY = "woldsvaults:greed_champion_pool";
    private static final String DEALT_KEY = "woldsvaults:greed_champion_dealt";
    private static final String DEFEATED_KEY = "woldsvaults:greed_champion_defeated";
    private static final String WAKE_KEY = "woldsvaults:greed_champion_wake";
    private static final float SCIENTIFIC_THRESHOLD = 1.0E10F;

    private static final Map<UUID, ServerBossEvent> BARS = new ConcurrentHashMap<>();

    private VaultChampionKills() {
    }

    public static void setPool(Entity champion, double pool) {
        CompoundTag data = champion.getPersistentData();
        data.putDouble(POOL_KEY, pool);
        data.putDouble(DEALT_KEY, 0.0D);
    }

    public static double poolOf(Entity champion) {
        return champion.getPersistentData().getDouble(POOL_KEY);
    }

    public static double dealtOf(Entity champion) {
        return champion.getPersistentData().getDouble(DEALT_KEY);
    }

    /**
     * Books one hit against the pool. Returns true when that hit emptied it, leaving the caller to
     * run the defeat - the damage listener has the level and the killer to hand and this does not.
     */
    public static boolean accumulate(Entity champion, float amount) {
        if (amount <= 0.0F) {
            return false;
        }
        CompoundTag data = champion.getPersistentData();
        double pool = data.getDouble(POOL_KEY);
        if (pool <= 0.0D) {
            return false;
        }
        double dealt = data.getDouble(DEALT_KEY) + amount;
        data.putDouble(DEALT_KEY, dealt);
        return dealt >= pool;
    }

    public static void markDefeated(Entity champion) {
        champion.getPersistentData().putBoolean(DEFEATED_KEY, true);
    }

    public static boolean isDefeated(Entity champion) {
        return champion.getPersistentData().getBoolean(DEFEATED_KEY);
    }

    /** Arms the arrival pause. The Champion stands inert until this game time passes. */
    public static void setWakeTime(Entity champion, long gameTime) {
        champion.getPersistentData().putLong(WAKE_KEY, gameTime);
    }

    /**
     * Wakes the Champion once its arrival pause is over. While dormant the Vessel suppresses its AI,
     * refuses damage and does not advance its own damage ramp, so nothing is lost or gained by the
     * wait on either side.
     */
    public static void tickWake(TheVesselEntity champion) {
        if (!champion.isDormant()) {
            return;
        }
        long wake = champion.getPersistentData().getLong(WAKE_KEY);
        if (wake > 0L && champion.level.getGameTime() >= wake) {
            champion.setDormant(false);
        }
    }

    /**
     * Ends the fight. The entity is discarded rather than killed, the summoner's rage bookkeeping is
     * settled, and the payout is dropped as world items where the Champion stood so that whoever
     * actually did the fighting - the summoner or a friend helping out - picks it up.
     */
    public static void defeat(Vault vault, ServerLevel level, LivingEntity champion) {
        GreedMedallionTier tier = VaultChampion.getTier(champion).orElse(null);
        UUID summoner = VaultChampion.getSummoner(champion);
        double x = champion.getX();
        double y = champion.getY();
        double z = champion.getZ();

        removeBar(champion.getUUID());
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.5D, z, 120, 0.8D, 1.5D, 0.8D, 0.03D);
        level.sendParticles(ParticleTypes.SMOKE, x, y + 1.0D, z, 80, 0.6D, 1.0D, 0.6D, 0.05D);
        level.playSound(null, x, y, z, ModSounds.GREED_COINS_SHAKE, SoundSource.HOSTILE, 2.0F, 0.7F);
        champion.discard();

        if (summoner != null && vault != null) {
            VaultChampionState.PlayerState state = VaultChampionState.get(vault, summoner);
            if (state != null) {
                state.onChampionDefeated();
            }
        }
        if (tier == null) {
            WoldsVaults.LOGGER.warn("A Vault Champion was defeated with no medallion rank stamped on it - no rewards "
                    + "were paid.");
            return;
        }
        dropRewards(level, tier, x, y, z);
        for (ServerPlayer runner : GodVaultUtil.runners(vault)) {
            runner.displayClientMessage(new TextComponent("The ").withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent("Vault Champion").withStyle(ChatFormatting.DARK_PURPLE))
                    .append((Component) new TextComponent(" falls.").withStyle(ChatFormatting.GRAY)), false);
        }
    }

    private static void dropRewards(ServerLevel level, GreedMedallionTier tier, double x, double y, double z) {
        GreedChampionConfig.Rewards rewards = VaultChampion.config().getRewards();
        int coins = rewards.greedCoinsPerRank * tier.getRankIndex();
        int tickets = rewards.greedyTicketsPerRank * tier.getRankIndex();
        drop(level, new ItemStack(ModItems.GREED_COIN, coins), x, y, z);
        drop(level, new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.GREEDY_TICKET, tickets), x, y, z);
    }

    private static void drop(ServerLevel level, ItemStack stack, double x, double y, double z) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity item = new ItemEntity(level, x, y + 0.5D, z, stack);
        item.setNoPickUpDelay();
        level.addFreshEntity(item);
    }

    /**
     * Creates this Champion's bar and shows it to every runner in the vault. Tracking range would be
     * the vanilla default, which is wrong here - anyone in the vault can help fight it, so anyone in
     * the vault should be able to see how the fight is going.
     */
    public static void openBar(Vault vault, LivingEntity champion) {
        ServerBossEvent bar = new ServerBossEvent(barName(champion), BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS);
        bar.setProgress(1.0F);
        setAudience(bar, vault);
        BARS.put(champion.getUUID(), bar);
    }

    /** Refreshes progress, name and audience. Called on the manager's cadence, not every tick. */
    public static void syncBar(Vault vault, LivingEntity champion) {
        ServerBossEvent bar = BARS.get(champion.getUUID());
        if (bar == null) {
            openBar(vault, champion);
            return;
        }
        double pool = poolOf(champion);
        double dealt = dealtOf(champion);
        float remaining = pool <= 0.0D ? 1.0F : (float) Math.max(0.0D, 1.0D - dealt / pool);
        bar.setProgress(remaining);
        bar.setName(barName(champion));
        bar.setColor(remaining <= 0.25F ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE);
        setAudience(bar, vault);
    }

    /**
     * Rebuilds who can see the bar. It has to shrink as well as grow: a runner who extracts mid-fight
     * would otherwise keep a boss bar on their screen for the rest of the session.
     */
    private static void setAudience(ServerBossEvent bar, Vault vault) {
        List<ServerPlayer> runners = GodVaultUtil.runners(vault);
        for (ServerPlayer stale : new java.util.ArrayList<>(bar.getPlayers())) {
            if (!runners.contains(stale)) {
                bar.removePlayer(stale);
            }
        }
        for (ServerPlayer runner : runners) {
            bar.addPlayer(runner);
        }
    }

    public static void removeBar(UUID championId) {
        ServerBossEvent bar = BARS.remove(championId);
        if (bar != null) {
            bar.removeAllPlayers();
            bar.setVisible(false);
        }
    }

    public static void clearAllBars() {
        BARS.values().forEach(bar -> {
            bar.removeAllPlayers();
            bar.setVisible(false);
        });
        BARS.clear();
    }

    /**
     * The bar caption, carrying the remaining pool as a number. Switches to scientific notation past
     * ten billion, the same place the greed trial's own readout does, because a Legend pool with vault
     * modifiers on top runs to ten digits and would otherwise overrun the bar.
     */
    private static Component barName(LivingEntity champion) {
        double pool = poolOf(champion);
        double remaining = Math.max(0.0D, pool - dealtOf(champion));
        String format = remaining >= SCIENTIFIC_THRESHOLD || pool >= SCIENTIFIC_THRESHOLD ? "%.3e / %.3e" : "%.0f / %.0f";
        return new TextComponent("Vault Champion  ").withStyle(ChatFormatting.DARK_PURPLE)
                .append(new TextComponent(String.format(format, remaining, pool)).withStyle(ChatFormatting.GRAY));
    }
}
