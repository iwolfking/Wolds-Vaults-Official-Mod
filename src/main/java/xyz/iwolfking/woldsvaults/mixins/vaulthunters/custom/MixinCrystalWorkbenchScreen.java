package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;


import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.container.CrystalWorkbenchContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;

//TODO: Figure out how Crystal Workbench screen works now, fix up this logic
@Mixin(value = iskallia.vault.client.gui.screen.block.crystal.CrystalWorkbenchScreen.class, remap = false)
public abstract class MixinCrystalWorkbenchScreen extends AbstractElementContainerScreen<CrystalWorkbenchContainer> {
    public MixinCrystalWorkbenchScreen(CrystalWorkbenchContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementContainerScreen<CrystalWorkbenchContainer>> tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }
//    public MixinCrystalWorkbenchScreen(CrystalWorkbenchContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementContainerScreen<CrystalWorkbenchContainer>> tooltipRendererFactory) {
//        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
//    }
//
//    @Inject(method = "renderItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getSlotIndex()I", ordinal = 0))
//    private void addAdditionalSlotTooltips(PoseStack poseStack, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir, @Local List<Component> tooltips) {
//        if(this.hoveredSlot.getSlotIndex() == 4) {
//            tooltips.add(new TextComponent("Layout Manipulator").withStyle(ChatFormatting.WHITE));
//            tooltips.add(new TextComponent("Insert a layout manipulator here to define the layout of the vault, or leave it empty for a random layout").withStyle(ChatFormatting.GRAY));
//        }
//        else if(this.hoveredSlot.getSlotIndex() == 5) {
//            tooltips.add(new TextComponent("Miscellaneous Modification").withStyle(ChatFormatting.WHITE));
//            tooltips.add(new TextComponent("Insert supported items like Vault Maps here for various ways of modifying your vault").withStyle(ChatFormatting.GRAY));
//        }
//    }
//
//
//    @Override
//    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
//        super.render(poseStack, mouseX, mouseY, partialTick);
//
//        ItemStack crystalStack = this.getMenu().getSlot(36).getItem();
//        if(crystalStack.isEmpty()) {
//            return;
//        }
//
//        CrystalData crystalData = CrystalData.read(crystalStack);
//        if (!(crystalData.getProperties() instanceof CapacityCrystalProperties capacityProps)) {
//            return;
//        }
//
//
//        ItemStack ingredientStack = this.getMenu().getSlot(102).getItem();
//        if(ingredientStack.getItem() instanceof IVaultCrystalModifier vaultCrystalModifier) {
//            ItemStack capstoneStack = this.getMenu().getSlot(99).getItem();
//            if(!capstoneStack.isEmpty() && !ingredientStack.isEmpty()) {
//                woldsVaults$renderCursedSlot(getGuiLeft() + 147, getGuiTop() + 33, poseStack);
//                return;
//            }
//
//            if(vaultCrystalModifier.hasApplied(crystalStack)) {
//                ItemStack sealStack = this.getMenu().getSlot(97).getItem();
//                if(!sealStack.isEmpty() && ingredientStack.getItem() instanceof VaultMapItem) {
//                    woldsVaults$renderCursedSlot(getGuiLeft() + 172, getGuiTop() + 19, poseStack);
//                }
//
//                ItemStack augmentStack = this.getMenu().getSlot(98).getItem();
//                if(augmentStack.getItem() instanceof AugmentItem && ingredientStack.getItem() instanceof VaultMapItem) {
//                    woldsVaults$renderCursedSlot(getGuiLeft() + 197, getGuiTop() + 33, poseStack);
//                }
//                return;
//            }
//
//            int capacityRequired = vaultCrystalModifier.getCapacityConsumption(ingredientStack);
//            if (!capacityProps.canAccept(capacityRequired)) {
//                woldsVaults$renderCursedSlot(getGuiLeft() + 147, getGuiTop() + 33, poseStack);
//            }
//        }
//    }
//
//    @Unique
//    private void woldsVaults$renderCursedSlot(int x, int y, PoseStack poseStack) {
//        renderSlotHighlight(poseStack, x, y, 20, -1670497536);
//        poseStack.pushPose();
//        poseStack.translate(0.0D, 0.0D, 400.0D);
//        Minecraft.getInstance().font.draw(poseStack, "☠", (float) (x + 8), (float) (y + 8), -16777216);
//        poseStack.popPose();
//    }
}
