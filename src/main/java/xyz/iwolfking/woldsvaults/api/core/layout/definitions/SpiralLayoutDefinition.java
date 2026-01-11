package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import iskallia.vault.item.crystal.layout.ClassicSpiralCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.Rotation;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicSpiralCrystalLayoutAccessor;

import java.util.List;

public class SpiralLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "spiral";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        int tunnel = data.getInt("tunnel");
        int halfLength = data.getInt("halfLength");
        return new ClassicSpiralCrystalLayout(tunnel, halfLength, Rotation.CLOCKWISE_90);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        ClassicSpiralCrystalLayout spiral = (ClassicSpiralCrystalLayout) layout;
        data.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) spiral).getTunnelSpan());
        data.putInt("halfLength", ((ClassicSpiralCrystalLayoutAccessor) spiral).getHalfLength());
        data.putString("rotation", ((ClassicSpiralCrystalLayoutAccessor) spiral).getRotation().name());
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Spiral").withStyle(s -> s.withColor(0x30B3F2))));
        tooltip.add(new TextComponent("Tunnel Span: ")
                .append(new TextComponent(String.valueOf(data.getInt("tunnel")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Half-Length: ")
                .append(new TextComponent(String.valueOf(data.getInt("halfLength")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Rotation: ")
                .append(new TextComponent(data.getString("rotation"))
                        .withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof ClassicSpiralCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putInt("halfLength", root.getInt("value"));
        data.putString("rotation", Rotation.NONE.name());
        return data;
    }
}
