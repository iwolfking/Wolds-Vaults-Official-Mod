package xyz.iwolfking.woldsvaults.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Set gear (config/the_vault/gear_sets.json): which uniques form each set, and what each piece-count tier grants.
 * A grant is a gear attribute and value added to the wearer's attribute snapshot while the tier is active. Also
 * holds the shared riptide and lightning tuning the set mechanics read.
 */
public class GearSetConfig extends PackAuthoredConfig {
    @Expose private Map<String, String> documentation;
    @Expose private Riptide riptide;
    @Expose private Lightning lightning;
    @Expose private Map<String, GearSet> sets;

    private transient boolean lightningChecked;
    private transient Map<ResourceLocation, ResourceLocation> setByPiece;
    private transient Map<ResourceLocation, GearSet> setsById;

    public static class Riptide {
        @Expose public float hoverHorizontalFriction;
        @Expose public float hoverVerticalDamping;
        @Expose public float shockConeAhead;
        @Expose public float shockConeBehind;
        @Expose public float shockConeRadius;
        @Expose public int stormVisualBoltsPerDash;
        @Expose public Float contactInflate;
        @Expose public Float stallSpeed;
        @Expose public Integer launchGraceTicks;

        /** Blocks the swept box that finds riptide contacts grows by on every side. */
        public float contactInflate() {
            if (this.contactInflate == null) {
                warnMissing("contactInflate", DEFAULT_CONTACT_INFLATE);
                this.contactInflate = DEFAULT_CONTACT_INFLATE;
            }
            return this.contactInflate;
        }

        /** Horizontal blocks per tick below which a dash that runs into a wall ends; faster dashes slide along it. */
        public float stallSpeed() {
            if (this.stallSpeed == null) {
                warnMissing("stallSpeed", DEFAULT_STALL_SPEED);
                this.stallSpeed = DEFAULT_STALL_SPEED;
            }
            return this.stallSpeed;
        }

        /** Ticks after a launch during which a wall never ends the dash. */
        public int launchGraceTicks() {
            if (this.launchGraceTicks == null) {
                warnMissing("launchGraceTicks", DEFAULT_LAUNCH_GRACE_TICKS);
                this.launchGraceTicks = DEFAULT_LAUNCH_GRACE_TICKS;
            }
            return this.launchGraceTicks;
        }

        private static void warnMissing(String key, Object value) {
            WoldsVaults.LOGGER.warn("gear_sets.json riptide section has no {}; using the built-in {}.", key, value);
        }
    }

    private static final float DEFAULT_CONTACT_INFLATE = 0.5F;
    private static final float DEFAULT_STALL_SPEED = 0.1F;
    private static final int DEFAULT_LAUNCH_GRACE_TICKS = 3;

    public static class Lightning {
        @Expose public Float chainRange;
        @Expose public Integer formDurationTicks;
        @Expose public Float dashDistance;
        @Expose public Float dashWidth;
        @Expose public Integer dashCooldownTicks;
        @Expose public Integer dashTravelTicks;
        @Expose public Float arcLashRange;
        @Expose public Float arcLashDamage;
        @Expose public Float arcLashStrikeDamage;
        @Expose public Float arcLashConeAngle;
        @Expose public Integer arcLashBolts;
        @Expose public Float arcLashSpeed;
        @Expose public Integer arcLashCooldownTicks;
        @Expose public Float rodRange;
        @Expose public Float rodLockAngle;
        @Expose public Float rodOvershoot;
        @Expose public Float rodDamageMultiplier;
        @Expose public Integer rodCooldownTicks;
        @Expose public Float thunderBaseDamage;
        @Expose public Float thunderMaxMultiplier;
        @Expose public Float thunderBaseRadius;
        @Expose public Float thunderMaxRadius;
        @Expose public Float chargeMax;
        @Expose public Float chargePerThrowHit;
        @Expose public Float chargePerArcLash;
        @Expose public Float chargePerDash;
        @Expose public Float chargePerRod;
        @Expose public Float chargeBossMultiplier;
        @Expose public Float formResistanceCapBonus;

        /** Fills every key the pack file left out from the built-in tuning, logging each one. */
        private void fillMissing(Lightning defaults) {
            for (Field field : Lightning.class.getFields()) {
                try {
                    if (field.get(this) == null) {
                        Object value = field.get(defaults);
                        WoldsVaults.LOGGER.warn("gear_sets.json lightning section has no {}; using the built-in {}.", field.getName(), value);
                        field.set(this, value);
                    }
                } catch (IllegalAccessException e) {
                    WoldsVaults.LOGGER.error("Could not fill the lightning tuning key {}.", field.getName(), e);
                }
            }
        }
    }

    public static class GearSet {
        @Expose public String name;
        @Expose public String color;
        @Expose public String ancientColor;
        @Expose public List<String> pieces;
        @Expose public List<Tier> tiers;

        public int colorRgb() {
            return parseColor(this.color, 0x1C8F3A);
        }

        public int ancientColorRgb() {
            return parseColor(this.ancientColor == null ? this.color : this.ancientColor, this.colorRgb());
        }
    }

    public static class Tier {
        @Expose public int pieces;
        @Expose public String text;
        @Expose public List<Grant> grants;
    }

    public static class Grant {
        @Expose public String attribute;
        @Expose public JsonElement value;
    }

    @Override
    public String getName() {
        return "gear_sets";
    }

    @Override
    protected void reset() {
        this.documentation = new LinkedHashMap<>();
        this.documentation.put("sets", "Set id -> name, colours, pieces (unique ids from unique_gear) and tiers. A piece counts while it sits in the player's attribute snapshot (worn in its intended slot, not broken, not above the player's level). Each unique counts once.");
        this.documentation.put("tiers", "pieces = pieces needed; text = the tooltip line; grants = gear attributes added to the wearer's snapshot while the tier is active (values in the attribute's own units, 1.0 = 100%)");
        this.documentation.put("riptide", "Shared riptide tuning: hover friction is the extra per-tick horizontal speed multiplier during the hover, vertical damping the per-tick vertical one; the shock cone spans shockConeAhead blocks ahead of the player to shockConeBehind blocks behind, widening to shockConeRadius; contactInflate grows the box that finds dash contacts by that many blocks per side; a dash that hits a wall ends only once its horizontal speed is below stallSpeed blocks per tick, and never in its first launchGraceTicks ticks");
        this.documentation.put("lightning", "Shared lightning tuning: chainRange = how far a channeling strike jumps to the next enemy; throwing a trident with the lightning dash grant turns the thrower into lightning for formDurationTicks, during which each tap of a movement key sends them dashDistance blocks that way as a bolt of lightning, taking dashTravelTicks, striking enemies within dashWidth of the path, then waiting dashCooldownTicks after reappearing before the next dash. Left click is Arc Lash: arcLashBolts spinning lightning bolts thrown out evenly across arcLashConeAngle degrees either side of the look, flying arcLashSpeed blocks a tick for up to arcLashRange, striking each enemy they pass through (once per throw) for arcLashDamage of the thrower's attack damage at the throw and calling a channeling strike (vanilla lightning) down on it for arcLashStrikeDamage of a channeling strike's damage, once per arcLashCooldownTicks. Right click is Lightning Rod: marks the enemy aimed at within rodRange (once per rodCooldownTicks); a dash within rodLockAngle degrees of the target locks on, running through it and on for rodOvershoot of a dash, and strikes it for rodDamageMultiplier of the dash damage. Middle click is Thunderstrike: ends the form with a giant bolt on the player striking every enemy within a radius growing from thunderBaseRadius to thunderMaxRadius (across) with charge, for thunderBaseDamage of the thrower's attack damage times 1 + (thunderMaxMultiplier - 1) x (charge / chargeMax)^2. Charge builds while lightning: chargePerThrowHit per enemy the throw hits, chargePerArcLash per Arc Lash contact, chargePerDash per enemy a dash strikes (chargePerRod on the Lightning Rod's mark), times chargeBossMultiplier on a boss (the the_vault:bosses group), up to chargeMax. The form raises the resistance cap by formResistanceCapBonus (never past 95%)");

        this.riptide = new Riptide();
        this.riptide.hoverHorizontalFriction = 0.8F;
        this.riptide.hoverVerticalDamping = 0.5F;
        this.riptide.shockConeAhead = 2.0F;
        this.riptide.shockConeBehind = 3.0F;
        this.riptide.shockConeRadius = 4.0F;
        this.riptide.stormVisualBoltsPerDash = 3;
        this.riptide.contactInflate = DEFAULT_CONTACT_INFLATE;
        this.riptide.stallSpeed = DEFAULT_STALL_SPEED;
        this.riptide.launchGraceTicks = DEFAULT_LAUNCH_GRACE_TICKS;

        this.lightning = new Lightning();
        this.lightning.chainRange = 6.0F;
        this.lightning.formDurationTicks = 200;
        this.lightning.dashDistance = 7.0F;
        this.lightning.dashWidth = 1.5F;
        this.lightning.dashCooldownTicks = 10;
        this.lightning.dashTravelTicks = 2;
        this.lightning.arcLashRange = 12.0F;
        this.lightning.arcLashDamage = 0.5F;
        this.lightning.arcLashStrikeDamage = 0.05F;
        this.lightning.arcLashConeAngle = 30.0F;
        this.lightning.arcLashBolts = 5;
        this.lightning.arcLashSpeed = 1.2F;
        this.lightning.arcLashCooldownTicks = 10;
        this.lightning.rodRange = 24.0F;
        this.lightning.rodLockAngle = 60.0F;
        this.lightning.rodOvershoot = 0.5F;
        this.lightning.rodDamageMultiplier = 1.5F;
        this.lightning.rodCooldownTicks = 10;
        this.lightning.thunderBaseDamage = 5.0F;
        this.lightning.thunderMaxMultiplier = 30.0F;
        this.lightning.thunderBaseRadius = 3.0F;
        this.lightning.thunderMaxRadius = 7.0F;
        this.lightning.chargeMax = 500.0F;
        this.lightning.chargePerThrowHit = 5.0F;
        this.lightning.chargePerArcLash = 5.0F;
        this.lightning.chargePerDash = 10.0F;
        this.lightning.chargePerRod = 15.0F;
        this.lightning.chargeBossMultiplier = 3.0F;
        this.lightning.formResistanceCapBonus = 0.1F;

        GearSet tidebreaker = new GearSet();
        tidebreaker.name = "Tidebreaker";
        tidebreaker.color = "#1C8F3A";
        tidebreaker.ancientColor = "#1C8F3A";
        tidebreaker.pieces = List.of(
                "woldsvaults:tidebreaker_reign", "woldsvaults:tidebreaker_heart", "woldsvaults:tidebreaker_leggings",
                "woldsvaults:tidebreaker_greaves", "woldsvaults:tidebreaker_tideripper", "woldsvaults:tidebreaker_hurricane");
        tidebreaker.tiers = List.of(
                tier(2, "Riptide pierces through enemies and deals 100% more damage.",
                        grant("woldsvaults:riptide_pierce", new JsonPrimitive(true)),
                        grant("woldsvaults:riptide_damage_more", new JsonPrimitive(1.0F))),
                tier(4, "Riptide releases a shock cone, striking enemies from 2 blocks ahead of you to a 4-block radius 3 blocks behind you.",
                        grant("woldsvaults:riptide_shock_cone", new JsonPrimitive(1.0F))),
                tier(6, "While dashing, a storm shrouds you: you hover briefly after each dash, take 75% less damage, and lightning strikes every enemy your dash or its shock cone hits for 5x damage (15x if chilled).",
                        grant("woldsvaults:riptide_hover", new JsonPrimitive(30)),
                        grant("woldsvaults:riptide_boost_damage_reduction", new JsonPrimitive(0.75F)),
                        grant("woldsvaults:riptide_bolt", new JsonPrimitive(5.0F)),
                        grant("woldsvaults:riptide_bolt_chilled", new JsonPrimitive(15.0F))));

        GearSet lightningFiend = new GearSet();
        lightningFiend.name = "Lightning Fiend";
        lightningFiend.color = "#6FCFFF";
        lightningFiend.ancientColor = "#6FCFFF";
        lightningFiend.pieces = List.of(
                "woldsvaults:lightning_fiend_crown", "woldsvaults:lightning_fiend_mantle", "woldsvaults:lightning_fiend_legguards",
                "woldsvaults:lightning_fiend_talons", "woldsvaults:lightning_fiend_orb", "woldsvaults:lightning_fiend_pure_lightning");
        lightningFiend.tiers = List.of(
                tier(2, "Channeling strikes deal 100% more damage and chain to 2 more enemies within 3 blocks.",
                        grant("woldsvaults:channeling_damage_more", new JsonPrimitive(1.0F)),
                        grant("woldsvaults:channeling_chain", new JsonPrimitive(2))),
                tier(4, "Your thrown trident pierces up to 3 enemies and calls a channeling strike down on each one it passes through.",
                        grant("woldsvaults:thrown_trident_pierce", new JsonPrimitive(3))),
                tier(6, "Throwing your trident turns you into lightning for 10 seconds. During lightning form, using movement keys causes you to dash through enemies for 10% channeling damage, and left clicking releases a fan of arc lightning outwards. You can also mark a single enemy with a lightning rod for increased damage by right clicking them, and middle click (pick block) releases a devastating thunderbolt attack centered on you, dealing more damage the more lightning strikes you land while in lightning form.",
                        grant("woldsvaults:lightning_dash", new JsonPrimitive(0.1F))));

        this.sets = new LinkedHashMap<>();
        this.sets.put("woldsvaults:tidebreaker", tidebreaker);
        this.sets.put("woldsvaults:lightning_fiend", lightningFiend);
    }

    private static Tier tier(int pieces, String text, Grant... grants) {
        Tier tier = new Tier();
        tier.pieces = pieces;
        tier.text = text;
        tier.grants = List.of(grants);
        return tier;
    }

    private static Grant grant(String attribute, JsonElement value) {
        Grant grant = new Grant();
        grant.attribute = attribute;
        grant.value = value;
        return grant;
    }

    private static int parseColor(@Nullable String hex, int fallback) {
        if (hex == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(hex.startsWith("#") ? hex.substring(1) : hex, 16);
        } catch (NumberFormatException e) {
            WoldsVaults.LOGGER.error("gear_sets.json: colour {} is not a hex colour; using #{}.", hex, Integer.toHexString(fallback));
            return fallback;
        }
    }

    private void index() {
        if (this.setByPiece != null) {
            return;
        }
        Map<ResourceLocation, ResourceLocation> byPiece = new LinkedHashMap<>();
        Map<ResourceLocation, GearSet> byId = new LinkedHashMap<>();
        if (this.sets != null) {
            this.sets.forEach((id, set) -> {
                ResourceLocation setId = ResourceLocation.tryParse(id);
                if (setId == null || set == null || set.pieces == null || set.tiers == null) {
                    WoldsVaults.LOGGER.error("gear_sets.json: set {} is malformed (needs a valid id, pieces and tiers); skipping it.", id);
                    return;
                }
                byId.put(setId, set);
                for (String piece : set.pieces) {
                    ResourceLocation pieceId = ResourceLocation.tryParse(piece);
                    if (pieceId == null) {
                        WoldsVaults.LOGGER.error("gear_sets.json: set {} lists an invalid piece id {}; skipping that piece.", id, piece);
                        continue;
                    }
                    ResourceLocation previous = byPiece.put(pieceId, setId);
                    if (previous != null) {
                        WoldsVaults.LOGGER.error("gear_sets.json: {} is listed in both {} and {}; it counts for {} only.", pieceId, previous, setId, setId);
                    }
                }
            });
        }
        this.setByPiece = byPiece;
        this.setsById = byId;
    }

    public Optional<ResourceLocation> getSetOf(ResourceLocation uniqueKey) {
        this.index();
        return Optional.ofNullable(this.setByPiece.get(uniqueKey));
    }

    public Optional<GearSet> getSet(ResourceLocation setId) {
        this.index();
        return Optional.ofNullable(this.setsById.get(setId));
    }

    public Map<ResourceLocation, GearSet> getSets() {
        this.index();
        return Collections.unmodifiableMap(this.setsById);
    }

    public Riptide getRiptide() {
        if (this.riptide == null) {
            WoldsVaults.LOGGER.error("gear_sets.json has no riptide section; using the built-in tuning.");
            GearSetConfig defaults = new GearSetConfig();
            defaults.reset();
            this.riptide = defaults.riptide;
        }
        return this.riptide;
    }

    public Lightning getLightning() {
        if (this.lightning == null) {
            WoldsVaults.LOGGER.error("gear_sets.json has no lightning section; using the built-in tuning.");
            this.lightning = defaultLightning();
            this.lightningChecked = true;
        } else if (!this.lightningChecked) {
            this.lightning.fillMissing(defaultLightning());
            this.lightningChecked = true;
        }
        return this.lightning;
    }

    private static Lightning defaultLightning() {
        GearSetConfig defaults = new GearSetConfig();
        defaults.reset();
        return defaults.lightning;
    }

    /** The grants of one tier resolved to registered attributes; unknown attributes are logged and skipped. */
    public static List<ResolvedGrant> resolve(ResourceLocation setId, Tier tier) {
        List<ResolvedGrant> resolved = new ArrayList<>();
        if (tier.grants == null) {
            return resolved;
        }
        for (Grant grant : tier.grants) {
            ResourceLocation attributeId = grant.attribute == null ? null : ResourceLocation.tryParse(grant.attribute);
            VaultGearAttribute<?> attribute = attributeId == null ? null : VaultGearAttributeRegistry.getAttribute(attributeId);
            if (attribute == null || grant.value == null || !grant.value.isJsonPrimitive()) {
                WoldsVaults.LOGGER.error("gear_sets.json: set {} tier {} grants {} = {}, which is not a registered attribute with a plain value; skipping it.", setId, tier.pieces, grant.attribute, grant.value);
                continue;
            }
            JsonPrimitive primitive = grant.value.getAsJsonPrimitive();
            Object raw = primitive.isBoolean() ? primitive.getAsBoolean() : primitive.isNumber() ? primitive.getAsNumber() : primitive.getAsString();
            Object value;
            try {
                value = attribute.getType().cast(raw);
            } catch (RuntimeException e) {
                WoldsVaults.LOGGER.error("gear_sets.json: set {} tier {} grant {} = {} does not fit the attribute's type; skipping it.", setId, tier.pieces, grant.attribute, grant.value);
                continue;
            }
            resolved.add(new ResolvedGrant(attribute, value));
        }
        return resolved;
    }

    public record ResolvedGrant(VaultGearAttribute<?> attribute, Object value) {
    }
}
