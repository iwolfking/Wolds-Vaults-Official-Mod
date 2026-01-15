package xyz.iwolfking.woldsvaults.api.core.layout.definitions;

import com.ibm.icu.impl.Pair;
import iskallia.vault.item.crystal.layout.CompoundCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.LayoutDefinitionRegistry;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.CompoundLayoutTooltip;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompoundLayoutDefinition implements LayoutDefinition {

    @Override
    public String id() {
        return "compound";
    }

    @Override
    public CrystalLayout create(CompoundTag data) {
        CompoundTag layoutData = data.getCompound("layout_data");
        ListTag nestedDataLayouts = layoutData.getList("layouts", CompoundTag.TAG_COMPOUND);
        List<CrystalLayout> nestedCrystalLayouts = new ArrayList<>();
        for(int i = 0; i < nestedDataLayouts.size(); i++) {
            CompoundTag nestedLayout = nestedDataLayouts.getCompound(i);
            if(nestedLayout.contains("layout")) {
                LayoutDefinition layoutDefinition = LayoutDefinitionRegistry.get(nestedLayout.getString("layout")).orElse(null);
                if(layoutDefinition != null) {
                    CrystalLayout layout = layoutDefinition.create(nestedLayout);
                    if(layout != null) {
                        nestedCrystalLayouts.add(layout);
                    }
                }
            }
        }
        return new CompoundCrystalLayout(nestedCrystalLayouts);
    }

    @Override
    public void writeFromLayout(CrystalLayout layout, CompoundTag data) {
        CompoundCrystalLayout compound = (CompoundCrystalLayout) layout;
        data.putString("layout", id());
        CompoundTag layoutData = new CompoundTag();
        ListTag layouts = new ListTag();
        compound.getChildren().forEach(crystalEntry -> {
            if(crystalEntry instanceof CrystalLayout crystalLayout) {
                CompoundTag nestedLayoutData = new CompoundTag();
                LayoutDefinition definition = LayoutDefinitionRegistry.getForLayout(crystalLayout).orElse(null);
                if(definition != null) {
                    definition.writeFromLayout(crystalLayout, nestedLayoutData);
                }
                layouts.add(nestedLayoutData);
            }
        });
        layoutData.put("layouts", layouts);
        data.put("layout_data", layoutData);
    }

    @Override
    public void addTooltip(CompoundTag data, List<Component> tooltip) {
        tooltip.add(new TextComponent("Layout: ")
                .append(new TextComponent("Compound").withStyle(s -> s.withColor(0x30B3F2))));
        CompoundTag layoutData = data.getCompound("layout_data");
        ListTag nestedLayouts = layoutData.getList("layouts", Tag.TAG_COMPOUND);
        addNestedTooltips(nestedLayouts, tooltip);
    }

    public void addNestedTooltips(ListTag nestedLayouts, List<Component> tooltip) {
        for(int i = 0; i < nestedLayouts.size(); i++) {
            CompoundTag nestedLayout = nestedLayouts.getCompound(i);
            if(nestedLayout.contains("layout")) {
                LayoutDefinition layoutDefinition = LayoutDefinitionRegistry.get(nestedLayout.getString("layout")).orElse(null);
                if(layoutDefinition != null) {
                    layoutDefinition.addTooltip(nestedLayout, tooltip);
                }
            }
        }
    }

    public static Optional<Pair<LayoutDefinition, CompoundTag>> getFirstNestedDefinition(CompoundTag data) {
        ListTag nestedLayouts = data.getList("layouts", Tag.TAG_COMPOUND);
        for(int i = 0; i < nestedLayouts.size(); i++) {
            CompoundTag nestedLayout = nestedLayouts.getCompound(i);
            if(nestedLayout.contains("layout")) {
                LayoutDefinition layoutDefinition = LayoutDefinitionRegistry.get(nestedLayout.getString("layout")).orElse(null);
                if(layoutDefinition != null) {
                    if(layoutDefinition.id().equals("architect")) {
                        continue;
                    }

                    if(nestedLayout.contains("layout_data")) {
                        return Optional.of(Pair.of(layoutDefinition, nestedLayout.getCompound("layout_data")));
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(CrystalLayout layout) {
        return layout instanceof CompoundCrystalLayout;
    }

    @Override
    public CompoundTag upgradeLegacy(CompoundTag root) {
        return root;
    }

    @Override
    public @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        try {
            return CompoundLayoutTooltip.getTooltipImage(data);
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Failed to create compound layout manipulator preview.", e);
            return Optional.empty();
        }
    }
}
