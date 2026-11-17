package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.AncientsConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Ancient uniques: their names, the identify chance breakpoints and the ancient-only effect numbers. */
public class ModAncientsProvider extends AbstractWoldsTableProvider {
    public ModAncientsProvider(DataGenerator generator) {
        super(generator, "gear/ancients");
    }

    /** The ancients table; refuses an ancient name for a Wold's Vaults unique the unique gear provider does not define. */
    public static AncientsConfig ancients() {
        AncientsConfig table = new AncientsConfig();
        table.names = names();
        table.breakpoints = List.of(
                new AncientsConfig.Breakpoint(MilestoneRankLadder.HUNTER_1, 0.005F),
                new AncientsConfig.Breakpoint(MilestoneRankLadder.MASTER_1, 0.0065F),
                new AncientsConfig.Breakpoint(MilestoneRankLadder.CHAMPION_2, 0.008F));
        table.effects = effects();
        return table;
    }

    private static Map<String, String> names() {
        Map<String, String> names = new LinkedHashMap<>();
        vaultUnique(names, "crystal_double_blade", "Ancient Crystal Double Blade");
        vaultUnique(names, "honey_stick", "Ancient Honey Stick");
        vaultUnique(names, "inflated_justice", "Ancient Inflated Justice");
        vaultUnique(names, "iskallibur", "Ancient Iskallibur");
        woldsUnique(names, "lava_chicken_sword", "Ancient Lava Chicken Sword");
        woldsUnique(names, "everfrost", "Ancient Everfrost");
        woldsUnique(names, "everflame", "Ancient Everflame");
        woldsUnique(names, "hexblade", "Ancient Hexblade");
        woldsUnique(names, "mineral_greatsword", "Ancient Mineral Greatsword");
        woldsUnique(names, "grass_sword", "Ancient Grass Sword");
        woldsUnique(names, "aurora_scissors", "Ancient Aurora Scissors");
        woldsUnique(names, "young_kitsune", "Ancient Kitsune Blade");
        vaultUnique(names, "butcher_axe", "Ancient Butcher's Axe");
        vaultUnique(names, "starforge", "Ancient Starforge");
        woldsUnique(names, "leviathan", "Ancient Leviathan");
        woldsUnique(names, "zombie_horse_axe", "Ancient Zombie Horse Axe");
        woldsUnique(names, "ocean_current", "Ancient Zeus's Fury");
        woldsUnique(names, "fork_of_the_glutton", "Ancient Fork of the Glutton");
        woldsUnique(names, "trirang", "Ancient Quad-Rang");
        woldsUnique(names, "bamboo_fightstick", "Ancient Bamboo Fightstick");
        vaultUnique(names, "jester", "Ancient Jester Hat");
        vaultUnique(names, "stormcrown", "Ancient Stormcrown");
        vaultUnique(names, "warbound_helmet", "Ancient Helm of the Warbound");
        vaultUnique(names, "annmari", "Ancient Ann-Mari");
        vaultUnique(names, "castle", "Ancient The Castle");
        vaultUnique(names, "crystalplate", "Ancient Crystal Plate");
        vaultUnique(names, "frozen_throne", "Ancient Frozen Throne");
        vaultUnique(names, "sweetheart", "Ancient The Sweetheart");
        vaultUnique(names, "vitalis", "Ancient Vitalis Plate");
        vaultUnique(names, "crashguards", "Ancient Crashguards");
        vaultUnique(names, "swarmwalkers", "Ancient Swarmwalkers");
        vaultUnique(names, "frostguards", "Ancient Frostguards");
        vaultUnique(names, "pacifist_sandals", "Ancient Pacifist Sandals");
        woldsUnique(names, "plague_steppers", "Ancient Plague Steppers");
        vaultUnique(names, "gladiator_buckler", "Ancient Gladiator's Buckler");
        vaultUnique(names, "grim", "Ancient Grim Shield");
        vaultUnique(names, "ivy", "Ancient Rosethorn Ivy");
        vaultUnique(names, "pestilence_wall", "Ancient Pestilence Wall");
        vaultUnique(names, "frostwarden", "Ancient Frostwarden");
        vaultUnique(names, "broodmother", "Ancient The Broodmother");
        vaultUnique(names, "ender_rings", "Ancient Ender Rings");
        vaultUnique(names, "frozen_orb", "Ancient Frozen Orb");
        vaultUnique(names, "echoflare", "Ancient Echoflare");
        woldsUnique(names, "chroma_brew", "Ancient Chroma Brew");
        vaultUnique(names, "baguette", "Ancient The Baguette");
        vaultUnique(names, "chainlash", "Ancient Chainlash");
        vaultUnique(names, "inferno_reach", "Ancient Steel Inferno");
        woldsUnique(names, "pocket_penguin", "Ancient Pocket Penguin");
        woldsUnique(names, "wicked_witch", "Ancient The Wicked Witch");
        woldsUnique(names, "safer_spaces", "Ancient Safer Spaces");
        vaultUnique(names, "kaleidoscope", "Ancient Kaleidoscope");
        vaultUnique(names, "manabloom", "Ancient Manabloom");
        vaultUnique(names, "pax", "Ancient Pax");
        vaultUnique(names, "frozen_heart", "Ancient Frozen Heart");
        woldsUnique(names, "shattering_jewel", "Ancient Shatterering Jewel");
        woldsUnique(names, "eternal_stella", "Ancient Eternal Stella");
        woldsUnique(names, "treasure_jewel", "Ancient Treasured Jewel");
        vaultUnique(names, "quickstone", "Ancient Quickstone");
        woldsUnique(names, "aural_magnet", "Ancient Aural Magnet");
        woldsUnique(names, "chonknet", "Ancient Chonknet");
        woldsUnique(names, "treasure_magnet", "Ancient Treasure Hunter's Magnet");
        woldsUnique(names, "bloodseeking_magnet", "Ancient Bloodfetcher");
        woldsUnique(names, "tidebreaker_reign", "Ancient Tidebreaker's Reign");
        woldsUnique(names, "tidebreaker_heart", "Ancient Tidebreaker's Heart");
        woldsUnique(names, "tidebreaker_leggings", "Ancient Tidebreaker's Leggings");
        woldsUnique(names, "tidebreaker_greaves", "Ancient Tidebreaker's Greaves");
        woldsUnique(names, "tidebreaker_tideripper", "Ancient Tideripper");
        woldsUnique(names, "tidebreaker_hurricane", "Ancient Hurricane");
        woldsUnique(names, "lightning_fiend_crown", "Ancient Lightning Fiend's Crown");
        woldsUnique(names, "lightning_fiend_mantle", "Ancient Lightning Fiend's Mantle");
        woldsUnique(names, "lightning_fiend_legguards", "Ancient Lightning Fiend's Legguards");
        woldsUnique(names, "lightning_fiend_talons", "Ancient Lightning Fiend's Talons");
        woldsUnique(names, "lightning_fiend_orb", "Ancient Lightning Orb");
        woldsUnique(names, "lightning_fiend_pure_lightning", "Ancient Pure Lightning");
        woldsUnique(names, "simons_helmet", "Ancient Simon's Helmet");
        woldsUnique(names, "simons_chestplate", "Ancient Simon's Chestplate");
        woldsUnique(names, "simons_leggings", "Ancient Simon's Leggings");
        woldsUnique(names, "simons_boots", "Ancient Simon's Boots");
        woldsUnique(names, "bullet_bill_helmet", "Ancient Bullet Bill's Helmet");
        woldsUnique(names, "bullet_bill_leggings", "Ancient Bullet Bill's Leggings");
        woldsUnique(names, "banzai_bill_helmet", "Ancient Banzai Bill Helmet");
        woldsUnique(names, "king_bill_helmet", "Ancient King Bill Helmet");
        Set<ResourceLocation> defined = new ModUniqueGearProvider(null).baseItems().keySet();
        for (String key : names.keySet()) {
            ResourceLocation id = new ResourceLocation(key);
            if (id.getNamespace().equals(WoldsVaults.MOD_ID) && !defined.contains(id)) {
                throw new IllegalStateException("Ancient name given for " + key + ", which the unique gear provider does not define");
            }
        }
        return names;
    }

    private static AncientsConfig.Effects effects() {
        AncientsConfig.Effects effects = new AncientsConfig.Effects();
        effects.luckyHitCleavePercent = 0.6F;
        effects.quakingHitAbility = "Earthquake_Base";
        effects.crashwaveReferenceSpeed = 6.0F;
        effects.crashwaveDamageMultiplier = 2.0F;
        effects.crashwaveRetriggerTicks = 10;
        effects.crashwaveMinLookahead = 0.9;
        effects.crashwaveLookaheadFactor = 1.3;
        effects.radiationScanIntervalTicks = 10;
        effects.radiationEscalationTicks = 100;
        effects.radiationMaxAmplifier = 2;
        effects.radiationSlowDurationTicks = 60;
        return effects;
    }

    private static void vaultUnique(Map<String, String> names, String path, String ancientName) {
        names.put(VaultMod.id(path).toString(), ancientName);
    }

    private static void woldsUnique(Map<String, String> names, String path, String ancientName) {
        names.put(WoldsVaults.id(path).toString(), ancientName);
    }

    @Override
    public void registerConfigs() {
        this.table("ancients", ancients());
    }
}
