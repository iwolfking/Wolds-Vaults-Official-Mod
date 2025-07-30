package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.item.VaultDollItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.blocks.tiles.DollDismantlingTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

/**
 * This mixin allows to block Vault Doll placement on ground if enabled in config.
 */
@Mixin(value = VaultDollItem.class)
public class MixinVaultDollItem
{
    @Inject(method = "useOn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"),
            cancellable = true
    )
    private void onDollUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local ItemStack stack, @Local Player player, @Local Level level, @Local BlockPos clickedPos, @Local BlockState state)
    {
        if (state.is(ModBlocks.DOLL_DISMANTLING_BLOCK))
        {
            // This manages vault doll placement into dismantler, if it is empty.

            DollDismantlingTileEntity blockEntity =
                    (DollDismantlingTileEntity) level.getBlockEntity(clickedPos);

            if (blockEntity != null && blockEntity.getDoll().isEmpty())
            {
                if(!GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, player.getLevel())) {
                    cir.setReturnValue(InteractionResult.FAIL);
                }

                ItemStack clone = stack.copy();
                clone.setCount(1);
                stack.shrink(1);

                blockEntity.updateDoll(clone, (ServerPlayer) player);
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }
        }

        if(!GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, level)) {
            player.displayClientMessage(new TextComponent("Vaults Dolls are disabled!").withStyle(ChatFormatting.RED), true);
            cir.setReturnValue(InteractionResult.FAIL);
        }
        else if (!GameruleHelper.isEnabled(ModGameRules.ENABLE_PLACING_VAULT_DOLLS, level))
        {
            player.displayClientMessage(new TextComponent("Placing Vault Dolls is disabled, use a Doll Blender!").withStyle(ChatFormatting.RED), true);
            cir.setReturnValue(InteractionResult.FAIL);
        }

    }
}
