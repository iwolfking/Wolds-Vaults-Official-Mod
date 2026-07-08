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
 */
public final class HyperCrateRewards {
    public static final ResourceLocation RARE_MODIFIER = WoldsVaults.id("rare_crate_tier");
    public static final ResourceLocation EPIC_MODIFIER = WoldsVaults.id("epic_crate_tier");
    public static final ResourceLocation OMEGA_MODIFIER = WoldsVaults.id("omega_crate_tier");

    public static final int RARE_ROLLS = 4;
    public static final int EPIC_ROLLS = 3;
    public static final int OMEGA_ROLLS = 3;

    /** One weighted pool line: fixed or ranged count, plus optional special payloads. */
    public record Entry(String itemId, String boosterPackId, String deckCoreId, int min, int max,
                        boolean greedScaled, boolean randomEtching) {
        static Entry of(String itemId, int count) {
            return new Entry(itemId, null, null, count, count, false, false);
        }

        static Entry pack(String boosterPackId, int count) {
            return new Entry("the_vault:booster_pack", boosterPackId, null, count, count, false, false);
        }

        static Entry greedCoins(int min, int max) {
            return new Entry("the_vault:greed_coin", null, null, min, max, true, false);
        }

        /** The greater variant of one deck core (an id from config/the_vault/card/deck_modifiers.json). */
        static Entry greaterDeckCore(String deckCoreId) {
            return new Entry("the_vault:deck_socket", null, deckCoreId, 1, 1, false, false);
        }

        /** A random identified etching, rolled at the receiving player's greed tier. */
        static Entry etching() {
            return new Entry("the_vault:etching", null, null, 1, 1, false, true);
        }
    }

    public static final WeightedList<Entry> RARE = new WeightedList<Entry>()
            .add(Entry.of("the_vault:repair_core", 1), 100)
            .add(Entry.of("vending_companions:companion_temporalizer", 1), 25)
            .add(Entry.pack("the_vault:mega_resource_pack", 1), 1)
            .add(Entry.of("the_vault:map", 1), 10)
            .add(Entry.of("woldsvaults:legend_sigil", 1), 5)
            .add(Entry.of("the_vault:recharge_core", 1), 50)
            .add(Entry.of("the_vault:fundamental_focus", 15), 40)
            .add(Entry.of("the_vault:opportunistic_focus", 10), 10)
            .add(Entry.of("woldsvaults:altar_recatalyzer", 2), 50)
            .add(Entry.of("the_vault:vault_platinum", 5), 40)
            .add(Entry.etching(), 5);

    public static final WeightedList<Entry> EPIC = new WeightedList<Entry>()
            .add(Entry.greedCoins(1, 1), 10)
            .add(Entry.of("the_vault:vault_palladium", 5), 30)
            .add(Entry.of("woldsvaults:nullite_crystal", 1), 5)
            .add(Entry.of("the_vault:map", 1), 30)
            .add(Entry.of("woldsvaults:chunk_of_power", 1), 1)
            .add(Entry.pack("the_vault:mega_resource_pack", 1), 2)
            .add(Entry.pack("the_vault:evolution_pack", 10), 5)
            .add(Entry.pack("the_vault:shiny_pack", 2), 5)
            .add(Entry.of("the_vault:unique_shard", 10), 20)
            .add(Entry.greaterDeckCore("pure"), 2)
            .add(Entry.greaterDeckCore("arcane"), 2)
            .add(Entry.greaterDeckCore("construction"), 2)
            .add(Entry.greaterDeckCore("shiny"), 2)
            .add(Entry.greaterDeckCore("void"), 2);

    public static final WeightedList<Entry> OMEGA = new WeightedList<Entry>()
            .add(Entry.greedCoins(2, 3), 30)
            .add(Entry.of("the_vault:mystic_pear", 1), 2)
            .add(Entry.of("woldsvaults:wold_star_chunk", 1), 1)
            .add(Entry.of("woldsvaults:chunk_of_power", 1), 4)
            .add(Entry.of("woldsvaults:nullite_crystal", 2), 5)
            .add(Entry.pack("the_vault:mega_resource_pack", 2), 5)
            .add(Entry.pack("the_vault:shiny_pack", 5), 10)
            // woldsvaults:crystal_reinforcement displays as "Prismatic Reinforcement".
            .add(Entry.of("woldsvaults:crystal_reinforcement", 1), 2)
            .add(Entry.greaterDeckCore("archive"), 2);

    private HyperCrateRewards() {
    }

    /** All injection stacks the vault's tier markers earn — one full pool run per marker stack. */
    public static List<ItemStack> rollForVault(Vault vault, int greedTier, RandomSource random) {
        List<ItemStack> out = new ArrayList<>();
        roll(RARE, RARE_ROLLS, (int) VaultModifierUtils.getCountOfModifiers(vault, RARE_MODIFIER), greedTier, random, out);
        roll(EPIC, EPIC_ROLLS, (int) VaultModifierUtils.getCountOfModifiers(vault, EPIC_MODIFIER), greedTier, random, out);
        roll(OMEGA, OMEGA_ROLLS, (int) VaultModifierUtils.getCountOfModifiers(vault, OMEGA_MODIFIER), greedTier, random, out);
        return out;
    }

    private static void roll(WeightedList<Entry> pool, int rolls, int stacks, int greedTier, RandomSource random, List<ItemStack> out) {
        for (int stack = 0; stack < stacks; stack++) {
            for (int i = 0; i < rolls; i++) {
                pool.getRandom(random).ifPresent(entry -> {
                    if (entry.randomEtching()) {
                        rollEtching(greedTier, random, out);
                        return;
                    }
                    Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(entry.itemId()));
                    if (item == null || item == Items.AIR) {
                        WoldsVaults.LOGGER.error("Hyper crate reward item {} is not registered — this roll is lost. Fix the id in HyperCrateRewards.", entry.itemId());
                        return;
                    }
                    int count = entry.min() == entry.max()
                            ? entry.min()
                            : entry.min() + random.nextInt(entry.max() - entry.min() + 1);
                    if (entry.greedScaled()) {
                        count *= greedTier;
                        if (count <= 0) {
                            // Greed tier 0 earns no coins from a greed-scaled line; roll is spent.
                            return;
                        }
                    }
                    ItemStack reward = new ItemStack(item, count);
                    if (entry.boosterPackId() != null) {
                        // BoosterPackItem reads its pool from the "id" string tag.
                        reward.getOrCreateTag().putString("id", entry.boosterPackId());
                    }
                    if (entry.deckCoreId() != null) {
                        // DeckSocketItem self-initializes from these two tags on its first
                        // inventory tick (same shape the bounty/loot configs use).
                        reward.getOrCreateTag().putString("Modifier", entry.deckCoreId());
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
