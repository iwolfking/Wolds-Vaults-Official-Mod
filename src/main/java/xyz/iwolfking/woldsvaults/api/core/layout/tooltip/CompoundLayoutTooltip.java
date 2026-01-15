package xyz.iwolfking.woldsvaults.api.core.layout.tooltip;

import com.ibm.icu.impl.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.layout.definitions.CompoundLayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;
import xyz.iwolfking.woldsvaults.api.util.NbtUtils;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CompoundLayoutTooltip {

    public static @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        return CompoundLayoutDefinition.getFirstNonArchitect(data).flatMap(layoutDefinitionCompoundTagPair -> layoutDefinitionCompoundTagPair.first.getTooltipImage(NbtUtils.flatten(layoutDefinitionCompoundTagPair.second)));
    }

    public static @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data, long seed) {
        Optional<TooltipComponent> component = CompoundLayoutDefinition.getFirstNonArchitect(data).flatMap(layoutDefinitionCompoundTagPair -> layoutDefinitionCompoundTagPair.first.getTooltipImage(NbtUtils.flatten(layoutDefinitionCompoundTagPair.second)));
        if(component.isPresent()) {
            Optional<Pair<LayoutDefinition, CompoundTag>> architectLayout = CompoundLayoutDefinition.getNestedLayoutOfType("architect", data);
            if(architectLayout.isPresent()) {
                if(component.get() instanceof LayoutTooltipComponent layoutTooltipComponent) {
                    return ArchitectLayoutTooltip.getTooltipImage(architectLayout.get().second, layoutTooltipComponent, seed);
                }
            }
        }

        return component;
    }
}