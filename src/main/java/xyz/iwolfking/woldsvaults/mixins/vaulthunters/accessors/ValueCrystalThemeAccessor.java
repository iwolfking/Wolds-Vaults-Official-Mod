package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.world.generator.layout.GridLayout;
import iskallia.vault.core.world.generator.theme.Theme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ValueCrystalTheme.class, remap = false)
public interface ValueCrystalThemeAccessor {
    @Accessor("id")
    ResourceLocation getId();
}
