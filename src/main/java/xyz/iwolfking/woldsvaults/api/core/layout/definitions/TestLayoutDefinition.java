package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicTestCrystalLayout;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;

import java.util.List;

public class TestLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "testing";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        int tunnel = data.getInt("tunnel");
        int width = data.getInt("width");
        int rowStep = data.getInt("rowStep");
        int branchInterval = data.getInt("branchInterval");
        return new ClassicTestCrystalLayout(tunnel, width, rowStep, branchInterval);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        ClassicTestCrystalLayout circle = (ClassicTestCrystalLayout) layout;
        data.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) circle).getTunnelSpan());
        data.putInt("width",  circle.getWidth());
        data.putInt("rowStep",  circle.getRowStep());
        data.putInt("branchInterval",  circle.getBranchInterval());
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Testing").withStyle(s -> s.withColor(0x30B3F2))));
        tooltip.add(new TextComponent("Tunnel Span: ")
                .append(new TextComponent(String.valueOf(data.getInt("tunnel")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Width: ")
                .append(new TextComponent(String.valueOf(data.getInt("width")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Row Step: ")
                .append(new TextComponent(String.valueOf(data.getInt("rowStep")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Branch Interval: ")
                .append(new TextComponent(String.valueOf(data.getInt("branchInterval")))
                        .withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof ClassicTestCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putInt("width", root.getInt("value"));
        data.putInt("rowStep", root.getInt("value"));
        data.putInt("branchInterval", root.getInt("value"));
        return data;
    }
}
