package xyz.iwolfking.woldsvaults.mixins.accessors;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(Model.class)
public interface ModelAccessor {
    @Mutable
    @Accessor("renderType")
    void setRenderType(Function<ResourceLocation, RenderType> renderType);
}
