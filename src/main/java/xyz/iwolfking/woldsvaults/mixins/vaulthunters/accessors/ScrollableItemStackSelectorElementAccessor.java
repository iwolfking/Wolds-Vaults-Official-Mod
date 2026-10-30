package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.client.gui.framework.element.ClickableItemSlotElement;
import iskallia.vault.client.gui.screen.block.CrystalWorkbenchScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = CrystalWorkbenchScreen.ScrollableClickableItemStackSelectorElement.class, remap = false)
public interface ScrollableItemStackSelectorElementAccessor {

    @Invoker("getSelectorElements")
    List<ClickableItemSlotElement<?>> invokeGetSelectorElements();
}