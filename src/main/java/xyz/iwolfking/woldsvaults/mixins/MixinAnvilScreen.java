package xyz.iwolfking.woldsvaults.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Optional;

@Mixin(AnvilScreen.class)
public class MixinAnvilScreen extends ItemCombinerScreen<AnvilMenu> {
    public MixinAnvilScreen(AnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle,
                            ResourceLocation pMenuResource) {
        super(pMenu, pPlayerInventory, pTitle, pMenuResource);
    }

    @Inject(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;getCost()I"))
    private void addUnfinishedMapWarning(PoseStack pPoseStack, int pX, int pY, CallbackInfo ci) {
        var primary = this.menu.getSlot(0).getItem();
        var secondary = this.menu.getSlot(1).getItem();
        if (primary.getItem() != iskallia.vault.init.ModItems.VAULT_CRYSTAL
            || secondary.getItem() != ModItems.MAP
            || !this.menu.getSlot(3).hasItem()) {
            return;
        }
        VaultGearData mapData = VaultGearData.read(secondary);

        Optional<Integer> prefixSlots = mapData.getFirstValue(iskallia.vault.init.ModGearAttributes.PREFIXES);
        Optional<Integer> suffixSlots = mapData.getFirstValue(iskallia.vault.init.ModGearAttributes.SUFFIXES);

        if (prefixSlots.isEmpty() || suffixSlots.isEmpty()) {
            return;
        }

        int numberOfPrefixes = mapData.getModifiers(VaultGearModifier.AffixType.PREFIX).size();
        int numberOfSuffixes = mapData.getModifiers(VaultGearModifier.AffixType.SUFFIX).size();

        boolean unfinishedMap = prefixSlots.get() != numberOfPrefixes || suffixSlots.get() != numberOfSuffixes;

        if (unfinishedMap) {
            var component = new TextComponent("Unfinished Map!");
            int k = this.imageWidth - 8 - this.font.width(component) - 2;
            fill(pPoseStack, k - 2, 67, this.imageWidth - 8, 79, 1325400064);
            this.font.drawShadow(pPoseStack, component, k, 69.0F, 16736352);
        }
    }
}
