package xyz.iwolfking.woldsvaults.blocks.containers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.blocks.tiles.ConfigurableFloatingTextTileEntity;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.UpdateFloatingTextPacket;

import java.util.List;

public class FloatingTextEditScreen extends Screen {

    private static final int LINE_HEIGHT = 22;
    private static final int ROW_WIDTH = 300;

    private final ConfigurableFloatingTextTileEntity tile;
    private final List<ConfigurableFloatingTextTileEntity.TextLine> lines;

    private int draggingIndex = -1;
    private boolean preview = true;

    public FloatingTextEditScreen(ConfigurableFloatingTextTileEntity tile) {
        super(new TextComponent("Floating Text Editor"));
        this.tile = tile;
        this.lines = tile.copyLines();
    }

    @Override
    protected void init() {
        clearWidgets();

        int startX = width / 2 - ROW_WIDTH / 2;
        int startY = height / 2 - (lines.size() * LINE_HEIGHT) / 2;

        for (int i = 0; i < lines.size(); i++) {
            final int index = lines.size() - 1 - i;
            ConfigurableFloatingTextTileEntity.TextLine line = lines.get(index);

            int y = startY + i * LINE_HEIGHT;


            addRenderableWidget(new Button(
                startX - 18, y, 16, 18,
                new TextComponent("≡"),
                b -> draggingIndex = index,
                    (b, p, mx, my) -> {
                        this.renderTooltip(p, new TextComponent("Drag to Reorder"), mx, my);
                    }
            ));



            EditBox box = new EditBox(font, startX, y, 150, 18, TextComponent.EMPTY);
            box.setValue(line.text);
            box.setResponder(v -> line.text = v);
            addRenderableWidget(box);


            addRenderableWidget(styleButton("B", startX + 155, y, () -> line.bold = !line.bold, "Toggle Bold"));
            addRenderableWidget(styleButton("I", startX + 175, y, () -> line.italic = !line.italic, "Toggle Italic"));
            addRenderableWidget(styleButton("U", startX + 195, y, () -> line.underlined = !line.underlined, "Toggle Underline"));

            addRenderableWidget(new Button(
                    startX + 235, y, 18, 18,
                    new TextComponent("Ⓒ").withStyle(Style.EMPTY.withColor(line.color)),
                    b -> minecraft.setScreen(
                            new FloatingTextColorPickerScreen(this, line)
                    ),
                    (b, p, mx, my) -> {
                        this.renderTooltip(p, new TextComponent("Open Color Picker"), mx, my);
                    }
            ));


            addRenderableWidget(new Button(
                startX + 215, y, 18, 18,
                new TextComponent("✖").withStyle(ChatFormatting.RED),
                b -> {
                    lines.remove(index);
                    draggingIndex = -1;
                    init();
                },
                (b, p, mx, my) -> {
                    this.renderTooltip(p, new TextComponent("Delete Line"), mx, my);
                }
            ));
        }

        addRenderableWidget(new Button(
            startX, startY + lines.size() * LINE_HEIGHT + 8,
            80, 20,
            new TextComponent("+ Line"),
            b -> {
                lines.add(0, new ConfigurableFloatingTextTileEntity.TextLine(""));
                init();
            }
        ));

        addRenderableWidget(new Button(
                startX + 100, startY + lines.size() * LINE_HEIGHT + 8,
                80, 20,
                new TextComponent("Close"),
                b -> {
                    this.onClose();
                }
        ));

    }

    private Button styleButton(String label, int x, int y, Runnable action, String tooltip) {
        return new Button(x, y, 18, 18, new TextComponent(label), b -> action.run(), (b, p, mx, my) -> {
            this.renderTooltip(p, new TextComponent(tooltip), mx, my);
        });
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingIndex != -1) {
            int startY = height / 2 - (lines.size() * LINE_HEIGHT) / 2;

            int guiRow = (int) ((mouseY - startY) / LINE_HEIGHT);
            guiRow = Math.max(0, Math.min(lines.size() - 1, guiRow));

            int newIndex = lines.size() - 1 - guiRow;

            if (newIndex != draggingIndex) {
                var line = lines.remove(draggingIndex);
                lines.add(newIndex, line);
            }

            draggingIndex = -1;
            init();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
            new UpdateFloatingTextPacket(
                tile.getBlockPos(),
                lines,
                tile.isEditable(),
                tile.getScale()
            )
        );
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
