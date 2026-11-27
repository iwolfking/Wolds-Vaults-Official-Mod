package xyz.iwolfking.woldsvaults.mixins.vaulthunters.ancient;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.common.ItemScavengeTaskEvent;
import iskallia.vault.core.vault.objective.RuneBossObjective;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gear.ancient.AncientLootStamp;

import java.util.List;

@Mixin(value = RuneBossObjective.class, remap = false)
public class MixinRuneBossObjectiveLootRank {

    /** Stamps the vault's medallion rank on the loot a boss rune is filled with, which the base mod builds by level only. */
    @ModifyExpressionValue(method = "lambda$initServer$6", at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultBossConfig;getRandomLootItems(ILiskallia/vault/core/random/RandomSource;)Ljava/util/List;"))
    private static List<ItemStack> stampRuneLootRank(List<ItemStack> items, @Local(argsOnly = true) ItemScavengeTaskEvent.Data data) {
        return AncientLootStamp.stampAll(items, data.getVault());
    }
}
