package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.block.entity.VaultGlobeBlockEntity;
import iskallia.vault.core.vault.RoomCache;
import iskallia.vault.core.vault.Vault;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = VaultGlobeBlockEntity.class, remap = false)
public abstract class MixinVaultGlobeBlockEntity {
    @Shadow
    protected abstract boolean discoverRoomsOfType(ServerPlayer player, Vault vault, RoomCache.RoomType roomType);

    @WrapOperation(method = "lambda$handleActivation$0", at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/VaultGlobeBlockEntity;discoverRoomsOfType(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/core/vault/Vault;Liskallia/vault/core/vault/RoomCache$RoomType;)Z"))
    private boolean alsoDiscoverRawRoomsForOreGlobes(VaultGlobeBlockEntity instance, ServerPlayer player, Vault vault, RoomCache.RoomType roomType, Operation<Boolean> original) {
        if(roomType.equals(RoomCache.RoomType.ORE)) {
            this.discoverRoomsOfType(player, vault, RoomCache.RoomType.RAW);
        }

        return original.call(instance, player, vault, roomType);
    }
}
