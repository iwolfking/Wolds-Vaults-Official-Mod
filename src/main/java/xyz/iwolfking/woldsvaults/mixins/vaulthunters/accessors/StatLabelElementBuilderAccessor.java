package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/** Reads one H-screen stat row's label, value and formatting, for the armory's saved stat snapshot. */
@Mixin(value = StatLabelElementBuilder.class, remap = false)
public interface StatLabelElementBuilderAccessor {
    @Accessor("labelSupplier")
    Supplier<String> woldsvaults$getLabelSupplier();

    @Accessor("descriptionSupplier")
    Supplier<List<String>> woldsvaults$getDescriptionSupplier();

    @Accessor("finalValueSupplier")
    Supplier<Object> woldsvaults$getFinalValueSupplier();

    @Accessor("valueFormatFunction")
    Function<Object, MutableComponent> woldsvaults$getValueFormatFunction();

    @Accessor("textColor")
    TextColor woldsvaults$getTextColor();
}
