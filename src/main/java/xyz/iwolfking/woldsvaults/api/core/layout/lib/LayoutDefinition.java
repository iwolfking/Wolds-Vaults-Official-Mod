package xyz.iwolfking.woldsvaults.api.core.layout.lib;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface LayoutDefinition {

    String id();

    CrystalLayout create(CompoundTag data);

    void writeFromLayout(CrystalLayout layout, CompoundTag data);

    void addTooltip(CompoundTag data, List<Component> tooltip);

    boolean supports(CrystalLayout layout);


    default CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putInt("value", root.getInt("value"));
        return data;
    }

    default @Nonnull Optional<LayoutTooltipComponent> getTooltipImage(CompoundTag data) {
        return Optional.empty();
    }

}
