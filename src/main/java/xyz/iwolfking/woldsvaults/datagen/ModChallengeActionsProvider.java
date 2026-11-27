package xyz.iwolfking.woldsvaults.datagen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.VaultMod;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.roll.DoubleRoll;
import iskallia.vault.core.world.roll.IntRoll;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.api.builtins.BuiltInChallengeActions;
import xyz.iwolfking.vhapi.api.builtins.BuiltInEntities;
import xyz.iwolfking.vhapi.api.builtins.BuiltInTiles;
import xyz.iwolfking.vhapi.api.datagen.AbstractChallengeActionsProvider;
import xyz.iwolfking.vhapi.api.builtins.BuiltInItemStacks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModChallengeActionsProvider extends AbstractChallengeActionsProvider {
    protected ModChallengeActionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_actions", builder -> {
            //Mob Attributes
           builder.addMobVanillaAttribute("mob_armor", null, null, Attributes.ARMOR, AttributeModifier.Operation.ADDITION, 0xFFF32525, DoubleRoll.ofConstant(2));
           builder.addMobVanillaAttribute("big_mob_armor", null, null, Attributes.ARMOR, AttributeModifier.Operation.ADDITION, 0xFFF32525, DoubleRoll.ofConstant(4));
           builder.addMobVanillaAttribute("big_mob_damage", null, null, Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.MULTIPLY_BASE, 0xFFF32525, DoubleRoll.ofConstant(0.5));
           builder.addMobVanillaAttribute("big_mob_health", null, null, Attributes.MAX_HEALTH, AttributeModifier.Operation.MULTIPLY_BASE, 0xFFF32525, DoubleRoll.ofConstant(0.5));
           builder.addMobVanillaAttribute("big_mob_movement_speed", null, null, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.MULTIPLY_BASE, 0xFFF32525, DoubleRoll.ofConstant(0.1));

           //Floating Item Rewards
            builder.addFloatingItemReward("gateway_pearl", new ItemStack(ModItems.UNIDENTIFIED_GATEWAY_PEARL, 3), null, 0xFF00FFFF, 0xFF6600FF, 0xFFF32525);
            builder.addFloatingItemReward("blazing_focus", new ItemStack(ModItems.BLAZING_FOCUS, 1), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("suspension_focus", new ItemStack(ModItems.SUSPENSION_FOCUS, 1), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("eccentric_focus", new ItemStack(ModItems.ECCENTRIC_FOCUS, 1), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("eccentric_focus_big", new ItemStack(ModItems.ECCENTRIC_FOCUS, 2), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("eccentric_focus_bigger", new ItemStack(ModItems.ECCENTRIC_FOCUS, 4), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("cryonic_focus_bigger", new ItemStack(iskallia.vault.init.ModItems.CRYONIC_FOCUS, 4), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("empowered_chaotic_focus_big", new ItemStack(iskallia.vault.init.ModItems.EMPOWERED_CHAOTIC_FOCUS, 4), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("deck_core", BuiltInItemStacks.deckCore("@default", null, 1), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("deck_core_greater", BuiltInItemStacks.deckCore("@default", "greater", 1), null, 0xFFffc600, 0xFFa8150a, 0xFFffb400);
            builder.addFloatingItemReward("shiny_pack", BuiltInItemStacks.boosterPack(VaultMod.id("shiny_pack"), 1), "Shiny Booster Pack", 0xFFf45832, 0xFFfb8ffc, 0xFFffb400);
            builder.addFloatingItemReward("knack_pack", BuiltInItemStacks.boosterPack(VaultMod.id("knack_pack"), 1), "Knack Booster Pack", 0xFFf45832, 0xFFfb8ffc, 0xFFffb400);
            builder.addFloatingItemReward("temporal_booster_pack", BuiltInItemStacks.boosterPack(VaultMod.id("temporal_pack"), 1), "Temporal Booster Pack", 0xFFf45832, 0xFFfb8ffc, 0xFFffb400);
            builder.addFloatingItemReward("temporal_booster_pack_big", BuiltInItemStacks.boosterPack(VaultMod.id("temporal_pack"), 3), "Temporal Booster Pack", 0xFFf45832, 0xFFfb8ffc, 0xFFffb400);
            builder.addFloatingItemReward("leg_plushie", BuiltInItemStacks.legendaryGearPiece(ModItems.PLUSHIE, "gear_treasure"), "Legendary Plushie", 0xFFffb400, 0xFFffe04e, 0xFFffb400);
            builder.addFloatingItemReward("leg_loot_sack", BuiltInItemStacks.legendaryGearPiece(ModItems.LOOT_SACK, "gear_treasure"), "Legendary Loot Sack", 0xFFffb400, 0xFFffe04e, 0xFFffb400);
            builder.addFloatingItemReward("leg_trident", BuiltInItemStacks.legendaryGearPiece(ModItems.TRIDENT, "gear_treasure"), "Legendary Trident", 0xFFffb400, 0xFFffe04e, 0xFFffb400);
            builder.addFloatingItemReward("leg_rang", BuiltInItemStacks.legendaryGearPiece(ModItems.RANG, "gear_treasure"), "Legendary Vaultarang", 0xFFffb400, 0xFFffe04e, 0xFFffb400);
            builder.addFloatingItemReward("leg_battlestaff", BuiltInItemStacks.legendaryGearPiece(ModItems.BATTLESTAFF, "gear_treasure"), "Legendary Vaultarang", 0xFFffb400, 0xFFffe04e, 0xFFffb400);
            builder.addFloatingItemReward("leg_scepter", BuiltInItemStacks.legendaryGearPiece(ModItems.SCEPTER, "gear_treasure"), "Legendary Scepter", 0xFFffb400, 0xFFffe04e, 0xFFffb400);
            builder.addFloatingItemReward("leg_dagger", BuiltInItemStacks.legendaryGearPiece(ModItems.DAGGER_MAIN, "gear_treasure"), "Legendary Dagger", 0xFFffb400, 0xFFffe04e, 0xFFffb400);
            builder.addFloatingItemReward("rare_plushie", BuiltInItemStacks.gearWithRollType(ModItems.PLUSHIE, "Rare+"), "Plushie", 0xFFffea00, 0xFFffd200, 0xFFffb400);
            builder.addFloatingItemReward("rare_loot_sack", BuiltInItemStacks.gearWithRollType(ModItems.LOOT_SACK, "Rare+"), "Loot Sack", 0xFFffea00, 0xFFffd200, 0xFFffb400);
            builder.addFloatingItemReward("rare_trident", BuiltInItemStacks.gearWithRollType(ModItems.TRIDENT, "Rare+"), "Trident", 0xFFffea00, 0xFFffd200, 0xFFffb400);
            builder.addFloatingItemReward("rare_rang", BuiltInItemStacks.gearWithRollType(ModItems.RANG, "Rare+"), "Vaultarang", 0xFFffea00, 0xFFffd200, 0xFFffb400);
            builder.addFloatingItemReward("rare_battlestaff", BuiltInItemStacks.gearWithRollType(ModItems.BATTLESTAFF, "Rare+"), "Vaultarang", 0xFFffea00, 0xFFffd200, 0xFFffb400);
            builder.addFloatingItemReward("rare_scepter", BuiltInItemStacks.gearWithRollType(ModItems.SCEPTER, "Rare+"), "Scepter", 0xFFffea00, 0xFFffd200, 0xFFffb400);
            builder.addFloatingItemReward("rare_dagger", BuiltInItemStacks.gearWithRollType(ModItems.DAGGER_MAIN, "Rare+"), "Dagger", 0xFFffea00, 0xFFffd200, 0xFFffb400);
            builder.addFloatingItemReward("treasure_key_big", new ItemStack(iskallia.vault.init.ModItems.UNIDENTIFIED_TREASURE_KEY, 3), null, 0xFFff8400, 0xFFffcc00, 0xFFffb400);
            builder.addFloatingItemReward("pocket_penguin", BuiltInItemStacks.uniqueGear(ModItems.PLUSHIE, WoldsVaults.id("pocket_penguin").toString()), "Pocket Penguin", 0xFFED7B24, 0xFFe2a631, -1213660);
            builder.addFloatingItemReward("chroma_brew", BuiltInItemStacks.uniqueGear(iskallia.vault.init.ModItems.FOCUS, WoldsVaults.id("chroma_brew").toString()), "Chroma Brew", 0xFFED7B24, 0xFFe2a631, -1213660);
            builder.addFloatingItemReward("shattering_jewel", BuiltInItemStacks.uniqueGear(iskallia.vault.init.ModItems.JEWEL, WoldsVaults.id("shattering_jewel").toString()), "Shattering Jewel", 0xFFED7B24, 0xFFe2a631, -1213660);
            builder.addFloatingItemReward("raid_rock_infinite_hard", new ItemStack(ModItems.CRYSTAL_SEAL_RAID_ROCK_INFINITE_HARD, 1), "Infinite Brutal Raid Rock", 0xFFff381d, 0xFFb51500, 0xFFffb400);
            builder.addFloatingItemReward("echo_gem_bigger", new ItemStack(iskallia.vault.init.ModItems.ECHO_GEM, 12), "Echo Gems", 0xFF6b00cf, 0xFF10021e, 0xFFffb400);

            try {
                //Tile Rewards -- Replaces vanilla values
                builder.addTileReward("gilded_chest", BuiltInTiles.tileWithNoTrap("gilded_chest"), 10, "Gilded Chests", 0xFFfff0a9);
                builder.addTileReward("ornate_chest", BuiltInTiles.tileWithNoTrap("ornate_chest"), 10, "Ornate Chests", 0xFFfff0a9);
                builder.addTileReward("living_chest", BuiltInTiles.tileWithNoTrap("living_chest"), 10, "Living Chests", 0xFFfff0a9);
                builder.addTileReward("wooden_chest", BuiltInTiles.tileWithNoTrap("wooden_chest"), 16, "Wooden Chests", 0xFFfff0a9);
                builder.addTileReward("coin_pile", PartialTile.parse("the_vault:placeholder[type=coin_stacks,facing=up]"), 10, "Coin Piles", 0xFFfff0a9);
                builder.addTileReward("gilded_chest_big", BuiltInTiles.tileWithNoTrap("gilded_chest"), 16, "Gilded Chests", 0xFFfff0a9);
                builder.addTileReward("ornate_chest_big", BuiltInTiles.tileWithNoTrap("ornate_chest"), 16, "Ornate Chests", 0xFFfff0a9);
                builder.addTileReward("living_chest_big", BuiltInTiles.tileWithNoTrap("living_chest"), 16, "Living Chests", 0xFFfff0a9);
                builder.addTileReward("wooden_chest_big", BuiltInTiles.tileWithNoTrap("wooden_chest"), 24, "Wooden Chests", 0xFFfff0a9);
                builder.addTileReward("coin_pile_big", PartialTile.parse("the_vault:placeholder[type=coin_stacks,facing=up]"), 16, "Coin Piles", 0xFFfff0a9);

                //New Tile Rewards
                builder.addTileReward("gilded_chest_bigger", BuiltInTiles.tileWithNoTrap("gilded_chest"), 24, "Gilded Chests", 0xFFfff0a9);
                builder.addTileReward("ornate_chest_bigger", BuiltInTiles.tileWithNoTrap("ornate_chest"), 24, "Ornate Chests", 0xFFfff0a9);
                builder.addTileReward("living_chest_bigger", BuiltInTiles.tileWithNoTrap("living_chest"), 24, "Living Chests", 0xFFfff0a9);
                builder.addTileReward("wooden_chest_bigger", BuiltInTiles.tileWithNoTrap("wooden_chest"), 32, "Wooden Chests", 0xFFfff0a9);
                builder.addTileReward("coin_pile_bigger", PartialTile.parse("the_vault:placeholder[type=coin_stacks,facing=up]"), 24, "Coin Piles", 0xFFfff0a9);

            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }

            //Add Mobs
            builder.addMobs("add_iceologer", "Iceologer", BuiltInEntities.raidMob(ResourceLocation.fromNamespaceAndPath("dungeons_mobs", "iceologer")), "ground", IntRoll.ofConstant(1), 0xFFfff0a9);
            builder.addMobs("add_mage", "Mage", BuiltInEntities.raidMob(ResourceLocation.fromNamespaceAndPath("dungeons_mobs", "mage")), "elevated", IntRoll.ofConstant(1), 0xFFfff0a9);
            builder.addMobs("add_geomancer", "Geomancer", BuiltInEntities.raidMob(ResourceLocation.fromNamespaceAndPath("dungeons_mobs", "geomancer")), "ground", IntRoll.ofConstant(1), 0xFFfff0a9);
            builder.addMobs("add_windcaller", "Windcaller", BuiltInEntities.raidMob(ResourceLocation.fromNamespaceAndPath("dungeons_mobs", "windcaller")), "ground", IntRoll.ofConstant(1), 0xFFfff0a9);
            builder.addMobs("add_incinerator", "Incinerator", BuiltInEntities.raidMob(ResourceLocation.fromNamespaceAndPath("grimoireofgaia", "incinerator")), "ground", IntRoll.ofConstant(1), 0xFFfff0a9);
            builder.addMobs("add_mountaineer", "Mountaineer", BuiltInEntities.raidMob(ResourceLocation.fromNamespaceAndPath("dungeons_mobs", "mountaineer")), "ground", IntRoll.ofConstant(1), 0xFFfff0a9);
            builder.addMobs("add_royal_guard", "Royal Guard", BuiltInEntities.raidMob(ResourceLocation.fromNamespaceAndPath("dungeons_mobs", "royal_guard")), "ground", IntRoll.ofConstant(1), 0xFFfff0a9);
            builder.addMobs("add_royal_guard_champion", "Royal Guard Champion", BuiltInEntities.raidChampion(ResourceLocation.fromNamespaceAndPath("dungeons_mobs", "royal_guard")), "ground", IntRoll.ofConstant(1), 0xFFfff0a9);

            //Action Pools
            builder.addPool("negative", 0, poolBuilder -> {
               poolBuilder.addLevel(0, entries -> {
                   entries.add(BuiltInChallengeActions.referenceChallengeAction("mob_armor"), 4);
               });
            });

            builder.addPool("negative_map", 0, poolBuilder -> {
                poolBuilder.addLevel(0, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("mob_armor"), 4);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("big_mob_armor"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("mob_damage"), 4);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("big_mob_damage"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("mob_health"), 4);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("big_mob_health"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("mob_movement_speed"), 4);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("big_mob_movement_speed"), 2);
                });
            });

            builder.addPool("positive", 0, poolBuilder -> {
                poolBuilder.addLevel(0, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_plushie"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_rang"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_trident"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_loot_sack"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_battlestaff"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_scepter"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_dagger"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_plushie"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_loot_sack"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_trident"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_rang"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_battlestaff"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_scepter"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_dagger"), 2);
                });
                poolBuilder.addLevel(50, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_plushie"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_rang"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_trident"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_loot_sack"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_battlestaff"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_scepter"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_dagger"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_plushie"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_loot_sack"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_trident"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_rang"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_battlestaff"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_scepter"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_dagger"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("knack_pack"), 15);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("temporal_booster_pack"), 5);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("temporal_booster_pack"), 5);
                });
            });

            builder.addPool("positive_base", 0, poolBuilder -> {
                poolBuilder.addLevel(0, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_plushie"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_rang"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_trident"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_loot_sack"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_battlestaff"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_scepter"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_dagger"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_plushie"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_loot_sack"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_trident"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_rang"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_battlestaff"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_scepter"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_dagger"), 2);
                });
                poolBuilder.addLevel(50, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_plushie"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_rang"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_trident"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_loot_sack"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_battlestaff"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_scepter"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_dagger"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_plushie"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_loot_sack"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_trident"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_rang"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_battlestaff"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_scepter"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_dagger"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("knack_pack"), 15);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("temporal_booster_pack"), 5);
                });
            });

            builder.addPool("xmark", 0, poolBuilder -> {
                poolBuilder.addLevel(0, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_plushie"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_rang"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_trident"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_loot_sack"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_battlestaff"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_scepter"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_dagger"), 20);
                });
                poolBuilder.addLevel(50, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_plushie"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_rang"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_trident"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_loot_sack"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_battlestaff"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_scepter"), 30);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("rare_dagger"), 30);
                });
            });

            builder.addPool("positive_lab", 0, poolBuilder -> {
                poolBuilder.addLevel(0, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("pocket_penguin"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("chroma_brew"), 8);
                });
                poolBuilder.addLevel(50, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("pocket_penguin"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("chroma_brew"), 8);
                });
            });

            builder.addPool("lab_uniques", 0, poolBuilder -> {
                poolBuilder.addLevel(0, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("pocket_penguin"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("chroma_brew"), 8);
                });
            });

            builder.addPool("positive_base_map", 0, poolBuilder -> {
                poolBuilder.addLevel(0, entries -> {
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("treasure_key_big"), 2);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("treasure_key"), 4);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("shattering_jewel"), 1);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("blazing_focus"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("gateway_pearl"), 20);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("deck_core"), 10);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("deck_core_greater"), 5);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("eccentric_focus_big"), 8);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("cryonic_focus_big"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("gilded_chest_bigger"), 60);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("ornate_chest_bigger"), 60);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("living_chest_bigger"), 60);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("wooden_chest_bigger"), 60);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("coin_pile_bigger"), 60);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("echo_gem_bigger"), 8);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("temporal_booster_pack_big"), 4);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("shiny_pack"), 4);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_helmet"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_chestplate"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_leggings"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_boots"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_sword"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_axe"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_shield"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_wand"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_plushie"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_loot_sack"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_rang"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_battlestaff"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_scepter"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("leg_dagger"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("castle_chestplate"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("warbound_helmet"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("inferno_reach"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("frozen_orb"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("grim_shield"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("swarmwalkers"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("stormcrown"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("inflated_justice"), 6);
                    entries.add(BuiltInChallengeActions.referenceChallengeAction("raid_rock_infinite_hard"), 4);
                });
            });
        });
    }
}
