package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonPrimitive;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.LinkedHashMap;
import java.util.List;

import static xyz.iwolfking.woldsvaults.config.GearSetConfig.grant;
import static xyz.iwolfking.woldsvaults.config.GearSetConfig.tier;

/**
 * Set gear: the pieces of each set, what each piece-count tier grants, and the shared riptide and lightning tuning
 * the set mechanics read. Every number a tier's tooltip quotes is formatted from the grant or tuning value it
 * describes, so the text cannot disagree with what the tier does.
 */
public class ModGearSetsProvider extends AbstractWoldsTableProvider {
    private static final int TICKS_PER_SECOND = 20;

    public ModGearSetsProvider(DataGenerator generator) {
        super(generator, "gear/sets");
    }

    @Override
    public void registerConfigs() {
        GearSetConfig table = new GearSetConfig();

        GearSetConfig.Riptide riptide = new GearSetConfig.Riptide();
        riptide.hoverHorizontalFriction = 0.8F;
        riptide.hoverVerticalDamping = 0.5F;
        riptide.shockConeAhead = 2.0F;
        riptide.shockConeBehind = 3.0F;
        riptide.shockConeRadius = 4.0F;
        riptide.stormVisualBoltsPerDash = 3;
        riptide.contactInflate = 0.5F;
        riptide.stallSpeed = 0.1F;
        riptide.launchGraceTicks = 3;
        table.riptide = riptide;

        GearSetConfig.Lightning lightning = new GearSetConfig.Lightning();
        lightning.chainRange = 6.0F;
        lightning.formDurationTicks = 200;
        lightning.dashDistance = 7.0F;
        lightning.dashWidth = 1.5F;
        lightning.dashCooldownTicks = 10;
        lightning.dashTravelTicks = 2;
        lightning.arcLashRange = 12.0F;
        lightning.arcLashDamage = 0.5F;
        lightning.arcLashStrikeDamage = 0.05F;
        lightning.arcLashConeAngle = 30.0F;
        lightning.arcLashBolts = 5;
        lightning.arcLashSpeed = 1.2F;
        lightning.arcLashCooldownTicks = 10;
        lightning.rodRange = 24.0F;
        lightning.rodLockAngle = 60.0F;
        lightning.rodOvershoot = 0.5F;
        lightning.rodDamageMultiplier = 1.5F;
        lightning.rodCooldownTicks = 10;
        lightning.thunderBaseDamage = 5.0F;
        lightning.thunderMaxMultiplier = 30.0F;
        lightning.thunderBaseRadius = 3.0F;
        lightning.thunderMaxRadius = 7.0F;
        lightning.chargeMax = 500.0F;
        lightning.chargePerThrowHit = 5.0F;
        lightning.chargePerArcLash = 5.0F;
        lightning.chargePerDash = 10.0F;
        lightning.chargePerRod = 15.0F;
        lightning.chargeBossMultiplier = 3.0F;
        lightning.formResistanceCapBonus = 0.1F;
        table.lightning = lightning;

        table.sets = new LinkedHashMap<>();
        table.sets.put("woldsvaults:tidebreaker", tidebreaker(riptide));
        table.sets.put("woldsvaults:lightning_fiend", lightningFiend(lightning));
        this.table("gear_sets", table);
    }

    private static GearSetConfig.GearSet tidebreaker(GearSetConfig.Riptide riptide) {
        float damageMore = 1.0F;
        int hoverTicks = 30;
        float boostDamageReduction = 0.75F;
        float bolt = 5.0F;
        float boltChilled = 15.0F;
        GearSetConfig.GearSet set = new GearSetConfig.GearSet();
        set.name = "Tidebreaker";
        set.color = "#1C8F3A";
        set.ancientColor = "#1C8F3A";
        set.pieces = List.of(
                "woldsvaults:tidebreaker_reign", "woldsvaults:tidebreaker_heart", "woldsvaults:tidebreaker_leggings",
                "woldsvaults:tidebreaker_greaves", "woldsvaults:tidebreaker_tideripper", "woldsvaults:tidebreaker_hurricane");
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

    private static GearSetConfig.GearSet lightningFiend(GearSetConfig.Lightning lightning) {
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

    private static String number(float value) {
        return value == Math.rint(value) ? Integer.toString(Math.round(value)) : Float.toString(value);
    }

    private static String percent(float fraction) {
        return number(Math.round(fraction * 1000.0F) / 10.0F) + "%";
    }
}
