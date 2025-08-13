package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.VaultMod;
import iskallia.vault.core.util.ThemeBlockRetriever;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.init.ModConfigs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(TagLoader.class)
public class MixinTagLoader {
    @Shadow @Final private String directory;

    @Inject(method = "build", at = @At("HEAD"))
    private void afterBuild(Map<ResourceLocation, Tag.Builder> pBuilders, CallbackInfoReturnable<Map<ResourceLocation, Tag<?>>> cir) {
        if ("tags/items".equals(this.directory)){
            woldsVaults$genCrucibleTag(pBuilders);
        }
    }

    @Unique private static void woldsVaults$genCrucibleTag(Map<ResourceLocation, Tag.Builder> pBuilders) {
        Set<ResourceLocation> allItems = new HashSet<>(ModConfigs.VOID_CRUCIBLE_CUSTOM_ROOMS.getAllItems());
        for (var theme : VaultRegistry.THEME.getKeys()) {
            allItems.addAll(ThemeBlockRetriever.getBlocksForTheme(theme.getId()));
        }

        List<ResourceLocation> holders =
            allItems.stream()
                .filter(ThemeBlockRetriever::allowVaultBlock)
                .filter(x -> Registry.ITEM.getOptional(x).isPresent()) // some mods might not be loaded (for example in dev)
                .toList();
        Tag.Builder voidedByCrucible = pBuilders.computeIfAbsent(VaultMod.id("voided_by_crucible"), id -> Tag.Builder.tag());

        for (ResourceLocation item : holders) {
            voidedByCrucible.addElement(item, "Wold's dynamic tags");
        }


        List<ResourceLocation> notAllowedHolders =
            allItems.stream()
                .filter(x -> !ThemeBlockRetriever.allowVaultBlock(x))
                .filter(x -> Registry.ITEM.getOptional(x).isPresent()) // some mods might not be loaded (for example in dev)
                .toList();
        Tag.Builder voidCrucibleExtras = pBuilders.computeIfAbsent(VaultMod.id("void_crucible_extras"), id -> Tag.Builder.tag());

        for (ResourceLocation item : notAllowedHolders) {
            voidCrucibleExtras.addElement(item, "Wold's dynamic tags");
        }
    }
}
