package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.gear.EtchingItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.config.HyperObjectiveConfig;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Score-gated completion-crate injections. Killing a hyperboss whose score clears a threshold
 * adds the matching tier marker to the vault; at crate award every stack of a marker rolls its
 * pool independently and the items go straight into the crate's additional-items list —
 * bypassing every crate quantity/tier modifier by design.
 *
 * <p>The pools themselves (items, counts, weights, draws per stack) are data:
 * config/the_vault/hyper_objective.json, see {@link HyperObjectiveConfig}.
 */
public final class HyperCrateRewards {
    public static final ResourceLocation RARE_MODIFIER = WoldsVaults.id("rare_crate_tier");
    public static final ResourceLocation EPIC_MODIFIER = WoldsVaults.id("epic_crate_tier");
    public static final ResourceLocation OMEGA_MODIFIER = WoldsVaults.id("omega_crate_tier");

    private HyperCrateRewards() {
    }

    /**
     * All injection stacks the vault's tier markers earn — one full pool run per marker stack;
     * stacks earned by kills scoring the extra-draw threshold roll one extra draw each.
     */
    public static List<ItemStack> rollForVault(Vault vault, int greedTier, RandomSource random) {
        HyperObjectiveConfig cfg = HyperVaultObjective.cfg();
        List<ItemStack> out = new ArrayList<>();
        int epicPlus = HyperVaultObjective.get(vault).map(o -> o.getOr(HyperVaultObjective.EPIC_PLUS, 0)).orElse(0);
        int omegaPlus = HyperVaultObjective.get(vault).map(o -> o.getOr(HyperVaultObjective.OMEGA_PLUS, 0)).orElse(0);
        roll(cfg.getRarePool(), cfg.getRareRolls(), (int) VaultModifierUtils.getCountOfModifiers(vault, RARE_MODIFIER), 0, greedTier, random, out);
        roll(cfg.getEpicPool(), cfg.getEpicRolls(), (int) VaultModifierUtils.getCountOfModifiers(vault, EPIC_MODIFIER), epicPlus, greedTier, random, out);
        roll(cfg.getOmegaPool(), cfg.getOmegaRolls(), (int) VaultModifierUtils.getCountOfModifiers(vault, OMEGA_MODIFIER), omegaPlus, greedTier, random, out);
        return out;
    }

    /**
     * Rolls one pool. Greed-coin lines multiply their count by the player's greed tier (a
     * tier-0 player spends the roll for nothing); booster packs carry their pool id in the
     * "id" string tag; deck cores self-initialize from the Modifier/ModifierRoll tags on their
     * first inventory tick — the same shape the bounty/loot configs use.
     */
    private static void roll(WeightedList<HyperObjectiveConfig.InjectionEntry> pool, int baseRolls,
                             int stacks, int plusStacks, int greedTier, RandomSource random, List<ItemStack> out) {
        for (int stack = 0; stack < stacks; stack++) {
            int rolls = baseRolls + (stack < plusStacks ? 1 : 0);
            for (int i = 0; i < rolls; i++) {
                pool.getRandom(random).ifPresent(entry -> {
                    if (entry.isRandomEtching()) {
                        rollEtching(greedTier, random, out);
                        return;
                    }
                    Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(entry.getItem()));
                    if (item == null || item == Items.AIR) {
                        WoldsVaults.LOGGER.error("Hyper crate reward item {} is not registered — this roll is lost. Fix the id in hyper_objective.json.", entry.getItem());
                        return;
                    }
                    int count = entry.getMin() == entry.getMax()
                            ? entry.getMin()
                            : entry.getMin() + random.nextInt(entry.getMax() - entry.getMin() + 1);
                    if (entry.isGreedCoins()) {
                        count *= greedTier;
                        if (count <= 0) {
                            return;
                        }
                    }
                    ItemStack reward = new ItemStack(item, count);
                    if (entry.getBoosterPack() != null) {
                        reward.getOrCreateTag().putString("id", entry.getBoosterPack());
                    }
                    if (entry.getDeckCore() != null) {
                        reward.getOrCreateTag().putString("Modifier", entry.getDeckCore());
                        reward.getOrCreateTag().putString("ModifierRoll", "greater");
                    }
                    out.add(reward);
                });
            }
        }
    }

    /**
     * A random identified etching at the player's greed tier (floored at 1 — a tier-0 etching
     * has no modifier pool). A config entry with an empty attribute pool wastes an attempt, so
     * a few different etchings are tried before the roll is declared lost.
     */
    private static void rollEtching(int greedTier, RandomSource random, List<ItemStack> out) {
        int tier = Math.max(1, greedTier);
        if (tier != greedTier) {
            WoldsVaults.LOGGER.info("Etching reward rolled for a greed-tier-{} player; creating it at tier 1 instead.", greedTier);
        }
        List<ResourceLocation> ids = new ArrayList<>(ModConfigs.ETCHINGS.getEtchingIds());
        if (ids.isEmpty()) {
            WoldsVaults.LOGGER.error("The etching config is empty — the etching reward roll is lost.");
            return;
        }
        Random javaRandom = new Random(random.nextLong());
        Collections.shuffle(ids, javaRandom);
        for (ResourceLocation id : ids.subList(0, Math.min(5, ids.size()))) {
            VaultEtchingConfig.EtchingEntry entry = ModConfigs.ETCHINGS.getEtchingConfig(id);
            if (entry == null) {
                continue;
            }
            Optional<ItemStack> etching = EtchingItem.create(id, entry, javaRandom, tier);
            if (etching.isPresent()) {
                out.add(etching.get());
                return;
            }
        }
        WoldsVaults.LOGGER.error("No etching produced a modifier at greed tier {} — the etching reward roll is lost.", tier);
    }
}
