package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.CompanionRelicsConfig;
import iskallia.vault.core.world.roll.IntRoll;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractCompanionRelicProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;

public class ModCompanionRelicsProvider extends AbstractCompanionRelicProvider{
    protected ModCompanionRelicsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_relic_pools", builder -> {
            builder.addPool(WoldsVaults.id("random"), entryBuilder -> {
                entryBuilder.add(0, entryWeightedListBuilder -> {
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(2), List.of(VaultMod.id("coin_cascade"), VaultMod.id("companion_challenge"))), 8);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(2), List.of(VaultMod.id("coin_pile"), VaultMod.id("companion_challenge"))), 4);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(4), List.of(VaultMod.id("wooden_cascade"), VaultMod.id("companion_challenge"))), 8);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(4), List.of(VaultMod.id("wooden"), VaultMod.id("companion_challenge"))), 4);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(1), List.of(VaultMod.id("ornate_cascade"), VaultMod.id("companion_challenge"))), 8);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(1), List.of(VaultMod.id("ornate"), VaultMod.id("companion_challenge"))), 4);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(3), List.of(VaultMod.id("gilded_cascade"), VaultMod.id("companion_challenge"))), 8);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(3), List.of(VaultMod.id("gilded"), VaultMod.id("companion_challenge"))), 4);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(6), List.of(VaultMod.id("living_cascade"), VaultMod.id("companion_challenge"))), 8);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(6), List.of(VaultMod.id("living"), VaultMod.id("companion_challenge"))), 4);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(5), List.of(VaultMod.id("plentiful"), VaultMod.id("companion_challenge"))), 8);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(9), List.of(VaultMod.id("serendipitous"), VaultMod.id("companion_challenge"))), 8);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(9), List.of(VaultMod.id("more_mobs"), VaultMod.id("companion_challenge"))), 8);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(8), List.of(VaultMod.id("sparkling"), VaultMod.id("companion_challenge"), VaultMod.id("companion_challenge"))), 6);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(8), List.of(VaultMod.id("exorcising"), VaultMod.id("companion_challenge"))), 4);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(List.of(VaultMod.id("hoard").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString()), IntRoll.ofConstant(9), true), 1);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(List.of(WoldsVaults.id("cardboard_boxes").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString()), IntRoll.ofConstant(9), true), 1);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(List.of(VaultMod.id("treasure").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString()), IntRoll.ofConstant(9), true), 1);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(List.of(VaultMod.id("phoenix").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString()), IntRoll.ofConstant(9), true), 1);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(List.of(VaultMod.id("champion_chance").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString()), IntRoll.ofConstant(9), true), 1);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(List.of(VaultMod.id("omega_cascade").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString()), IntRoll.ofConstant(9), true), 1);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(List.of(VaultMod.id("omega_bonus").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString(), VaultMod.id("companion_challenge").toString()), IntRoll.ofConstant(9), true), 1);

                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(2), List.of(VaultMod.id("coin_cascade"), VaultMod.id("coin_cascade"), VaultMod.id("companion_challenge"), VaultMod.id("companion_challenge"))), 3);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(4), List.of(VaultMod.id("wooden_cascade"), VaultMod.id("wooden_cascade"), VaultMod.id("companion_challenge"), VaultMod.id("companion_challenge"))), 3);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(1), List.of(VaultMod.id("ornate_cascade"), VaultMod.id("ornate_cascade"), VaultMod.id("companion_challenge"), VaultMod.id("companion_challenge"))), 3);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(3), List.of(VaultMod.id("gilded_cascade"), VaultMod.id("gilded_cascade"), VaultMod.id("companion_challenge"), VaultMod.id("companion_challenge"))), 3);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(6), List.of(VaultMod.id("living_cascade"), VaultMod.id("living_cascade"), VaultMod.id("companion_challenge"), VaultMod.id("companion_challenge"))), 3);
                   entryWeightedListBuilder.add(new CompanionRelicsConfig.Entry(IntRoll.ofConstant(5), List.of(VaultMod.id("plentiful"), VaultMod.id("plentiful"), VaultMod.id("companion_challenge"), VaultMod.id("companion_challenge"))), 3);

                });
            });
        });
    }
}
