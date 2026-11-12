package xyz.iwolfking.woldsvaults.client.screens.stats;

import iskallia.vault.client.gui.framework.text.LabelTextStyle;

/**
 * Text style for a coloured stat row. The row builds its name label with {@code left()} and then its value label with
 * {@code right()} from the same builder, so the shadow is added at {@code right()}: the value gets a shadow derived
 * from its colour and the name stays flat black.
 */
public class StatValueStyle extends LabelTextStyle.Builder {
    @Override
    public LabelTextStyle.Builder right() {
        super.right();
        return this.shadowFromTextColor();
    }
}
