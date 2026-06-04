package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.vault.CrateLootGenerator;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CrateLootGenerator.class)
public interface CrateLootGeneratorAccessor {
    @Accessor(value = "additionalItems", remap = false)
    List<ItemStack> getAdditionalItemsWolds();
}