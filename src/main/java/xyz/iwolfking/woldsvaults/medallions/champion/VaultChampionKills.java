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
 * The Champion's damage pool, the bar that draws it, and what happens when it empties. The Vessel cannot
 * be killed, so a Champion is defeated by absorbing a fixed amount of damage and then discarded, and the
 * bar reads the pool rather than the entity's health, which stays pinned near its maximum.
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

    /** Books one hit against the pool; returns true when that hit emptied it, leaving the caller to run the defeat. */
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

    public static void setWakeTime(Entity champion, long gameTime) {
        champion.getPersistentData().putLong(WAKE_KEY, gameTime);
    }

    /**
     * Wakes the Champion once its arrival pause is over; while dormant it suppresses its AI, refuses damage and
     * does not advance its damage ramp.
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
     * Ends the fight: the entity is discarded rather than killed, the summoner's rage bookkeeping is settled, and
     * the payout drops as world items.
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
        dropAll(level, new ItemStack(ModItems.GREED_COIN), rewards.greedCoinsPerRank * tier.getRankIndex(), x, y, z);
        dropAll(level, new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.GREEDY_TICKET),
                rewards.greedyTicketsPerRank * tier.getRankIndex(), x, y, z);
    }

    /**
     * Drops {@code count} of an item as however many legal stacks that takes; the payout passes one stack at the
     * top of the table.
     */
    private static void dropAll(ServerLevel level, ItemStack prototype, int count, double x, double y, double z) {
        int max = prototype.getMaxStackSize();
        if (count <= 0 || max <= 0) {
            return;
        }
        for (int remaining = count; remaining > 0; ) {
            int size = Math.min(remaining, max);
            ItemStack stack = prototype.copy();
            stack.setCount(size);
            drop(level, stack, x, y, z);
            remaining -= size;
        }
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
     * Pushes the pool, the damage dealt against it and the Vessel's time-ramp multiplier to every runner; the bar
     * itself is the greed trial's, drawn client side.
     */
    public static void syncBar(Vault vault, LivingEntity champion) {
        double pool = poolOf(champion);
        float multiplier = champion instanceof TheVesselEntity vessel ? vessel.getDamageMultiplier() : 1.0F;
        ChampionHudMessage message = new ChampionHudMessage(true, (float) dealtOf(champion), (float) pool, multiplier);
        for (ServerPlayer runner : GodVaultUtil.runners(vault)) {
            ModNetwork.sendToClient(message, runner);
        }
    }

    public static void closeBar(Vault vault) {
        ChampionHudMessage message = new ChampionHudMessage(false, 0.0F, 0.0F, 1.0F);
        for (ServerPlayer runner : GodVaultUtil.runners(vault)) {
            ModNetwork.sendToClient(message, runner);
        }
    }
}
