package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.block.model.VaultChestModel;
import iskallia.vault.init.ModChestModels;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModChestModels.class, remap = false)
public class MixinModChestModels {

    @Unique
    private static final ModelLayerLocation CORRUPTED_LOCATION = new ModelLayerLocation(VaultMod.id("corrupted_chest"), "main");

    @Inject(method = "registerLayerDefinitions", at = @At("HEAD"))
    private static void registerAdditionalLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event, CallbackInfo ci) {
        event.registerLayerDefinition(CORRUPTED_LOCATION, VaultChestModel::createTreasureLayer);
    }
}