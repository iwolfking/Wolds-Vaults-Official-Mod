package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.item.tool.IManualModelLoading;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.item.tool.ToolMaterial;
import iskallia.vault.item.tool.ToolType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.lib.ExtendedToolType;

import java.util.function.Consumer;

@Mixin(value = ToolItem.class, remap = false)
public abstract class MixinToolItem extends TieredItem implements VaultGearItem, Vanishable, IManualModelLoading {
    @Shadow
    public static ToolMaterial getMaterial(@NotNull ItemStack stack) {
        return null;
    }

    @Shadow @Final public static ResourceLocation SPAWNER_ID;

    public MixinToolItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    /**
     * @author iwolfking
     * @reason Load new custom models
     */
    @OnlyIn(Dist.CLIENT)
    @Inject(method = "loadModels", at = @At("TAIL"))
    public void loadExtendedModels(Consumer<ModelResourceLocation> consumer, CallbackInfo ci) {
        ExtendedToolType[] extendedToolTypes = ExtendedToolType.values();
        for (ExtendedToolType type : extendedToolTypes) {
            consumer.accept(new ModelResourceLocation("the_vault:tool/%s/handle#inventory.".formatted(type.getId())));

            for (ToolMaterial material : ToolMaterial.values()) {
                consumer.accept(new ModelResourceLocation("the_vault:tool/%s/head/%s#inventory".formatted(type.getId(), material.getId())));
            }
        }

    }

    /**
     * @author iwolfking
     * @reason Add custom tool names
     */
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true, remap = true)
    public void getName(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        ToolType type = ToolType.of(stack);
        ToolMaterial material = getMaterial(stack);
        ExtendedToolType extendedToolType = ExtendedToolType.of(stack);

        if(type == null && extendedToolType != null && material != null) {
            cir.setReturnValue((new TranslatableComponent(material.getDescription())).append(" ").append(new TranslatableComponent(extendedToolType.getDescription())));
        }
    }

    @Inject(method = "hasAffinity", at = @At("TAIL"), cancellable = true)
    public void hasAffinity(ItemStack stack, BlockState state, CallbackInfoReturnable<Boolean> cir, @Local VaultGearData data) {
        if(data.get(ModGearAttributes.TREASURE_AFFINITY, VaultGearAttributeTypeMerger.anyTrue()) && state.is(ModBlocks.TREASURE_CHEST)) {
            cir.setReturnValue(true);
        }
    }
}
