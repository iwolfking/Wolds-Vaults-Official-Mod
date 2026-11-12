package xyz.iwolfking.woldsvaults.extraction.parts;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.util.calc.ItemRarityHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;

import java.util.Random;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Stamps rarity and quality onto every extractor blueprint a chest generates, from the opening player's
 * item rarity. The loot tables decide whether a blueprint drops; this decides how good it is.
 */
public final class BlueprintDrops {
    private static final Object OWNER = new Object();
    private static final Random RANDOM = new Random();

    private BlueprintDrops() {
    }

    public static void register() {
        CommonEvents.CHEST_LOOT_GENERATION.post().register(OWNER, data -> {
            Player player = data.getPlayer();
            if (player == null) {
                return;
            }
            double points = itemRarityPoints(player);
            for (ItemStack stack : data.getLoot()) {
                if (stack.getItem() instanceof ExtractorBlueprintItem && !ExtractorBlueprintItem.hasQuality(stack)) {
                    ExtractorBlueprintItem.stamp(stack, points, RANDOM);
                    WoldLogger.info("Stamped {} at {} item-rarity points: {} Q{}", stack.getItem().getRegistryName(),
                            (int) points, ExtractorBlueprintItem.getBlueprintRarity(stack), ExtractorBlueprintItem.getQuality(stack));
                }
            }
        });
    }

    /** the_vault item rarity is a fraction (0.5 = +50 %); the quality curve works in percent points. */
    public static double itemRarityPoints(Player player) {
        return ItemRarityHelper.getItemRarity(player) * 100.0D;
    }
}
