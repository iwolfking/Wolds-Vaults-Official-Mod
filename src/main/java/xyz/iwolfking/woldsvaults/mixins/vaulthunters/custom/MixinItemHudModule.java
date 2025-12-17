package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.client.render.hud.module.AbstractHudModule;
import iskallia.vault.client.render.hud.module.ItemHudModule;
import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemHudModule.class, remap = false)
public abstract class MixinItemHudModule extends AbstractHudModule<ModuleRenderContext> {
    protected MixinItemHudModule(String key, String displayName, String description) {
        super(key, displayName, description);
    }

    @Inject(method = "getBaseTexture", at = @At("HEAD"), cancellable = true)
    private void getBaseTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        switch (this.key()) {
            case "trinket_3" -> cir.setReturnValue(VaultMod.id("textures/gui/inventory_hud/green_trinket.png"));
            case "shard_pouch" -> cir.setReturnValue(VaultMod.id("textures/gui/inventory_hud/shard_pouch.png"));
            case "trinket_pouch" -> cir.setReturnValue(VaultMod.id("textures/gui/inventory_hud/trinket_pouch.png"));
        }
    }

    @Inject(method = "getOverlayTexture", at = @At("HEAD"), cancellable = true)
    private void getOverlayTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        switch (this.key()) {
            case "trinket_3" -> cir.setReturnValue(VaultMod.id("textures/gui/inventory_hud/green_trinket.png"));
            case "shard_pouch" -> cir.setReturnValue(VaultMod.id("textures/gui/inventory_hud/shard_pouch.png"));
            case "trinket_pouch" -> cir.setReturnValue(VaultMod.id("textures/gui/inventory_hud/trinket_pouch.png"));
        }
    }
}
