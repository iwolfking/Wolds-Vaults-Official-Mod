package xyz.iwolfking.woldsvaults.mixins.modernfix;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTextures;
import iskallia.vault.item.tool.ToolMaterial;
import iskallia.vault.item.tool.ToolType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.embeddedt.modernfix.dynamicresources.ModelBakeryHelpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.lib.ExtendedToolType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(value = ModelBakeryHelpers.class, remap = false)
public class MixinModelBakeryHelpers {

    @Inject(method = "getExtraTextureFolders", at = @At("RETURN"), cancellable = true)
    private static void wv$addVaultToolTextureFolders(CallbackInfoReturnable<String[]> cir) {
        String[] original = cir.getReturnValue();
        List<String> folders = new ArrayList<>(Arrays.asList(original));
        
        folders.add("tool");
        folders.add("item/tool");
        
        cir.setReturnValue(folders.toArray(new String[0]));
    }

    @Inject(
            method = "gatherModelMaterials",
            at = @At("RETURN")
    )
    private static void wv$injectVaultToolMaterials(
            ResourceManager manager,
            Predicate<?> isTrustedPack,
            Set<Material> materialSet,
            Set<ResourceLocation> blockStateFiles,
            Set<ResourceLocation> modelFiles,
            UnbakedModel missingModel,
            Function<?, ?> modelDeserializer,
            Function<ResourceLocation, UnbakedModel> bakeryModelGetter,
            CallbackInfo ci
    ) {

        for (ToolType type : ToolType.values()) {
            materialSet.add(new Material(
                    TextureAtlas.LOCATION_BLOCKS,
                    ResourceLocation.fromNamespaceAndPath("the_vault", "item/tool/%s/handle".formatted(type.getId()))
            ));
        }

        for (ToolType type : ToolType.values()) {
            for (ToolMaterial mat : ToolMaterial.values()) {
                materialSet.add(new Material(
                        TextureAtlas.LOCATION_BLOCKS,
                        ResourceLocation.fromNamespaceAndPath("the_vault", "item/tool/%s/head/%s".formatted(type.getId(), mat.getId()))
                ));
            }
        }

        for (ExtendedToolType type : ExtendedToolType.values()) {
            for (ToolMaterial mat : ToolMaterial.values()) {
                materialSet.add(new Material(
                        TextureAtlas.LOCATION_BLOCKS,
                        ResourceLocation.fromNamespaceAndPath("the_vault", "item/tool/%s/head/%s".formatted(type.getId(), mat.getId()))
                ));
            }
        }
    }
}