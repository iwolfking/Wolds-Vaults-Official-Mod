package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

@Mixin(value = ResearchTree.class, remap = false)
public class MixinResearchTree {
    @Redirect(method = "restrictedBy(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/research/Restrictions$Type;)Ljava/lang/String;", at = @At(value = "INVOKE", target = "Liskallia/vault/research/type/Research;getName()Ljava/lang/String;"))
    private String checkResearchExclusionConfigForItem(Research instance, @Local(argsOnly = true) ItemStack item) {
        if(ModConfigs.RESEARCH_EXCLUSIONS.isExcludedFromResearch(instance.getName(), item.getItem().getRegistryName())) {
            return null;
        }

        return instance.getName();
    }

    @Redirect(method = "restrictedBy(Lnet/minecraft/world/level/block/Block;Liskallia/vault/research/Restrictions$Type;)Ljava/lang/String;", at = @At(value = "INVOKE", target = "Liskallia/vault/research/type/Research;getName()Ljava/lang/String;"))
    private String checkResearchExclusionConfigForBlock(Research instance, @Local(argsOnly = true) Block block) {
        if(ModConfigs.RESEARCH_EXCLUSIONS.isExcludedFromResearch(instance.getName(), block.getRegistryName())) {
            return null;
        }

        return instance.getName();
    }
}
