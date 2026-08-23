package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.MagnetItem;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;

import java.util.UUID;

@Mixin(value = VaultGearHelper.class, remap = false)
public class MixinVaultGearHelper {
    @Inject(method = "initializeGearRollType(Lnet/minecraft/world/item/ItemStack;ILiskallia/vault/core/random/RandomSource;)V", at= @At(value = "TAIL"))
    private static void initializeMapTier(ItemStack stack, int gearLevel, RandomSource random, CallbackInfo ci, @Local VaultGearData data) {
        if(stack.getItem() instanceof VaultMapItem mapItem) {
            if(!data.hasAttribute(ModGearAttributes.MAP_TIER)) {
                int tier = stack.getOrCreateTag().getInt("the_vault:map_tier");
                data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, tier);
                data.write(stack);
            }
        }
    }

    /** Applies {@code health_boost} as MULTIPLY_TOTAL on max health, so a 0.10 roll is 1.10x the total. */
    @ModifyReturnValue(method = "getModifiers(Liskallia/vault/gear/data/AttributeGearData;)Lcom/google/common/collect/Multimap;", at = @At("RETURN"))
    private static Multimap<Attribute, AttributeModifier> addTotalHealthMultiplier(Multimap<Attribute, AttributeModifier> original, AttributeGearData data) {
        if (!data.hasAttribute(ModGearAttributes.HEALTH_BOOST)) {
            return original;
        }
        float boost = data.get(ModGearAttributes.HEALTH_BOOST, VaultGearAttributeTypeMerger.floatSum());
        if (boost == 0.0F) {
            return original;
        }
        UUID identifier = data.getIdentifier();
        if (identifier == null) {
            WoldsVaults.LOGGER.error("health_boost skipped on gear with no identifier; cannot derive a stable attribute modifier id");
            return original;
        }
        AttributeModifier modifier = new AttributeModifier(seededId(identifier, Attributes.MAX_HEALTH, AttributeModifier.Operation.MULTIPLY_TOTAL), "VaultGear %s".formatted(Attributes.MAX_HEALTH.getDescriptionId()), boost, AttributeModifier.Operation.MULTIPLY_TOTAL);
        return ImmutableMultimap.<Attribute, AttributeModifier>builder().putAll(original).put(Attributes.MAX_HEALTH, modifier).build();
    }

    /** Mirrors {@code VaultGearHelper}'s private seededId, giving the added modifier a stable, unique id. */
    private static UUID seededId(UUID seed, Attribute attribute, AttributeModifier.Operation operation) {
        long attrHash = hashName(attribute.getRegistryName().toString());
        attrHash ^= hashName(operation.name());
        return new UUID(seed.getMostSignificantBits() ^ attrHash, seed.getLeastSignificantBits() ^ attrHash);
    }

    private static long hashName(String str) {
        long hash = 1125899906842597L;
        int length = str.length();
        for (int i = 0; i < length; ++i) {
            hash = 31L * hash + (long) str.charAt(i);
        }
        return hash;
    }
}
