package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.HeraldTrophyBlock;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.HeraldObjective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredRecipesData;

@Mixin(value = HeraldObjective.class, remap = false)
public class MixinHeraldObjective {
    @Inject(method = "lambda$initServer$5", at = @At(value = "INVOKE", target = "Liskallia/vault/block/item/HeraldTrophyItem;create(Ljava/util/UUID;Ljava/lang/String;Liskallia/vault/block/HeraldTrophyBlock$Variant;I)Lnet/minecraft/world/item/ItemStack;"))
    private void unlockHyperTrinketPouch(VirtualWorld world, Vault vault, LivingDeathEvent event, CallbackInfo ci, @Local int time, @Local Listener listener) {
        if(ModConfigs.HERALD_TROPHY.getTrophy(time).equals(HeraldTrophyBlock.Variant.PLATINUM)) {
            if(listener.getPlayer().isPresent()) {
                ServerPlayer player = listener.getPlayer().get();
                if(!DiscoveredRecipesData.get(player.getLevel()).hasDiscovered(player, WoldsVaults.id("hyper_trinket_pouch"))) {
                    DiscoveredRecipesData.get(player.getLevel()).discoverRecipeAndBroadcast(WoldsVaults.id("hyper_trinket_pouch"), player);
                }
            }
        }
    }
}
