package xyz.iwolfking.woldsvaults.mixins.vhatcaniroll;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import com.radimous.vhatcaniroll.ui.*;
import iskallia.vault.client.gui.framework.element.*;
import iskallia.vault.client.gui.framework.element.spi.ILayoutStrategy;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.BossRuneItemRenderer;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.integration.vhatcaniroll.EtchingListContainer;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "vhatcaniroll")
        }
)
@Mixin(value = GearModifierScreen.class, remap = false)
public abstract class MixinGearModifierScreen extends AbstractElementScreen {
    @Unique
    private InnerGearScreen woldsvaults$lastScreen;
    @Unique
    private Component woldsvaults$lastLabel;

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

    @Shadow protected abstract void enableCategoryButtons();

    @Shadow protected abstract void switchTab(int tabIndex);

    @Unique
    private static final Component ETCHING_COMPONENT = new TranslatableComponent("vhatcaniroll.screen.title.etching").withStyle(ChatFormatting.BLACK);

    public MixinGearModifierScreen(Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementScreen> tooltipRendererFactory) {
        super(title, elementRenderer, tooltipRendererFactory);
    }

    @Unique
    private void woldsVaults$switchToEtching() {
        this.removeElement(this.innerScreen);
        this.modifierCategory = ModifierCategory.NORMAL;
        this.updateModifierCategoryButtonLabel();
        ISpatial modListSpatial = Spatials.positionXY(7, 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57);
        this.innerScreen = new EtchingListContainer(modListSpatial, this.lvlInput.getValue(), modifierCategory, this.getCurrGear()).layout(this.translateWorldSpatial());
        this.disableLvlButtons();
        this.disableCategoryButtons();
        this.windowNameLabel.set(ETCHING_COMPONENT);
        this.addElement(this.innerScreen);
        ScreenLayout.requestLayout();
    }

    @Inject(method = "updateModifierList", at = @At(value = "HEAD"), cancellable = true)
    private void createEtchingScreen(boolean keepScroll, CallbackInfo ci) {
        if(this.windowNameLabel.getComponent().equals(ETCHING_COMPONENT) && !this.getCurrGear().is(ModItems.ETCHING)) {
            this.removeElement(this.innerScreen);
            if (woldsvaults$lastScreen != null){
                if (woldsvaults$lastScreen instanceof TransmogListContainer){
                    this.disableCategoryButtons();
                    this.disableLvlButtons();
                }
                if (woldsvaults$lastScreen instanceof ModifierListContainer){
                    this.enableCategoryButtons();
                    this.enableLvlButtons();
                }
                if (woldsvaults$lastScreen instanceof CraftedModifiersListContainer ||
                    woldsvaults$lastScreen instanceof UniqueGearListContainer){
                    this.enableLvlButtons();
                    this.disableCategoryButtons();
                }
                this.innerScreen = woldsvaults$lastScreen;
                woldsvaults$lastScreen = null;
            }
            if (woldsvaults$lastLabel != null){
                this.windowNameLabel.set(woldsvaults$lastLabel);
                woldsvaults$lastLabel = null;
            }
        }
        if(this.getCurrGear().is(ModItems.ETCHING) && !(this.innerScreen instanceof EtchingListContainer)) {
            woldsvaults$lastScreen = this.innerScreen;
            woldsvaults$lastLabel = this.windowNameLabel.getComponent();
            woldsVaults$switchToEtching();
            ci.cancel();
        }
    }

    @Inject(method = "switchToTransmog", at = @At("HEAD"))
    private void switchToTransmog(CallbackInfo ci) {
        if(this.getCurrGear().is(ModItems.ETCHING)) {
            this.switchTab(0);
        }
    }

    @Inject(method = "switchToCrafted", at = @At("HEAD"))
    private void switchToCrafted(CallbackInfo ci) {
        if(this.getCurrGear().is(ModItems.ETCHING)) {
            this.switchTab(0);
        }
    }

    @Inject(method = "switchToUnique", at = @At("HEAD"))
    private void switchToUnique(CallbackInfo ci) {
        if(this.getCurrGear().is(ModItems.ETCHING)) {
            this.switchTab(0);
        }
    }
    @Inject(method = "switchToModifiers", at = @At("HEAD"))
    private void switchToModifiers(CallbackInfo ci) {
        if(this.getCurrGear().is(ModItems.ETCHING)) {
            this.switchTab(0);
        }
    }
}
