package xyz.iwolfking.woldsvaults.mixins.vaulthunters.modifiers;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.registry.VaultModifierType;
import iskallia.vault.core.vault.modifier.registry.VaultModifierTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.modifiers.vault.*;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.*;

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
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/player_attribute_settable"), VaultModifierType.of(PlayerAttributeModifierSettable.class, PlayerAttributeModifierSettable.Properties.class, PlayerAttributeModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/grouped_settable"), VaultModifierType.of(GroupedSettableModifier.class, GroupedSettableModifier.Properties.class, GroupedSettableModifier::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/add_inscription"), VaultModifierType.of(InscriptionCrystalModifierSettable.class, InscriptionCrystalModifierSettable.Properties.class, InscriptionCrystalModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/decorator_cascade_settable"), VaultModifierType.of(CascadeDecoratorModifierSettable.class, CascadeDecoratorModifierSettable.Properties.class, CascadeDecoratorModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/chance_catalyst_settable"), VaultModifierType.of(ChanceCatalystModifierSettable.class, ChanceCatalystModifierSettable.Properties.class, ChanceCatalystModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/chance_chest_trap_settable"), VaultModifierType.of(TrapChanceModifierSettable.class, TrapChanceModifierSettable.Properties.class, TrapChanceModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/crate_item_quantity_settable"), VaultModifierType.of(CrateItemQuantityModifierSettable.class, CrateItemQuantityModifierSettable.Properties.class, CrateItemQuantityModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/spawner_mobs_settable"), VaultModifierType.of(MobSpawnModifierSettable.class, MobSpawnModifierSettable.Properties.class, MobSpawnModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/vault_lootable_weight"), VaultModifierType.of(VaultLootableWeightModifierSettable.class, VaultLootableWeightModifierSettable.Properties.class, VaultLootableWeightModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/champion_chance_settable"), VaultModifierType.of(ChampionChanceModifierSettable.class, ChampionChanceModifierSettable.Properties.class, ChampionChanceModifierSettable::new));
        MODIFIER_TYPE_REGISTRY.put(VaultMod.id("modifier_type/greedy"), VaultModifierType.of(GreedyVaultModifier.class, GreedyVaultModifier.Properties.class, GreedyVaultModifier::new));
    }
}
