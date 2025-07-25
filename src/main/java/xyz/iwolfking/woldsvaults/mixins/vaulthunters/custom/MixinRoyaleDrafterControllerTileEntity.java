package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.RoyaleDrafterControllerTileEntity;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(value = RoyaleDrafterControllerTileEntity.class, remap = false)
public class MixinRoyaleDrafterControllerTileEntity {
//    @Unique
//    private Map<UUID, List<ResourceLocation>> woldsVaults$greenTrinketPresets = new HashMap<>();
//
//    @Inject(method = "generatePlayerIfNotExists", at = @At("TAIL"))
//    private void addGreenTrinketSupport(Player player, CallbackInfo ci, @Local Vault vault, @Local int vaultLevel, @Local UUID playerId) {
//        if(vault == null) {
//            return;
//        }
//
//        if(this.woldsVaults$greenTrinketPresets.containsKey(player.getUUID())) {
//            this.woldsVaults$greenTrinketPresets.put(playerId, ModConfigs.PRESET_CONFIG.getRandomBlueTrinketPresets(vaultLevel));
//        }
//    }
}
