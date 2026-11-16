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
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.medallions.GreedKills;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.milestones.MilestoneIds;
import xyz.iwolfking.woldsvaults.milestones.Milestones;

/**
 * The payout for killing a greed assassin: the assassin greed coins of the rank it was spawned under plus Assassin
 * Assassinator progress, and no greed reputation directly.
 */
public final class GreedAssassinKills {
    private GreedAssassinKills() {
    }

    public static void handleKill(LivingEntity killed, LivingDeathEvent event, ServerLevel level) {
        GreedAssassinRegistry.forget(killed);
        ServerPlayer player = GreedKills.resolveKiller(event);
        if (player == null) {
            return;
        }
        Milestones.advance(player, MilestoneIds.ASSASSIN_ASSASSINATOR, 1L);
        GreedMedallionTier tier = GreedAssassins.getTier(killed).orElse(null);
        if (tier == null) {
            WoldLogger.error("Greed assassin died with no medallion tier on it and in no medallion vault - dropping no coins.");
            return;
        }
        int coinCount = tier.getAssassinGreedCoins();
        if (coinCount <= 0) {
            return;
        }
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
