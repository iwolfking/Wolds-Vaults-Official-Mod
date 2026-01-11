package xyz.iwolfking.woldsvaults.api.core.layout;

import iskallia.vault.item.crystal.layout.ClassicCircleCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicCircleCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;

import java.util.List;

public class CircleLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "circle";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        int tunnel = data.getInt("tunnel");
        int radius = data.getInt("radius");
        return new ClassicCircleCrystalLayout(tunnel, radius);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        ClassicCircleCrystalLayout circle = (ClassicCircleCrystalLayout) layout;
        data.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) circle).getTunnelSpan());
        data.putInt("radius", ((ClassicCircleCrystalLayoutAccessor) circle).getRadius());
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Circle").withStyle(s -> s.withColor(0x30B3F2))));
        tooltip.add(new TextComponent("Tunnel Span: ")
                .append(new TextComponent(String.valueOf(data.getInt("tunnel")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Radius: ")
                .append(new TextComponent(String.valueOf(data.getInt("radius")))
                        .withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof ClassicCircleCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putInt("radius", root.getInt("value"));
        return data;
    }
}
