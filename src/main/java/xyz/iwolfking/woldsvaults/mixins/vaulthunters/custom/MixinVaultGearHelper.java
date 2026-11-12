package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import iskallia.vault.gear.VaultGearRarity;
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
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;

import iskallia.vault.init.ModConfigs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;
import java.util.UUID;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

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

    /** Unidentified gear that is already flagged ancient says so in its name, in the ancient ember colour. */
    @Inject(method = "getDisplayName(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private static void nameUnidentifiedAncient(ItemStack stack, Component defaultName, CallbackInfoReturnable<Component> cir) {
        if (!ModConfigs.isInitialized()) {
            return;
        }
        GearDataCache cache = GearDataCache.of(stack);
        if (cache.getState() != VaultGearState.UNIDENTIFIED || !cache.hasAttribute(ModGearAttributes.ANCIENT_UNIQUE)) {
            return;
        }
        Style style = defaultName.getStyle().withColor(TextColor.fromRgb(AncientUniqueHelper.ANCIENT_COLOR));
        cir.setReturnValue(new TextComponent("Unidentified Ancient ").setStyle(style).append(defaultName.copy().setStyle(style)));
    }

    /** Identified set pieces show their name in the set colour instead of the Unique colour. */
    @Inject(method = "getDisplayName(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private static void nameSetPiece(ItemStack stack, Component defaultName, CallbackInfoReturnable<Component> cir) {
        if (!ModConfigs.isInitialized() || GearSetHelper.config() == null) {
            return;
        }
        GearDataCache cache = GearDataCache.of(stack);
        if (cache.getState() != VaultGearState.IDENTIFIED || cache.getRarity() != VaultGearRarity.UNIQUE) {
            return;
        }
        GearSetHelper.setOf(stack).flatMap(GearSetHelper.config()::getSet).ifPresent(set -> {
            boolean ancient = cache.hasAttribute(ModGearAttributes.ANCIENT_UNIQUE);
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(ancient ? set.ancientColorRgb() : set.colorRgb()));
            String name = cache.getGearName();
            cir.setReturnValue(name == null ? defaultName.copy().setStyle(style) : new TextComponent(name).setStyle(style));
        });
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
            WoldLogger.error("health_boost skipped on gear with no identifier; cannot derive a stable attribute modifier id");
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
