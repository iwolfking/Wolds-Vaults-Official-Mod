package xyz.iwolfking.woldsvaults.datagen;

import com.cursedcauldron.wildbackport.common.registry.WBMobEffects;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.google.gson.annotations.SerializedName;
import iskallia.vault.VaultMod;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.config.CustomEntitySpawnerConfig;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.modifier.modifier.*;
import iskallia.vault.core.vault.modifier.spi.EntityAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.data.tile.TilePredicate;
import iskallia.vault.core.world.processor.tile.TileProcessor;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModEffects;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.world.VaultDifficulty;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultModifierProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.BasicListBuilder;
import xyz.iwolfking.vhapi.api.datagen.lib.WeightedListBuilder;
import xyz.iwolfking.vhapi.api.datagen.lib.modifiers.ModifierBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.modifiers.vault.ChestOpenBombModifier;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.DecoratorAddModifierSettable;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ModVaultModifiersProvider extends AbstractVaultModifierProvider {

    public ModVaultModifiersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }


    public static VaultModifier.Display display(String name, TextColor color, String description, String descriptionFormatted, ResourceLocation icon) {
        return new VaultModifier.Display(name, color, description, descriptionFormatted, icon);
    }

    @Override
    public void addFiles(Map<String, Consumer<ModifierBuilder>> map) {
        map.put("test", modifierBuilder -> {
            difficultyLock(modifierBuilder, VaultMod.id("piece_of_cake"), VaultDifficulty.PIECE_OF_CAKE, true, "Piece of Cake", "#EBFF8D", "This vault's difficulty is locked to Piece of Cake.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("easy"), VaultDifficulty.EASY, true, "Easy", "#EBFF8D", "This vault's difficulty is locked to Easy.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("normal"), VaultDifficulty.NORMAL, true, "Normal", "#EBFF8D", "This vault's difficulty is locked to Normal.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("hard"), VaultDifficulty.HARD, true, "Hard", "#EBFF8D", "This vault's difficulty is locked to Hard.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("impossible"), VaultDifficulty.IMPOSSIBLE, true, "Impossible", "#EBFF8D", "This vault's difficulty is locked to Impossible.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("fragged"), VaultDifficulty.FRAGGED, true, "Fragged", "#EBFF8D", "This vault's difficulty is locked to Fragged.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("normalized"), VaultDifficulty.NORMAL, false, "Normalized", "#EBFF8D", "This vault's difficulty is locked to Normal.", null, VaultMod.id("gui/modifiers/impossible"));

            artifactChance(modifierBuilder, VaultMod.id("more_artifact_chance"), 0.01F, "Increased Artifact Chance", "#EBFF8D", "Every stack increases the artifact chance by 1 percent.", "+%d%% Artifact Chance", VaultMod.id("gui/modifiers/more_artifact1"));
            artifactChance(modifierBuilder, VaultMod.id("pogging"), 0.05F, "Pogging", "#EBFF8D", "Every stack increases the artifact chance by 5 percent.", "+%d%% Artifact Chance", VaultMod.id("gui/modifiers/more_artifact1"));

            catalystChacne(modifierBuilder, VaultMod.id("more_catalysts"), 0.01F, "More Catalyst Fragments", "#FC00E3", "+1% Catalyst Fragments", "+%d%% Catalyst Fragments", VaultMod.id("gui/modifiers/more_catalyst"));
            catalystChacne(modifierBuilder, VaultMod.id("sparkling"), 0.05F, "Sparkling", "#FC00E3", "+5% Catalyst Fragments", "+%d%% Catalyst Fragments", VaultMod.id("gui/modifiers/more_catalyst"));
            catalystChacne(modifierBuilder, VaultMod.id("iridescent"), 1.0F, "Iridescent", "#FC00E3", "+100% Catalyst Fragments", "+%d%% Catalyst Fragments", VaultMod.id("gui/modifiers/more_catalyst"));

            soulShardChance(modifierBuilder, VaultMod.id("exorcising"), 0.5F, "Exorcising", "#6410A1", "+50% Soul Shard Drop Rate", "+%d%% Soul Shards", VaultMod.id("gui/modifiers/soul_shard_increase_pink"));


            decoratorAdd(modifierBuilder, VaultMod.id("vexation"), customSpawnerTile("champion_vex"), 1, false, "Vexation","#237da1", "+1 Randomly Spawning Champion Vex", null,  VaultMod.id("gui/modifiers/vexation"));
            decoratorAdd(modifierBuilder, WoldsVaults.id("ghost_city"), customSpawnerTile("all_ghosts"), 2, false, "Ghost City","#237da1", "+8 Randomly Spawning Ghosts", null,  VaultMod.id("gui/modifiers/ghost_town"));
            decoratorAdd(modifierBuilder, WoldsVaults.id("ghost_town"), customSpawnerTile("all_ghosts"), 1, false, "Ghost Town","#237da1", "+4 Randomly Spawning Ghosts", null,  VaultMod.id("gui/modifiers/ghost_town"));
            decoratorAdd(modifierBuilder, VaultMod.id("spooky"), PartialTile.of(PartialBlockState.of(ModBlocks.WILD_SPAWNER), PartialCompoundNbt.empty()), 16, false, "Spooky","#237da1", "+1 Set of Randomly Spawning Ghosts", null,  VaultMod.id("gui/modifiers/spooky"));
            decoratorAdd(modifierBuilder, VaultMod.id("fungal"), customSpawnerTile("fungal"), 7, false, "Fungal","#ff0000", "This vault is bathed in spores.", null,  VaultMod.id("gui/modifiers/fungal"));
            decoratorAdd(modifierBuilder, VaultMod.id("winter"), customSpawnerTile("winter"), 7, false, "Winter","#ff0000", "This vault is frosty.", null,  VaultMod.id("gui/modifiers/winter"));
            decoratorAdd(modifierBuilder, VaultMod.id("electric"), customSpawnerTile("electric"), 6, false, "Electric","#ff0000", "This vault is electrifying.", null,  VaultMod.id("gui/modifiers/electric"));
            decoratorAdd(modifierBuilder, VaultMod.id("safari"), customSpawnerTile("safari"), 2, false, "Safari","#ff0000", "This vault is in a safari zone.", null,  VaultMod.id("gui/modifiers/safari"));

            crateQuantity(modifierBuilder, VaultMod.id("super_crate_tier"), 4.0F, "Super Crate Tier", "#38C9C0", "+400% Crate Quantity", "+%d%% Crate Quantity", VaultMod.id("gui/modifiers/crate_item_quantity"));

            itemQuantity(modifierBuilder, VaultMod.id("pilfered"), -0.15F, "Pilfered", "#a54726", "Item Quantity reduced by 15%", "-%d%% Item Quantity", VaultMod.id("gui/modifiers/poor"));

            itemRarity(modifierBuilder, VaultMod.id("archaic"), -0.15F, "Archaic", "#f3ff70", "Item Rarity reduced by 15%", "-%d%% Item Rarity", VaultMod.id("gui/modifiers/poor"));
            itemRarity(modifierBuilder, VaultMod.id("mediocre"), -0.25F, "Mediocre", "#f3ff70", "Item Rarity reduced by 25%", "-%d%% Item Rarity", VaultMod.id("gui/modifiers/poor"));

            castOnKill(modifierBuilder, VaultMod.id("bingo_kill_charm"), EntityPredicate.TRUE, "charm_8", 0.03F, "Cute", "#e6fffe", "When killing a mob it has a 3% chance to cast Charm around it", "", VaultMod.id("gui/modifiers/kill_charm"));
            castOnKill(modifierBuilder, VaultMod.id("bingo_kill_nuke"), EntityPredicate.TRUE, "nova_explosion_8", 0.03F, "Mini Nova", "#FFEA00", "When killing a mob it has a 3% chance to cast Nova around it", "", VaultMod.id("gui/modifiers/kill_nuke"));

            mobAttribute(modifierBuilder, VaultMod.id("bingo_critical_mobs"), EntityAttributeModifier.ModifierType.CRIT_CHANCE_ADDITIVE, 0.075F,  "Sharpened Mobs", "#FFEA00", "+7.5% Mob Critical Strike Chance", "+%d%% Mob Critical Strike Chance", VaultMod.id("gui/modifiers/destructive"));
            mobAttribute(modifierBuilder, VaultMod.id("critical_mobs"), EntityAttributeModifier.ModifierType.CRIT_CHANCE_ADDITIVE, 0.15F,  "Sharpened Mobs", "#FFEA00", "+15% Mob Critical Strike Chance", "+%d%% Mob Critical Strike Chance", VaultMod.id("gui/modifiers/destructive"));
            mobAttribute(modifierBuilder, VaultMod.id("no_crit_mobs"), EntityAttributeModifier.ModifierType.CRIT_CHANCE_ADDITIVE, -10.0F,  "Non-Lethal", "#dc693c", "-1000% Mob Critical Strike Chance", "+%d%% Mob Critical Strike Chance", VaultMod.id("gui/modifiers/destructive"));

            mobAttribute(modifierBuilder, VaultMod.id("baby_mobs2"), EntityAttributeModifier.ModifierType.MAX_HEALTH_ADDITIVE_PERCENTILE, -0.25F,  "Lean Mobs", "#D4E157", "-25% Mob Max Health", "+%d%% Mob Max Health", VaultMod.id("gui/modifiers/chunky"));
            mobAttribute(modifierBuilder, VaultMod.id("baby_mobs4"), EntityAttributeModifier.ModifierType.MAX_HEALTH_ADDITIVE_PERCENTILE, -0.5F,  "Leaner Mobs", "#D4E157", "-50% Mob Max Health", "+%d%% Mob Max Health", VaultMod.id("gui/modifiers/chunky"));
            mobAttribute(modifierBuilder, VaultMod.id("brutal_chunky_mobs"), EntityAttributeModifier.ModifierType.MAX_HEALTH_ADDITIVE_PERCENTILE, 0.5F,  "Brutally Chunky Mobs", "#dc693c", "+50% Mob Max Health", "+%d%% Mob Max Health", VaultMod.id("gui/modifiers/chunky"));
            mobAttribute(modifierBuilder, VaultMod.id("chunky_mobs4"), EntityAttributeModifier.ModifierType.MAX_HEALTH_ADDITIVE_PERCENTILE, 1.0F,  "Chungus Mobs", "#dc693c", "+100% Mob Max Health", "+%d%% Mob Max Health", VaultMod.id("gui/modifiers/chunky"));

            mobAttribute(modifierBuilder, VaultMod.id("enraged_mobs"), EntityAttributeModifier.ModifierType.SPEED_ADDITIVE_PERCENTILE, 0.3F,  "Super Rapid Mobs", "#f66868", "+30% Mob Speed", "+%d%% Mob Speed", VaultMod.id("gui/modifiers/rapid_mobs"));

            mobCurseOnHit(modifierBuilder, WoldsVaults.id("bleeding_mobs"), ModEffects.BLEED.getRegistryName(), 2,  100, 0.1F,"Bleeding", "#9B3E56", "Mobs can inflict Bleeding on hit", null, VaultMod.id("gui/modifiers/hex_chaining"));
            mobCurseOnHit(modifierBuilder, VaultMod.id("fatiguing"), MobEffects.DIG_SLOWDOWN.getRegistryName(), 4,  200, 0.1F,"Fatiguing", "#9B3E56", "Mobs can inflict Mining Fatigue on hit", null, VaultMod.id("gui/modifiers/hex_chaining"));
            mobCurseOnHit(modifierBuilder, VaultMod.id("toxic"), MobEffects.POISON.getRegistryName(), 3,  200, 0.075F,"Toxic", "#84BF17", "Mobs can inflict greater Poison on hit", null, VaultMod.id("gui/modifiers/hex_poison"));
            mobCurseOnHit(modifierBuilder, VaultMod.id("stunning"), AMEffectRegistry.FEAR.getRegistryName(), 0,  20, 0.03F,"Stunning", "#5A5851", "Mobs can inflict a Stun on hit", null, VaultMod.id("gui/modifiers/hex_stunning"));
            mobCurseOnHit(modifierBuilder, VaultMod.id("dark"), WBMobEffects.DARKNESS.get().getRegistryName(), 0,  100, 0.1F,"Blinding", "#5A5851", "Mobs can inflict Darkness on hit", null, VaultMod.id("gui/modifiers/hex_blinding"));
            mobCurseOnHit(modifierBuilder, VaultMod.id("haunting"), ModEffects.TIMER_ACCELERATION.getRegistryName(), 1,  20, 0.05F,"Haunting", "#e6f7fa", "Mobs can inflict Haunting on hit", null, VaultMod.id("gui/modifiers/hex_wither"));

            frenzy(modifierBuilder, VaultMod.id("catastrophic_brew"), 2.0F, 0.25F, 2,  false, "Catastrophic Brew", "#FC7C5C", "Yikes! +200% Mob Damage, +25% Mob Speed", "" , VaultMod.id("gui/modifiers/frenzy"));

            mobIncrease(modifierBuilder, VaultMod.id("unhinged_mob_increase"), 3.0F, "Super Onslaught", "#FC7C5C", "+300% Mob Spawns", "+%d%% Mob Spawns", VaultMod.id("gui/modifiers/mob_increase"));

            playerAttribute(modifierBuilder, VaultMod.id("drought"), EntityAttributeModifier.ModifierType.MANA_REGEN_ADDITIVE_PERCENTILE, -0.25F, "Drought", "#849dc8", "-25% Mana Regeneration", "-%d%% Mana Regeneration", VaultMod.id("gui/modifiers/draining"));
            playerAttribute(modifierBuilder, VaultMod.id("bingo_drained"), EntityAttributeModifier.ModifierType.MANA_REGEN_ADDITIVE_PERCENTILE, -0.15F, "Draining", "#849dc8", "-15% Mana Regeneration", "-%d%% Mana Regeneration", VaultMod.id("gui/modifiers/draining"));
            playerAttribute(modifierBuilder, VaultMod.id("healthy"), EntityAttributeModifier.ModifierType.MAX_HEALTH_ADDITIVE, 4F, "Healthy", "#FF5555", "+2 Hearts", "+%d Hit Points", VaultMod.id("gui/modifiers/regeneration"));

            playerDurability(modifierBuilder, VaultMod.id("acidic"), 1.4F, "Acidic", "#7B7E7F", "+40% Durability Damage", "+%d%% Durability Damage", VaultMod.id("gui/modifiers/acidic"));
            playerDurability(modifierBuilder, VaultMod.id("corrosive"), 2.0F, "Corrosive", "#7B7E7F", "+100% Durability Damage", "+%d%% Durability Damage", VaultMod.id("gui/modifiers/corrosive"));
            playerDurability(modifierBuilder, VaultMod.id("plated"), 0.5F, "Plated", "#9550FF", "-50% Durability Damage", "-%d%% Durability Damage", VaultMod.id("gui/modifiers/plated"));

            playerEffect(modifierBuilder, VaultMod.id("springy"), MobEffects.JUMP.getRegistryName(), 1, "Springy", "#e6fbfa", "+1 Jump Boost", "+%d Jump Boost", VaultMod.id("gui/modifiers/springy"));
            playerEffect(modifierBuilder, VaultMod.id("jumpy_deluxe"), MobEffects.JUMP.getRegistryName(), 4, "Jumpy Deluxe", "#e6fbfa", "+4 Jump Boost", "+%d Jump Boost", VaultMod.id("gui/modifiers/springy"));
            playerEffect(modifierBuilder, VaultMod.id("tailwind"), MobEffects.MOVEMENT_SPEED.getRegistryName(), 3, "Tailwind", "#fbed3f", "+3 Speed", "+%d Speed", VaultMod.id("gui/modifiers/speed"));
            playerEffect(modifierBuilder, VaultMod.id("super_stronk"), MobEffects.DAMAGE_BOOST.getRegistryName(), 6, "Super Stronk", "#d73b46", "+6 Strength", "+%d Strength", VaultMod.id("gui/modifiers/stronk"));

            inventoryRestore(modifierBuilder, VaultMod.id("map_afterlife"), false, 0.0F, 1.0F, false, "Map Afterlife", "#3ffb9c", "No item loss on death.", null, VaultMod.id("gui/modifiers/afterlife"));
            inventoryRestore(modifierBuilder, VaultMod.id("oneup"), false, 0.0F, 1.0F, false, "Spirit Restore", "#3ffb9c", "No item loss on death.", null, VaultMod.id("gui/modifiers/afterlife"));

            playerStat(modifierBuilder, VaultMod.id("looters_lair"), PlayerStat.TRAP_DISARM_CHANCE, 0.5F,  "Safer Zone", "#A3E2F5", "+50% Trap Disarm Chance", "+%d%% Trap Disarm Chance", VaultMod.id("gui/modifiers/safezone"));
            playerStat(modifierBuilder, VaultMod.id("disarming"), PlayerStat.TRAP_DISARM_CHANCE, 0.25F,  "Disarming", "#cb9be5", "+25% Trap Disarm Chance", "+%d%% Trap Disarm Chance", VaultMod.id("gui/modifiers/safezone"));
            playerStat(modifierBuilder, VaultMod.id("bingo_trapped"), PlayerStat.TRAP_DISARM_CHANCE, -0.25F,  "Trapped", "#CB866D", "-25% Trap Disarm Chance", "-%d%% Trap Disarm Chance", VaultMod.id("gui/modifiers/trapped"));
            playerStat(modifierBuilder, VaultMod.id("rigged"), PlayerStat.TRAP_DISARM_CHANCE, -1.0F,  "Rigged", "#CB866D", "-100% Trap Disarm Chance", "-%d%% Trap Disarm Chance", VaultMod.id("gui/modifiers/trapped"));
            playerStat(modifierBuilder, VaultMod.id("rending"), PlayerStat.COOLDOWN_REDUCTION, -0.25F,  "Rending", "#6DACB5", "-25% Cooldown Reduction", "-%d%% Cooldown Reduction", VaultMod.id("gui/modifiers/inert"));
            playerStat(modifierBuilder, VaultMod.id("nullifying"), PlayerStat.COOLDOWN_REDUCTION, -0.5F,  "Nullifying", "#6DACB5", "-50% Cooldown Reduction", "-%d%% Cooldown Reduction", VaultMod.id("gui/modifiers/inert"));
            playerStat(modifierBuilder, VaultMod.id("dangerous"), PlayerStat.RESISTANCE, -0.25F,  "Dangerous", "#CA9A5B", "-25% Resistance", "-%d%% Resistance", VaultMod.id("gui/modifiers/vulnerable"));
            playerStat(modifierBuilder, VaultMod.id("piercing"), PlayerStat.RESISTANCE, -0.5F,  "Piercing", "#CA9A5B", "-50% Resistance", "-%d%% Resistance", VaultMod.id("gui/modifiers/vulnerable"));
            playerStat(modifierBuilder, VaultMod.id("protected"), PlayerStat.RESISTANCE, 0.1F,  "Protected", "#CA9A5B", "+10% Resistance", "+%d%% Resistance", VaultMod.id("gui/modifiers/vulnerable"));
            playerStat(modifierBuilder, VaultMod.id("corroded_veins"), PlayerStat.COPIOUSLY, -1.0F,  "Corroded Veins", "#F74780", "-100% Copiously", "-%d%% Copiously", VaultMod.id("gui/modifiers/impossible"));
            playerStat(modifierBuilder, VaultMod.id("perfect_ores"), PlayerStat.COPIOUSLY, 1.0F,  "Perfect Veins", "#ffba00", "+100% Copiously", "+%d%% Copiously", VaultMod.id("gui/modifiers/rich"));
            playerStat(modifierBuilder, VaultMod.id("orematic"), PlayerStat.COPIOUSLY, 0.35F,  "Rich Veins", "#F74780", "+35% Copiously", "+%d%% Copiously", VaultMod.id("gui/modifiers/oremania"));
            playerStat(modifierBuilder, VaultMod.id("leeching"), PlayerStat.LEECH, 0.05F,  "Bloodsucking", "#FF5555", "+5% Leech", "+%d%% Leech", VaultMod.id("gui/modifiers/god_token_idona"));

            lootWeight(modifierBuilder, VaultMod.id("super_plentiful"), PlaceholderBlock.Type.ORE, 0.6F,  "Super Plentiful", "#FF85FF", "+60% Vault Ores", "+%d%% Vault Ores", VaultMod.id("gui/modifiers/plentiful"));












        });
    }

    public static PartialTile customSpawnerTile(String customEntitySpawnerId) {
        CompoundTag tag = new CompoundTag();
        tag.putString("spawnerGroupName", customEntitySpawnerId);
        return PartialTile.of(PartialBlockState.of(ModBlocks.CUSTOM_ENTITY_SPAWNER), PartialCompoundNbt.of(tag));
    }


    public static void difficultyLock(ModifierBuilder builder, ResourceLocation modifierId, VaultDifficulty difficulty, boolean shouldLockHigher, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/difficulty_lock").toString(), typeBuilder -> {
           typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("difficulty", difficulty.name());
                modifierEntryBuilder.property("lockHigher", shouldLockHigher);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
           });
        });
    }

    public static void chance(ModifierBuilder builder, ResourceLocation modifierTypeId, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(modifierTypeId.toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("chance", chance);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void percentage(ModifierBuilder builder, ResourceLocation modifierTypeId, ResourceLocation modifierId, float percentage, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(modifierTypeId.toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("percentage", percentage);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void decoratorAdd(ModifierBuilder builder, ResourceLocation modifierId, PartialTile tile, int attemptsPerChunk, boolean requiresConditions, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/decorator_add").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("output", tile.toString());
                modifierEntryBuilder.property("attemptsPerChunk", attemptsPerChunk);
                modifierEntryBuilder.property("requiresConditions", requiresConditions);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void castOnKill(ModifierBuilder builder, ResourceLocation modifierId, EntityPredicate filter, String abilityId, float probability, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/cast_on_kill").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                if(filter == EntityPredicate.TRUE) {
                    modifierEntryBuilder.property("filter", "");
                }
                else {
                    modifierEntryBuilder.property("filter", filter.toString());
                }
                modifierEntryBuilder.property("ability", abilityId);
                modifierEntryBuilder.property("probability", probability);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void dropOnKill(ModifierBuilder builder, ResourceLocation modifierId, EntityPredicate filter, ResourceLocation lootTable, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/drop_on_kill").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("filter", filter.toString());
                modifierEntryBuilder.property("lootTable", lootTable.toString());
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void mobAttribute(ModifierBuilder builder, ResourceLocation modifierId, EntityAttributeModifier.ModifierType attribute, float amount, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        try {
            Field attributeField = EntityAttributeModifier.ModifierType.class.getDeclaredField(attribute.name());
            attributeField.setAccessible(true);
            SerializedName serializedName = attributeField.getAnnotation(SerializedName.class);
            String serializedNameValue = serializedName != null ? serializedName.value() : attribute.name();
            builder.type(VaultMod.id("modifier_type/mob_attribute").toString(), typeBuilder -> {
                typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                    modifierEntryBuilder.property("type", serializedNameValue);
                    modifierEntryBuilder.property("amount", amount);
                    modifierEntryBuilder.property("attributeStackingStrategy", "STACK");
                    createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
                });
            });
        }
        catch (NoSuchFieldException e) {
            WoldsVaults.LOGGER.error("Invalid EntityAttributeModifier.ModifierType passed in, skipping adding {} modifier.", modifierId);
        }
    }

    public static void mobCurseOnHit(ModifierBuilder builder, ResourceLocation modifierId, ResourceLocation effect, int amplifier, int durationTicks, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/mob_curse_on_hit").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("effect", effect.toString());
                modifierEntryBuilder.property("effectAmplifier", amplifier);
                modifierEntryBuilder.property("effectDurationTicks", durationTicks);
                modifierEntryBuilder.property("onHitApplyChance", chance);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void mobIncrease(ModifierBuilder builder, ResourceLocation modifierId, float increase, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/spawner_mobs").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("increase", increase);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void objectiveDifficulty(ModifierBuilder builder, ResourceLocation modifierId, float increase, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/objective_target").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("increase", increase);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void hunter(ModifierBuilder builder, ResourceLocation modifierId, Consumer<HunterBuilder> hunterBuilderConsumer, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/hunter").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                HunterBuilder entriesBuilder = new HunterBuilder();
                hunterBuilderConsumer.accept(entriesBuilder);
                modifierEntryBuilder.property("entries", entriesBuilder.build());
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }


    public static void legacyMobIncrease(ModifierBuilder builder, ResourceLocation modifierId, int maxMobsAdded, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/mob_spawn_count").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("maxMobsAdded", maxMobsAdded);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void playerAttribute(ModifierBuilder builder, ResourceLocation modifierId, EntityAttributeModifier.ModifierType attribute, float amount, String name, String color, String description, String formattedDescription, ResourceLocation icon)  {
        try {
            Field attributeField = EntityAttributeModifier.ModifierType.class.getDeclaredField(attribute.name());
            attributeField.setAccessible(true);
            SerializedName serializedName = attributeField.getAnnotation(SerializedName.class);
            String serializedNameValue = serializedName != null ? serializedName.value() : attribute.name();
            builder.type(VaultMod.id("modifier_type/player_attribute").toString(), typeBuilder -> {
                typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                    modifierEntryBuilder.property("type", serializedNameValue);
                    modifierEntryBuilder.property("amount", amount);
                    modifierEntryBuilder.property("attributeStackingStrategy", "STACK");
                    createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
                });
            });
        } catch (NoSuchFieldException ignored) {
            WoldsVaults.LOGGER.error("Invalid EntityAttributeModifier.ModifierType passed in, skipping adding {} modifier.", modifierId);
        }

    }

    public static void playerDurability(ModifierBuilder builder, ResourceLocation modifierId, float durabilityDamageTakenMultiplier, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/player_durability_damage").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("durabilityDamageTakenMultiplier", durabilityDamageTakenMultiplier);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void playerEffect(ModifierBuilder builder, ResourceLocation modifierId, ResourceLocation effect, int amplifier, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/player_effect").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("effect", effect.toString());
                modifierEntryBuilder.property("effectAmplifier", amplifier);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void entityEffect(ModifierBuilder builder, ResourceLocation modifierId, EntityPredicate filter, ResourceLocation effect, int amplifier, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/entity_effect").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("filter", filter.toString());
                modifierEntryBuilder.property("effect", effect.toString());
                modifierEntryBuilder.property("effectAmplifier", amplifier);
                modifierEntryBuilder.property("chance", chance);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void noSoulShards(ModifierBuilder builder, ResourceLocation modifierId, EntityPredicate filter, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/no_soul_shards").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("filter", filter.toString());
                modifierEntryBuilder.property("chance", chance);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void inventoryRestore(ModifierBuilder builder, ResourceLocation modifierId, boolean preventsArtifact, float experienceMultiDeath, float experienceMultiSuccess, boolean isInstantRevival, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/player_inventory_restore").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("preventsArtifact", preventsArtifact);
                modifierEntryBuilder.property("experienceMultiplierOnDeath", experienceMultiDeath);
                modifierEntryBuilder.property("experienceMultiplierOnSuccess", experienceMultiSuccess);
                modifierEntryBuilder.property("isInstantRevival", isInstantRevival);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void locked(ModifierBuilder builder, ResourceLocation modifierId, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/player_no_exit").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void noFruit(ModifierBuilder builder, ResourceLocation modifierId, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/player_no_vault_fruit").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void noCrate(ModifierBuilder builder, ResourceLocation modifierId, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/no_crate").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void noLoot(ModifierBuilder builder, ResourceLocation modifierId, boolean destroyChests, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/no_loot").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("destroyChests", destroyChests);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void healCut(ModifierBuilder builder, ResourceLocation modifierId, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/player_heal_cut").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }


    public static void noTemporal(ModifierBuilder builder, ResourceLocation modifierId, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/player_no_temporal_shard").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void playerStat(ModifierBuilder builder, ResourceLocation modifierId, PlayerStat stat, float addend, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        try {
            Field attributeField = EntityAttributeModifier.ModifierType.class.getDeclaredField(stat.name());
            attributeField.setAccessible(true);
            SerializedName serializedName = attributeField.getAnnotation(SerializedName.class);
            String serializedNameValue = serializedName != null ? serializedName.value() : stat.name();
            builder.type(VaultMod.id("modifier_type/player_stat").toString(), typeBuilder -> {
                typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                    modifierEntryBuilder.property("stat", serializedNameValue);
                    modifierEntryBuilder.property("addend", addend);
                    createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
                });
            });
        } catch (NoSuchFieldException ignored) {
            WoldsVaults.LOGGER.error("Invalid PlayerStat passed in, skipping adding {} modifier.", modifierId);
        }

    }

    public static void vaultLevel(ModifierBuilder builder, ResourceLocation modifierId, int levelAdded, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/vault_level").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("levelAdded", levelAdded);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void lootWeight(ModifierBuilder builder, ResourceLocation modifierId, PlaceholderBlock.Type type, float increase, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/vault_lootable_weight").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("type", type.name());
                modifierEntryBuilder.property("chance", increase);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void vaultTime(ModifierBuilder builder, ResourceLocation modifierId, int timeAddedTicks, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/vault_time").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("timeAddedTicks", timeAddedTicks);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void champLoot(ModifierBuilder builder, ResourceLocation modifierId, float increase, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/champion_loot_chance").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("addend", increase);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void experience(ModifierBuilder builder, ResourceLocation modifierId, float increase, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/experience").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("addend", increase);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void experienceMultiplier(ModifierBuilder builder, ResourceLocation modifierId, float increase, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/experience_multiplier").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("addend", increase);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void inline(ModifierBuilder builder, ResourceLocation modifierId, ResourceLocation pool, int level, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/inline_pool").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("pool", pool.toString());
                modifierEntryBuilder.property("level", level);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void decoratorCascade(ModifierBuilder builder, ResourceLocation modifierId, TilePredicate tile, float increase, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/decorator_cascade").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("filter", tile.toString());
                modifierEntryBuilder.property("chance", increase);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void grouped(ModifierBuilder builder, ResourceLocation modifierId, Consumer<Map<ResourceLocation, Integer>> modifiersConsumer, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/grouped").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                Map<ResourceLocation, Integer> modifiers = new HashMap<>();
                modifiersConsumer.accept(modifiers);
                modifierEntryBuilder.property("children", modifiers);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void enchantedEventChance(ModifierBuilder builder, ResourceLocation modifierId, float chance, int ticksPerCheck, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/enchanted_event_chance").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("chance", chance);
                modifierEntryBuilder.property("ticksPerCheck", ticksPerCheck);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void chestBreakBomb(ModifierBuilder builder, ResourceLocation modifierId, float chance, Consumer<WeightedListBuilder<Integer>> weightedListConsumer, Consumer<WeightedListBuilder<CustomEntitySpawnerConfig.SpawnerEntity>> spawnerEntitiesConsumer, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/chest_break_bomb").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                WeightedListBuilder<Integer> amountsBuilder = new WeightedListBuilder<>();
                WeightedListBuilder<CustomEntitySpawnerConfig.SpawnerEntity> entitiesBuilder = new WeightedListBuilder<>();
                weightedListConsumer.accept(amountsBuilder);
                spawnerEntitiesConsumer.accept(entitiesBuilder);
                modifierEntryBuilder.property("chance", chance);
                modifierEntryBuilder.property("amounts", amountsBuilder.build());
                modifierEntryBuilder.property("entities", entitiesBuilder.build());
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void mobDeathBomb(ModifierBuilder builder, ResourceLocation modifierId, EntityPredicate filter, boolean filterShouldExclude, float chance, Consumer<WeightedListBuilder<Integer>> weightedListConsumer, Consumer<WeightedListBuilder<CustomEntitySpawnerConfig.SpawnerEntity>> spawnerEntitiesConsumer, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/mob_death_bomb").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                WeightedListBuilder<Integer> amountsBuilder = new WeightedListBuilder<>();
                WeightedListBuilder<CustomEntitySpawnerConfig.SpawnerEntity> entitiesBuilder = new WeightedListBuilder<>();
                weightedListConsumer.accept(amountsBuilder);
                spawnerEntitiesConsumer.accept(entitiesBuilder);
                modifierEntryBuilder.property("filter", filter.toString());
                modifierEntryBuilder.property("filterShouldExclude", filterShouldExclude);
                modifierEntryBuilder.property("chance", chance);
                modifierEntryBuilder.property("amounts", amountsBuilder.build());
                modifierEntryBuilder.property("entities", entitiesBuilder.build());
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void infernalChance(ModifierBuilder builder, ResourceLocation modifierId, EntityPredicate filter, float chance, int numberOfAffixes, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/infernal_mobs").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("filter", filter.toString());
                modifierEntryBuilder.property("chance", chance);
                modifierEntryBuilder.property("amount", numberOfAffixes);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void retroSpawn(ModifierBuilder builder, ResourceLocation modifierId, float chance, int ticksPerCheck, int entityCap, Consumer<WeightedListBuilder<Integer>> weightedListConsumer, Consumer<WeightedListBuilder<CustomEntitySpawnerConfig.SpawnerEntity>> spawnerEntitiesConsumer, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/retro_spawn_modifier").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                WeightedListBuilder<Integer> amountsBuilder = new WeightedListBuilder<>();
                WeightedListBuilder<CustomEntitySpawnerConfig.SpawnerEntity> entitiesBuilder = new WeightedListBuilder<>();
                weightedListConsumer.accept(amountsBuilder);
                spawnerEntitiesConsumer.accept(entitiesBuilder);
                modifierEntryBuilder.property("chance", chance);
                modifierEntryBuilder.property("ticksPerCheck", ticksPerCheck);
                modifierEntryBuilder.property("amounts", amountsBuilder.build());
                modifierEntryBuilder.property("entities", entitiesBuilder.build());
                modifierEntryBuilder.property("cap", entityCap);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void frenzy(ModifierBuilder builder, ResourceLocation modifierId, float damage, float movementSpeed, float maxHealth, boolean doHealthReduction, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/mob_frenzy").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                modifierEntryBuilder.property("damage", damage);
                modifierEntryBuilder.property("movementSpeed", movementSpeed);
                modifierEntryBuilder.property("maxHealth", maxHealth);
                modifierEntryBuilder.property("doHealthReduction", doHealthReduction);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void templateProcessor(ModifierBuilder builder, ResourceLocation modifierId, float probability, Consumer<List<TilePredicate>> blacklistConsumer, Consumer<List<TilePredicate>> whitelistConsumer, Consumer<List<TileProcessor>> fullBlockConsumer, Consumer<List<TileProcessor>> partialBlockConsumer, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        builder.type(VaultMod.id("modifier_type/template_processor").toString(), typeBuilder -> {
            typeBuilder.modifier(modifierId.toString(), modifierEntryBuilder -> {
                List<TilePredicate> blacklist = new ArrayList<>();
                List<TilePredicate> whitelist = new ArrayList<>();
                List<TileProcessor> fullBlock = new ArrayList<>();
                List<TileProcessor> partialBlock = new ArrayList<>();
                blacklistConsumer.accept(blacklist);
                whitelistConsumer.accept(whitelist);
                fullBlockConsumer.accept(fullBlock);
                partialBlockConsumer.accept(partialBlock);
                modifierEntryBuilder.property("probability", probability);
                modifierEntryBuilder.property("blacklist", blacklist);
                modifierEntryBuilder.property("whitelist", whitelist);
                modifierEntryBuilder.property("fullBlock", fullBlock);
                modifierEntryBuilder.property("partialBlock", partialBlock);
                createModifierDisplay(modifierEntryBuilder, name, color, description, formattedDescription, icon);
            });
        });
    }

    public static void artifactChance(ModifierBuilder builder, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        chance(builder, VaultMod.id("modifier_type/chance_artifact"), modifierId, chance, name, color, description, formattedDescription, icon);
    }

    public static void catalystChacne(ModifierBuilder builder, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        chance(builder, VaultMod.id("modifier_type/chance_catalyst"), modifierId, chance, name, color, description, formattedDescription, icon);
    }

    public static void trapChestChance(ModifierBuilder builder, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        chance(builder, VaultMod.id("modifier_type/chance_chest_trap"), modifierId, chance, name, color, description, formattedDescription, icon);
    }

    public static void soulShardChance(ModifierBuilder builder, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        chance(builder, VaultMod.id("modifier_type/chance_soul_shard"), modifierId, chance, name, color, description, formattedDescription, icon);
    }

    public static void championChance(ModifierBuilder builder, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        chance(builder, VaultMod.id("modifier_type/chance_champion"), modifierId, chance, name, color, description, formattedDescription, icon);
    }

    public static void itemQuantity(ModifierBuilder builder, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        percentage(builder, VaultMod.id("modifier_type/loot_item_quantity"), modifierId, chance, name, color, description, formattedDescription, icon);
    }

    public static void itemRarity(ModifierBuilder builder, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        percentage(builder, VaultMod.id("modifier_type/loot_item_rarity"), modifierId, chance, name, color, description, formattedDescription, icon);
    }

    public static void crateQuantity(ModifierBuilder builder, ResourceLocation modifierId, float chance, String name, String color, String description, String formattedDescription, ResourceLocation icon) {
        percentage(builder, VaultMod.id("modifier_type/crate_item_quantity"), modifierId, chance, name, color, description, formattedDescription, icon);
    }


    public static void createModifierDisplay(ModifierBuilder.ModifierEntryBuilder builder, String name, @Nullable String color, String description, @Nullable String formattedDescription, @Nullable ResourceLocation icon) {
        builder.display(name, color == null ? "#ffffff" : color, description, formattedDescription == null ? description : formattedDescription, icon == null ? VaultMod.id("impossible").toString() : icon.toString());
    }

    public static class HunterBuilder extends BasicListBuilder<HunterModifier.Properties.Entry> {
        public HunterBuilder entry(TilePredicate filter, int radius, String target, int color) {
            HunterModifier.Properties.Entry entry = new HunterModifier.Properties.Entry();
            entry.color = color;
            entry.filter = filter;
            entry.radius = radius;
            entry.target = target;
            add(entry);
            return this;
        }
    }

}
