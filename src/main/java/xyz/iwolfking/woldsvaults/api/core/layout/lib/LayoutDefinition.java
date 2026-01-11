package xyz.iwolfking.woldsvaults.api.core.layout.lib;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;

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
}
