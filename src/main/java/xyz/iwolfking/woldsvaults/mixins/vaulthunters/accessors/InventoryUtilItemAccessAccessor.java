package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@Mixin(value = InventoryUtil.ItemAccess.class, remap = false)
public interface InventoryUtilItemAccessAccessor {
    @Accessor("setter")
    Consumer<ItemStack> getSetter();

    @Accessor("stack")
    ItemStack getActualStack();
}
