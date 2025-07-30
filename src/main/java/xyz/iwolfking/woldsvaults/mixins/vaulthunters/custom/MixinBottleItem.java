package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.bottle.BottleEffect;
import iskallia.vault.item.bottle.BottleEffectManager;
import iskallia.vault.item.bottle.BottleItem;
import iskallia.vault.item.core.DataInitializationItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(value = BottleItem.class, remap = false)
public class MixinBottleItem implements DataInitializationItem  {
    @Override
    public void initialize(ItemStack itemStack, RandomSource randomSource) {
        if(itemStack.hasTag() && itemStack.getTag() != null && itemStack.getTag().contains("VaultRoyaleVial")) {
            Random rand = new Random();
            String rechargeType = switch (rand.nextInt(0, 3)) {
                case 0 -> "time";
                case 1 -> "chests";
                case 2 -> "mobs";
                default -> "time";
            };
            itemStack.getOrCreateTag().putString(BottleItem.TYPE, "vial");
            itemStack.getOrCreateTag().putString(BottleItem.RECHARGE, rechargeType);
            itemStack.getOrCreateTag().putInt(BottleItem.CHARGES, rand.nextInt(0, 7));
            if(rand.nextFloat() < 0.8) {
                int effectNum = rand.nextInt(0, ModConfigs.VAULT_ALCHEMY_TABLE.getCraftableEffects().size());
                ModConfigs.VAULT_ALCHEMY_TABLE.getCraftableEffects().get(effectNum).createEffect(BottleItem.Type.VIAL).ifPresent(effect -> BottleItem.setEffect(itemStack, effect));
            }
        }
    }

    @Inject(method = "isActive(Liskallia/vault/core/vault/Vault;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void isActiveInVault(Vault vault, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(stack.hasTag() && stack.getTag() != null && stack.getTag().contains("VaultRoyaleVial")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isActive(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void isActiveOverride(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(stack.hasTag() && stack.getTag() != null && stack.getTag().contains("VaultRoyaleVial")) {
            cir.setReturnValue(true);
        }
    }
}
