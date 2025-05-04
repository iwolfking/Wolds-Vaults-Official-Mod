package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.GodCharmRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = GodCharmRollHelper.class, remap = false)
public class MixinGodCharmRollHelper {
    @Inject(method = "initializeGodCharm", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/GodCharmRollHelper;getPrefixSlotsForRarity(Liskallia/vault/gear/VaultGearRarity;)I"))
    private static void addAffinityImplicit(ItemStack stack, Player player, CallbackInfo ci, @Local Optional<VaultGod> godOpt, @Local VaultGearData data) {
        if(godOpt.isPresent()) {
            VaultGod god = godOpt.get();

            VaultGearRarity rarity = data.getRarity();
            float affinityValue = 0.01F;

            switch (rarity) {
                case SCRAPPY -> affinityValue = player.getRandom().nextFloat(0.03F, 0.1F);
                case COMMON -> affinityValue = player.getRandom().nextFloat(0.05F, 0.14F);
                case RARE -> affinityValue = player.getRandom().nextFloat(0.08F, 0.18F);
                case EPIC -> affinityValue = player.getRandom().nextFloat(0.1F, 0.25F);
                case OMEGA -> affinityValue = player.getRandom().nextFloat(0.15F, 0.3F);
            }

            switch (god) {
                case IDONA -> data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<Float>(ModGearAttributes.IDONA_AFFINITY, affinityValue));
                case VELARA -> data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<Float>(ModGearAttributes.VELARA_AFFINITY, affinityValue));
                case WENDARR -> data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<Float>(ModGearAttributes.WENDARR_AFFINITY, affinityValue));
                case TENOS -> data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<Float>(ModGearAttributes.TENOS_AFFINITY, affinityValue));
            }
        }
    }
}
