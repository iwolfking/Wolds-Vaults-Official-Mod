package xyz.iwolfking.woldsvaults.mixins.vaulthunters.ancient;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.util.LootInitialization;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gear.ancient.AncientLootStamp;

import javax.annotation.Nullable;

@Mixin(value = LootInitialization.class, remap = false)
public class MixinLootInitializationLootRank {

    /** Stamps the vault's medallion rank on vault loot once it is fully initialised. */
    @ModifyReturnValue(method = "initializeVaultLoot(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/core/vault/Vault;Lnet/minecraft/core/BlockPos;Liskallia/vault/core/random/RandomSource;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private static ItemStack stampLootRank(ItemStack initialized, ItemStack stack, @Nullable Vault vault, @Nullable BlockPos pos, RandomSource random) {
        return AncientLootStamp.stamp(initialized, vault);
    }
}
