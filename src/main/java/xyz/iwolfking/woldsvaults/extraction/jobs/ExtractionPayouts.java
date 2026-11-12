package xyz.iwolfking.woldsvaults.extraction.jobs;

import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.BoosterPackItem;
import iskallia.vault.util.LootInitialization;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Turns a finished job into item stacks. Every count and weight comes from the job's {@code payout}
 * block; unidentified vault items (maps, charms, uniques) are seeded with the_vault's loot NBT and
 * initialised the way chest loot is, so they identify normally in the player's hands.
 */
public final class ExtractionPayouts {
    private static final ResourceLocation MAP_ITEM = new ResourceLocation("the_vault", "map");
    private static final ResourceLocation OMEGA_CHARM = new ResourceLocation("the_vault", "vault_god_charm");
    private static final ResourceLocation MYTHIC_CHARM = new ResourceLocation("woldsvaults", "mythic_vault_charm");
    private static final String ROLL_TYPE_TAG = "the_vault:gear_roll_type";
    private static final String MAP_TIER_TAG = "the_vault:map_tier";

    private ExtractionPayouts() {
    }

    public static NonNullList<ItemStack> roll(String jobId, ExtractionObjectiveConfig.JobEntry job, ExtractorEntity extractor, Vault vault, BlockPos pos, Random random) {
        NonNullList<ItemStack> payout = NonNullList.create();
        ExtractionObjectiveConfig.PayoutEntry spec = job.payout;
        int totalPower = extractor.getPower();
        int extraPower = Math.max(0, totalPower - job.minPower);
        float quant = extractor.getDrillQuantity();
        float itemRarity = extractor.getDrillRarity();
        int count = rollCount(spec, job, totalPower, quant, random);
        String type = spec.type == null ? "item" : spec.type.toLowerCase(Locale.ROOT);
        switch (type) {
            case "item" -> payItems(payout, spec, count, vault, pos);
            case "maps" -> payMaps(payout, spec, count, totalPower, extraPower, itemRarity, vault, pos, random);
            case "charms" -> payCharms(payout, spec, count, extraPower, itemRarity, vault, pos, random);
            case "uniques" -> payUniques(payout, spec, count, extraPower, itemRarity, vault, pos, random);
            case "cards" -> WoldLogger.info("Extraction job '{}' upgraded {} greed card(s) in place.", jobId, extractor.upgradeCards());
            case "fluid" -> payFluid(jobId, spec, count, extractor);
            case "crystals" -> payout.addAll(AncientCrystals.roll(extractor, vault, random));
            case "seal" -> WoldLogger.error(
                    "Extraction job '{}' uses payout type '{}', which is not implemented yet; the site completed with no payout.", jobId, type);
            default -> WoldLogger.error("Extraction job '{}' has unknown payout type '{}'; the site completed with no payout.", jobId, type);
        }
        int total = 0;
        for (ItemStack stack : payout) {
            total += stack.getCount();
        }
        WoldLogger.info("Extraction job '{}' at power {} (quant {}, rarity {}) paid out {} items in {} stacks.", jobId, totalPower, (int) quant, (int) itemRarity, total, payout.size());
        return payout;
    }

    static int rollCount(ExtractionObjectiveConfig.PayoutEntry spec, ExtractionObjectiveConfig.JobEntry job, int totalPower, float quant, Random random) {
        double base = spec.countMin + (spec.countMax - spec.countMin) * random.nextDouble();
        double value = base * countFactor(spec, job, totalPower, quant);
        int whole = (int) Math.floor(value);
        return whole + (random.nextDouble() < value - whole ? 1 : 0);
    }

    /** The deterministic part of the count roll: the power and quantity scaling applied to the uniform base count. */
    public static double countFactor(ExtractionObjectiveConfig.PayoutEntry spec, ExtractionObjectiveConfig.JobEntry job, int totalPower, float quant) {
        double reference = spec.powerReference > 0.0F ? spec.powerReference : Math.max(1, job.minPower);
        double powerRatio = Math.max(totalPower, 1) / reference;
        double powerFactor = switch (spec.powerScaling == null ? "linear" : spec.powerScaling.toLowerCase(Locale.ROOT)) {
            case "linear" -> powerRatio;
            case "sqrt" -> Math.sqrt(powerRatio);
            case "cbrt" -> Math.cbrt(powerRatio);
            default -> 1.0D;
        };
        double quantBase = 1.0D + Math.max(0.0F, quant) / 100.0D;
        double quantFactor = switch (spec.quantScaling == null ? "sqrt" : spec.quantScaling.toLowerCase(Locale.ROOT)) {
            case "sqrt" -> Math.sqrt(quantBase);
            case "cbrt" -> Math.cbrt(quantBase);
            case "linear" -> quantBase;
            default -> 1.0D;
        };
        return powerFactor * quantFactor;
    }

    /** Resolves the payout's rarity table for this extractor: base weights, extra-power shifts, item-rarity damping. */
    public static Map<String, Double> rarityWeights(ExtractionObjectiveConfig.PayoutEntry spec, int extraPower, float itemRarity) {
        Map<String, Double> weights = new LinkedHashMap<>();
        spec.rarityWeights.forEach((key, weight) -> weights.put(key, (double) weight));
        spec.rarityPerExtraPower.forEach((key, perPower) -> weights.merge(key, perPower * (double) extraPower, Double::sum));
        spec.rarityPerExtraPowerSquared.forEach((key, perPower) -> weights.merge(key, perPower * (double) extraPower * extraPower, Double::sum));
        if (spec.itemRarityDampedKey != null && !spec.itemRarityDampedKey.isEmpty() && weights.containsKey(spec.itemRarityDampedKey) && spec.itemRarityDampK > 0.0F) {
            double damp = spec.itemRarityDampK / (spec.itemRarityDampK + Math.max(0.0F, itemRarity));
            weights.computeIfPresent(spec.itemRarityDampedKey, (key, weight) -> weight * damp);
        }
        return weights;
    }

    static String pickWeighted(Map<String, Double> weights, Random random, String fallback) {
        double total = 0.0D;
        for (double weight : weights.values()) {
            total += Math.max(0.0D, weight);
        }
        if (total <= 0.0D) {
            return fallback;
        }
        double pick = random.nextDouble() * total;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            pick -= Math.max(0.0D, entry.getValue());
            if (pick < 0.0D) {
                return entry.getKey();
            }
        }
        return fallback;
    }

    private static void payItems(NonNullList<ItemStack> payout, ExtractionObjectiveConfig.PayoutEntry spec, int count, Vault vault, BlockPos pos) {
        Item item = spec.item == null || spec.item.isEmpty() ? null : ForgeRegistries.ITEMS.getValue(new ResourceLocation(spec.item));
        if (item == null) {
            WoldLogger.error("Extraction payout names unknown item '{}'; nothing was paid.", spec.item);
            return;
        }
        int maxStack = new ItemStack(item).getMaxStackSize();
        boolean tagged = spec.itemNbtId != null && !spec.itemNbtId.isEmpty();
        if (tagged) {
            maxStack = 1;
        }
        int remaining = count;
        while (remaining > 0) {
            int size = Math.min(remaining, maxStack);
            ItemStack stack = new ItemStack(item, size);
            if (tagged) {
                if (item instanceof BoosterPackItem) {
                    BoosterPackItem.setId(stack, spec.itemNbtId);
                } else {
                    stack.getOrCreateTag().putString("id", spec.itemNbtId);
                }
            }
            payout.add(stack);
            remaining -= size;
        }
    }

    /** Fluid jobs keep their payout on the rig (in mB); it is collected by right-clicking the rig with a fluid container. */
    private static void payFluid(String jobId, ExtractionObjectiveConfig.PayoutEntry spec, int amount, ExtractorEntity extractor) {
        ResourceLocation id = spec.item == null ? null : ResourceLocation.tryParse(spec.item);
        Fluid fluid = id == null ? null : ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null || fluid == Fluids.EMPTY) {
            WoldLogger.error("Extraction job '{}' names unknown fluid '{}'; the site completed with no fluid.", jobId, spec.item);
            return;
        }
        extractor.storeFluid(new FluidStack(fluid, Math.max(0, amount)));
    }

    private static void payMaps(NonNullList<ItemStack> payout, ExtractionObjectiveConfig.PayoutEntry spec, int count, int totalPower, int extraPower,
                                float itemRarity, Vault vault, BlockPos pos, Random random) {
        Item map = ForgeRegistries.ITEMS.getValue(MAP_ITEM);
        if (map == null) {
            WoldLogger.error("Vault map item {} is not registered; the maps job paid nothing.", MAP_ITEM);
            return;
        }
        int tierMin = 1;
        int tierMax = 3;
        for (ExtractionObjectiveConfig.TierWindow window : spec.tierWindows) {
            if (totalPower >= window.minPower) {
                tierMin = window.tierMin;
                tierMax = window.tierMax;
            }
        }
        Map<String, Double> weights = rarityWeights(spec, extraPower, itemRarity);
        for (int i = 0; i < count; i++) {
            ItemStack stack = new ItemStack(map);
            int tier = tierMin + random.nextInt(Math.max(1, tierMax - tierMin + 1));
            stack.getOrCreateTag().putInt(MAP_TIER_TAG, Math.max(0, tier - 1));
            stack.getOrCreateTag().putString(ROLL_TYPE_TAG, capitalize(pickWeighted(weights, random, "rare")));
            payout.add(LootInitialization.initializeVaultLoot(stack, vault, pos));
        }
    }

    private static void payCharms(NonNullList<ItemStack> payout, ExtractionObjectiveConfig.PayoutEntry spec, int count, int extraPower, float itemRarity,
                                  Vault vault, BlockPos pos, Random random) {
        Map<String, Double> weights = rarityWeights(spec, extraPower, itemRarity);
        for (int i = 0; i < count; i++) {
            boolean mythic = "mythic".equals(pickWeighted(weights, random, "omega"));
            Item charm = ForgeRegistries.ITEMS.getValue(mythic ? MYTHIC_CHARM : OMEGA_CHARM);
            if (charm == null) {
                WoldLogger.error("Charm item {} is not registered; one charm payout was skipped.", mythic ? MYTHIC_CHARM : OMEGA_CHARM);
                continue;
            }
            ItemStack stack = new ItemStack(charm);
            stack.getOrCreateTag().putString(ROLL_TYPE_TAG, mythic ? "Mythic" : "Omega+");
            payout.add(LootInitialization.initializeVaultLoot(stack, vault, pos));
        }
    }

    private static void payUniques(NonNullList<ItemStack> payout, ExtractionObjectiveConfig.PayoutEntry spec, int count, int extraPower, float itemRarity,
                                   Vault vault, BlockPos pos, Random random) {
        Map<String, Double> weights = rarityWeights(spec, extraPower, itemRarity);
        List<ResourceLocation> keys = new ArrayList<>(ModConfigs.UNIQUE_GEAR.getRegistry().keySet());
        keys.removeIf(GearSetHelper::isSetPiece);
        for (int i = 0; i < count; i++) {
            if ("ancient".equals(pickWeighted(weights, random, "unique"))) {
                ItemStack ancient = AncientUniqueHelper.rollUnidentifiedAncient(random, false);
                if (!ancient.isEmpty()) {
                    payout.add(LootInitialization.initializeVaultLoot(ancient, vault, pos));
                }
                continue;
            }
            if (keys.isEmpty()) {
                WoldLogger.error("The unique gear registry is empty; one unique payout was skipped.");
                continue;
            }
            ResourceLocation key = keys.get(random.nextInt(keys.size()));
            ItemStack unique = createUnidentifiedUnique(key, vault, pos);
            if (!unique.isEmpty()) {
                payout.add(unique);
            }
        }
    }

    /** An unidentified piece of gear locked to one unique; it rolls into that unique when identified. */
    static ItemStack createUnidentifiedUnique(ResourceLocation key, Vault vault, BlockPos pos) {
        UniqueGearConfig.Entry entry = ModConfigs.UNIQUE_GEAR.getEntry(key).orElse(null);
        if (entry == null) {
            WoldLogger.error("Unique {} has no registry entry; skipped.", key);
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(entry.getItem());
        if (stack.isEmpty()) {
            WoldLogger.error("Unique {} resolved to an empty base item; skipped.", key);
            return ItemStack.EMPTY;
        }
        VaultGearData data = VaultGearData.read(stack);
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, "Unique");
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_UNIQUE_POOL, ModConfigs.UNIQUE_GEAR.findPoolForUnique(key).orElse(UniqueGearConfig.DEFAULT_POOL));
        data.createOrReplaceAttributeValue(ModGearAttributes.UNIQUE_ITEM_KEY, key);
        data.write(stack);
        return LootInitialization.initializeVaultLoot(stack, vault, pos);
    }

    private static String capitalize(String value) {
        return value.isEmpty() ? value : value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
