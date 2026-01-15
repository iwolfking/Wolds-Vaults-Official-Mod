package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import iskallia.vault.item.render.core.IManualModelLoading;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.core.layout.LayoutDefinitionRegistry;
import xyz.iwolfking.woldsvaults.api.core.layout.definitions.CompoundLayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = VaultCrystalItem.class, remap = false)
public abstract class MixinItemVaultCrystal extends Item implements IManualModelLoading {
    public MixinItemVaultCrystal(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        CrystalData data = CrystalData.read(pStack);
        CrystalLayout layout = data.getLayout();

        if(layout != null) {
            Optional<LayoutDefinition> layoutDefinition = LayoutDefinitionRegistry.getForLayout(data.getLayout());
            if(layoutDefinition.isPresent()) {
                CompoundTag layoutData = new CompoundTag();
                layoutDefinition.get().writeFlatLayout(layout, layoutData);
                if(layoutDefinition.get() instanceof CompoundLayoutDefinition compoundLayoutDefinition) {
                    return compoundLayoutDefinition.getTooltipImage(layoutData, data.getProperties().getCustomSeed());
                }
                return layoutDefinition.get().getTooltipImage(layoutData);
            }
        }

        return Optional.empty();
    }

    @Inject(method = "loadModels", at = @At("TAIL"))
    public void loadModels(Consumer<ModelResourceLocation> consumer, CallbackInfo ci) {
        consumer.accept(new ModelResourceLocation("the_vault:crystal/core/unhinged#inventory"));
    }

}
