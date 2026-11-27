package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.CardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;

import java.util.function.Consumer;

@Mixin(value = CardItem.class, remap = false)
public class MixinCardItemModels {
    /**
     * Registers every non-Vault Hunters card icon model. Vault Hunters only registers the icons named in the card
     * modifier config on disk, which misses cards added through VHAPI overlays.
     */
    @Inject(method = "loadModels", at = @At("TAIL"))
    private void woldsvaults$loadOverlayCardIcons(Consumer<ModelResourceLocation> consumer, CallbackInfo ci) {
        for (ResourceLocation location : Minecraft.getInstance().getResourceManager().listResources("models/item/card/icon", path -> path.endsWith(".json"))) {
            if (!location.getNamespace().equals("the_vault")) {
                consumer.accept(ResourceLocUtils.stripLocationForItemModel(location));
            }
        }
    }
}
