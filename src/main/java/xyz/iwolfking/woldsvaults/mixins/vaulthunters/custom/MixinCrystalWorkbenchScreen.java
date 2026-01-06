package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.screen.block.CrystalWorkbenchScreen;
import iskallia.vault.container.CrystalWorkbenchContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = CrystalWorkbenchScreen.class, remap = false)
public class MixinCrystalWorkbenchScreen extends AbstractElementContainerScreen<CrystalWorkbenchContainer> {
    public MixinCrystalWorkbenchScreen(CrystalWorkbenchContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementContainerScreen<CrystalWorkbenchContainer>> tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }

    @Inject(method = "renderHoveredSlotTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getSlotIndex()I", ordinal = 0))
    private void addAdditionalSlotTooltips(PoseStack poseStack, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir, @Local List<Component> tooltips) {
        if(this.hoveredSlot.getSlotIndex() == 4) {
            tooltips.add(new TextComponent("Layout Manipulator").withStyle(ChatFormatting.WHITE));
            tooltips.add(new TextComponent("Insert a layout manipulator here to define the layout of the vault, or leave it empty for a random layout").withStyle(ChatFormatting.GRAY));
        }
        else if(this.hoveredSlot.getSlotIndex() == 5) {
            tooltips.add(new TextComponent("Miscellaneous Modification").withStyle(ChatFormatting.WHITE));
            tooltips.add(new TextComponent("Insert supported items like Vault Maps here for various ways of modifying your vault").withStyle(ChatFormatting.GRAY));
        }
    }
}
