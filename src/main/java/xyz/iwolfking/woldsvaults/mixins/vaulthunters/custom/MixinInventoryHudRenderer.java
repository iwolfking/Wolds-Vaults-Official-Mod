package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.render.InventoryHudRenderer;
import iskallia.vault.client.render.hud.module.InventoryHudModule;
import iskallia.vault.client.render.hud.module.ItemHudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModOptions;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = InventoryHudRenderer.class, remap = false)
public abstract class MixinInventoryHudRenderer {


    @Shadow
    @Final
    public static List<String> HUD_KEYS;

    @Shadow
    @Final
    private static List<InventoryHudModule<?>> MODULES;

    @Shadow
    private static ItemStack getCurioSlotByName(Player player, String identifier) {
        return null;
    }

    @Inject(method = "initModules", at = @At("HEAD"))
    private static void registerWoldsModules(CallbackInfo ci) {
        MODULES.add(new ItemHudModule("trinket_3", () -> getCurioSlotByName(Minecraft.getInstance().player, "green_trinket"), ModOptions.GREEN_TRINKET));
    }

    static {
        List<String> hudKeys = new ArrayList<>(HUD_KEYS);
        hudKeys.add("trinket_3");
        HUD_KEYS = hudKeys;
    }
}
