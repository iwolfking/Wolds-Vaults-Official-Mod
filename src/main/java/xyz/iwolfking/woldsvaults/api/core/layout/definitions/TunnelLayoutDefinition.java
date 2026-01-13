package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicTunnelCrystalLayout;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.TunnelLayoutTooltip;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class TunnelLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "tunnels";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        int tunnel = data.getInt("tunnel");
        int width = data.getInt("width");
        int height = data.getInt("height");
        int branchInterval = data.getInt("branchInterval");
        return new ClassicTunnelCrystalLayout(tunnel, width, height, branchInterval);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        ClassicTunnelCrystalLayout tunnelLayout = (ClassicTunnelCrystalLayout) layout;
        data.putString("layout", id());
        CompoundTag layoutData = new CompoundTag();
        layoutData.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) tunnelLayout).getTunnelSpan());
        layoutData.putInt("width",  tunnelLayout.getWidth());
        layoutData.putInt("height",  tunnelLayout.getHeight());
        layoutData.putInt("branchInterval",  tunnelLayout.getBranchInterval());
        data.put("layout_data", layoutData);
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Tunnels").withStyle(s -> s.withColor(0x5b3e94))));
        tooltip.add(new TextComponent("Tunnel Span: ")
                .append(new TextComponent(String.valueOf(data.getInt("tunnel")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Width: ")
                .append(new TextComponent(String.valueOf(data.getInt("width")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Height: ")
                .append(new TextComponent(String.valueOf(data.getInt("height")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Branch Interval: ")
                .append(new TextComponent(String.valueOf(data.getInt("branchInterval")))
                        .withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof ClassicTunnelCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putInt("width", root.getInt("value"));
        data.putInt("height", root.getInt("value"));
        data.putInt("branchInterval", root.getInt("value"));
        return data;
    }

    @Override
    public @Nonnull Optional<LayoutTooltipComponent> getTooltipImage(CompoundTag data) {
        try {
            return TunnelLayoutTooltip.getTooltipImage(data);
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Failed to create tunnel layout manipulator preview.", e);
            return Optional.empty();
        }
    }

}
