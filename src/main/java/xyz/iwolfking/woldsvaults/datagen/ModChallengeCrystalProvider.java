package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.entry.IntRangeEntry;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import iskallia.vault.item.crystal.objective.ChaosCrystalObjective;
import iskallia.vault.item.crystal.theme.PoolCrystalTheme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import iskallia.vault.item.crystal.time.ValueCrystalTime;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractChallengeCrystalProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.CrystalObjectiveHelper;
import xyz.iwolfking.woldsvaults.objectives.*;

import java.util.List;

public class ModChallengeCrystalProvider extends AbstractChallengeCrystalProvider {
    protected ModChallengeCrystalProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_challenges", builder -> {
            builder.addEntry("pitch_black", challengeEntryBuilder -> {
               challengeEntryBuilder
                       .objective(new HauntedBraziersCrystalObjective(IntRoll.ofConstant(10), 1.0F))
                       .theme(new ValueCrystalTheme(VaultMod.id("classic_vault_dark_cavern")))
                       .time(new ValueCrystalTime(IntRoll.ofConstant(12000)))
                       .modifier(WoldsVaults.id("lights_out"), 1)
                       .modifier(WoldsVaults.id("blindness_immunity_cancel"), 1)
                       .modifier(VaultMod.id("challenge_rules"), 1)
                       .embers(new IntRangeEntry(15, 30))
                       .unlockLevel(50)
                       .flavour("Find your way through the dark to light the way in this scary place!")
                       .exhausted();
            });

            builder.addEntry("ultra_hard", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(CrystalObjectiveHelper.createElixir(0.4F))
                        .theme(new PoolCrystalTheme(VaultMod.id("classic_vault_dark_cavern")))
                        .modifier(VaultMod.id("fading"), 1)
                        .modifier(VaultMod.id("champions_realm"), 4)
                        .modifier(VaultMod.id("no_champ_drops"), 1)
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .modifier(VaultMod.id("hard"), 1)
                        .sigil("expert")
                        .embers(new IntRangeEntry(50, 50))
                        .unlockLevel(100)
                        .flavour("No one said challenges were supposed to be fun.")
                        .exhausted();
            });

            builder.addEntry("elixir_of_doom", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(new EnchantedElixirCrystalObjective(0.25F))
                        .modifier(WoldsVaults.id("all_bad_enchanted_elixir"), 1)
                        .modifier(WoldsVaults.id("enchanted_elixir_50_events"), 1)
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .sigil("expert")
                        .embers(new IntRangeEntry(20, 30))
                        .unlockLevel(50)
                        .flavour("RNG is a hell of a drug, see if you can handle it")
                        .exhausted();
            });

            builder.addEntry("ballistic_bingo_blackout", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(new ScalingBallisticBingoCrystalObjective(0.25F, 2, true))
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .embers(new IntRangeEntry(20, 30))
                        .unlockLevel(50)
                        .flavour("Blackout that Ballistic Bingo board! You can do it, champ!")
                        .exhausted();
            });

            builder.addEntry("blackout_scavingo", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(CrystalObjectiveHelper.createScavingo(0.25F, 5, 5, true, VaultMod.id("bingos")))
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .embers(new IntRangeEntry(20, 30))
                        .unlockLevel(50)
                        .flavour("Blackout that Collector board! You can do it, champ!")
                        .exhausted();
            });

            builder.addEntry("gods_challenge", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(new ZealotCrystalObjective(IntRoll.ofConstant(10)))
                        .modifier(VaultMod.id("velara_challenge"), 1)
                        .modifier(VaultMod.id("wendarr_challenge"), 1)
                        .modifier(VaultMod.id("tenos_challenge"), 1)
                        .modifier(VaultMod.id("idona_challenge"), 1)
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .layout(new ClassicInfiniteCrystalLayout(1))
                        .theme(new PoolCrystalTheme(WoldsVaults.id("god_themes")))
                        .embers(new IntRangeEntry(40, 50))
                        .unlockLevel(100)
                        .flavour("See if you are up to the challenge of impressing the Vault Gods")
                        .exhausted();
            });

            builder.addEntry("big_bad_brew", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(new AlchemyCrystalObjective(0.5F, 10.0F))
                        .modifier(VaultMod.id("bubbling_trouble"), 4)
                        .layout(new ClassicInfiniteCrystalLayout(1))
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .embers(new IntRangeEntry(25, 40))
                        .unlockLevel(80)
                        .flavour("Fill up that big ol' brew with alchemy wizmos and whatzits!")
                        .exhausted();
            });

            builder.addEntry("chaos_chaos_chaos", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(new ChaosCrystalObjective(1.0F))
                        .modifier(VaultMod.id("unhinged"), 1)
                        .modifier(VaultMod.id("chaos_return"), 1)
                        .sigil("adept")
                        .layout(new ClassicInfiniteCrystalLayout(1))
                        .theme(new ValueCrystalTheme(VaultMod.id("classic_vault_chaos")))
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .embers(new IntRangeEntry(25, 40))
                        .unlockLevel(75)
                        .flavour("It's just a simple little chaos")
                        .exhausted();
            });

            builder.addEntry("rage_cage", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(new BrutalBossesCrystalObjective(IntRoll.ofConstant(7), IntRoll.ofConstant(5), 1.0F))
                        .modifier(VaultMod.id("challenge_stack"), 5)
                        .modifier(WoldsVaults.id("infernal_fury"), 1)
                        .modifier(WoldsVaults.id("rage_cage"), 1)
                        .layout(new ClassicInfiniteCrystalLayout(1))
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .embers(new IntRangeEntry(30, 45))
                        .unlockLevel(100)
                        .flavour("The Elites are pissed, see if you can survive their RAGE!")
                        .exhausted();
            });

            builder.addEntry("survival_of_the_fittest", challengeEntryBuilder -> {
                challengeEntryBuilder
                        .objective(new SurvivalCrystalObjective(0.5F, 30, List.of("t1", "t1_t2", "t2", "t2_t3", "t3", "t3_t4", "t4", "t5", "t6", "t7")))
                        .layout(new ClassicInfiniteCrystalLayout(1))
                        .modifier(VaultMod.id("challenge_rules"), 1)
                        .embers(new IntRangeEntry(30, 50))
                        .unlockLevel(100)
                        .flavour("Survive the onslaught for as long as you can!")
                        .exhausted();
            });

        });
    }
}
