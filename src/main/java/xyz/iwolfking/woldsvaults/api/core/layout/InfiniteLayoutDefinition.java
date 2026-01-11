package xyz.iwolfking.woldsvaults.api.core.layout;

import iskallia.vault.item.crystal.layout.ClassicCircleCrystalLayout;
import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
        data.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) infinite).getTunnelSpan());
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
        return layout instanceof ClassicInfiniteCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        return data;
    }
}
