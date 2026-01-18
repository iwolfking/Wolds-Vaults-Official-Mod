package xyz.iwolfking.woldsvaults.client.screens.widgets;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.function.IntConsumer;

public class HexColorField extends EditBox {

    private final IntConsumer colorSetter;

    public HexColorField(Font font, int x, int y, int width, IntConsumer setter) {
        super(font, x, y, width, 18, new TextComponent("#RRGGBB"));
        this.colorSetter = setter;
        setMaxLength(7);
    }

    @Override
    public void setValue(String value) {
        if (!value.startsWith("#") && !value.isEmpty()) {
            value = "#" + value;
        }
        super.setValue(value.toUpperCase());
        parseAndSetColor();
    }

    @Override
    public void insertText(String pTextToWrite) {
        super.insertText(pTextToWrite);
        parseAndSetColor();
    }

    private void parseAndSetColor() {
        String val = getValue();
        if (val.startsWith("#") && val.length() == 7) {
            try {
                int rgb = Integer.parseInt(val.substring(1), 16);
                colorSetter.accept(rgb);
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
