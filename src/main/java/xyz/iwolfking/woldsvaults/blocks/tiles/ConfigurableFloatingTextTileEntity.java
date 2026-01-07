package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConfigurableFloatingTextTileEntity extends BlockEntity {
    @Nonnull
    private final List<ConfigurableFloatingTextTileEntity.TextLine> lines = new ArrayList<>();
    private boolean editable = true;
    private float scale = 0.02f;

    public ConfigurableFloatingTextTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONFIGURABLE_FLOATING_TEXT_TILE_ENTITY, pos, state);

        // default lines
        lines.add(new ConfigurableFloatingTextTileEntity.TextLine("A sample floating text"));
        lines.add(new ConfigurableFloatingTextTileEntity.TextLine("You can edit this!"));
    }

    @Nonnull
    public List<ConfigurableFloatingTextTileEntity.TextLine> getLines() {
        return lines;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        setChanged();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        setChanged();
    }

    public void setLine(int index, ConfigurableFloatingTextTileEntity.TextLine  line) {
        if (!editable) return;
        while (lines.size() <= index) lines.add(new ConfigurableFloatingTextTileEntity.TextLine());
        lines.set(index, line);
        setChanged();
    }

    public void addLine(ConfigurableFloatingTextTileEntity.TextLine line) {
        if (!editable) return;
        lines.add(line);
        setChanged();
    }

    public void removeLine(int index) {
        if (!editable || index < 0 || index >= lines.size()) return;
        lines.remove(index);
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lines.clear();
        ListTag listTag = tag.getList("Lines", 10); // CompoundTag
        listTag.forEach(nbt -> {
            if (nbt instanceof CompoundTag ct) {
                ConfigurableFloatingTextTileEntity.TextLine  line = new ConfigurableFloatingTextTileEntity.TextLine();
                line.text = ct.getString("Text");
                line.color = ct.getInt("Color");
                line.bold = ct.getBoolean("Bold");
                line.italic = ct.getBoolean("Italic");
                line.underlined = ct.getBoolean("Underlined");
                lines.add(line);
            }
        });
        editable = tag.getBoolean("Editable");
        scale = tag.getFloat("Scale");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        ListTag listTag = new ListTag();
        for (ConfigurableFloatingTextTileEntity.TextLine line : lines) {
            CompoundTag ct = new CompoundTag();
            ct.putString("Text", line.text);
            ct.putInt("Color", line.color);
            ct.putBoolean("Bold", line.bold);
            ct.putBoolean("Italic", line.italic);
            ct.putBoolean("Underlined", line.underlined);
            listTag.add(ct);
        }
        tag.put("Lines", listTag);
        tag.putBoolean("Editable", editable);
        tag.putFloat("Scale", scale);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public List<TextLine> copyLines() {
        return new ArrayList<>(this.lines);
    }

    public void setLines(List<TextLine> newLines) {
        lines.clear();
        lines.addAll(newLines);
    }

    private List<TextLine> previewLines = null;

    public void setPreviewLines(@Nullable List<TextLine> preview) {
        this.previewLines = preview;
    }

    public List<TextLine> getRenderLines() {
        return previewLines != null ? previewLines : lines;
    }

    public CompoundTag saveToItem() {
        CompoundTag tag = new CompoundTag();

        ListTag linesTag = new ListTag();
        for (TextLine line : this.getLines()) {
            CompoundTag lineTag = new CompoundTag();
            lineTag.putString("text", line.text);
            lineTag.putInt("color", line.color);
            lineTag.putBoolean("bold", line.bold);
            lineTag.putBoolean("italic", line.italic);
            lineTag.putBoolean("underlined", line.underlined);
            linesTag.add(lineTag);
        }
        tag.put("lines", linesTag);
        tag.putFloat("scale", this.getScale());

        return tag;
    }


    public void loadFromItem(CompoundTag tag) {
        if (tag.contains("lines")) {
            ListTag linesTag = tag.getList("lines", Tag.TAG_COMPOUND);
            List<TextLine> loadedLines = new ArrayList<>();
            for (Tag t : linesTag) {
                CompoundTag lineTag = (CompoundTag) t;
                TextLine line = new TextLine(lineTag.getString("text"));
                line.color = lineTag.getInt("color");
                line.bold = lineTag.getBoolean("bold");
                line.italic = lineTag.getBoolean("italic");
                line.underlined = lineTag.getBoolean("underlined");
                loadedLines.add(line);
            }
            this.setLines(loadedLines);
        }

        if (tag.contains("scale")) {
            this.setScale(tag.getFloat("scale"));
        }
    }



    public static class TextLine {
        @Nonnull
        public String text = "";
        public int color = 0xFFFFFF; // default white
        public boolean bold = false;
        public boolean italic = false;
        public boolean underlined = false;

        public TextLine() {}
        public TextLine(String text) { this.text = text; }
    }
}


