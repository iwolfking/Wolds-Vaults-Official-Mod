package xyz.iwolfking.woldsvaults.client.screens.stats;

import net.minecraft.network.chat.TextColor;

import java.util.Objects;
import java.util.function.Supplier;

/** A row's value colour: the layout colour while the base supplier gives the plain row colour, so a capped value keeps its cap colour. */
public class StatValueColor implements Supplier<TextColor> {
    private final Supplier<TextColor> base;
    private final TextColor plain;
    private final TextColor value;

    public StatValueColor(Supplier<TextColor> base, TextColor plain, TextColor value) {
        this.base = base;
        this.plain = plain;
        this.value = value;
    }

    @Override
    public TextColor get() {
        TextColor color = this.base.get();
        return Objects.equals(color, this.plain) ? this.value : color;
    }
}
