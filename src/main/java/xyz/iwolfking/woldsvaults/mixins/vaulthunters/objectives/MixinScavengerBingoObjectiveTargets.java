package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.scavenger.ScavengerBingoTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;

@Mixin(value = ScavengerBingoObjective.class, remap = false)
public class MixinScavengerBingoObjectiveTargets {
    /** Every collector tile's item count rises by the scavenger bingo exponent, on generation and on reshuffle. */
    @WrapOperation(method = {"generateTiles", "regenerateIncompleteTiles"},
            at = @At(value = "NEW", target = "(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/core/vault/objective/scavenger/ScavengerBingoTile$SourceType;Lnet/minecraft/resources/ResourceLocation;II)Liskallia/vault/core/vault/objective/scavenger/ScavengerBingoTile;"))
    private ScavengerBingoTile woldsvaults$scaleTileCount(ItemStack item, ScavengerBingoTile.SourceType source, ResourceLocation icon, int itemCount, int color,
                                                          Operation<ScavengerBingoTile> original, @Local(argsOnly = true) Vault vault) {
        int scaled = ObjectiveDifficulty.scale(itemCount, ObjectiveDifficulty.factor(ObjectiveDifficulty.multiplier(vault), "scavenger_bingo"));
        return original.call(item, source, icon, scaled, color);
    }
}
