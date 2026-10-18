package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.entity.pet.PetModelType;
import iskallia.vault.www.Reward;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PetModelType.class, remap = false)
public class MixinPetModelType {
    @WrapOperation(method = "getVariants(Ljava/util/UUID;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Liskallia/vault/www/Reward;hasModel(Liskallia/vault/www/Reward$ArmorPiece;Lnet/minecraft/resources/ResourceLocation;)Z"))
    private boolean unlockAllPetModels(Reward instance, Reward.ArmorPiece armor, ResourceLocation id, Operation<Boolean> original) {
        return true;
    }
}
