package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.companion.CompanionEggHunt;
import iskallia.vault.core.vault.companion.HuntInstance;
import iskallia.vault.core.vault.companion.HuntMap;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.VaultCompassMode;
import iskallia.vault.world.data.VaultPlayerCompassData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CompanionEggHunt.class, remap = false, priority = 15000)
public abstract class MixinCompanionEggHunt extends DataObject<CompanionEggHunt> {
    @Shadow
    @Final
    public static FieldKey<HuntMap> HUNTS;

    @Inject(method = "initServer", at = @At("TAIL"))
    private void addEndergizedEggUnlockSupport(VirtualWorld world, Vault vault, CallbackInfo ci) {
        CommonEvents.PLAYER_ENTITY_PICKUP.register(this, event -> {
           if(event.getPlayer() instanceof ServerPlayer player) {
               if(player.level == world) {
                   if(event.getItem().getItem().is(ModItems.COMPANION_EGG)) {
                       BlockPos spawnPosition = this.map(HUNTS, hunt -> hunt.get(player.getUUID()).getOptional(HuntInstance.SPAWNED_POSITION).orElse(null), null);
                       if(spawnPosition != null) {
                           if(!(event.getPlayer().blockPosition().distSqr(spawnPosition) > 16.0)) {
                               VaultPlayerCompassData.addProgress(player, VaultCompassMode.COMPANION_EGG, 1);
                           }
                       }
                   }
               }
           }
        });
    }

    @Inject(method = "offerCompanionEgg", at = @At(value = "INVOKE", target = "Liskallia/vault/item/CompanionEggItem;setSeries(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/item/CompanionSeries;)V"))
    private void completeCompanionEggCompass(ServerPlayer player, Vault vault, VirtualWorld world, CallbackInfoReturnable<Pair<Boolean, BlockPos>> cir) {
        VaultPlayerCompassData.addProgress(player, VaultCompassMode.COMPANION_EGG, 1);
    }
}
