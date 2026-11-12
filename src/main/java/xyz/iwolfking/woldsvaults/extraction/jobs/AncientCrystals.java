package xyz.iwolfking.woldsvaults.extraction.jobs;

import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.VaultModifierPoolsConfig;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.modifiers.DefaultCrystalModifiers;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Builds the Ancient Crystals payout: pre-filled level-locked vault crystals carrying strong and random modifier
 * rolls, a raised capacity, a pre-rolled objective and an ancient sigil, flagged so maps and sigils are refused.
 */
public final class AncientCrystals {
    private static final Set<String> REPORTED = new HashSet<>();

    private AncientCrystals() {
    }

    private static ExtractionObjectiveConfig.AncientCrystalEntry config() {
        return xyz.iwolfking.woldsvaults.init.ModConfigs.EXTRACTION_OBJECTIVE.ANCIENT_CRYSTAL;
    }

    public static NonNullList<ItemStack> roll(ExtractorEntity extractor, Vault vault, Random random) {
        ExtractionObjectiveConfig.AncientCrystalEntry cfg = config();
        NonNullList<ItemStack> crystals = NonNullList.create();
        double tries = cfg.crystalRolls + (cfg.quantityPerExtraRoll > 0.0F ? Math.max(0.0F, extractor.getDrillQuantity()) / cfg.quantityPerExtraRoll : 0.0D);
        int whole = (int) Math.floor(tries);
        if (random.nextDouble() < tries - whole) {
            whole++;
        }
        for (int i = 0; i < whole; i++) {
            if (random.nextDouble() < cfg.crystalChance) {
                crystals.add(create(extractor, vault, random));
            }
        }
        return crystals;
    }

    /** Expected crystal tries for the given quantity, for the assembler preview. */
    public static double tries(float quantity) {
        ExtractionObjectiveConfig.AncientCrystalEntry cfg = config();
        return cfg.crystalRolls + (cfg.quantityPerExtraRoll > 0.0F ? Math.max(0.0F, quantity) / cfg.quantityPerExtraRoll : 0.0D);
    }

    private static ItemStack create(ExtractorEntity extractor, Vault vault, Random random) {
        ExtractionObjectiveConfig.AncientCrystalEntry cfg = config();
        int level = vaultLevel(vault);
        float power = extractor.getPower();
        float rarity = Math.max(0.0F, extractor.getDrillRarity());
        ItemStack stack = new ItemStack(ModItems.VAULT_CRYSTAL);
        CrystalData data = CrystalData.read(stack);
        data.getProperties().setLevel(level);

        if (data.getProperties() instanceof CapacityCrystalProperties capacity) {
            int base = cfg.capacityMin + (cfg.capacityMax > cfg.capacityMin ? random.nextInt(cfg.capacityMax - cfg.capacityMin + 1) : 0);
            float bonus = (cfg.powerPerCapacity > 0.0F ? power / cfg.powerPerCapacity : 0.0F) + (cfg.rarityPerCapacity > 0.0F ? rarity / cfg.rarityPerCapacity : 0.0F);
            capacity.setVolume(Math.round((base + bonus + extractor.getPlacerCapacityFlat()) * extractor.getPlacerCapacityMultiplier()));
        } else {
            WoldLogger.error("A fresh vault crystal has no capacity properties; the ancient crystal keeps the default capacity.");
        }

        DefaultCrystalModifiers modifiers = new DefaultCrystalModifiers();
        modifiers.setRandomModifiers(cfg.vaultRandomModifiers);
        for (int i = 0; i < cfg.strongNegativeRolls; i++) {
            draw(cfg.strongNegative, level, random).ifPresent(modifier -> modifiers.add(VaultModifierStack.of(modifier)));
        }
        for (int i = 0; i < cfg.strongPositiveRolls; i++) {
            draw(cfg.strongPositive, level, random).ifPresent(modifier -> modifiers.add(VaultModifierStack.of(modifier)));
        }
        double randomRolls = cfg.randomRolls + (cfg.powerPerRandomRoll > 0.0F ? power / cfg.powerPerRandomRoll : 0.0D)
                + (cfg.rarityPerRandomRoll > 0.0F ? rarity / cfg.rarityPerRandomRoll : 0.0D);
        int rolls = (int) Math.floor(randomRolls);
        if (random.nextDouble() < randomRolls - rolls) {
            rolls++;
        }
        double positive = Math.max(0.0D, cfg.positiveWeight + cfg.positiveWeightPerRarity * rarity);
        double negative = Math.max(0.0D, cfg.negativeWeight);
        for (int i = 0; i < rolls; i++) {
            boolean good = positive + negative <= 0.0D || random.nextDouble() * (positive + negative) < positive;
            draw(good ? cfg.randomPositive : cfg.randomNegative, level, random).ifPresent(modifier -> modifiers.add(VaultModifierStack.of(modifier)));
        }
        data.setModifiers(modifiers);

        CrystalObjective objective = rollObjective(cfg, level, random);
        if (objective != null) {
            data.setObjective(objective);
        }

        String sigil = rollSigil(cfg, random);
        if (sigil != null) {
            data.setSigil(sigil);
        }

        if ((Object) data instanceof AncientCrystalFlag flag) {
            flag.woldsvaults$setAncient(true);
        } else {
            WoldLogger.error("CrystalData does not carry the ancient flag mixin; the ancient crystal will accept maps and sigils.");
        }
        data.write(stack);
        return stack;
    }

    private static int vaultLevel(Vault vault) {
        if (vault != null && vault.has(Vault.LEVEL)) {
            return vault.get(Vault.LEVEL).get(VaultLevel.VALUE);
        }
        WoldLogger.error("Ancient crystal payout could not read the vault's level; the crystal was made at level 100.");
        return 100;
    }

    private static Optional<VaultModifier<?>> draw(List<ExtractionObjectiveConfig.ModifierSource> sources, int level, Random random) {
        double total = 0.0D;
        for (ExtractionObjectiveConfig.ModifierSource source : sources) {
            total += Math.max(0.0F, source.weight);
        }
        if (total <= 0.0D) {
            report("sources", "An ancient crystal modifier bucket has no weighted sources in extraction_objective.json ANCIENT_CRYSTAL; its rolls were skipped.");
            return Optional.empty();
        }
        double pick = random.nextDouble() * total;
        ExtractionObjectiveConfig.ModifierSource chosen = sources.get(sources.size() - 1);
        for (ExtractionObjectiveConfig.ModifierSource source : sources) {
            pick -= Math.max(0.0F, source.weight);
            if (pick < 0.0D) {
                chosen = source;
                break;
            }
        }
        String id = "greed_trader".equals(chosen.type) ? drawTrader(chosen.pool, random) : drawVaultPool(chosen.pool, level, random);
        if (id == null) {
            return Optional.empty();
        }
        ResourceLocation key = ResourceLocation.tryParse(id);
        Optional<VaultModifier<?>> modifier = key == null ? Optional.empty() : VaultModifierRegistry.getOpt(key);
        if (modifier.isEmpty()) {
            report("modifier:" + id, "Ancient crystal rolled modifier '" + id + "' from '" + chosen.pool + "', which is not a registered vault modifier; that roll was skipped.");
        }
        return modifier;
    }

    @Nullable
    private static String drawTrader(String pool, Random random) {
        List<String> ids = ModConfigs.GREED_TRADER.rollModifiersFromPool(pool, 1, 1, random, true);
        if (ids.isEmpty()) {
            report("trader:" + pool, "Greed trader modifier list '" + pool + "' is missing or empty; ancient crystal rolls from it were skipped.");
            return null;
        }
        return ids.get(0);
    }

    @Nullable
    private static String drawVaultPool(String pool, int level, Random random) {
        ResourceLocation id = ResourceLocation.tryParse(pool);
        if (id == null) {
            report("pool:" + pool, "Ancient crystal source pool '" + pool + "' is not a valid id; its rolls were skipped.");
            return null;
        }
        WeightedList<ResourceLocation> combined = new WeightedList<>();
        for (VaultModifierPoolsConfig.Entry entry : ModConfigs.VAULT_MODIFIER_POOLS.getForLevel(id, level).entries) {
            entry.pool.forEach((modifier, weight) -> combined.put(modifier, combined.getOrDefault(modifier, 0.0D) + weight));
        }
        if (combined.isEmpty()) {
            report("pool:" + pool, "Vault modifier pool '" + pool + "' has no entries at level " + level + "; ancient crystal rolls from it were skipped.");
            return null;
        }
        return combined.getRandom(random).map(ResourceLocation::toString).orElse(null);
    }

    @Nullable
    private static CrystalObjective rollObjective(ExtractionObjectiveConfig.AncientCrystalEntry cfg, int level, Random random) {
        ResourceLocation poolId = ResourceLocation.tryParse(cfg.objectivePool);
        Optional<VaultCrystalConfig.ObjectiveEntry> entry = poolId == null || ModConfigs.VAULT_CRYSTAL.OBJECTIVES == null
                ? Optional.empty()
                : Optional.ofNullable(ModConfigs.VAULT_CRYSTAL.OBJECTIVES.get(poolId)).flatMap(list -> list.getForLevel(level));
        if (entry.isEmpty()) {
            WoldLogger.error("Objective pool '{}' has nothing at level {}; the ancient crystal's objective will roll at vault start.", cfg.objectivePool, level);
            return null;
        }
        WeightedList<CrystalObjective> allowed = new WeightedList<>();
        entry.get().pool.forEach((objective, weight) -> {
            String type = CrystalData.OBJECTIVE.getType(objective);
            if (type == null || !cfg.excludedObjectiveTypes.contains(type)) {
                allowed.put(objective, weight);
            }
        });
        if (allowed.isEmpty()) {
            WoldLogger.error("Every objective in pool '{}' at level {} is excluded; the ancient crystal's objective will roll at vault start.", cfg.objectivePool, level);
            return null;
        }
        return allowed.getRandom(random).orElse(null);
    }

    @Nullable
    private static String rollSigil(ExtractionObjectiveConfig.AncientCrystalEntry cfg, Random random) {
        double total = 0.0D;
        for (ExtractionObjectiveConfig.WeightedId sigil : cfg.sigils) {
            total += Math.max(0.0F, sigil.weight);
        }
        if (total <= 0.0D) {
            WoldLogger.error("extraction_objective.json ANCIENT_CRYSTAL lists no sigils; the ancient crystal has none.");
            return null;
        }
        double pick = random.nextDouble() * total;
        String chosen = cfg.sigils.get(cfg.sigils.size() - 1).id;
        for (ExtractionObjectiveConfig.WeightedId sigil : cfg.sigils) {
            pick -= Math.max(0.0F, sigil.weight);
            if (pick < 0.0D) {
                chosen = sigil.id;
                break;
            }
        }
        if (ModConfigs.SIGIL_DEFINITIONS.get(chosen).isEmpty()) {
            report("sigil:" + chosen, "Ancient sigil '" + chosen + "' has no sigils/definitions.json entry; the crystal was made without a sigil.");
            return null;
        }
        return chosen;
    }

    private static void report(String key, String message) {
        if (REPORTED.add(key)) {
            WoldLogger.error(message);
        }
    }
}
