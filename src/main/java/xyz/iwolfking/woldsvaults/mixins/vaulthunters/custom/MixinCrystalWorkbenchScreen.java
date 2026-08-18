package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.screen.block.CrystalWorkbenchScreen;
import iskallia.vault.container.CrystalWorkbenchContainer;
import iskallia.vault.item.AugmentItem;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;

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


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        CrystalWorkbenchScreen screen = (CrystalWorkbenchScreen) (Object) this;
        CrystalWorkbenchContainer container = screen.getMenu();

        ItemStack crystalStack = container.getEntity().getOutput().getItem(0);
        if(crystalStack.isEmpty()) {
            return;
        }

        CrystalData crystalData = CrystalData.read(crystalStack);
        if (!(crystalData.getProperties() instanceof CapacityCrystalProperties capacityProps)) {
            return;
        }


        ItemStack ingredientStack = this.getMenu().getEntity().getUniqueIngredients().getItem(5);
        if(ingredientStack.getItem() instanceof IVaultCrystalModifier vaultCrystalModifier) {
            ItemStack capstoneStack = this.getMenu().getEntity().getUniqueIngredients().getItem(2);
            if(!capstoneStack.isEmpty() && !ingredientStack.isEmpty()) {
                woldsVaults$renderCursedSlot(getGuiLeft() + 147, getGuiTop() + 33, poseStack);
                return;
            }

            if(vaultCrystalModifier.hasApplied(crystalStack)) {
                ItemStack sealStack = this.getMenu().getEntity().getUniqueIngredients().getItem(0);
                if(!sealStack.isEmpty() && ingredientStack.getItem() instanceof VaultMapItem) {
                    woldsVaults$renderCursedSlot(getGuiLeft() + 172, getGuiTop() + 19, poseStack);
                }

                ItemStack augmentStack = this.getMenu().getEntity().getUniqueIngredients().getItem(1);
                if(augmentStack.getItem() instanceof AugmentItem && ingredientStack.getItem() instanceof VaultMapItem) {
                    woldsVaults$renderCursedSlot(getGuiLeft() + 197, getGuiTop() + 33, poseStack);
                }
                return;
            }

            int capacityRequired = vaultCrystalModifier.getCapacityConsumption(ingredientStack);
            if (!capacityProps.canAccept(capacityRequired)) {
                woldsVaults$renderCursedSlot(getGuiLeft() + 147, getGuiTop() + 33, poseStack);
            }
        }
    }

    @Unique
    private void woldsVaults$renderCursedSlot(int x, int y, PoseStack poseStack) {
        renderSlotHighlight(poseStack, x, y, 20, -1670497536);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 400.0D);
        Minecraft.getInstance().font.draw(poseStack, "☠", (float) (x + 8), (float) (y + 8), -16777216);
        poseStack.popPose();
    }
}
