package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import iskallia.vault.item.crystal.layout.ClassicCircleCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicRingsCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicCircleCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicPolygonCrystalLayoutAccessor;

import java.util.List;

public class RingsLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "rings";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        int tunnel = data.getInt("tunnel");
        int radius = data.getInt("radius");
        int ringInterval = data.getInt("ringInterval");
        return new ClassicRingsCrystalLayout(tunnel, radius, ringInterval);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        ClassicRingsCrystalLayout rings = (ClassicRingsCrystalLayout) layout;
        data.putString("layout", id());
        CompoundTag layoutData = new CompoundTag();
        layoutData.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) rings).getTunnelSpan());
        layoutData.putInt("radius", rings.getRadius());
        layoutData.putInt("ringInterval", rings.getRingInterval());
        data.put("layout_data", layoutData);
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Rings").withStyle(s -> s.withColor(0xab73c7))));
        tooltip.add(new TextComponent("Tunnel Span: ")
                .append(new TextComponent(String.valueOf(data.getInt("tunnel")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Radius: ")
                .append(new TextComponent(String.valueOf(data.getInt("radius")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Ring Interval: ")
                .append(new TextComponent(String.valueOf(data.getInt("ringInterval")))
                        .withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof ClassicRingsCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putInt("radius", root.getInt("value"));
        data.putInt("ringInterval", root.getInt("value"));
        return data;
    }
}
