package xyz.iwolfking.woldsvaults.items;

import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import iskallia.vault.block.entity.CompanionHomeTileEntity;
import iskallia.vault.block.entity.VaultAltarTileEntity;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModSounds;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.CompanionItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class CompanionRerollingItem extends BasicItem {
    public CompanionRerollingItem(ResourceLocation id) {
        super(id);
    }

    public static InteractionResult rerollCompanionModifier(Level level, ItemStack stackInHand, Player player, BlockPos pos) {
        if(level.isClientSide) {
            return InteractionResult.PASS;
        }

        if(player == null) {
            return InteractionResult.FAIL;
        }

        if(level.getBlockEntity(pos) instanceof CompanionHomeTileEntity home && stackInHand.getItem() instanceof CompanionRerollingItem) {
            if(!home.getCompanion().isEmpty() && player.isShiftKeyDown()) {
                ItemStack companion = home.getCompanion();
                VaultModifier<?> modifier;
                if(stackInHand.hasTag() && stackInHand.getOrCreateTag().contains("modifier")) {
                    String modifierId = stackInHand.getOrCreateTag().getString("modifier");
                    modifier = VaultModifierRegistry.get(new ResourceLocation(modifierId));
                }
                else {
                    modifier = CompanionItem.getRandomTemporalModifier();
                }

                CompanionItem.setTemporalModifier(companion, modifier.getId());
                stackInHand.shrink(1);
                player.displayClientMessage(new TextComponent("Your Companion now has the ").append(modifier.getNameComponent()).append(" modifier!"), true);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player == null) {
            return InteractionResult.FAIL;
        }

        return CompanionRerollingItem.rerollCompanionModifier(context.getLevel(), player.getItemInHand(context.getHand()), player, context.getClickedPos());
    }
}
