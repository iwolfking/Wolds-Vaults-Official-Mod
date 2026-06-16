package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.vault.objective.scavenger.ScavengerBingoTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(targets = "iskallia.vault.core.vault.objective.ScavengerBingoObjective$TileCandidate")
public interface TileCandidateAccessor {
    @Accessor
    int getColor();

    @Accessor
    ItemStack getItem();

    @Accessor
    ScavengerBingoTile.SourceType getSourceType();

    @Accessor
    ResourceLocation getIcon();

    @Accessor
    double getMultiplier();
}
