package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.LayoutDefinitionRegistry;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicCircleCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;

import java.util.List;

public class InfiniteLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "infinite";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        int tunnel = data.getInt("tunnel");
        return new ClassicInfiniteCrystalLayout(tunnel);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        ClassicInfiniteCrystalLayout infinite = (ClassicInfiniteCrystalLayout) layout;
        data.putString("layout", id());
        CompoundTag layoutData = new CompoundTag();
        data.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) infinite).getTunnelSpan());
        data.put("layout_data", layoutData);
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Infinite").withStyle(s -> s.withColor(0x30B3F2))));
        tooltip.add(new TextComponent("Tunnel Span: ")
                .append(new TextComponent(String.valueOf(data.getInt("tunnel")))
                        .withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout.getClass().equals(ClassicInfiniteCrystalLayout.class);
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        return data;
    }
}
