package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ExpertiseOrbItem extends BasicItem {
    public ExpertiseOrbItem(ResourceLocation id) {
        super(id);
    }

    protected SoundEvent getSuccessSound() {
        return SoundEvents.PLAYER_LEVELUP;
    }

    protected Style getNameColor() {
        return Style.EMPTY.withColor(TextColor.parseColor("#ff80ff"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getMainHandItem();
        if(!hand.equals(InteractionHand.MAIN_HAND) || !(heldStack.getItem() instanceof ExpertiseOrbItem)) {
            return InteractionResultHolder.pass(heldStack);
        }

        if(!level.isClientSide()) {
            MinecraftServer server = player.getServer();
            if(server != null) {
                PlayerVaultStatsData stats = PlayerVaultStatsData.get(server);
                PlayerVaultStats playerStats = stats.getVaultStats(player);
                if((playerStats.getTotalSpentExpertisePoints() + playerStats.getUnspentExpertisePoints()) <= 500 && playerStats.getVaultLevel() >= 100) {
                    playerStats.addExpertisePoints(1).sync(server);
                    heldStack.shrink(1);
                    level.playSound(player, player.getOnPos(), getSuccessSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    return InteractionResultHolder.success(heldStack);
                }
            }

        }

        return InteractionResultHolder.pass(heldStack);
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return new TranslatableComponent(this.getDescriptionId(stack)).withStyle(getNameColor());
    }
}
