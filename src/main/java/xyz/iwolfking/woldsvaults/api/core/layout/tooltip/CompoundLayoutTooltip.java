package xyz.iwolfking.woldsvaults.api.core.layout.tooltip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.definitions.CompoundLayoutDefinition;
import xyz.iwolfking.woldsvaults.api.util.NbtUtils;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CompoundLayoutTooltip {

    public static @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        return CompoundLayoutDefinition.getFirstNestedDefinition(data).flatMap(layoutDefinitionCompoundTagPair -> layoutDefinitionCompoundTagPair.first.getTooltipImage(NbtUtils.flatten(layoutDefinitionCompoundTagPair.second)));
    }
}