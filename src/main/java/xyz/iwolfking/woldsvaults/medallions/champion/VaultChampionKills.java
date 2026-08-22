package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import iskallia.vault.entity.boss.TheVesselEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.ChampionHudMessage;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;

import java.util.UUID;

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

        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.5D, z, 120, 0.8D, 1.5D, 0.8D, 0.03D);
        level.sendParticles(ParticleTypes.SMOKE, x, y + 1.0D, z, 80, 0.6D, 1.0D, 0.6D, 0.05D);
        level.playSound(null, x, y, z, ModSounds.GREED_COINS_SHAKE, SoundSource.HOSTILE, 2.0F, 0.7F);
        champion.discard();

        closeBar(vault);
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
     * Pushes the health bar to every runner in the vault.
     *
     * <p>The Champion is the greed trial's Vessel, so it gets the greed trial's bar rather than a
     * vanilla boss bar - which, on a wither-adjacent boss, read as exactly the wrong thing. That bar
     * is drawn client side from the base mod's own greed HUD sheet, so what goes over the wire is the
     * pool, the damage dealt against it and the Vessel's time-ramp multiplier.
     *
     * <p>Sent to every runner rather than to trackers, because anyone in the vault can join the fight
     * and should be able to see how it is going.</p>
     */
    public static void syncBar(Vault vault, LivingEntity champion) {
        double pool = poolOf(champion);
        float multiplier = champion instanceof TheVesselEntity vessel ? vessel.getDamageMultiplier() : 1.0F;
        ChampionHudMessage message = new ChampionHudMessage(true, (float) dealtOf(champion), (float) pool, multiplier);
        for (ServerPlayer runner : GodVaultUtil.runners(vault)) {
            ModNetwork.sendToClient(message, runner);
        }
    }

    /** Clears the bar for everyone still in the vault. */
    public static void closeBar(Vault vault) {
        ChampionHudMessage message = new ChampionHudMessage(false, 0.0F, 0.0F, 1.0F);
        for (ServerPlayer runner : GodVaultUtil.runners(vault)) {
            ModNetwork.sendToClient(message, runner);
        }
    }
}
