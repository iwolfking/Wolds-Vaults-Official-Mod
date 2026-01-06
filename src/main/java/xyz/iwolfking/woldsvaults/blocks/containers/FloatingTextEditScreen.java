package xyz.iwolfking.woldsvaults.blocks.containers;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.blocks.tiles.ConfigurableFloatingTextTileEntity;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.UpdateFloatingTextPacket;

import java.util.ArrayList;
import java.util.List;

public class FloatingTextEditScreen extends Screen {

    private static final int LINE_HEIGHT = 22;
    private static final int[] COLORS = {
        0xFFFFFF, 0xFF5555, 0x55FF55, 0x5555FF,
        0xFFFF55, 0xFF55FF, 0x55FFFF, 0xAAAAAA
    };

    private final ConfigurableFloatingTextTileEntity tile;
    private final List<ConfigurableFloatingTextTileEntity.TextLine> lines;

    private boolean preview = true;

    public FloatingTextEditScreen(ConfigurableFloatingTextTileEntity tile) {
        super(new TextComponent("Floating Text Editor"));
        this.tile = tile;
        this.lines = tile.copyLines();
    }

    @Override
    protected void init() {
        clearWidgets();

        int startX = width / 2 - 150;
        int y = height / 2 - (lines.size() * LINE_HEIGHT) / 2;

        for (int i = 0; i < lines.size(); i++) {
            ConfigurableFloatingTextTileEntity.TextLine line = lines.get(i);
            int rowY = y + i * LINE_HEIGHT;
            int index = i;

            EditBox box = new EditBox(font, startX, rowY, 160, 18, TextComponent.EMPTY);
            box.setValue(line.text);
            box.setResponder(v -> line.text = v);
            addRenderableWidget(box);

            addRenderableWidget(new Button(startX + 165, rowY, 18, 18, new TextComponent("B"),
                b -> line.bold = !line.bold));

            addRenderableWidget(new Button(startX + 185, rowY, 18, 18, new TextComponent("I"),
                b -> line.italic = !line.italic));

            addRenderableWidget(new Button(startX + 205, rowY, 18, 18, new TextComponent("U"),
                b -> line.underlined = !line.underlined));

            addRenderableWidget(new Button(startX + 225, rowY, 18, 18, new TextComponent("■"),
                b -> {
                    int idx = 0;
                    for (int c = 0; c < COLORS.length; c++)
                        if (COLORS[c] == line.color) idx = c;
                    line.color = COLORS[(idx + 1) % COLORS.length];
                }));
        }

        addRenderableWidget(new Button(startX, y + lines.size() * LINE_HEIGHT + 8, 80, 20,
            new TextComponent("+ Line"),
            b -> { lines.add(new ConfigurableFloatingTextTileEntity.TextLine("")); init(); }));

        addRenderableWidget(new Button(startX + 85, y + lines.size() * LINE_HEIGHT + 8, 80, 20,
            new TextComponent("- Line"),
            b -> { if (!lines.isEmpty()) { lines.remove(lines.size() - 1); init(); }}));

        addRenderableWidget(new Button(startX + 170, y + lines.size() * LINE_HEIGHT + 8, 80, 20,
            new TextComponent(preview ? "Preview ON" : "Preview OFF"),
            b -> preview = !preview));
    }

    @Override
    public void tick() {
        if (preview) {
            tile.setPreviewLines(lines);
        } else {
            tile.setPreviewLines(null);
        }
    }

    @Override
    public void onClose() {
        tile.setPreviewLines(null);
        ModNetwork.CHANNEL.sendToServer(
            new UpdateFloatingTextPacket(tile.getBlockPos(), lines, tile.isEditable(), tile.getScale())
        );
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
