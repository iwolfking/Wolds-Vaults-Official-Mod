package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.MagnetItem;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;

@Mixin(value = VaultGearHelper.class, remap = false)
public class MixinVaultGearHelper {

//    @Inject(method = "rightClick", at = @At("HEAD"), cancellable = true)
//    private static void tryToQuickEquipGear(Level world, Player player, InteractionHand hand, InteractionResultHolder<ItemStack> defaultUse, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
//        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof VaultGearItem vaultGearItem) {
//            ItemStack mainhandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
//            GearDataCache cache = GearDataCache.of(mainhandStack);
//            if (cache.getState() != null && cache.getState().equals(VaultGearState.IDENTIFIED)) {
//                VaultGearType gearType = vaultGearItem.getGearType(mainhandStack);
//                boolean quickEquipped = false;
//
//                if (gearType.equals(VaultGearType.MAGNET)) {
//                    quickEquipped = quickEquipGear("belt", mainhandStack, player);
//                }
//                else if (gearType.equals(VaultGearType.VOID_STONE)) {
//                    quickEquipped = quickEquipGear("necklace", mainhandStack, player);
//                }
//                else if (gearType.equals(VaultGearType.CHARM)) {
//                    quickEquipped = quickEquipGear("charm", mainhandStack, player);
//                }
//
//
//                if (quickEquipped) {
//                    cir.setReturnValue(defaultUse);
//                }
//            }
//        }
//    }

    private static boolean quickEquipGear(String curioSlot, ItemStack quickEquipStack, Player player) {
        ItemStack existingStack = IntegrationCurios.getCurioItemStack(player, curioSlot, 0);
        if(!existingStack.isEmpty()) {
            return false;
        }

        IntegrationCurios.setCurioItemStack(player, quickEquipStack.copy(), curioSlot, 0);
        quickEquipStack.setCount(0);
        return true;
    }

    @Inject(method = "initializeGearRollType(Lnet/minecraft/world/item/ItemStack;ILiskallia/vault/core/random/RandomSource;)V", at= @At(value = "TAIL"))
    private static void initializeMapTier(ItemStack stack, int gearLevel, RandomSource random, CallbackInfo ci, @Local VaultGearData data) {
        if(stack.getItem() instanceof VaultMapItem mapItem) {
            if(!data.hasAttribute(ModGearAttributes.MAP_TIER)) {
                int tier = stack.getOrCreateTag().getInt("the_vault:map_tier");
                data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, tier);
                data.write(stack);
            }
        }
    }
}
