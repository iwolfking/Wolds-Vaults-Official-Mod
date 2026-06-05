package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.item.crystal.objective.ScavengerBingoCrystalObjective;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ScavengerBingoCrystalObjective.class, remap = false)
public interface ScavengerBingoCrystalObjectiveAccessor {
    @Accessor("blackout")
    void setBlackout(boolean blackout);

    @Accessor("width")
    void setWidth(int width);

    @Accessor("height")
    void setHeight(int height);

    @Accessor("modifierPool")
    void setModifierPool(ResourceLocation pool);

    @Accessor
    float getObjectiveProbability();

    @Accessor
    int getWidth();

    @Accessor
    int getHeight();
}
