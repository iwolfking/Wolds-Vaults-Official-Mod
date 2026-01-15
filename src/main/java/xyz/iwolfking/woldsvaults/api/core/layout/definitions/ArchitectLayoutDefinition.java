package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import iskallia.vault.core.world.generator.layout.ArchitectRoomEntry;
import iskallia.vault.item.crystal.layout.ArchitectCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ArchitectCrystalLayoutAccessor;
import java.util.ArrayList;
import java.util.List;

public class ArchitectLayoutDefinition implements LayoutDefinition {
    @Override
    public String id() {
        return "architect";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        CompoundTag layoutData = data.getCompound("layout_data");
        int tunnel = layoutData.getInt("tunnel");
        ListTag architectRoomEntries = layoutData.getList("entries", CompoundTag.TAG_COMPOUND);
        List<ArchitectRoomEntry> nestedArchitectEntries = new ArrayList<>();
        for(int i = 0; i < architectRoomEntries.size(); i++) {
            CompoundTag nestedEntry = architectRoomEntries.getCompound(i);
            nestedArchitectEntries.add(ArchitectRoomEntry.fromNBT(nestedEntry));
        }

        return new ArchitectCrystalLayout(tunnel, nestedArchitectEntries);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        ArchitectCrystalLayout compound = (ArchitectCrystalLayout) layout;
        data.putString("layout", id());
        CompoundTag layoutData = new CompoundTag();
        ListTag entries = new ListTag();
        ((ArchitectCrystalLayoutAccessor)compound).getEntries().forEach(roomEntry -> {
                entries.add(roomEntry.serializeNBT());
        });
        layoutData.put("entries", entries);
        data.put("layout_data", layoutData);
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Architect").withStyle(s -> s.withColor(0x30B3F2))));
        CompoundTag layoutData = data.getCompound("layout_data");
        ListTag entries = layoutData.getList("entries", Tag.TAG_COMPOUND);
        for(int i = 0; i < entries.size(); i++) {
            ArchitectRoomEntry entry = ArchitectRoomEntry.fromNBT(entries.getCompound(i));
            tooltip.add(entry.getName());
        }
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof ArchitectCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        return root;
    }
}
