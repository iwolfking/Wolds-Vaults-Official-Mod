package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.etching;

import iskallia.vault.config.gear.EtchingTierConfig;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = EtchingHelper.class, remap = false)
public class MixinEtchingHelper {
    @Unique
    private static final Set<ResourceLocation> woldsvaults$reportedEtchings = ConcurrentHashMap.newKeySet();

    /**
     * EtchingHelper.getEtching looks the etched value up under a gear attribute named like the etching itself, so an
     * etching whose modifier uses another attribute (Pyramid Scheme adds woldsvaults:additional_stacking_stacks) shows
     * its raw description placeholders. When that lookup finds nothing, this reads the value through the attributes
     * the etching's config lists, logging each etching it has to resolve this way once.
     */
    @Inject(method = "getEtching", at = @At("RETURN"), cancellable = true)
    private static void woldsvaults$findValueByConfiguredAttribute(ItemStack stack, CallbackInfoReturnable<Optional<?>> cir) {
        if (cir.getReturnValue().isPresent()) {
            return;
        }
        VaultGearData data = VaultGearData.read(stack);
        ResourceLocation etchingId = data.getFirstValue(ModGearAttributes.ETCHING).orElse(null);
        if (etchingId == null) {
            return;
        }
        VaultEtchingConfig.EtchingEntry entry = ModConfigs.ETCHINGS.getEtchingConfig(etchingId);
        if (entry == null || entry.getAttributes() == null) {
            return;
        }
        for (EtchingTierConfig.EtchingModifierTierGroup group : entry.getAttributes()) {
            VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(group.getAttribute());
            if (attribute == null) {
                continue;
            }
            Optional<?> value = data.getFirstValue(attribute);
            if (value.isPresent()) {
                if (woldsvaults$reportedEtchings.add(etchingId)) {
                    WoldsVaults.LOGGER.error("Etching {} has no gear attribute of the same id; its tooltip value is read from {} instead.",
                            etchingId, group.getAttribute());
                }
                cir.setReturnValue(value);
                return;
            }
        }
    }
}
