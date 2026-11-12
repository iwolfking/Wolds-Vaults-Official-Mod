package xyz.iwolfking.woldsvaults.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Set gear, generated as {@code vault_configs/gear/sets/gear_sets.json}: which uniques form each set, and what each piece-count tier grants.
 * A grant is a gear attribute and value added to the wearer's attribute snapshot while the tier is active. Also
 * holds the shared riptide and lightning tuning the set mechanics read.
 */
public class GearSetConfig extends Config implements WoldsTable {
    @Expose public Riptide riptide;
    @Expose public Lightning lightning;
    @Expose public Map<String, GearSet> sets;

    private transient Map<ResourceLocation, ResourceLocation> setByPiece;
    private transient Map<ResourceLocation, GearSet> setsById;

    public static class Riptide {
        @Expose public float hoverHorizontalFriction;
        @Expose public float hoverVerticalDamping;
        @Expose public float shockConeAhead;
        @Expose public float shockConeBehind;
        @Expose public float shockConeRadius;
        @Expose public int stormVisualBoltsPerDash;
        @Expose public float contactInflate;
        @Expose public float stallSpeed;
        @Expose public int launchGraceTicks;

        /** Blocks the swept box that finds riptide contacts grows by on every side. */
        public float contactInflate() {
            return this.contactInflate;
        }

        /** Horizontal blocks per tick below which a dash that runs into a wall ends; faster dashes slide along it. */
        public float stallSpeed() {
            return this.stallSpeed;
        }

        /** Ticks after a launch during which a wall never ends the dash. */
        public int launchGraceTicks() {
            return this.launchGraceTicks;
        }
    }

    public static class Lightning {
        @Expose public float chainRange;
        @Expose public int formDurationTicks;
        @Expose public float dashDistance;
        @Expose public float dashWidth;
        @Expose public int dashCooldownTicks;
        @Expose public int dashTravelTicks;
        @Expose public float arcLashRange;
        @Expose public float arcLashDamage;
        @Expose public float arcLashStrikeDamage;
        @Expose public float arcLashConeAngle;
        @Expose public int arcLashBolts;
        @Expose public float arcLashSpeed;
        @Expose public int arcLashCooldownTicks;
        @Expose public float rodRange;
        @Expose public float rodLockAngle;
        @Expose public float rodOvershoot;
        @Expose public float rodDamageMultiplier;
        @Expose public int rodCooldownTicks;
        @Expose public float thunderBaseDamage;
        @Expose public float thunderMaxMultiplier;
        @Expose public float thunderBaseRadius;
        @Expose public float thunderMaxRadius;
        @Expose public float chargeMax;
        @Expose public float chargePerThrowHit;
        @Expose public float chargePerArcLash;
        @Expose public float chargePerDash;
        @Expose public float chargePerRod;
        @Expose public float chargeBossMultiplier;
        @Expose public float formResistanceCapBonus;
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

    /** A config with no sets and zeroed tuning: nothing counts as a set piece on it. */
    public static GearSetConfig inert() {
        GearSetConfig config = new GearSetConfig();
        config.riptide = new Riptide();
        config.lightning = new Lightning();
        config.sets = new LinkedHashMap<>();
        return config;
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.riptide == null || this.lightning == null) {
            return "the riptide or lightning tuning is missing";
        }
        if (this.sets == null || this.sets.isEmpty()) {
            return "it defines no sets";
        }
        return null;
    }

    public static Tier tier(int pieces, String text, Grant... grants) {
        Tier tier = new Tier();
        tier.pieces = pieces;
        tier.text = text;
        tier.grants = List.of(grants);
        return tier;
    }

    public static Grant grant(String attribute, JsonElement value) {
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
            WoldLogger.error("gear_sets.json: colour {} is not a hex colour; using #{}.", hex, Integer.toHexString(fallback));
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
                    WoldLogger.error("gear_sets.json: set {} is malformed (needs a valid id, pieces and tiers); skipping it.", id);
                    return;
                }
                byId.put(setId, set);
                for (String piece : set.pieces) {
                    ResourceLocation pieceId = ResourceLocation.tryParse(piece);
                    if (pieceId == null) {
                        WoldLogger.error("gear_sets.json: set {} lists an invalid piece id {}; skipping that piece.", id, piece);
                        continue;
                    }
                    ResourceLocation previous = byPiece.put(pieceId, setId);
                    if (previous != null) {
                        WoldLogger.error("gear_sets.json: {} is listed in both {} and {}; it counts for {} only.", pieceId, previous, setId, setId);
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
        return this.riptide;
    }

    public Lightning getLightning() {
        return this.lightning;
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
                WoldLogger.error("gear_sets.json: set {} tier {} grants {} = {}, which is not a registered attribute with a plain value; skipping it.", setId, tier.pieces, grant.attribute, grant.value);
                continue;
            }
            JsonPrimitive primitive = grant.value.getAsJsonPrimitive();
            Object raw = primitive.isBoolean() ? primitive.getAsBoolean() : primitive.isNumber() ? primitive.getAsNumber() : primitive.getAsString();
            Object value;
            try {
                value = attribute.getType().cast(raw);
            } catch (RuntimeException e) {
                WoldLogger.error("gear_sets.json: set {} tier {} grant {} = {} does not fit the attribute's type; skipping it.", setId, tier.pieces, grant.attribute, grant.value);
                continue;
            }
            resolved.add(new ResolvedGrant(attribute, value));
        }
        return resolved;
    }

    public record ResolvedGrant(VaultGearAttribute<?> attribute, Object value) {
    }
}
