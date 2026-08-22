package xyz.iwolfking.woldsvaults.medallions.assassins;

import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;

/**
 * The medallion-era replacement for {@code GreedAssassinSpawnHandler#handleGreedAssassinKill}.
 *
 * <p>Two changes. The coin drop is no longer a 1-3 roll but half the medallion's base greed coin
 * value, which is what makes assassins one of the two main greed coin faucets in the rework without
 * making them the only one worth farming. And the greed reputation grant is gone entirely:
 * reputation is a milestone-only currency now, so an assassin kill pays coins and nothing else.</p>
 */
public final class GreedAssassinKills {
    private GreedAssassinKills() {
    }

    public static void handleKill(LivingEntity killed, LivingDeathEvent event, ServerLevel level) {
        GreedAssassinRegistry.forget(killed);
        ServerPlayer player = GreedAssassinSpawner.resolveKiller(event);
        if (player == null) {
            return;
        }
        GreedMedallionTier tier = GreedAssassins.getTier(killed).orElse(null);
        if (tier == null) {
            WoldsVaults.LOGGER.warn("Greed assassin died with no medallion tier on it and in no medallion vault - dropping no coins.");
            return;
        }
        int coinCount = Math.max(1, tier.getBaseGreedCoins() / 2);
        ItemStack coins = new ItemStack(ModItems.GREED_COIN, coinCount);
        ItemEntity coinEntity = new ItemEntity(killed.level, killed.getX(), killed.getY(), killed.getZ(), coins);
        coinEntity.setNoPickUpDelay();
        killed.level.addFreshEntity(coinEntity);
        player.displayClientMessage(new TextComponent("+" + coinCount + " ").withStyle(ChatFormatting.GRAY)
                .append((Component) new TextComponent("Greed Coins").withStyle(ChatFormatting.GOLD)), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GREED_COINS_SHAKE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
