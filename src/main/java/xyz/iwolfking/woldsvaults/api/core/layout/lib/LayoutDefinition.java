package xyz.iwolfking.woldsvaults.api.core.layout.lib;

import iskallia.vault.item.crystal.layout.CompoundCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface LayoutDefinition {

    String id();

    CrystalLayout create(CompoundTag data);

    void writeFromLayout(CrystalLayout layout, CompoundTag data);

    default void writeFlatLayout(CrystalLayout layout, CompoundTag data) {
        writeFromLayout(layout, data);

        if (!data.contains("layout_data", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag layoutData = data.getCompound("layout_data");

        for (String key : layoutData.getAllKeys()) {
            data.put(key, layoutData.get(key).copy());
        }

        data.remove("layout_data");
    }


    void addTooltip(CompoundTag data, List<Component> tooltip);

    boolean supports(CrystalLayout layout);


    default CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putInt("value", root.getInt("value"));
        return data;
    }

    default @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        return Optional.empty();
    }

}
