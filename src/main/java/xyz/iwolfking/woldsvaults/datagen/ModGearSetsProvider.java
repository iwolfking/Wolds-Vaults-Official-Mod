package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonPrimitive;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import xyz.iwolfking.woldsvaults.config.LightningFiendConfig;
import xyz.iwolfking.woldsvaults.config.RiptideConfig;
import xyz.iwolfking.woldsvaults.config.SimonConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.LinkedHashMap;
import java.util.List;

import static xyz.iwolfking.woldsvaults.config.GearSetConfig.grant;
import static xyz.iwolfking.woldsvaults.config.GearSetConfig.tier;

/**
 * Set gear: the pieces of each set and what each piece-count tier grants. Every number a tier's tooltip quotes is
 * formatted from the grant or mechanic tuning value it describes, so the text cannot disagree with what the tier does.
 */
public class ModGearSetsProvider extends AbstractWoldsTableProvider {
    private static final int TICKS_PER_SECOND = 20;

    public ModGearSetsProvider(DataGenerator generator) {
        super(generator, "gear/sets");
    }

    @Override
    public void registerConfigs() {
        GearSetConfig table = new GearSetConfig();
        table.sets = new LinkedHashMap<>();
        table.sets.put("woldsvaults:tidebreaker", tidebreaker(ModRiptideProvider.riptide()));
        table.sets.put("woldsvaults:lightning_fiend", lightningFiend(ModLightningFiendProvider.lightningFiend()));
        table.sets.put("woldsvaults:simons_set", simon(ModSimonProvider.simon()));
        this.table("gear_sets", table);
    }

    /** The unique ids of the Tidebreaker set's pieces. */
    public static List<String> tidebreakerPieces() {
        return List.of(
                "woldsvaults:tidebreaker_reign", "woldsvaults:tidebreaker_heart", "woldsvaults:tidebreaker_leggings",
                "woldsvaults:tidebreaker_greaves", "woldsvaults:tidebreaker_tideripper", "woldsvaults:tidebreaker_hurricane");
    }

    private static GearSetConfig.GearSet tidebreaker(RiptideConfig riptide) {
        float damageMore = 1.0F;
        int hoverTicks = 30;
        float boostDamageReduction = 0.75F;
        float bolt = 5.0F;
        float boltChilled = 15.0F;
        GearSetConfig.GearSet set = new GearSetConfig.GearSet();
        set.name = "Tidebreaker";
        set.color = "#1C8F3A";
        set.ancientColor = "#1C8F3A";
        set.pieces = tidebreakerPieces();
        set.tiers = List.of(
                tier(2, "Riptide pierces through enemies and deals " + percent(damageMore) + " more damage.",
                        grant("woldsvaults:riptide_pierce", new JsonPrimitive(true)),
                        grant("woldsvaults:riptide_damage_more", new JsonPrimitive(damageMore))),
                tier(4, "Riptide releases a shock cone, striking enemies from " + number(riptide.shockConeAhead)
                                + " blocks ahead of you to a " + number(riptide.shockConeRadius) + "-block radius "
                                + number(riptide.shockConeBehind) + " blocks behind you.",
                        grant("woldsvaults:riptide_shock_cone", new JsonPrimitive(1.0F))),
                tier(6, "While dashing, a storm shrouds you: you hover briefly after each dash, take "
                                + percent(boostDamageReduction) + " less damage, and lightning strikes every enemy your dash "
                                + "or its shock cone hits for " + number(bolt) + "x damage (" + number(boltChilled) + "x if chilled).",
                        grant("woldsvaults:riptide_hover", new JsonPrimitive(hoverTicks)),
                        grant("woldsvaults:riptide_boost_damage_reduction", new JsonPrimitive(boostDamageReduction)),
                        grant("woldsvaults:riptide_bolt", new JsonPrimitive(bolt)),
                        grant("woldsvaults:riptide_bolt_chilled", new JsonPrimitive(boltChilled))));
        return set;
    }

    private static GearSetConfig.GearSet lightningFiend(LightningFiendConfig lightning) {
        float channelingMore = 1.0F;
        int chain = 2;
        int pierce = 3;
        float dashDamage = 0.1F;
        GearSetConfig.GearSet set = new GearSetConfig.GearSet();
        set.name = "Lightning Fiend";
        set.color = "#6FCFFF";
        set.ancientColor = "#6FCFFF";
        set.pieces = List.of(
                "woldsvaults:lightning_fiend_crown", "woldsvaults:lightning_fiend_mantle", "woldsvaults:lightning_fiend_legguards",
                "woldsvaults:lightning_fiend_talons", "woldsvaults:lightning_fiend_orb", "woldsvaults:lightning_fiend_pure_lightning");
        set.tiers = List.of(
                tier(2, "Channeling strikes deal " + percent(channelingMore) + " more damage and chain to " + chain
                                + " more enemies within " + number(lightning.chainRange) + " blocks.",
                        grant("woldsvaults:channeling_damage_more", new JsonPrimitive(channelingMore)),
                        grant("woldsvaults:channeling_chain", new JsonPrimitive(chain))),
                tier(4, "Your thrown trident pierces up to " + pierce + " enemies and calls a channeling strike down on each "
                                + "one it passes through.",
                        grant("woldsvaults:thrown_trident_pierce", new JsonPrimitive(pierce))),
                tier(6, "Throwing your trident turns you into lightning for " + lightning.formDurationTicks / TICKS_PER_SECOND
                                + " seconds. During lightning form, using movement keys causes you to dash through enemies for "
                                + percent(dashDamage) + " channeling damage, and left clicking releases a fan of arc lightning "
                                + "outwards. You can also mark a single enemy with a lightning rod for increased damage by right "
                                + "clicking them, and middle click (pick block) releases a devastating thunderbolt attack centered on "
                                + "you, dealing more damage the more lightning strikes you land while in lightning form.",
                        grant("woldsvaults:lightning_dash", new JsonPrimitive(dashDamage))));
        return set;
    }

    private static GearSetConfig.GearSet simon(SimonConfig simon) {
        int crystals = 1;
        int budding = 4;
        float sequence = 1.0F;
        int facets = 3;
        float prismatic = 0.05F;
        float finale = 1.0F;
        GearSetConfig.GearSet set = new GearSetConfig.GearSet();
        set.name = "Simon's Set";
        set.color = "#FFD23F";
        set.ancientColor = "#FFD23F";
        set.pieces = List.of("woldsvaults:simons_helmet", "woldsvaults:simons_chestplate", "woldsvaults:simons_leggings", "woldsvaults:simons_boots");
        set.tiers = List.of(
                tier(2, "Right click to shoot a prism seed: it grows into a crystal where it lands (one at a time, "
                                + seconds(simon.plantCooldownTicks) + " second cooldown). Damage you deal within " + number(simon.growRadius)
                                + " blocks of a crystal grows it: Seed, Shard, Cluster, Geode. Unfed crystals shrink a stage every "
                                + seconds(simon.shrinkTicks) + " seconds. Arcane, Rail and Prism beams refract from crystal to crystal, and "
                                + "your Prism beams reach " + percent(simon.prismReach - 1.0F) + " further. Brittle: each crystal you have costs "
                                + percent(simon.brittleResistance) + " Resistance.",
                        grant("woldsvaults:simon_crystals", new JsonPrimitive(crystals))),
                tier(3, "Lucky Hits from your swings near your crystals make the one you planted bud a new seed around it (up to "
                                + budding + " crystals). With " + simon.resonateMinCrystals + " or more Shards, sneak and right click to "
                                + "Resonate: the crystals light up biggest first, then repeat; aim and left click them in that order and each "
                                + "turn bursts for " + percent(sequence) + " of your ability power per detonation value (Shard "
                                + number(simon.detonationValues[1]) + ", Cluster " + number(simon.detonationValues[2]) + ", Geode "
                                + number(simon.detonationValues[3]) + "). Every sequence you get right in a row makes the next one a step "
                                + "longer and the explosions " + percent(simon.streakDamage) + " stronger. A wrong click shatters the rest for "
                                + percent(simon.failDamage) + " damage and ends the streak.",
                        grant("woldsvaults:simon_budding", new JsonPrimitive(budding)),
                        grant("woldsvaults:simon_sequence", new JsonPrimitive(sequence))),
                tier(4, "Lucky Hits from your swings near a Geode give it a facet (up to " + facets + "): +"
                                + percent(simon.facetDamage) + " burst damage, then a slowing burst, then a Flawless crystal that leaves a "
                                + "shard field. Budded seeds have a " + percent(prismatic) + " chance to be Prismatic, standing in for any "
                                + "crystal in the sequence. A Lucky Hit roll on each correct click fractures the crystal so it bursts twice, "
                                + "and finishing a chain collapses every burst into one final blast.",
                        grant("woldsvaults:simon_facets", new JsonPrimitive(facets)),
                        grant("woldsvaults:simon_prismatic", new JsonPrimitive(prismatic)),
                        grant("woldsvaults:simon_fracture", new JsonPrimitive(true)),
                        grant("woldsvaults:simon_finale", new JsonPrimitive(finale))));
        return set;
    }

    private static String seconds(int ticks) {
        return number(ticks / (float) TICKS_PER_SECOND);
    }

    private static String number(float value) {
        return value == Math.rint(value) ? Integer.toString(Math.round(value)) : Float.toString(value);
    }

    private static String percent(float fraction) {
        return number(Math.round(fraction * 1000.0F) / 10.0F) + "%";
    }
}
