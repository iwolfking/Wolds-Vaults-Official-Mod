package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import iskallia.vault.item.crystal.layout.ClassicPolygonCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.PolygonLayoutTooltip;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicPolygonCrystalLayoutAccessor;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PolygonLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "polygon";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        int tunnel = data.getInt("tunnel");
        int[] vertices = data.getIntArray("vertices");
        return new ClassicPolygonCrystalLayout(tunnel, vertices);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        ClassicPolygonCrystalLayout poly = (ClassicPolygonCrystalLayout) layout;
        data.putString("layout", id());
        CompoundTag layoutData = new CompoundTag();
        layoutData.putInt("tunnel", ((ClassicInfiniteCrystalLayoutAccessor) poly).getTunnelSpan());
        layoutData.putIntArray("vertices",
                ((ClassicPolygonCrystalLayoutAccessor) poly).getVertices());
        data.put("layout_data", layoutData);
    }
    

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Polygon").withStyle(s -> s.withColor(0xFFD700))));
        tooltip.add(new TextComponent("Tunnel Span: ")
                .append(new TextComponent(String.valueOf(data.getInt("tunnel")))
                        .withStyle(ChatFormatting.GOLD)));
        tooltip.add(new TextComponent("Vertices: ")
                .append(new TextComponent(Arrays.toString(data.getIntArray("vertices")))
                        .withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof ClassicPolygonCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        int value = root.getInt("value");
        CompoundTag data = new CompoundTag();
        data.putInt("tunnel", root.getInt("tunnel"));
        data.putIntArray("vertices", new int[]{
                -value, value, value, value,
                value, -value, -value, -value
        });
        return data;
    }

    @Override
    public @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        try {
            return PolygonLayoutTooltip.getTooltipImage(data);
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Failed to create polygon layout manipulator preview.", e);
            return Optional.empty();
        }
    }
}
