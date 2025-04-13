package xyz.iwolfking.woldsvaults.mixins.vhatcaniroll;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import com.radimous.vhatcaniroll.ui.*;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.*;
import iskallia.vault.client.gui.framework.element.spi.ILayoutElement;
import iskallia.vault.client.gui.framework.element.spi.ILayoutStrategy;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.gear.EtchingItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.integration.vhatcaniroll.EtchingListContainer;

@Mixin(value = GearModifierScreen.class, remap = false)
public abstract class MixinGearModifierScreen extends AbstractElementScreen {

    @Shadow private InnerGearScreen innerScreen;
    @Shadow private ModifierCategory modifierCategory;
    @Shadow @Final private LabelElement<?> windowNameLabel;
    @Shadow @Final private ScrollableLvlInputElement lvlInput;
    @Shadow protected abstract ILayoutStrategy translateWorldSpatial();
    @Shadow protected abstract void updateModifierCategoryButtonLabel();
    @Shadow protected abstract void disableCategoryButtons();
    @Shadow public abstract ItemStack getCurrGear();

    @Shadow protected abstract void disableLvlButtons();

    @Shadow protected abstract void enableLvlButtons();

    public MixinGearModifierScreen(Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementScreen> tooltipRendererFactory) {
        super(title, elementRenderer, tooltipRendererFactory);
    }

//    @Unique
//    private void woldsVaults$switchToEtching() {
//        this.removeElement(this.innerScreen);
//        this.modifierCategory = ModifierCategory.NORMAL;
//        this.updateModifierCategoryButtonLabel();
//        ISpatial modListSpatial = Spatials.positionXY(7, 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57);
//        this.innerScreen = (InnerGearScreen)(new EtchingListContainer(modListSpatial)).layout(this.translateWorldSpatial());
//        this.disableLvlButtons();
//        this.disableCategoryButtons();
//        this.windowNameLabel.set((new TranslatableComponent("vhatcaniroll.screen.title.etching")).withStyle(ChatFormatting.BLACK));
//        this.addElement(this.innerScreen);
//        ScreenLayout.requestLayout();
//    }

    /**
     * @author iwolfking
     * @reason Change handling when Etching is the selected item.
     */
    @Inject(method = "updateModifierList", at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/framework/spatial/Spatials;positionXY(II)Liskallia/vault/client/gui/framework/spatial/spi/IMutableSpatial;"), cancellable = true)
    private void updateModifierList(boolean keepScroll, CallbackInfo ci) {
        if(this.getCurrGear().getItem().equals(ModItems.ETCHING)) {
            this.innerScreen = new EtchingListContainer(Spatials.positionXY(7, 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57));
            this.disableCategoryButtons();
            this.disableLvlButtons();
            this.windowNameLabel.set((new TranslatableComponent("vhatcaniroll.screen.title.etching")).withStyle(ChatFormatting.BLACK));
        }
        float oldScroll = this.innerScreen.getScroll();
        InnerGearScreen var5 = this.innerScreen;
        if (var5 instanceof ILayoutElement<?> layoutElement) {
            layoutElement.layout(this.translateWorldSpatial());
        }

        if (keepScroll) {
            this.innerScreen.setScroll(oldScroll);
        }

        this.addElement(this.innerScreen);
        ScreenLayout.requestLayout();
        ci.cancel();
    }

    @Inject(method = "switchToTransmog", at = @At("HEAD"), cancellable = true)
    private void switchToTransmog(CallbackInfo ci) {
        if(this.getCurrGear().is(ModItems.ETCHING)) {
            ci.cancel();
        }
    }

    @Inject(method = "switchToCrafted", at = @At("HEAD"), cancellable = true)
    private void switchToCrafted(CallbackInfo ci) {
        if(this.getCurrGear().is(ModItems.ETCHING)) {
            ci.cancel();
        }
    }

    @Inject(method = "switchToUnique", at = @At("HEAD"), cancellable = true)
    private void switchToUnique(CallbackInfo ci) {
        if(this.getCurrGear().is(ModItems.ETCHING)) {
            ci.cancel();
        }
    }
}
