package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.VaultCrateBlock;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

@Mixin(value = VaultCrateBlock.class, remap = false)
public abstract class MixinVaultCrateBlock {

    @Inject(method = "getCrateBlock", at = @At("HEAD"), cancellable = true)
    private static void handleAdditionalCrates(VaultCrateBlock.Type type, CallbackInfoReturnable<Block> cir) {
        if(ModBlocks.CUSTOM_VAULT_CRATES.containsKey(type.name().toLowerCase())) {
            cir.setReturnValue(ModBlocks.getCrateFor(type.name()));
        }
        else if(type.equals(VaultCrateBlock.Type.valueOf("CORRUPTED"))) {
            cir.setReturnValue(ModBlocks.getCrateFor("corrupt"));
        }
    }

}