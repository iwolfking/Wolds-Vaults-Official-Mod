package xyz.iwolfking.woldsvaults.datagen;

import com.cursedcauldron.wildbackport.common.registry.WBMobEffects;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.google.gson.annotations.SerializedName;
import iskallia.vault.VaultMod;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.block.base.GodAltarBlock;
import iskallia.vault.config.CustomEntitySpawnerConfig;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.modifier.modifier.*;
import iskallia.vault.core.vault.modifier.spi.EntityAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.data.entity.*;
import iskallia.vault.core.world.data.tile.*;
import iskallia.vault.core.world.processor.tile.TileProcessor;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModEffects;
import iskallia.vault.init.ModEntities;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.world.VaultDifficulty;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultModifierProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.BasicListBuilder;
import xyz.iwolfking.vhapi.api.datagen.lib.WeightedListBuilder;
import xyz.iwolfking.vhapi.api.datagen.lib.modifiers.ModifierBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

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
        map.put("wolds_builtin_modifiers", modifierBuilder -> {
            difficultyLock(modifierBuilder, VaultMod.id("piece_of_cake"), VaultDifficulty.PIECE_OF_CAKE, true, "Piece of Cake", "#EBFF8D", "This vault's difficulty is locked to Piece of Cake.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("easy"), VaultDifficulty.EASY, true, "Easy", "#EBFF8D", "This vault's difficulty is locked to Easy.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("normal"), VaultDifficulty.NORMAL, true, "Normal", "#EBFF8D", "This vault's difficulty is locked to Normal.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("hard"), VaultDifficulty.HARD, true, "Hard", "#EBFF8D", "This vault's difficulty is locked to Hard.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("impossible"), VaultDifficulty.IMPOSSIBLE, true, "Impossible", "#EBFF8D", "This vault's difficulty is locked to Impossible.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("fragged"), VaultDifficulty.FRAGGED, true, "Fragged", "#EBFF8D", "This vault's difficulty is locked to Fragged.", null, VaultMod.id("gui/modifiers/impossible"));
            difficultyLock(modifierBuilder, VaultMod.id("normalized"), VaultDifficulty.NORMAL, false, "Normalized", "#EBFF8D", "This vault's difficulty is locked to Normal.", null, VaultMod.id("gui/modifiers/impossible"));

            artifactChance(modifierBuilder, VaultMod.id("more_artifact_chance"), 0.01F, "Increased Artifact Chance", "#EBFF8D", "Every stack increases the artifact chance by 1 percent.", "+%d%% Artifact Chance", VaultMod.id("gui/modifiers/more_artifact1"));
            artifactChance(modifierBuilder, VaultMod.id("pogging"), 0.05F, "Pogging", "#EBFF8D", "Every stack increases the artifact chance by 5 percent.", "+%d%% Artifact Chance", VaultMod.id("gui/modifiers/more_artifact1"));

            catalystChance(modifierBuilder, VaultMod.id("more_catalysts"), 0.01F, "More Catalyst Fragments", "#FC00E3", "+1% Catalyst Fragments", "+%d%% Catalyst Fragments", VaultMod.id("gui/modifiers/more_catalyst"));
            catalystChance(modifierBuilder, VaultMod.id("sparkling"), 0.05F, "Sparkling", "#FC00E3", "+5% Catalyst Fragments", "+%d%% Catalyst Fragments", VaultMod.id("gui/modifiers/more_catalyst"));
            catalystChance(modifierBuilder, VaultMod.id("iridescent"), 1.0F, "Iridescent", "#FC00E3", "+100% Catalyst Fragments", "+%d%% Catalyst Fragments", VaultMod.id("gui/modifiers/more_catalyst"));

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

            experienceMultiplier(modifierBuilder, VaultMod.id("true_noxp"),0.0F,  "No XP", "#DB7C00", "x0 Experience Multiplier", null, VaultMod.id("gui/modifiers/impossible"));

            inline(modifierBuilder, VaultMod.id("unhinged"), VaultMod.id("unhinged"),  0, "Unhinged", "#DB7C00", "An Unhinged experience.", null, VaultMod.id("gui/modifiers/impossible"));
            inline(modifierBuilder, VaultMod.id("random_harsh_negative"), VaultMod.id("random_harsh_negative"),  0, "Random Harsh Negative", "#fe3333", "Unknown Negative Modifier.", null, VaultMod.id("gui/modifiers/impossible"));

            decoratorCascade(modifierBuilder, VaultMod.id("super_gilded_cascade"), PartialBlock.of(ModBlocks.GILDED_CHEST),0.75F, "Super Gilded", "#FFEC00", "+75% Gilded Chests", "+%d%% Gilded Chests", VaultMod.id("gui/modifiers/gilded"));
            decoratorCascade(modifierBuilder, VaultMod.id("super_living_cascade"), PartialBlock.of(ModBlocks.LIVING_CHEST),0.75F, "Super Living", "#38d800", "+75% Living Chests", "+%d%% Living Chests", VaultMod.id("gui/modifiers/living"));
            decoratorCascade(modifierBuilder, VaultMod.id("super_ornate_cascade"), PartialBlock.of(ModBlocks.ORNATE_CHEST),0.75F, "Super Ornate", "#c90d73", "+75% Ornate Chests", "+%d%% Ornate Chests", VaultMod.id("gui/modifiers/ornate"));
            decoratorCascade(modifierBuilder, VaultMod.id("super_wooden_cascade"), PartialBlock.of(ModBlocks.WOODEN_CHEST),0.75F, "Super Wooden", "#c7b281", "+75% Wooden Chests", "+%d%% Wooden Chests", VaultMod.id("gui/modifiers/wooden"));
            decoratorCascade(modifierBuilder, VaultMod.id("super_coin_cascade"), PartialBlock.of(ModBlocks.COIN_PILE),0.75F, "Super Wealthy", "#ffae00", "+75% Coin Piles", "+%d%% Coin Piles", VaultMod.id("gui/modifiers/wealthy"));

            decoratorCascade(modifierBuilder, WoldsVaults.id("flesh"), PartialBlock.of(ModBlocks.FLESH_CHEST),0.25F, "Flesh", "#bd6d68", "+25% Flesh Chests", "+%d%% Flesh Chests", WoldsVaults.id("gui/modifiers/flesh"));
            decoratorCascade(modifierBuilder, WoldsVaults.id("hardened"), PartialBlock.of(ModBlocks.HARDENED_CHEST),0.25F, "Hardened", "#635858", "+25% Hardened Chests", "+%d%% Hardened Chests", WoldsVaults.id("gui/modifiers/hardened"));

            championChance(modifierBuilder, VaultMod.id("champions_realm"), 0.25F, "Champion's Realm", "#bbf333", "+25% Champions", "+%d%% Champions", VaultMod.id("gui/modifiers/champions_abode"));

            champLoot(modifierBuilder, VaultMod.id("more_champ_drops"), 0.25F, "More Champion Drops", "#bbf333", "+25% Champion Loot", "+%d%% Champion Loot", VaultMod.id("gui/modifiers/champion_domain"));
            champLoot(modifierBuilder, VaultMod.id("no_champ_drops"), -10.0F, "No Champion Drops", "#fe3333", "-1000% Champion Loot", "-%d%% Champion Loot", VaultMod.id("gui/modifiers/champion_domain"));

            noTemporal(modifierBuilder, VaultMod.id("no_temporal"),  "No Temporal", "#fe3333", "Temporal Relics, Companions, and Vault God Charms can't be used", null, VaultMod.id("gui/modifiers/champion_domain"));

            entityEffect(modifierBuilder, WoldsVaults.id("regenerating_mobs"), EMPTY_ENTITY_PREDICATE, MobEffects.REGENERATION.getRegistryName(), 5, 0.1F, "Regenerating Mobs", "#a996ca", "Mobs in this vault gain Regeneration V 10% of the time", "Mobs in this vault gain Regeneration some of the time", VaultMod.id("gui/modifiers/chunky"));
            entityEffect(modifierBuilder, WoldsVaults.id("resistant_mobs"),  EMPTY_ENTITY_PREDICATE, MobEffects.DAMAGE_RESISTANCE.getRegistryName(), 2, 0.1F, "Resistant Mobs", "#a996ca", "Mobs in this vault gain Resistance II 10% of the time", "Mobs in this vault gain Resistance some of the time", VaultMod.id("gui/modifiers/resistant_mobs"));
            entityEffect(modifierBuilder, WoldsVaults.id("phantasmal_mobs"),  EMPTY_ENTITY_PREDICATE, MobEffects.INVISIBILITY.getRegistryName(), 0, 0.1F, "Resistant Mobs", "#a996ca", "Mobs in this vault gain Invisibility 10% of the time", "Mobs in this vault gain Invisibility some of the time", VaultMod.id("gui/modifiers/phantasmal_mobs"));
            entityEffect(modifierBuilder, WoldsVaults.id("fleet_footed_mobs"), EMPTY_ENTITY_PREDICATE, MobEffects.MOVEMENT_SPEED.getRegistryName(), 3, 0.1F, "Fleet Footed Mobs", "#a996ca", "Mobs in this vault gain Speed III 10% of the time", "Mobs in this vault gain Speed some of the time", VaultMod.id("gui/modifiers/fleetfooted_mobs"));
            entityEffect(modifierBuilder, VaultMod.id("big_mobs"),  EMPTY_ENTITY_PREDICATE, xyz.iwolfking.woldsvaults.init.ModEffects.GROWING.getRegistryName(), 1, 0.1F, "Maximized", "#a996ca", "Mobs in this vault gain Growing II 10% of the time", "Mobs in this vault gain Growing some of the time", VaultMod.id("gui/modifiers/chunky"));
            entityEffect(modifierBuilder, VaultMod.id("chungus"),  EMPTY_ENTITY_PREDICATE, xyz.iwolfking.woldsvaults.init.ModEffects.GROWING.getRegistryName(), 1, 1.0F, "Big Chungus", "#a996ca", "Mobs in this vault gain Growing II 100% of the time", "Mobs in this vault gain Growing some of the time", VaultMod.id("gui/modifiers/chunky"));
            entityEffect(modifierBuilder, VaultMod.id("mini_mobs"),  EMPTY_ENTITY_PREDICATE, xyz.iwolfking.woldsvaults.init.ModEffects.SHRINKING.getRegistryName(), 1, 0.1F, "Minimized", "#a996ca", "Mobs in this vault gain Shrinking II 10% of the time", "Mobs in this vault gain Shrinking some of the time", VaultMod.id("gui/modifiers/chunky"));
            entityEffect(modifierBuilder, VaultMod.id("daycare"),  EMPTY_ENTITY_PREDICATE, xyz.iwolfking.woldsvaults.init.ModEffects.SHRINKING.getRegistryName(), 1, 1.0F, "Daycare", "#a996ca", "Mobs in this vault gain Shrinking II 100% of the time", "Mobs in this vault gain Shrinking all of the time", VaultMod.id("gui/modifiers/chunky"));

            hunter(modifierBuilder, VaultMod.id("living_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialBlockState.of(ModBlocks.LIVING_CHEST), 64, "living", 65280);
            } , "Living Hunter", "#00FF00", "Highlights Living Chests in this Vault", null, VaultMod.id("gui/modifiers/living"));
            hunter(modifierBuilder, VaultMod.id("coin_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialBlockState.of(ModBlocks.COIN_PILE), 64, "coins", 16750592);
            } , "Coin Pile Hunter", "#FF9800", "Highlights Coin Piles in this Vault", null, VaultMod.id("gui/modifiers/wealthy"));
            hunter(modifierBuilder, VaultMod.id("ornate_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialBlockState.of(ModBlocks.ORNATE_CHEST), 64, "coins", 16721408);
            } , "Ornate Hunter", "#FF2600", "Highlights Ornate Chests in this Vault", null, VaultMod.id("gui/modifiers/ornate"));
            hunter(modifierBuilder, VaultMod.id("gilded_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialBlockState.of(ModBlocks.GILDED_CHEST), 64, "gilded", 16776960);
            } , "Gilded Hunter", "#FFFF00", "Highlights Gilded Chests in this Vault", null, VaultMod.id("gui/modifiers/gilded"));
            hunter(modifierBuilder, VaultMod.id("wooden_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialBlockState.of(ModBlocks.WOODEN_CHEST), 64, "wooden", 16738560);
            } , "Wooden Hunter", "#FF6900", "Highlights Wooden Chests in this Vault", null, VaultMod.id("gui/modifiers/wooden"));
            hunter(modifierBuilder, VaultMod.id("altar_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialBlockState.of(ModBlocks.GOD_ALTAR), 64, "god_altars", 5817009);
            } , "God Altar Hunter", "#58C2B1", "Highlights God Altars in this Vault", null, VaultMod.id("gui/modifiers/god_token_tenos"));
            hunter(modifierBuilder, VaultMod.id("velara_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialTile.of(PartialBlockState.of(ModBlocks.GOD_ALTAR).set(GodAltarBlock.GOD, VaultGod.VELARA), PartialCompoundNbt.empty()), 64, "god_altars", 65280);
            } , "God Altar Hunter (Velara)", "#00FF00", "Highlights Velara God Altars in this Vault", null, VaultMod.id("gui/modifiers/god_token_velara"));
            hunter(modifierBuilder, VaultMod.id("idona_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialTile.of(PartialBlockState.of(ModBlocks.GOD_ALTAR).set(GodAltarBlock.GOD, VaultGod.IDONA), PartialCompoundNbt.empty()), 64, "god_altars", 16711680);
            } , "God Altar Hunter (Idona)", "#FF0000", "Highlights Idona God Altars in this Vault", null, VaultMod.id("gui/modifiers/god_token_idona"));
            hunter(modifierBuilder, VaultMod.id("tenos_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialTile.of(PartialBlockState.of(ModBlocks.GOD_ALTAR).set(GodAltarBlock.GOD, VaultGod.TENOS), PartialCompoundNbt.empty()), 64, "god_altars", 197);
            } , "God Altar Hunter (Tenos)", "#0000C5", "Highlights Tenos God Altars in this Vault", null, VaultMod.id("gui/modifiers/god_token_tenos"));
            hunter(modifierBuilder, VaultMod.id("wendarr_hunter"), hunterBuilder -> {
                hunterBuilder.entry(PartialTile.of(PartialBlockState.of(ModBlocks.GOD_ALTAR).set(GodAltarBlock.GOD, VaultGod.WENDARR), PartialCompoundNbt.empty()), 64, "god_altars", 16748800);
            } , "God Altar Hunter (Wendarr)", "#FF9100", "Highlights Wendarr God Altars in this Vault", null, VaultMod.id("gui/modifiers/god_token_wendarr"));

            enchantedEventChance(modifierBuilder, VaultMod.id("mildly_enchanted"),0.25F,  900, "Mildly Enchanted", "#CE93D8", "25% chance for an Event to occur every 45 seconds", "Chance to trigger an Enchanted Elixir event every 45 seconds", VaultMod.id("gui/modifiers/mildly_enchanted"));
            enchantedEventChance(modifierBuilder, VaultMod.id("enchanted"),0.05F,  200, "Enchanted", "#CE93D8", "5% chance for an Event to occur every 10 seconds", "Chance to trigger an Enchanted Elixir event every 10 seconds", VaultMod.id("gui/modifiers/enchanted"));
            enchantedEventChance(modifierBuilder, VaultMod.id("bingo_enchanted"),1.0F,  1200, "Enchanted", "#CE93D8", "100% chance for an Event to occur every minute", "Chance to trigger an Enchanted Elixir event every minute", VaultMod.id("gui/modifiers/enchanted"));

            chestBreakBomb(modifierBuilder, VaultMod.id("armed_chest"),0.04F, weightedBuilder -> {
                weightedBuilder.add(1, 1);
            }, spawnerEntityWeightedListBuilder -> {
                    spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "slime_grenade"), null, true), 1);
                    spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "explosive_grenade"), null, true), 1);
                    spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "fire_grenade"), null, true), 1);
                    spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "lightning_grenade"), null, true), 1);

                }, "Armed", "#33691E", "4% chance for grenades to drop from chests upon looting them", "Chance for grenades to drop from chests upon looting them", VaultMod.id("gui/modifiers/armed"));


            chestBreakBomb(modifierBuilder, VaultMod.id("bingo_slime_grenade_chest"),0.05F, weightedBuilder -> {
                weightedBuilder.add(1, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "slime_grenade"), null, true), 1);

            }, "Boing Traps", "#33691E", "5% chance for Slime grenades to drop from chests upon looting them", "Chance for Slime grenades to drop from chests upon looting them", VaultMod.id("gui/modifiers/slime_grenade"));

            chestBreakBomb(modifierBuilder, VaultMod.id("bingo_lightning_grenade_chest"),0.05F, weightedBuilder -> {
                weightedBuilder.add(1, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "lightning_grenade"), null, true), 1);

            }, "Electric Traps", "#33691E", "5% chance for Lightning grenades to drop from chests upon looting them", "Chance for Lightning grenades to drop from chests upon looting them", VaultMod.id("gui/modifiers/lightning_grenade"));

            chestBreakBomb(modifierBuilder, VaultMod.id("bingo_fire_grenade_chest"),0.05F, weightedBuilder -> {
                weightedBuilder.add(1, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "fire_grenade"), null, true), 1);

            }, "Fire Traps", "#33691E", "5% chance for Fire grenades to drop from chests upon looting them", "Chance for Fire grenades to drop from chests upon looting them", VaultMod.id("gui/modifiers/fire_grenade"));

            chestBreakBomb(modifierBuilder, VaultMod.id("bingo_ice_grenade_chest"),0.05F, weightedBuilder -> {
                weightedBuilder.add(1, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "ice_grenade"), null, true), 1);

            }, "Ice Traps", "#33691E", "5% chance for Ice grenades to drop from chests upon looting them", "Chance for Ice grenades to drop from chests upon looting them", VaultMod.id("gui/modifiers/fire_grenade"));

            chestBreakBomb(modifierBuilder, VaultMod.id("surprise_boxes"),0.075F, weightedBuilder -> {
                weightedBuilder.add(1, 7);
                weightedBuilder.add(2, 2);
                weightedBuilder.add(3, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("aggressive_cow"), null, false), 4);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(AMEntityRegistry.MIMICUBE.get().getRegistryName(), null, false), 1);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("t3_enderman"), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("vault_fighter_4"), null, false), 7);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("t3_wither_skeleton"), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("bruiser_guardian"), null, false), 6);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("arbalist_guardian"), null, false), 6);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("mushroom_t5"), null, false), 4);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("mummy_t2"), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("t3_husk"), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(VaultMod.id("dungeon_black_widow_spider"), null, false), 1);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.withDefaultNamespace("shulker"), null, false), 4);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.withDefaultNamespace("witch"), null, false), 6);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.withDefaultNamespace("vindicator"), null, false), 9);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("alexsmobs", "tiger"), null, false), 1);

            }, "Surprise Boxes", "#33691E", "7.5% chance for enemies to spawn from chests upon looting them", "Chance for enemies to spawn from chests upon looting them", VaultMod.id("gui/modifiers/surprise_boxes"));


            retroSpawn(modifierBuilder, WoldsVaults.id("witch_party"),0.2F, 400, 8, weightedBuilder -> {
                weightedBuilder.add(2, 3);
                weightedBuilder.add(3, 6);
                weightedBuilder.add(4, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.withDefaultNamespace("witch"), null, false), 6);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.DUNGEON_WITCH.getRegistryName(), null, false), 6);
            }, "Witch Party", "#33691E", "20% chance to spawn Witches every 20 seconds", "Chance to spawn Witches every 20 seconds", VaultMod.id("gui/modifiers/witch_party"));

            retroSpawn(modifierBuilder, WoldsVaults.id("ghost_party"),0.2F, 400, 10, weightedBuilder -> {
                weightedBuilder.add(2, 3);
                weightedBuilder.add(3, 6);
                weightedBuilder.add(4, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.PURPLE_GHOST.getRegistryName(), null, false), 1);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.BLACK_GHOST.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.GREEN_GHOST.getRegistryName(), null, false), 8);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.RED_GHOST.getRegistryName(), null, false), 7);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.DARK_RED_GHOST.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.DARK_GRAY_GHOST.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.BLUE_GHOST.getRegistryName(), null, false), 7);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.DARK_BLUE_GHOST.getRegistryName(), null, false), 3);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.BROWN_GHOST.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(xyz.iwolfking.woldsvaults.init.ModEntities.YELLOW_GHOST.getRegistryName(), null, false), 9);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_WRAITH_YELLOW.getRegistryName(), null, false), 3);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_WRAITH_WHITE.getRegistryName(), null, false), 12);
            }, "Ghost Party", "#33691E", "20% chance to spawn Ghosts every 20 seconds", "Chance to spawn Ghosts every 20 seconds", VaultMod.id("gui/modifiers/ghost_party"));

            retroSpawn(modifierBuilder, VaultMod.id("sweet_retro"),0.1F, 200, 24, weightedBuilder -> {
                weightedBuilder.add(2, 3);
                weightedBuilder.add(3, 6);
                weightedBuilder.add(4, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_BLUE_GUMMY_SOLDIER.getRegistryName(), null, false), 4);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_GREEN_GUMMY_SOLDIER.getRegistryName(), null, false), 16);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_RED_GUMMY_SOLDIER.getRegistryName(), null, false), 4);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_YELLOW_GUMMY_SOLDIER.getRegistryName(), null, false), 8);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("auxiliaryblocks", "gingerbread_man"), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("auxiliaryblocks", "gingerbread_man_small"), null, false), 4);
            }, "Candy Party", "#33691E", "10% chance to spawn Candy mobs every 10 seconds", "Chance to spawn Candy mobs every 10 seconds", VaultMod.id("gui/modifiers/candy"));

            retroSpawn(modifierBuilder, VaultMod.id("classic_retro"),0.16F, 200, 6, weightedBuilder -> {
                weightedBuilder.add(2, 3);
                weightedBuilder.add(3, 6);
                weightedBuilder.add(4, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_PIGLIN.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_PIGLIN.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_SPIDER.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_DROWNED.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_DROWNED.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_HUSK.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_HUSK.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_SKELETON.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_SKELETON.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_STRAY.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_CREEPER.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_CREEPER.getRegistryName(), null, false), 2);
            }, "Mob Party", "#33691E", "16% chance to spawn mobs every 10 seconds", "Chance to spawn mobs every 10 seconds", VaultMod.id("gui/modifiers/retro"));

            retroSpawn(modifierBuilder, VaultMod.id("classic_retro_mini"),0.08F, 200, 6, weightedBuilder -> {
                weightedBuilder.add(2, 3);
                weightedBuilder.add(3, 6);
                weightedBuilder.add(4, 1);
            }, spawnerEntityWeightedListBuilder -> {
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_PIGLIN.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_PIGLIN.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_SPIDER.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_DROWNED.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_DROWNED.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_HUSK.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_HUSK.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_SKELETON.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_SKELETON.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_STRAY.getRegistryName(), null, false), 2);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T2_CREEPER.getRegistryName(), null, false), 10);
                spawnerEntityWeightedListBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.T3_CREEPER.getRegistryName(), null, false), 2);
            }, "Mini Mob Party", "#33691E", "8% chance to spawn mobs every 10 seconds", "Chance to spawn mobs every 10 seconds", VaultMod.id("gui/modifiers/retro"));

            try {
                mobDeathBomb(modifierBuilder, VaultMod.id("haunted_mansion"), PartialEntity.parse(ModEntities.VAULT_WRAITH_WHITE.getRegistryName().toString(), false).orElse(null), true, 0.15F, integerWeightedListBuilder -> {
                    integerWeightedListBuilder.add(1, 1);
                }, weightedBuilder -> {
                    weightedBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.VAULT_WRAITH_WHITE.getRegistryName(), null, false), 1);
                }, "Haunted Grounds", "#33691E", "10% chance to spawn a white Ghost on mob death", "Chance to spawn a white Ghost on mob death", VaultMod.id("gui/modifiers/haunted"));

                mobDeathBomb(modifierBuilder, VaultMod.id("fungal_infestation"), PartialEntity.parse(ModEntities.SMOLCAP.getRegistryName().toString(), false).orElse(null), true, 0.3F, integerWeightedListBuilder -> {
                    integerWeightedListBuilder.add(2, 6);
                    integerWeightedListBuilder.add(4, 3);
                    integerWeightedListBuilder.add(6, 1);
                }, weightedBuilder -> {
                    weightedBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ModEntities.SMOLCAP.getRegistryName(), null, false), 1);
                }, "Fungal Infestation", "#33691E", "30% chance to spawn Smolcaps on mob death", "Chance to spawn Smolcaps on mob death", VaultMod.id("gui/modifiers/fungal_infestation"));

                mobDeathBomb(modifierBuilder, VaultMod.id("bingo_slime_grenade_mob"), EMPTY_ENTITY_PREDICATE, false, 0.04F,  integerWeightedListBuilder -> {
                    integerWeightedListBuilder.add(1, 1);
                }, weightedBuilder -> {
                    weightedBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "slime_grenade"), null, true), 1);
                }, "Armed Mobs (Slime Grenade)", "#33691E", "4% chance to spawn Slime Grenade on mob death", "Chance to spawn Smolcaps on mob death", VaultMod.id("gui/modifiers/slime_grenade"));

                mobDeathBomb(modifierBuilder, VaultMod.id("bingo_ice_grenade_mob"), EMPTY_ENTITY_PREDICATE, true, 0.04F, integerWeightedListBuilder -> {
                    integerWeightedListBuilder.add(1, 1);
                }, weightedBuilder -> {
                    weightedBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "ice_grenade"), null, true), 1);
                }, "Armed Mobs (Ice Grenade)", "#33691E", "4% chance to spawn Ice Grenade on mob death", "Chance to spawn Ice Grenade on mob death", VaultMod.id("gui/modifiers/ice_grenade"));

                mobDeathBomb(modifierBuilder, VaultMod.id("bingo_fire_grenade_mob"), EMPTY_ENTITY_PREDICATE, true, 0.04F, integerWeightedListBuilder -> {
                    integerWeightedListBuilder.add(1, 1);
                }, weightedBuilder -> {
                    weightedBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "fire_grenade"), null, true), 1);
                }, "Armed Mobs (Fire Grenade)", "#33691E", "4% chance to spawn Fire Grenade on mob death", "Chance to spawn Fire Grenade on mob death", VaultMod.id("gui/modifiers/fire_grenade"));

                mobDeathBomb(modifierBuilder, VaultMod.id("bingo_lightning_grenade_mob"), EMPTY_ENTITY_PREDICATE, true, 0.04F, integerWeightedListBuilder -> {
                    integerWeightedListBuilder.add(1, 1);
                }, weightedBuilder -> {
                    weightedBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("thermal", "lightning_grenade"), null, true), 1);
                }, "Armed Mobs (Lightning Grenade)", "#33691E", "4% chance to spawn Lightning Grenade on mob death", "Chance to spawn Lightning Grenade on mob death", VaultMod.id("gui/modifiers/lightning_grenade"));

                mobDeathBomb(modifierBuilder, VaultMod.id("bingo_spore_grenade_mob"), EMPTY_ENTITY_PREDICATE, true, 0.04F, integerWeightedListBuilder -> {
                    integerWeightedListBuilder.add(1, 1);
                }, weightedBuilder -> {
                    weightedBuilder.add(new CustomEntitySpawnerConfig.SpawnerEntity(ResourceLocation.fromNamespaceAndPath("archers_paradox", "spore_arrow"), null, true), 1);
                }, "Armed Mobs (Spore Grenade)", "#33691E", "4% chance to spawn Spore Grenade on mob death", "Chance to spawn Spore Grenade on mob death", VaultMod.id("gui/modifiers/lightning_grenade"));

            } catch (Exception ignored) {
                WoldsVaults.LOGGER.error("Error trying to create a Mob Death Bomb modifier, ignoring all modifiers of this type.");
            }

            infernalChance(modifierBuilder, VaultMod.id("raging"), EMPTY_ENTITY_PREDICATE, 0.03F,  3, "Raging", "#a996ca", "3% chance for mobs to spawn with Infernal modifiers", "Chance for mobs to spawn with Infernal modifiers", VaultMod.id("gui/modifiers/infernal"));
            infernalChance(modifierBuilder, VaultMod.id("bingo_infernal"), EMPTY_ENTITY_PREDICATE, 0.015F,  3, "Infernal", "#a996ca", "1.5% chance for mobs to spawn with Infernal modifiers", "Chance for mobs to spawn with Infernal modifiers", VaultMod.id("gui/modifiers/infernal"));

            grouped(modifierBuilder, VaultMod.id("greedy"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("greedy_single"), 1);
            },"Greedy", "#3ffbf4", "Just asking for trouble...", null, VaultMod.id("gui/modifiers/impossible"));

            grouped(modifierBuilder, VaultMod.id("omega_bonus"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("gilded"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("wooden"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("ornate"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("living"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("coin_pile"), 1);
            },"Omega Bonus", "#3ffbf4", "+1 of All Chests and Coin Piles", null, VaultMod.id("gui/modifiers/item_quant_4"));

            grouped(modifierBuilder, VaultMod.id("omega_cascade"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("gilded_cascade"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("wooden_cascade"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("ornate_cascade"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("living_cascade"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("wealthy"), 1);
            },"Omega Cascade", "#3ffbf4", "+20 percent more of All Chests and Coin Piles", null, VaultMod.id("gui/modifiers/item_quant_4"));

            grouped(modifierBuilder, VaultMod.id("safari_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("safari"), 1);
            },"Safari Bingo!", "#4CAF50", "Every BINGO adds more Item Rarity and Item Quantity and unleashes zoo animals in newly visited rooms", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("challenge_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_challenge"), 5);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("champion_chance"), 2);
            },"Challenge Bingo!", "#B71C1C", "Every BINGO adds more Item Rarity and Item Quantity and Champion Chance + 5 Bingo Challenge Stacks", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("challenge_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_slime_grenade_mob"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_slime_grenade_chest"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("springy"), 1);
            },"Boinging Bingo!", "#A5D6A7", "Every BINGO adds more Item Rarity and Item Quantity + Slime Grenades fall out of chests and mobs!", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("challenge_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("classic_retro"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("soul_boost"), 2);
            },"Party Bingo!", "#E3F2FD", "Every BINGO adds more Item Rarity and Item Quantity and Soul Quantity + Mobs randomly spawn everywhere", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("surprise_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("surprise_boxes"), 1);
            },"Surprise Bingo!", "#E3F2FD", "Every BINGO adds more Item Rarity and Item Quantity and mob can spawn when you open chests", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("enchanted_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_enchanted"), 1);
            },"Surprise Bingo!", "#CE93D8", "Every BINGO adds more Item Rarity and Item Quantity and Enchanted Events can occur randomly", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("cursed_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("curse"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("soul_boost"), 1);
            },"Cursed Bingo!", "#546E7A", "Every BINGO adds more Item Rarity and Item Quantity and Soul Quantity, and your vault is cursed", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("spooky_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("haunted_mansion"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("soul_boost"), 3);
            },"Spooky Bingo!", "#616161", "Every BINGO adds more Item Rarity and Item Quantity and Soul Quantity, and ghosts can spawn on mob death", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("infernal_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_infernal"), 1);
            },"Infernal Bingo!", "#9FA8DA", "Every BINGO adds more Item Rarity and Item Quantity and Soul Quantity, and mobs can spawn with Infernal modifiers", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("sweet_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("cake_layer"), 20);
                resourceLocationIntegerMap.put(VaultMod.id("sweet_retro"), 1);
            },"Sweet Bingo!", "#F8BBD0", "Every BINGO adds 20 Cake Layers, and candy mobs can spawn randomly", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("fungal_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("fungal"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("fungal_infestation"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_spore_grenade_mob"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("poisonous"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("chunky_mobs2"), 1);
            },"Fungal Bingo!", "#F8BBD0", "Every BINGO adds Item Rarity and Quantity, your vault is covered in spores", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("freezing_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("winter"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_ice_grenade_chest"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_ice_grenade_mob"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("freezing"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("slowed"), 1);
            },"Freezing Bingo!", "#80DEEA", "Every BINGO adds Item Rarity and Quantity, your vault is frozen", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("burning_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("burning"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_fire_grenade_chest"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_fire_grenade_mob"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("furious_mobs"), 2);
            },"Burning Bingo!", "#EF6C00", "Every BINGO adds Item Rarity and Quantity, your vault is on fire", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("mini_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("daycare"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("baby_mobs2"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
            },"Mini Bingo!", "#EDD9F0", "Every BINGO adds Item Rarity and Quantity, and mobs can spawn small sometimes", null, VaultMod.id("gui/modifiers/bingo"));


            grouped(modifierBuilder, VaultMod.id("big_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("chungus"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("chunky_mobs2"), 4);
            },"Big Bingo!", "#FFC107", "Every BINGO adds Item Rarity and Quantity, and mobs can spawn bigger sometimes", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("bonus_bingo"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("bingo_quantity"), 3);
                resourceLocationIntegerMap.put(VaultMod.id("bingo_rarity"), 3);
                resourceLocationIntegerMap.put(VaultMod.id("soul_boost"), 3);
            },"Bonus Bingo!", "#E91E63", "Every BINGO adds Item Rarity and Quantity and Soul Quantity", null, VaultMod.id("gui/modifiers/bingo"));

            grouped(modifierBuilder, VaultMod.id("velara_challenge"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("furious_mobs"), 3);
                resourceLocationIntegerMap.put(VaultMod.id("chunky_mobs"), 3);
                resourceLocationIntegerMap.put(WoldsVaults.id("regenerating_mobs"), 1);
            },"Velara's Challenge", "#ba0000", "Increases mob damage, mob health and mob spawns by 20 percent per stack, mobs can spawn with Regeneration", null, VaultMod.id("gui/modifiers/challenging"));

            grouped(modifierBuilder, VaultMod.id("tenos_challenge"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("furious_mobs"), 3);
                resourceLocationIntegerMap.put(VaultMod.id("chunky_mobs"), 3);
                resourceLocationIntegerMap.put(VaultMod.id("rending"), 1);
            },"Tenos' Challenge", "#ba0000", "Increases mob damage and mob health 20 percent per stack, cooldowns increased by 25 percent", null, VaultMod.id("gui/modifiers/challenging"));

            grouped(modifierBuilder, VaultMod.id("wendarr_challenge"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("furious_mobs"), 3);
                resourceLocationIntegerMap.put(VaultMod.id("chunky_mobs"), 3);
                resourceLocationIntegerMap.put(VaultMod.id("voiding"), 1);
            },"Wendarr's Challenge", "#ba0000", "Increases mob damage, mob health by 10% per stack, mobs can steal time on hit", null, VaultMod.id("gui/modifiers/challenging"));

            grouped(modifierBuilder, VaultMod.id("idona_challenge"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("furious_mobs"), 6);
                resourceLocationIntegerMap.put(VaultMod.id("chunky_mobs"), 6);
                resourceLocationIntegerMap.put(VaultMod.id("cake_layer_mobspeed"), 4);
            },"Idona's Challenge", "#ba0000", "Increases mob damage, mob health by 40 percent per stack, increases mob speed by 6.0 percent per stac", null, VaultMod.id("gui/modifiers/challenging"));

            grouped(modifierBuilder, VaultMod.id("all_seeing_eye"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("dungeon_doors"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("treasure_doors"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("vendoor_doors"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("coin_hunter"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("wooden_hunter"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("gilded_hunter"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("ornate_hunter"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("living_hunter"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("altar_hunter"), 1);
            },"All Seeing Eye", "#ad2b2b", "Adds all Hunter modifiers", null, VaultMod.id("gui/modifiers/beginners_grace"));

            grouped(modifierBuilder, VaultMod.id("unfair_mobs"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("infuriated_mobs"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("slowed"), 1);
            },"Unfair", "#da6d5a", "Mobs are faster, you are slower", null, VaultMod.id("gui/modifiers/armored_mobs"));

            grouped(modifierBuilder, VaultMod.id("chemical_bath"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("freezing"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("poisoning"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("wither"), 1);
                resourceLocationIntegerMap.put(WoldsVaults.id("bleeding_mobs"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("dark"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("stunning"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("fatiguing"), 1);
            },"Chemical Bath", "#da6d5a", "Mobs can hit you with a wide number of negative effects on hit", null, VaultMod.id("gui/modifiers/impossible"));

            grouped(modifierBuilder, VaultMod.id("bubbling_trouble"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("ruthless_mobs"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("chunky_mobs2"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("freezing"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("poisoning"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("wither"), 1);
                resourceLocationIntegerMap.put(WoldsVaults.id("bleeding_mobs"), 1);
            },"Bubble, Toil, and Trouble", "#da6d5a", "Mobs can hit you with a wide number of negative effects on hit, mobs are a bit stronger", null, VaultMod.id("gui/modifiers/impossible"));

            grouped(modifierBuilder, VaultMod.id("ice_cold_essence"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("freezing"), 4);
                resourceLocationIntegerMap.put(VaultMod.id("ruthless_mobs"), 1);
            },"Ice Cold Essence", "#da6d5a", "Mobs have a high chance to slow you on hit, ", null, VaultMod.id("gui/modifiers/hex_chilling"));

            grouped(modifierBuilder, VaultMod.id("spicy_chili_speed"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("enraged_mobs"), 4);
                resourceLocationIntegerMap.put(VaultMod.id("critical_mobs"), 1);
            },"Spicy Chili Speed", "#da6d5a", "Mobs gain more speed and more crit chance", null, VaultMod.id("gui/modifiers/armored_mobs"));


            grouped(modifierBuilder, VaultMod.id("hearty_mobs"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("brutal_chunky_mobs"), 2);
            },"Hearty Mobs", "#da6d5a", "Mobs are significantly beefier", null, VaultMod.id("gui/modifiers/armored_mobs"));

            grouped(modifierBuilder, VaultMod.id("weak_heart"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("antiheal"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("weakened_cdr"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("mana_regen_down"), 1);
            },"Weak Heart", "#da6d5a", "Your healing and mana regeration is reduced, your cooldowns are increased", null, VaultMod.id("gui/modifiers/impossible"));

            grouped(modifierBuilder, VaultMod.id("weak_limbs"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("weakened"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("slowed"), 1);
            },"Weak Limbs", "#da6d5a", "You move slower and hit weaker", null, VaultMod.id("gui/modifiers/impossible"));

            grouped(modifierBuilder, VaultMod.id("ticking_clock"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("haunting"), 4);
                resourceLocationIntegerMap.put(VaultMod.id("enraged_mobs"), 4);
            },"Ticking Clock", "#d52424", "Mobs move faster and have a chance to take time away on hit", null, VaultMod.id("gui/modifiers/impossible"));

            grouped(modifierBuilder, VaultMod.id("abusive_mobs"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("ruthless_mobs"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("chunky_mobs2"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("enraged_mobs"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("critical_mobs"), 2);
                resourceLocationIntegerMap.put(VaultMod.id("unhinged_mob_increase"), 1);
            },"Abusive Mobs", "#d52424", "Mobs are stronger in every way", null, VaultMod.id("gui/modifiers/difficult"));

            grouped(modifierBuilder, VaultMod.id("raid"), resourceLocationIntegerMap -> {
                resourceLocationIntegerMap.put(VaultMod.id("rotten"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("extra_reinforced"), 1);
                resourceLocationIntegerMap.put(VaultMod.id("normalized"), 1);
            },"Raid", "#d89e01", "No Vault Fruit can be used, and you take no durability damage", null, VaultMod.id("gui/modifiers/raid"));


        });
    }



}
