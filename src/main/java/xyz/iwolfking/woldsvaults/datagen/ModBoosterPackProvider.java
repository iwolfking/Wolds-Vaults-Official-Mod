package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.card.BoosterPackConfig;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.VHAPI;
import xyz.iwolfking.vhapi.api.datagen.card.AbstractBoosterPackProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;
import java.util.Set;

public class ModBoosterPackProvider extends AbstractBoosterPackProvider {
    protected ModBoosterPackProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_packs", builder -> {
            builder.addEntry(VaultMod.id("deluxe_stat_pack").toString(), "Deluxe Stat Booster Pack", VaultMod.id("booster_pack/stat_pack_ripped").toString(), VHAPI.of("booster_pack/deluxe_stat_pack").toString(), rolls -> {
                rolls.add(3, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
                colors.add(CardEntry.Color.YELLOW, 1);
                colors.add(CardEntry.Color.GREEN, 1);
                colors.add(CardEntry.Color.BLUE, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_stat", new WeightedList<>(), Set.of(), "", "@deluxe_stat", 1.0)), 9);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_stat", new WeightedList<>(), Set.of("Foil"), "", "@deluxe_stat", 1.0)), 1);
            });

            builder.addEntry(VaultMod.id("knack_pack").toString(), "Knack Booster Pack", VHAPI.of("booster_pack/knack_pack_ripped").toString(), VHAPI.of("booster_pack/knack_pack").toString(), rolls -> {
                rolls.add(3, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
                colors.add(CardEntry.Color.YELLOW, 1);
                colors.add(CardEntry.Color.GREEN, 1);
                colors.add(CardEntry.Color.BLUE, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@knack", new WeightedList<>(), Set.of(), "", "@knack", 1.0)), 9);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@knack", new WeightedList<>(), Set.of("Foil"), "", "@knack", 1.0)), 1);
            });

            builder.addEntry(VaultMod.id("mega_knack_pack").toString(), "Mega Knack Booster Pack", VHAPI.of("booster_pack/mega_knack_pack_ripped").toString(), VHAPI.of("booster_pack/mega_knack_pack").toString(), rolls -> {
                rolls.add(5, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
                colors.add(CardEntry.Color.YELLOW, 1);
                colors.add(CardEntry.Color.GREEN, 1);
                colors.add(CardEntry.Color.BLUE, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@knack", new WeightedList<>(), Set.of(), "", "@knack", 1.0)), 9);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@knack", new WeightedList<>(), Set.of("Foil"), "", "@knack", 1.0)), 1);
            });

            builder.addEntry(VaultMod.id("deluxe_knack_pack").toString(), "Deluxe Knack Booster Pack", VHAPI.of("booster_pack/knack_pack_ripped").toString(), VHAPI.of("booster_pack/deluxe_knack_pack").toString(), rolls -> {
                rolls.add(3, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
                colors.add(CardEntry.Color.YELLOW, 1);
                colors.add(CardEntry.Color.GREEN, 1);
                colors.add(CardEntry.Color.BLUE, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_knack", new WeightedList<>(), Set.of(), "", "@deluxe_knack", 1.0)), 9);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_knack", new WeightedList<>(), Set.of("Foil"), "", "@deluxe_knack", 1.0)), 1);
            });

            builder.addEntry(VaultMod.id("deluxe_resource_pack").toString(), "Deluxe Resource Booster Pack", VaultMod.id("booster_pack/resource_pack_ripped").toString(), VHAPI.of("booster_pack/deluxe_resource_pack").toString(), rolls -> {
                rolls.add(3, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
                colors.add(CardEntry.Color.YELLOW, 1);
                colors.add(CardEntry.Color.GREEN, 1);
                colors.add(CardEntry.Color.BLUE, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_resource", new WeightedList<>(), Set.of(), null, null, 1.0)), 9);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_resource", new WeightedList<>(), Set.of("Foil"), null, null, 1.0)), 1);
            });

            builder.addEntry(VaultMod.id("deluxe_temporal_pack").toString(), "Deluxe Temporal Booster Pack", VaultMod.id("booster_pack/temporal_pack_ripped").toString(), VHAPI.of("booster_pack/deluxe_temporal_pack").toString(), rolls -> {
                rolls.add(2, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
                colors.add(CardEntry.Color.YELLOW, 1);
                colors.add(CardEntry.Color.GREEN, 1);
                colors.add(CardEntry.Color.BLUE, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_temporal", null, null, "@temporal", "@temporal", 1.0)), 9);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_temporal", null, Set.of("Foil"), "@temporal", "@temporal", 1.0)), 1);
            });

            builder.addEntry(VaultMod.id("mega_temporal_pack").toString(), "Mega Temporal Booster Pack", VaultMod.id("booster_pack/temporal_pack_ripped").toString(), VaultMod.id("booster_pack/temporal_pack").toString(), rolls -> {
                rolls.add(5, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
                colors.add(CardEntry.Color.YELLOW, 1);
                colors.add(CardEntry.Color.GREEN, 1);
                colors.add(CardEntry.Color.BLUE, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@temporal", null, null, "@temporal", "@temporal", 1.0)), 9);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@temporal", null, Set.of("Foil"), "@temporal", "@temporal", 1.0)), 1);
            });

            builder.addEntry(VaultMod.id("treasure_pack").toString(), "Deluxe Booster Pack", VHAPI.of("booster_pack/treasure_pack_ripped").toString(), VHAPI.of("booster_pack/treasure_pack").toString(), rolls -> {
                rolls.add(5, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
                colors.add(CardEntry.Color.YELLOW, 1);
                colors.add(CardEntry.Color.GREEN, 1);
                colors.add(CardEntry.Color.BLUE, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_stat", null, null, null, "@deluxe_stat", 1.0)), 25);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_stat", null, Set.of("Foil"), null, "@deluxe_stat", 1.0)), 5);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_resource", null, null, null, null, 1.0)), 25);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_resource", null, Set.of("Foil"), null, null, 1.0)), 5);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_knack", null, null, null, "@deluxe_knack", 1.0)), 25);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_knack", null, Set.of("Foil"), null, "@deluxe_knack", 1.0)), 5);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@temporal", null, Set.of("Foil"), "@temporal", "@temporal", 1.0)), 10);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_temporal", null, Set.of("Foil"), "@temporal", "@temporal", 1.0)), 5);
            });

            builder.addEntry(VaultMod.id("hellish_pack").toString(), "Brimstone Booster Pack", VHAPI.of("booster_pack/merchant_pack_ripped").toString(), VHAPI.of("booster_pack/merchant_pack").toString(), rolls -> {
                rolls.add(3, 1);
            }, tiers -> {
                tiers.add(1, 1);
            }, colors -> {
                colors.add(CardEntry.Color.RED, 1);
            }, cards -> {
                cards.add(List.of(new BoosterPackConfig.CardConfig("@resource", null, null, null, null, 1.0)), 9);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@resource", null, null, null, null, 1.0)), 3);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_resource", null, null, null, null, 1.0)), 3);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@deluxe_resource", null, Set.of("Foil"), null, null, 1.0)), 1);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@default", null, null, null, null, 1.0)), 6);
                cards.add(List.of(new BoosterPackConfig.CardConfig("@default", null, Set.of("Foil"), null, null, 1.0)), 4);
            });
        });


    }
}
