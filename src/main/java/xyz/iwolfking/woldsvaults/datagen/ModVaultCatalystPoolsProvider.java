package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.CatalystConfig;
import iskallia.vault.core.world.roll.IntRoll;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractCatalystProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.List;

public class ModVaultCatalystPoolsProvider extends AbstractCatalystProvider {
    protected ModVaultCatalystPoolsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("god_bonuses", builder -> {
            builder.addCatalystPool(VaultMod.id("tenos_bonus"), pb -> {
                pb.add(0, entryWeightedListBuilder -> {
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("plentiful"), VaultMod.id("plentiful"), VaultMod.id("tenos_challenge")), 8), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("pristine"), VaultMod.id("pristine"), VaultMod.id("pristine"), VaultMod.id("tenos_challenge")), 17), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("energizing"), VaultMod.id("energizing"), VaultMod.id("energizing"), VaultMod.id("tenos_challenge")), 2), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("extended"), VaultMod.id("extended"), VaultMod.id("tenos_challenge")), 6), 9);
                });
            });

            builder.addCatalystPool(VaultMod.id("velara_bonus"), pb -> {
                pb.add(0, entryWeightedListBuilder -> {
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("living"), VaultMod.id("velara_challenge")), 11), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("living_cascade"), VaultMod.id("living_cascade"), VaultMod.id("living_cascade"), VaultMod.id("velara_challenge")), 3), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("soul_boost"), VaultMod.id("soul_boost"), VaultMod.id("soul_boost"), VaultMod.id("velara_challenge")), 9), 9);
                });
            });

            builder.addCatalystPool(VaultMod.id("wendarr_bonus"), pb -> {
                pb.add(0, entryWeightedListBuilder -> {
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("gilded"), VaultMod.id("wendarr_challenge")), 11), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("coin_pile"), VaultMod.id("wendarr_challenge")), 11), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(10, 10), List.of(VaultMod.id("hoard"), VaultMod.id("wendarr_challenge")), 11), 3);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("gilded_cascade"), VaultMod.id("gilded_cascade"), VaultMod.id("gilded_cascade"), VaultMod.id("wendarr_challenge")), 3), 9);
                });
            });

            builder.addCatalystPool(VaultMod.id("idona_bonus"), pb -> {
                pb.add(0, entryWeightedListBuilder -> {
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("ornate"), VaultMod.id("idona_challenge")), 12), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("champion_chance"), VaultMod.id("idona_challenge")), 21), 3);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("ornate_cascade"), VaultMod.id("ornate_cascade"), VaultMod.id("ornate_cascade"), VaultMod.id("idona_challenge")), 4), 9);
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(5, 10), List.of(VaultMod.id("copiously"), VaultMod.id("copiously"), VaultMod.id("copiously"), VaultMod.id("idona_challenge")), 20), 9);
                });
            });
        });

        add("random_positive", builder -> {
            builder.addCatalystPool(VaultMod.id("random_positive"), pb -> {
                pb.add(0, entryWeightedListBuilder -> {
                    entryWeightedListBuilder.add(new CatalystConfig.Entry(IntRoll.ofUniform(10, 10), List.of(VaultMod.id("random_positive"), VaultMod.id("challenge_stack")), 22), 9);
                });
            });
        });
    }
}
