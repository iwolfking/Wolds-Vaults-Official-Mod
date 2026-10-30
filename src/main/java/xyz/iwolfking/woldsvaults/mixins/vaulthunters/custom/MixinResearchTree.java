package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.Restrictions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ResearchTree.class, remap = false)
public abstract class MixinResearchTree {
//    @Inject(method = "restrictedBy(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/research/Restrictions$Type;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
//    private void dontRestrictDyedBackpackCrafting(ItemStack item, Restrictions.Type restrictionType, CallbackInfoReturnable<String> cir) {
//        if(restrictionType.equals(Restrictions.Type.CRAFTABILITY)) {
//            if(item.getItem() instanceof BackpackItem) {
//                if(item.hasTag()) {
//                    if(item.getTag() != null && (item.getTag().contains("clothColor") || item.getTag().contains("borderColor"))) {
//                        cir.setReturnValue(null);
//                    }
//                }
//            }
//        }
//    }
}
