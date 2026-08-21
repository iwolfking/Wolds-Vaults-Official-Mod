package xyz.iwolfking.woldsvaults.gods.charms;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.custom.RandomGodVaultModifierAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectAvoidanceListGearAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.item.gear.VaultUsesHelper;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodPiety;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraAttributeProvider;
import xyz.iwolfking.woldsvaults.items.gear.MythicVaultCharmItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Roll tables, identify logic and piety rescaling for mythic god charms, straight from the design
 * sheet's "God Charm Rolls + Piety" page. Identification draws each affix as a quality step plus a
 * legendary flag and stores that draw in the charm's NBT; the visible modifiers are then
 * materialized from the stored draws at the owner's current piety. Because the draws never change,
 * the charm can be re-materialized losslessly whenever piety moves - including threshold affixes
 * (effect levels, talent levels, stack caps) that the base ratio rescale would corrupt. A stored
 * threshold draw below its piety threshold stays dormant and simply appears once piety reaches it.
 *
 * <p>Piety units for mythics are stored unclamped in {@value #UNITS_TAG} so scaling continues past
 * the base charm's 50-reputation cap; the base {@code godReputation} tag is kept in sync for the
 * base mod's display paths, which clamp on read. Rescaling only runs outside vaults - piety cannot
 * change inside one.
 *
 * <p>Legendary rolls use the base gear system verbatim: at identify, loot-dropped charms (or ones
 * carrying the {@code IS_LEGENDARY} force flag) roll once against
 * {@code VAULT_GEAR_COMMON.getLegendaryModifierChance()} plus the Legendary expertise bonus, and
 * on success exactly ONE prefix or suffix draw - never an implicit - is upgraded: its quality is
 * maxed and its value raised by {@link #LEGENDARY_MULTIPLIER}, carrying the base legendary
 * category for the usual gold display.
 */
public final class MythicCharmRolls {
    public static final String TEMPORAL_TOTAL_TAG = "woldsMythicTemporalTicks";
    public static final String TEMPORAL_REMAINING_TAG = "woldsMythicTemporalRemaining";
    public static final String TEMPORAL_VAULT_TAG = "woldsMythicTemporalVault";
    public static final String UNITS_TAG = "woldsMythicUnits";
    public static final String PINNED_TAG = "woldsMythicPinned";
    public static final String ROLLS_TAG = "woldsMythicRolls";
    public static final float LEGENDARY_MULTIPLIER = 1.5F;
    private static final int TEMPORAL_TICKS_PER_UNIT = 6 * 20;
    private static final int PREFIX_COUNT = 3;
    private static final int SUFFIX_COUNT = 3;
    private static final String OFFENSIVE_ABILITY_GROUP = "OFFENSIVE";

    private enum Kind {
        FLOAT, DOUBLE, INT, LEVEL, EFFECT, ABILITY_GROUP, TALENT, AVOIDANCE, TEMPORAL
    }

    private record Roll(String key, @Nullable VaultGearAttribute<?> attribute, float min, float max, float step,
                        Kind kind, String conflictGroup, String target) {
    }

    private record StoredRoll(VaultGearModifier.AffixType slot, String key, int quality, boolean legendary) {
    }

    private MythicCharmRolls() {
    }

    private static Roll pct(VaultGearAttribute<Float> attribute, float min, float max, float step) {
        return new Roll(attribute.getRegistryName().toString(), attribute, min, max, step, Kind.FLOAT, "", "");
    }

    private static Roll pctDouble(VaultGearAttribute<Double> attribute, float min, float max, float step) {
        return new Roll(attribute.getRegistryName().toString(), attribute, min, max, step, Kind.DOUBLE, "", "");
    }

    private static Roll pctGrouped(VaultGearAttribute<Float> attribute, float min, float max, float step, String group) {
        return new Roll(attribute.getRegistryName().toString(), attribute, min, max, step, Kind.FLOAT, group, "");
    }

    private static Roll flatInt(VaultGearAttribute<Integer> attribute, float min, float max, float step) {
        return new Roll(attribute.getRegistryName().toString(), attribute, min, max, step, Kind.INT, "", "");
    }

    private static Roll level(VaultGearAttribute<Integer> attribute, float perUnit) {
        return new Roll(attribute.getRegistryName().toString(), attribute, perUnit, perUnit, 0.0F, Kind.LEVEL, "", "");
    }

    private static Roll effectLevel(String effectId, float perUnit) {
        return new Roll("effect:" + effectId, iskallia.vault.init.ModGearAttributes.EFFECT,
                perUnit, perUnit, 0.0F, Kind.EFFECT, "", effectId);
    }

    private static Roll abilityGroupLevel(float perUnit) {
        return new Roll("ability_group:" + OFFENSIVE_ABILITY_GROUP, iskallia.vault.init.ModGearAttributes.ABILITY_LEVEL,
                perUnit, perUnit, 0.0F, Kind.ABILITY_GROUP, "", OFFENSIVE_ABILITY_GROUP);
    }

    private static Roll talentLevel(String talentId, float perUnit, String group) {
        return new Roll("talent:" + talentId, iskallia.vault.init.ModGearAttributes.TALENT_LEVEL,
                perUnit, perUnit, 0.0F, Kind.TALENT, group, talentId);
    }

    private static Roll avoidance(float min, float max, float step) {
        return new Roll("effect_avoidance", iskallia.vault.init.ModGearAttributes.EFFECT_LIST_AVOIDANCE,
                min, max, step, Kind.AVOIDANCE, "", "");
    }

    private static Roll temporal() {
        return new Roll("temporal", null, 0.0F, 0.0F, 0.0F, Kind.TEMPORAL, "", "");
    }

    private static Roll multi(VaultGearAttribute<Float> attribute) {
        return pct(attribute, 0.0005F, 0.002F, 0.0001F);
    }

    private static List<Roll> implicits(VaultGod god) {
        return switch (god) {
            case IDONA -> List.of(
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ATTACK_DAMAGE_MULTIPLIER),
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ABILITY_POWER_MULTIPLIER),
                    temporal());
            case VELARA -> List.of(
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ARMOR_MULTIPLIER),
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEALTH_MULTIPLIER),
                    temporal());
            case WENDARR -> List.of(
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.MOVEMENT_SPEED_MULTIPLIER),
                    pct(iskallia.vault.init.ModGearAttributes.FRUIT_EFFECTIVENESS, 0.0075F, 0.015F, 0.0001F),
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.AIR_MOVEMENT_SPEED_MULTIPLIER));
            case TENOS -> List.of(
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ITEM_QUANTITY_MULTIPLIER),
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ITEM_RARITY_MULTIPLIER),
                    temporal());
        };
    }

    private static List<Roll> prefixes(VaultGod god) {
        return switch (god) {
            case IDONA -> List.of(
                    pct(iskallia.vault.init.ModGearAttributes.DAMAGE_INCREASE, 0.01F, 0.02F, 0.0025F),
                    pct(iskallia.vault.init.ModGearAttributes.ABILITY_POWER_PERCENT, 0.01F, 0.02F, 0.0025F),
                    pct(iskallia.vault.init.ModGearAttributes.AREA_OF_EFFECT, 0.005F, 0.01F, 0.0025F),
                    pctDouble(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED_PERCENT, 0.0025F, 0.005F, 0.0025F),
                    pct(iskallia.vault.init.ModGearAttributes.THORNS_DAMAGE_FLAT, 2.0F, 5.0F, 0.5F),
                    pct(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY, 0.04F, 0.08F, 0.01F));
            case VELARA -> List.of(
                    flatInt(iskallia.vault.init.ModGearAttributes.ARMOR, 0.4F, 0.8F, 0.2F),
                    pct(iskallia.vault.init.ModGearAttributes.HEALTH, 0.25F, 0.5F, 0.25F),
                    avoidance(0.001F, 0.002F, 0.00025F),
                    pct(iskallia.vault.init.ModGearAttributes.HEALING_EFFECTIVENESS, 0.01F, 0.015F, 0.0025F),
                    pctGrouped(iskallia.vault.init.ModGearAttributes.BLOCK, 0.002F, 0.004F, 0.001F, "velara_avoid"),
                    pctGrouped(xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT, 0.002F, 0.004F, 0.001F, "velara_avoid"));
            case WENDARR -> List.of(
                    pct(iskallia.vault.init.ModGearAttributes.COOLDOWN_REDUCTION, 0.003F, 0.006F, 0.001F),
                    pct(iskallia.vault.init.ModGearAttributes.EFFECT_DURATION, 0.0075F, 0.015F, 0.0025F),
                    pct(iskallia.vault.init.ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, 0.02F, 0.03F, 0.005F),
                    pct(xyz.iwolfking.woldsvaults.init.ModGearAttributes.FRUIT_SAVER, 0.0002F, 0.0003F, 0.00005F));
            case TENOS -> List.of(
                    pct(iskallia.vault.init.ModGearAttributes.ITEM_QUANTITY, 0.02F, 0.03F, 0.005F),
                    pct(iskallia.vault.init.ModGearAttributes.ITEM_RARITY, 0.02F, 0.03F, 0.005F),
                    pct(iskallia.vault.init.ModGearAttributes.AREA_OF_EFFECT, 0.005F, 0.01F, 0.0025F),
                    pct(iskallia.vault.init.ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, 0.02F, 0.03F, 0.005F),
                    pct(iskallia.vault.init.ModGearAttributes.MANA_ADDITIVE_PERCENTILE, 0.003F, 0.006F, 0.001F),
                    multi(xyz.iwolfking.woldsvaults.init.ModGearAttributes.TRAP_DISARM_MULTIPLIER));
        };
    }

    private static List<Roll> suffixes(VaultGod god) {
        return switch (god) {
            case IDONA -> List.of(
                    pct(iskallia.vault.init.ModGearAttributes.DAMAGE_DWELLER, 0.02F, 0.04F, 0.005F),
                    pct(iskallia.vault.init.ModGearAttributes.DAMAGE_ASSASSIN, 0.02F, 0.04F, 0.005F),
                    pct(iskallia.vault.init.ModGearAttributes.DAMAGE_CHAMPION, 0.02F, 0.04F, 0.005F),
                    pct(iskallia.vault.init.ModGearAttributes.DAMAGE_TANK, 0.02F, 0.04F, 0.005F),
                    pct(xyz.iwolfking.woldsvaults.init.ModGearAttributes.DAMAGE_BOSS, 0.001F, 0.002F, 0.00025F),
                    abilityGroupLevel(1.0F / 50.0F));
            case VELARA -> List.of(
                    effectLevel("minecraft:regeneration", 1.0F / 25.0F),
                    pct(iskallia.vault.init.ModGearAttributes.HEALTH_PERCENTILE, 0.003F, 0.006F, 0.001F),
                    pct(iskallia.vault.init.ModGearAttributes.ARMOR_PERCENTILE, 0.003F, 0.006F, 0.001F),
                    pct(iskallia.vault.init.ModGearAttributes.RESISTANCE, 0.002F, 0.004F, 0.001F),
                    pct(iskallia.vault.init.ModGearAttributes.LEECH, 0.0002F, 0.0004F, 0.0001F));
            case WENDARR -> List.of(
                    talentLevel("Quickening", 1.0F / 25.0F, "wendarr_talent"),
                    talentLevel("Ethereal", 1.0F / 25.0F, "wendarr_talent"),
                    pct(xyz.iwolfking.woldsvaults.init.ModGearAttributes.TEMPORAL_SHARD_DURATION, 0.00025F, 0.0005F, 0.00005F),
                    level(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ADDITIONAL_STACKING_STACKS, 1.0F / 15.0F),
                    pct(iskallia.vault.init.ModGearAttributes.FRUIT_EFFECTIVENESS, 0.0025F, 0.005F, 0.0025F));
            case TENOS -> List.of(
                    effectLevel("minecraft:luck", 1.0F / 50.0F),
                    effectLevel("minecraft:haste", 1.0F / 50.0F),
                    effectLevel("minecraft:speed", 1.0F / 50.0F),
                    pct(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ASPECT_OF_TREASURE_LITE, 0.001F, 0.002F, 0.00025F),
                    pct(xyz.iwolfking.woldsvaults.init.ModGearAttributes.OMEGA_CASCADING, 0.001F, 0.0025F, 0.0005F));
        };
    }

    private static List<ResourceLocation> temporalPool(VaultGod god) {
        return switch (god) {
            case IDONA -> List.of(
                    VaultMod.id("glued_mobs"), VaultMod.id("lunar"), VaultMod.id("overpower"),
                    VaultMod.id("soul_fest"), VaultMod.id("kill_nova"));
            case VELARA -> List.of(
                    VaultMod.id("ultimate_regeneration"), VaultMod.id("rock_solid"), VaultMod.id("kill_totem_wrath"));
            case TENOS -> List.of(
                    VaultMod.id("pylon_hunter"), VaultMod.id("loot_goblin"), VaultMod.id("door_hunter"));
            case WENDARR -> List.of();
        };
    }

    /**
     * Identifies a mythic charm: binds a god (random unless already stamped), draws two or three
     * implicits plus three prefixes and three suffixes from the god's tables, stores the draws in
     * NBT, and materializes them at the player's current piety.
     */
    public static void initialize(ItemStack stack, @Nullable Player player) {
        initialize(stack, player, -1);
    }

    /**
     * As {@link #initialize(ItemStack, Player)}, with an explicit scale-unit override for testing.
     * An overridden charm is pinned: it keeps the override instead of rescaling to real piety.
     */
    public static void initialize(ItemStack stack, @Nullable Player player, int unitsOverride) {
        Random random = new Random();
        VaultGearData data = VaultGearData.read(stack);
        VaultGod god = data.getFirstValue(iskallia.vault.init.ModGearAttributes.CHARM_VAULT_GOD)
                .orElseGet(() -> VaultGod.values()[random.nextInt(VaultGod.values().length)]);
        data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.CHARM_VAULT_GOD, god);
        data.setRarity(VaultGearRarity.valueOf("MYTHIC"));
        data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.GEAR_MODEL,
                xyz.iwolfking.woldsvaults.models.MythicCharms.of(god).getId());
        data.write(stack);
        int units = unitsOverride >= 0 ? unitsOverride
                : (player != null ? GodPiety.scaleUnits(player, god) : 0);
        List<StoredRoll> rolls = new ArrayList<>();
        int implicitCount = 2 + (random.nextBoolean() ? 1 : 0);
        draw(implicits(god), implicitCount, VaultGearModifier.AffixType.IMPLICIT, god, rolls, random);
        draw(prefixes(god), PREFIX_COUNT, VaultGearModifier.AffixType.PREFIX, god, rolls, random);
        draw(suffixes(god), SUFFIX_COUNT, VaultGearModifier.AffixType.SUFFIX, god, rolls, random);
        applyLegendary(data, player, god, rolls, random);
        writeRolls(stack, rolls);
        stack.getOrCreateTag().putBoolean(PINNED_TAG, unitsOverride >= 0);
        stack.getOrCreateTag().putInt(TEMPORAL_TOTAL_TAG, 0);
        stack.getOrCreateTag().putInt(TEMPORAL_REMAINING_TAG, 0);
        materialize(stack, units);
        VaultUsesHelper.setUses(stack, 30);
    }

    /**
     * Re-materializes the charm at the owner's current piety. Runs only for identified, unpinned
     * mythics outside a vault; a no-op when the stored units already match.
     */
    public static void rescale(ItemStack stack, ServerPlayer player) {
        if (!MythicVaultCharmItem.isMythic(stack) || !VaultCharmItem.isIdentified(stack)
                || stack.getOrCreateTag().getBoolean(PINNED_TAG)
                || ServerVaults.get(player.getLevel()).isPresent()) {
            return;
        }
        VaultGod god = VaultCharmItem.getGod(stack).orElse(null);
        if (god == null) {
            return;
        }
        int units = GodPiety.scaleUnits(player, god);
        if (units == storedUnits(stack)) {
            return;
        }
        materialize(stack, units);
    }

    /**
     * The blessing clock refills for every new vault: the first time the worn charm is seen in a
     * vault it has not been in yet, remaining time snaps back to the full total and the vault is
     * remembered, so within one vault the drain-and-bank accounting still applies.
     */
    public static void resetTemporalForVault(ItemStack stack, java.util.UUID vaultId) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getInt(TEMPORAL_TOTAL_TAG) <= 0) {
            return;
        }
        String stored = tag.getString(TEMPORAL_VAULT_TAG);
        if (stored.equals(vaultId.toString())) {
            return;
        }
        tag.putString(TEMPORAL_VAULT_TAG, vaultId.toString());
        tag.putInt(TEMPORAL_REMAINING_TAG, tag.getInt(TEMPORAL_TOTAL_TAG));
    }

    /** The charm's unclamped piety scale units; falls back to the base clamped reputation tag. */
    public static int storedUnits(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(UNITS_TAG, Tag.TAG_ANY_NUMERIC)) {
            return tag.getInt(UNITS_TAG);
        }
        return VaultCharmItem.getGodReputation(stack);
    }

    /**
     * Per-piety roll hints for the given affix group, aligned index-for-index with the group's
     * MATERIALIZED modifiers (dormant draws are skipped exactly as {@link #materialize} skips
     * them). Percent rolls report their per-10-piety gain, flat rolls their per-10-piety amount,
     * threshold rolls the piety each level costs, and the temporal its seconds per ten piety.
     */
    public static List<String> rollHints(ItemStack stack, VaultGearModifier.AffixType type) {
        VaultGod god = VaultCharmItem.getGod(stack).orElse(null);
        if (god == null) {
            return List.of();
        }
        List<String> hints = new ArrayList<>();
        int scale = storedUnits(stack) + 1;
        for (StoredRoll stored : readRolls(stack)) {
            if (stored.slot() != type) {
                continue;
            }
            if (stored.key().startsWith("temporal:")) {
                hints.add(" (+" + (TEMPORAL_TICKS_PER_UNIT / 20) + "s per 10 Piety)");
                continue;
            }
            Roll roll = lookup(god, stored);
            if (roll == null || buildModifier(roll, stored, scale) == null) {
                continue;
            }
            hints.add(hintFor(roll, stored));
        }
        return hints;
    }

    private static String hintFor(Roll roll, StoredRoll stored) {
        float legendaryMultiplier = stored.legendary() ? LEGENDARY_MULTIPLIER : 1.0F;
        java.text.DecimalFormat format = new java.text.DecimalFormat("0.###");
        switch (roll.kind()) {
            case FLOAT: {
                float perUnit = qualityValue(roll, stored.quality()) * legendaryMultiplier;
                if (roll.attribute() == iskallia.vault.init.ModGearAttributes.THORNS_DAMAGE_FLAT) {
                    return " (+" + format.format(perUnit) + " per 10 Piety)";
                }
                return " (+" + format.format(perUnit * 100.0F) + "% per 10 Piety)";
            }
            case DOUBLE:
            case AVOIDANCE: {
                float perUnit = qualityValue(roll, stored.quality()) * legendaryMultiplier;
                return " (+" + format.format(perUnit * 100.0F) + "% per 10 Piety)";
            }
            case INT: {
                float perUnit = qualityValue(roll, stored.quality()) * legendaryMultiplier;
                return " (+" + format.format(perUnit) + " per 10 Piety)";
            }
            case LEVEL:
            case EFFECT:
            case ABILITY_GROUP:
            case TALENT: {
                int perLevelPiety = Math.round(10.0F / (roll.min() * legendaryMultiplier));
                return " (+1 per " + perLevelPiety + " Piety)";
            }
            default:
                return "";
        }
    }

    /** Stored draws that materialize to no modifier at the given units - shown as dormant. */
    public static List<String> dormantRollNames(ItemStack stack, int units) {
        VaultGod god = VaultCharmItem.getGod(stack).orElse(null);
        if (god == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        int scale = units + 1;
        for (StoredRoll stored : readRolls(stack)) {
            if (stored.key().startsWith("temporal:")) {
                continue;
            }
            Roll roll = lookup(god, stored);
            if (roll == null) {
                continue;
            }
            if (buildModifier(roll, stored, scale) == null) {
                names.add(displayName(roll));
            }
        }
        return names;
    }

    private static String displayName(Roll roll) {
        return switch (roll.kind()) {
            case EFFECT -> {
                MobEffect effect = resolveEffect(roll.target());
                yield (effect == null ? roll.target() : effect.getDisplayName().getString()) + " levels";
            }
            case ABILITY_GROUP -> "Offensive Ability levels";
            case TALENT -> roll.target() + " Talent levels";
            default -> roll.attribute() != null ? roll.attribute().getReader().getModifierName() : roll.key();
        };
    }

    private static void draw(List<Roll> table, int count, VaultGearModifier.AffixType slot, VaultGod god,
                             List<StoredRoll> out, Random random) {
        List<Roll> available = new ArrayList<>(table);
        Set<String> usedGroups = new HashSet<>();
        int drawn = 0;
        while (drawn < count && !available.isEmpty()) {
            Roll roll = available.remove(random.nextInt(available.size()));
            if (!roll.conflictGroup().isEmpty() && !usedGroups.add(roll.conflictGroup())) {
                continue;
            }
            if (roll.kind() == Kind.TEMPORAL) {
                List<ResourceLocation> pool = temporalPool(god);
                if (pool.isEmpty()) {
                    continue;
                }
                ResourceLocation modifierId = pool.get(random.nextInt(pool.size()));
                out.add(new StoredRoll(slot, "temporal:" + modifierId, 0, false));
            } else {
                out.add(new StoredRoll(slot, roll.key(), rollQuality(roll, random), false));
            }
            drawn++;
        }
        if (drawn < count) {
            WoldsVaults.LOGGER.warn("Mythic charm drew only {}/{} {} affixes; the {} roll table is too small.",
                    drawn, count, slot.name().toLowerCase(java.util.Locale.ROOT), god.getName());
        }
    }

    private static int rollQuality(Roll roll, Random random) {
        return random.nextInt(maxQuality(roll) + 1);
    }

    private static int maxQuality(Roll roll) {
        if (roll.step() <= 0.0F || roll.max() <= roll.min()) {
            return 0;
        }
        return Math.round((roll.max() - roll.min()) / roll.step());
    }

    /**
     * The base gear legendary system, applied to the stored draws: one roll against the shared
     * config chance (plus the Legendary expertise bonus), loot-gated unless the
     * {@code IS_LEGENDARY} force flag is set, upgrading exactly one prefix or suffix draw to a
     * maxed-quality legendary. Implicits are never eligible, matching base gear.
     */
    private static void applyLegendary(VaultGearData data, @Nullable Player player, VaultGod god,
                                       List<StoredRoll> rolls, Random random) {
        boolean forced = data.get(iskallia.vault.init.ModGearAttributes.IS_LEGENDARY,
                iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger.anyTrue());
        if (!forced) {
            boolean loot = data.getFirstValue(iskallia.vault.init.ModGearAttributes.IS_LOOT).orElse(false);
            float chance = iskallia.vault.init.ModConfigs.VAULT_GEAR_COMMON.getLegendaryModifierChance()
                    + legendaryExpertiseChance(player);
            if (!loot || random.nextFloat() >= chance) {
                return;
            }
        }
        List<Integer> eligible = new ArrayList<>();
        for (int i = 0; i < rolls.size(); i++) {
            VaultGearModifier.AffixType slot = rolls.get(i).slot();
            if (slot == VaultGearModifier.AffixType.PREFIX || slot == VaultGearModifier.AffixType.SUFFIX) {
                eligible.add(i);
            }
        }
        if (eligible.isEmpty()) {
            return;
        }
        int pick = eligible.get(random.nextInt(eligible.size()));
        StoredRoll chosen = rolls.get(pick);
        Roll roll = lookup(god, chosen);
        int quality = roll == null ? chosen.quality() : maxQuality(roll);
        rolls.set(pick, new StoredRoll(chosen.slot(), chosen.key(), quality, true));
    }

    private static float legendaryExpertiseChance(@Nullable Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0.0F;
        }
        float extra = 0.0F;
        iskallia.vault.skill.tree.ExpertiseTree expertises = iskallia.vault.world.data.PlayerExpertisesData
                .get(serverPlayer.getLevel()).getExpertises(serverPlayer);
        for (iskallia.vault.skill.expertise.type.LegendaryExpertise expertise
                : expertises.getAll(iskallia.vault.skill.expertise.type.LegendaryExpertise.class,
                iskallia.vault.skill.base.Skill::isUnlocked)) {
            extra += expertise.getExtraLegendaryChance();
        }
        return extra;
    }

    private static float qualityValue(Roll roll, int quality) {
        return roll.min() + roll.step() * quality;
    }

    private static int thresholdValue(Roll roll, int scale, boolean legendary) {
        float perUnit = roll.min();
        float value = perUnit * scale;
        if (legendary) {
            value *= LEGENDARY_MULTIPLIER;
        }
        return (int) Math.floor(value);
    }

    /**
     * Rebuilds the charm's implicit, prefix and suffix modifiers from the stored draws at the given
     * piety units, and settles the temporal blessing clock: unspent blessing time shifts by exactly
     * the change in total time.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void materialize(ItemStack stack, int units) {
        VaultGearData data = VaultGearData.read(stack);
        VaultGod god = data.getFirstValue(iskallia.vault.init.ModGearAttributes.CHARM_VAULT_GOD).orElse(null);
        if (god == null) {
            return;
        }
        for (VaultGearModifier.AffixType type : new VaultGearModifier.AffixType[]{
                VaultGearModifier.AffixType.IMPLICIT, VaultGearModifier.AffixType.PREFIX, VaultGearModifier.AffixType.SUFFIX}) {
            new ArrayList<>(data.getModifiers(type)).forEach(data::removeModifier);
        }
        int scale = units + 1;
        int newTemporalTotal = 0;
        for (StoredRoll stored : readRolls(stack)) {
            if (stored.key().startsWith("temporal:")) {
                ResourceLocation modifierId = ResourceLocation.parse(stored.key().substring("temporal:".length()));
                int time = TEMPORAL_TICKS_PER_UNIT * scale;
                if (stored.legendary()) {
                    time = Math.round(time * LEGENDARY_MULTIPLIER);
                }
                newTemporalTotal = time;
                VaultGearModifier<RandomGodVaultModifierAttribute> temporal = new VaultGearModifier<>(
                        iskallia.vault.init.ModGearAttributes.RANDOM_VAULT_GOD_MODIFIER,
                        new RandomGodVaultModifierAttribute(modifierId, 1, time, god));
                if (stored.legendary()) {
                    temporal.addCategory(VaultGearModifier.AffixCategory.LEGENDARY);
                }
                data.addModifier(stored.slot(), temporal);
                continue;
            }
            Roll roll = lookup(god, stored);
            if (roll == null) {
                WoldsVaults.LOGGER.error("Mythic charm stored roll {} no longer exists in the {} tables; dropping it.",
                        stored.key(), god.getName());
                continue;
            }
            VaultGearModifier<?> modifier = buildModifier(roll, stored, scale);
            if (modifier == null) {
                continue;
            }
            if (stored.legendary()) {
                modifier.addCategory(VaultGearModifier.AffixCategory.LEGENDARY);
            }
            data.addModifier(stored.slot(), modifier);
        }
        data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.PREFIXES, PREFIX_COUNT);
        data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.SUFFIXES, SUFFIX_COUNT);
        data.write(stack);
        CompoundTag tag = stack.getOrCreateTag();
        int oldTemporalTotal = tag.getInt(TEMPORAL_TOTAL_TAG);
        if (newTemporalTotal != oldTemporalTotal) {
            int remaining = tag.getInt(TEMPORAL_REMAINING_TAG) + (newTemporalTotal - oldTemporalTotal);
            tag.putInt(TEMPORAL_TOTAL_TAG, newTemporalTotal);
            tag.putInt(TEMPORAL_REMAINING_TAG, Math.max(0, Math.min(remaining, newTemporalTotal)));
        }
        tag.putInt(UNITS_TAG, units);
        VaultCharmItem.setGodReputation(stack, units);
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static VaultGearModifier<?> buildModifier(Roll roll, StoredRoll stored, int scale) {
        float legendaryMultiplier = stored.legendary() ? LEGENDARY_MULTIPLIER : 1.0F;
        switch (roll.kind()) {
            case FLOAT: {
                float value = qualityValue(roll, stored.quality()) * scale * legendaryMultiplier;
                return new VaultGearModifier<>((VaultGearAttribute) roll.attribute(), value);
            }
            case DOUBLE: {
                double value = (double) qualityValue(roll, stored.quality()) * scale * legendaryMultiplier;
                return new VaultGearModifier<>((VaultGearAttribute) roll.attribute(), value);
            }
            case INT: {
                int value = Math.round(qualityValue(roll, stored.quality()) * scale * legendaryMultiplier);
                return value > 0 ? new VaultGearModifier<>((VaultGearAttribute) roll.attribute(), value) : null;
            }
            case LEVEL: {
                int value = thresholdValue(roll, scale, stored.legendary());
                return value > 0 ? new VaultGearModifier<>((VaultGearAttribute) roll.attribute(), value) : null;
            }
            case EFFECT: {
                int value = thresholdValue(roll, scale, stored.legendary());
                MobEffect effect = resolveEffect(roll.target());
                if (effect == null) {
                    WoldsVaults.LOGGER.error("Mythic charm effect roll {} could not resolve its mob effect; dropping it.", roll.key());
                    return null;
                }
                return value > 0 ? new VaultGearModifier<>((VaultGearAttribute) roll.attribute(),
                        new EffectGearAttribute(effect, value)) : null;
            }
            case ABILITY_GROUP: {
                int value = thresholdValue(roll, scale, stored.legendary());
                return value > 0 ? new VaultGearModifier<>((VaultGearAttribute) roll.attribute(),
                        new AbilityLevelAttribute(roll.target(), value)) : null;
            }
            case TALENT: {
                int value = thresholdValue(roll, scale, stored.legendary());
                return value > 0 ? new VaultGearModifier<>((VaultGearAttribute) roll.attribute(),
                        new TalentLevelAttribute(roll.target(), value)) : null;
            }
            case AVOIDANCE: {
                float chance = qualityValue(roll, stored.quality()) * scale * legendaryMultiplier;
                List<MobEffect> badEffects = VelaraAttributeProvider.resolveBadEffects();
                if (badEffects.isEmpty()) {
                    return null;
                }
                return new VaultGearModifier<>((VaultGearAttribute) roll.attribute(),
                        new EffectAvoidanceListGearAttribute(badEffects, VelaraAttributeProvider.BAD_EFFECTS_KEY, chance));
            }
            default:
                return null;
        }
    }

    @Nullable
    private static MobEffect resolveEffect(String id) {
        return net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.parse(id));
    }

    @Nullable
    private static Roll lookup(VaultGod god, StoredRoll stored) {
        List<Roll> table = switch (stored.slot()) {
            case IMPLICIT -> implicits(god);
            case PREFIX -> prefixes(god);
            case SUFFIX -> suffixes(god);
        };
        for (Roll roll : table) {
            if (roll.key().equals(stored.key())) {
                return roll;
            }
        }
        return null;
    }

    private static void writeRolls(ItemStack stack, List<StoredRoll> rolls) {
        ListTag list = new ListTag();
        for (StoredRoll roll : rolls) {
            CompoundTag entry = new CompoundTag();
            entry.putByte("s", (byte) roll.slot().ordinal());
            entry.putString("k", roll.key());
            entry.putInt("q", roll.quality());
            entry.putBoolean("l", roll.legendary());
            list.add(entry);
        }
        stack.getOrCreateTag().put(ROLLS_TAG, list);
    }

    private static List<StoredRoll> readRolls(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ROLLS_TAG, Tag.TAG_LIST)) {
            return List.of();
        }
        List<StoredRoll> rolls = new ArrayList<>();
        ListTag list = tag.getList(ROLLS_TAG, Tag.TAG_COMPOUND);
        VaultGearModifier.AffixType[] slots = VaultGearModifier.AffixType.values();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("s");
            if (slot < 0 || slot >= slots.length) {
                continue;
            }
            rolls.add(new StoredRoll(slots[slot], entry.getString("k"), entry.getInt("q"), entry.getBoolean("l")));
        }
        return rolls;
    }
}
