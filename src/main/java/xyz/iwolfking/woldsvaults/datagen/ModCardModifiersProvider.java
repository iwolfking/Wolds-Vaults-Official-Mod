package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardNeighborType;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsCardModifiersProvider;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ModCardModifiersProvider extends AbstractWoldsCardModifiersProvider {
    public static final List<GreedTier> GREED_TIERS = List.of(
            new GreedTier("weak", "Weak", 1.5F, 1.6F, 1.75F),
            new GreedTier("medium", "Medium", 1.75F, 1.85F, 2.0F),
            new GreedTier("strong", "Strong", 2.0F, 2.1F, 2.25F),
            new GreedTier("elite", "Elite", 2.25F, 2.35F, 2.5F)
    );

    private static final List<CardNeighborType> GREED_NEIGHBORS = List.of(CardNeighborType.ABOVE, CardNeighborType.BELOW, CardNeighborType.LEFT, CardNeighborType.RIGHT);
    private static final List<CardEntry.Color> GREED_COLORS = List.of(CardEntry.Color.RED, CardEntry.Color.BLUE, CardEntry.Color.GREEN, CardEntry.Color.YELLOW);
    private static final int GREED_CARD_WEIGHT = 8;
    private static final String THORNS_PERCENT_MODEL = "woldsvaults:card/icon/thorns_percent#inventory";

    public ModCardModifiersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("greed_cards", builder -> {
            builder.value("thorns_percent", gearCard("Scaling Thorns Card", THORNS_PERCENT_MODEL, List.of("Offensive", "Defensive", "Stat"),
                    ModGearAttributes.THORNS_FLAT_PERCENT, 5, tiers(1.0, 0.005, 0.01, 0.015, 0.02, 0.025)));
            builder.value("deluxe_thorns_percent", gearCard("Deluxe Scaling Thorns Card", THORNS_PERCENT_MODEL, List.of("Offensive", "Defensive", "Stat", "Deluxe"),
                    ModGearAttributes.THORNS_FLAT_PERCENT, 1, tiers(0.0, 0.035)));
            builder.value("amp_thorns_percent", gearCard("Evolving Scaling Thorns Card", THORNS_PERCENT_MODEL, List.of("Offensive", "Evolution"),
                    ModGearAttributes.THORNS_FLAT_PERCENT, 5, tiers(1.0, 0.003, 0.005, 0.007, 0.01, 0.013)));

            for (GreedTier tier : GREED_TIERS) {
                for (CardNeighborType neighbor : GREED_NEIGHBORS) {
                    for (CardEntry.Color color : GREED_COLORS) {
                        builder.value(greedCardId(tier, neighbor, color), greedCard(tier.label() + " Greed Card", List.of("Greed"), neighbor, color, tier.rolls()));
                    }
                }
            }

            builder.pool("default", pool -> pool.add("thorns_percent", 1));
            builder.pool("deluxe_stat", pool -> pool.add("deluxe_thorns_percent", 1));
            builder.pool("scaling", pool -> pool.add("amp_thorns_percent", 1));

            for (GreedTier tier : GREED_TIERS) {
                builder.pool(tier.pool(), pool -> {
                    for (CardNeighborType neighbor : GREED_NEIGHBORS) {
                        for (CardEntry.Color color : GREED_COLORS) {
                            pool.add(greedCardId(tier, neighbor, color), GREED_CARD_WEIGHT);
                        }
                    }
                });
            }
        });

        add("greed_cards_remove", builder -> {
            builder.pool("default", pool -> pool.add("attack_damage", 1).add("apflat", 1).add("attack_speed", 1).add("lucky_hit_chance", 1).add("thorns_flat", 1));
            builder.pool("shiny", pool -> pool.add("attack_damage_shiny", 1).add("apflat_shiny", 1).add("attack_speed_shiny", 1).add("thorns_flat_shiny", 1));
            builder.pool("deluxe_stat", pool -> pool.add("deluxe_attack_damage", 1).add("deluxe_apflat", 1).add("deluxe_attack_speed", 1).add("deluxe_lucky_hit_chance", 1).add("deluxe_thorns_flat", 1));
            builder.pool("scaling", pool -> pool.add("amp_attack_damage", 1).add("amp_apflat", 1).add("amp_attack_speed", 1).add("amp_lucky_hit_chance", 1).add("amp_thorns_flat", 1));
        });
    }

    private static String greedCardId(GreedTier tier, CardNeighborType neighbor, CardEntry.Color color) {
        return "greed_" + tier.id() + "_" + neighbor.name().toLowerCase(Locale.ROOT) + "_" + color.name().toLowerCase(Locale.ROOT);
    }

    private static Map<Integer, String> tiers(double step, double... values) {
        Map<Integer, String> tiers = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            tiers.put(i + 1, range(values[i], values[i], step));
        }
        return tiers;
    }

    /**
     * One greed card strength: its card pool {@code greed_<id>}, the label used in card and booster pack names,
     * and the multiplier rolled at each card tier.
     */
    public record GreedTier(String id, String label, float... rolls) {
        public String pool() {
            return "greed_" + id;
        }
    }
}
