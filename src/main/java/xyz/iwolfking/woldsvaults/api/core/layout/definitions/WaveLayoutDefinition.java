package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicRingsCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;

import java.util.List;

public class WaveLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "wave";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        int tunnel = data.getInt("tunnel");
        int length = data.getInt("length");
        int amplitude = data.getInt("amplitude");
        double frequency = data.getDouble("frequency");
        return new ClassicWaveCrystalLayout(tunnel, length, amplitude, frequency);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        data.putString("layout", id());
        ClassicWaveCrystalLayout wave = (ClassicWaveCrystalLayout) layout;
        CompoundTag layoutData = new CompoundTag();
        layoutData.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) wave).getTunnelSpan());
        layoutData.putInt("length", wave.getLength());
        layoutData.putInt("amplitude", wave.getAmplitude());
        layoutData.putDouble("frequency", wave.getFrequency());
        data.put("layout_data", layoutData);
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Wave").withStyle(s -> s.withColor(0x30B3F2))));
        tooltip.add(new TextComponent("Tunnel Span: ")
                .append(new TextComponent(String.valueOf(data.getInt("tunnel")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Length: ")
                .append(new TextComponent(String.valueOf(data.getInt("length")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Amplitude: ")
                .append(new TextComponent(String.valueOf(data.getInt("amplitude")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Frequency: ")
                .append(new TextComponent(String.valueOf(data.getDouble("frequency")))
                        .withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof ClassicWaveCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putInt("length", root.getInt("value"));
        data.putInt("amplitude", root.getInt("value"));
        data.putDouble("frequency", root.getInt("value"));
        return data;
    }
}
