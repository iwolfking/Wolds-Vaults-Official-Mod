package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.command.GearDebugCommand;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;

import java.util.Map;
import java.util.Optional;

@Mixin(value = GearDebugCommand.class, remap = false)
public class MixinUniqueGearCommand {
    @Inject(method = "findPoolForUniqueItem", at = @At("HEAD"), cancellable = true)
    private void fixUniqueNameHandling(ResourceLocation uniqueId, CallbackInfoReturnable<ResourceLocation> cir) {
        Map<ResourceLocation, WeightedList<ResourceLocation>> pools = ModConfigs.UNIQUE_GEAR.getPools();
        ResourceLocation dedicatedPool = new ResourceLocation("woldsvaults", uniqueId.getPath());
        if (pools.containsKey(dedicatedPool)) {
            cir.setReturnValue(dedicatedPool);
        }
    }

    @Redirect(method = "giveUniqueItemByName", at = @At(value = "INVOKE", target = "Liskallia/vault/config/UniqueGearConfig;getEntry(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;"))
    private Optional<UniqueGearConfig.Entry> testForWoldsUnique(UniqueGearConfig instance, ResourceLocation id) {
        if(ModConfigs.UNIQUE_GEAR.getEntry(id).isEmpty()) {
            ResourceLocation woldsId = ResourceLocUtils.swapNamespace(id, "woldsvaults");
            if(ModConfigs.UNIQUE_GEAR.getEntry(woldsId).isPresent()) {
                return ModConfigs.UNIQUE_GEAR.getEntry(woldsId);
            }
        }

        return ModConfigs.UNIQUE_GEAR.getEntry(id);
    }

    @Inject(method = "determineItemStackForUnique", at = @At("HEAD"), cancellable = true)
    private void addAdditionalItemStacksForUniques(UniqueGearConfig.Entry uniqueEntry, CallbackInfoReturnable<ItemStack> cir) {
        ResourceLocation model = uniqueEntry.getModel();
        if(model != null) {
            String modelPath = model.getPath();
            if(modelPath.contains("jewel")) {
                cir.setReturnValue(new ItemStack(ModItems.JEWEL));
            }
            else if(modelPath.contains("magnet")) {
                cir.setReturnValue(new ItemStack(ModItems.MAGNET));
            }
            else if(modelPath.contains("battlestaff")) {
                cir.setReturnValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.BATTLESTAFF));
            }
            else if(modelPath.contains("trident")) {
                cir.setReturnValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.TRIDENT));
            }
            else if(modelPath.contains("rang")) {
                cir.setReturnValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.RANG));
            }
            else if(modelPath.contains("plushie")) {
                cir.setReturnValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PLUSHIE));
            }
            else if(modelPath.contains("loot_sack")) {
                cir.setReturnValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.LOOT_SACK));
            }
        }
    }
}
