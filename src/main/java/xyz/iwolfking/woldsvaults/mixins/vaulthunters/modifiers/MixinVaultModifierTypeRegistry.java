package xyz.iwolfking.woldsvaults.mixins.vaulthunters.modifiers;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.registry.VaultModifierType;
import iskallia.vault.core.vault.modifier.registry.VaultModifierTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.modifiers.vault.*;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.GroupedSettableModifier;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.LootItemQuantityModifierSettable;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.LootItemRarityModifierSettable;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.MobAttributeModifierSettable;

import java.util.Map;

@Mixin(value = VaultModifierTypeRegistry.class, remap = false)
public class MixinVaultModifierTypeRegistry {
    @Shadow @Final private static Map<ResourceLocation, VaultModifierType<?, ?>> MODIFIER_TYPE_REGISTRY;

    static {
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/infernal_mobs"), VaultModifierType.of(InfernalMobModifier.class, InfernalMobModifier.Properties.class, InfernalMobModifier::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/mob_death_bomb"), VaultModifierType.of(MobDeathBombModifier.class, MobDeathBombModifier.Properties.class, MobDeathBombModifier::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/chest_break_bomb"), VaultModifierType.of(ChestOpenBombModifier.class, ChestOpenBombModifier.Properties.class, ChestOpenBombModifier::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/enchanted_event_chance"), VaultModifierType.of(EnchantedVaultModifier.class, EnchantedVaultModifier.Properties.class, EnchantedVaultModifier::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/retro_spawn_modifier"), VaultModifierType.of(RetroSpawnVaultModifier.class, RetroSpawnVaultModifier.Properties.class, RetroSpawnVaultModifier::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/experience_multiplier"), VaultModifierType.of(ExperienceMultiplierModifier.class, ExperienceMultiplierModifier.Properties.class, ExperienceMultiplierModifier::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/difficulty_lock"), VaultModifierType.of(DifficultyLockModifier.class, DifficultyLockModifier.Properties.class, DifficultyLockModifier::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/loot_item_quantity_settable"), VaultModifierType.of(LootItemQuantityModifierSettable.class, LootItemQuantityModifierSettable.Properties.class, LootItemQuantityModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/loot_item_rarity_settable"), VaultModifierType.of(LootItemRarityModifierSettable.class, LootItemRarityModifierSettable.Properties.class, LootItemRarityModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/mob_attribute_settable"), VaultModifierType.of(MobAttributeModifierSettable.class, MobAttributeModifierSettable.Properties.class, MobAttributeModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/grouped_settable"), VaultModifierType.of(GroupedSettableModifier.class, GroupedSettableModifier.Properties.class, GroupedSettableModifier::new));
    }
}
