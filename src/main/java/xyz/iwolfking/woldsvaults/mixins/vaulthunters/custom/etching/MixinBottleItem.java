package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.etching;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.bottle.BottleItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.WoldEtchingHelper;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PotionDrinkingAccessor;

//TODO: Rework or remove Prudent Chaos etching
@Mixin(value = BottleItem.class, remap = false)
public class MixinBottleItem {
//    @Inject(method = "consumeCharge", at = @At(value = "TAIL"))
//    private static void prudentChaosEtching(ItemStack stack, ServerPlayer player, CallbackInfo ci, @Local(name = "probability") float probability) {
//        if(WoldEtchingHelper.hasEtching(player, ModEtchingGearAttributes.PRUDENT_CHAOS) && player.getRandom().nextFloat() <= probability) {
//            int effectNum = player.getRandom().nextInt(0, ModConfigs.VAULT_ALCHEMY_TABLE.getCraftableEffects().size());
//            ModConfigs.VAULT_ALCHEMY_TABLE.getCraftableEffects().get(effectNum).createEffect(BottleItem.Type.BREW).ifPresent(effect -> ((PotionDrinkingAccessor)effect).callTrigger(player));
//        }
//    }
}
