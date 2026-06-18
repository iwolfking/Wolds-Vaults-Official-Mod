package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.modifications;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.skill.ability.effect.spi.AbstractVeinMinerAbility;
import iskallia.vault.skill.ability.effect.spi.core.HoldAbility;
import iskallia.vault.util.BlockBreakHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.VeinMinerChainAbility;
import xyz.iwolfking.woldsvaults.api.util.WoldBreakHandler;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckGetRange;

@Mixin(value = AbstractVeinMinerAbility.class, remap = false)
public abstract class MixinAbstractVeinMinerAbility extends HoldAbility implements DuckGetRange {

    @Shadow
    protected abstract ItemStack getVeinMiningItemProxy(Player player);

    @Shadow
    public abstract boolean shouldVoid(ServerLevel level, ServerPlayer player, BlockState blockState);


    @Shadow @Mutable @Final
    private BlockBreakHandler blockBreakHandler;

    @Shadow public abstract int getUnmodifiedBlockLimit();

    /**
     * @author aida
     * @reason to fix veinmine being affected by +aoe%
     */
    @Overwrite
    public int getBlockLimit(Player player) {
        return getUnmodifiedBlockLimit();
    }

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void replaceHandlerInstance(CallbackInfo ci) {
        blockBreakHandler = new WoldBreakHandler() {
            @Override
            protected int getBlockLimit(Player player) {
                return MixinAbstractVeinMinerAbility.this.getUnmodifiedBlockLimit();
            }

            @Override
            protected ItemStack getMiningItemProxy(Player player) {
                return MixinAbstractVeinMinerAbility.this.getVeinMiningItemProxy(player);
            }

            @Override
            protected boolean shouldVoid(ServerLevel level, ServerPlayer player, BlockState blockState) {
                return MixinAbstractVeinMinerAbility.this.shouldVoid(level, player, blockState);
            }

            @Override
            public int getRange() {
                return MixinAbstractVeinMinerAbility.this.getRange();
            }
        };
    }

    @Unique
    private static boolean woldsVaults$preCheck(ServerLevel level, ServerPlayer player, BlockPos pos, Block targetBlock) {
        if (targetBlock == Blocks.AIR) {
            return false;
        } else if (!level.getBlockState(pos).canHarvestBlock(level, pos, player)) {
            return false;
        } else {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (heldItem.isDamageableItem()) {
                if (heldItem.getItem() instanceof VaultGearItem gearItem && gearItem.isBroken(heldItem)) {
                    return false;
                }

                int usesLeft = heldItem.getMaxDamage() - heldItem.getDamageValue();
                return usesLeft > 1;
            }

            return true;
        }
    }

    @Redirect(method = "lambda$onBlockMined$0",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/util/BlockBreakHandler;areaDig(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)Z"))
    private static boolean onBlockMined(BlockBreakHandler handler, ServerLevel level, ServerPlayer player, BlockPos pos, Block targetBlock, @Local(argsOnly = true) AbstractVeinMinerAbility ability) {
        if(!woldsVaults$preCheck(level, player, pos, targetBlock))
            return false;

        if(ability.getClass() == VeinMinerChainAbility.class
        && ((WoldBreakHandler)handler).chainDig(level, player, pos, targetBlock))
            return true;

        return handler.areaDig(level, player, pos, targetBlock);
    }

}
