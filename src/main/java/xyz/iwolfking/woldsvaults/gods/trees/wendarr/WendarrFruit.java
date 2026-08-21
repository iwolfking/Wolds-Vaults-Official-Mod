package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.item.ItemVaultFruit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.HealthReductionHelper;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

/**
 * The Wendarr fruit family. Every entry point here is called from a seam the addon already owns
 * ({@code MixinItemVaultFruit}, {@code MixinRunner}, {@code HealthReductionHelper}) so no new
 * mixin is introduced for fruit; a call that finds no active node returns its input unchanged.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class WendarrFruit {
    public static float gluttonTimeMultiplier() {
        return GodNodeValues.number(WendarrNodes.GLUTTON, "time_multiplier");
    }
    public static float gluttonRotMultiplier() {
        return GodNodeValues.number(WendarrNodes.GLUTTON, "rot_multiplier");
    }
    public static float pristineRotMultiplier() {
        return GodNodeValues.number(WendarrNodes.PRISTINE_CONDITION, "rot_multiplier");
    }
    public static float expertEaterSaveChance() {
        return GodNodeValues.number(WendarrNodes.EXPERT_EATER, "save_chance");
    }
    public static final double DEFAULT_HEALTH_SCALING = HealthReductionHelper.DEFAULT_MULT_SCALING;
    public static double toughStomachHealthScaling() {
        return GodNodeValues.precise(WendarrNodes.TOUGH_STOMACH, "health_scaling");
    }

    private static final Set<UUID> SAVED_FRUIT = ConcurrentHashMap.newKeySet();

    private WendarrFruit() {
    }

    /**
     * Replaces the fruit-efficiency time term. The addon's shipped curve is {@code e / (1 + e)},
     * which asymptotes the total time gain at 2x; Legend of the Pear swaps it for {@code sqrt(e)},
     * which keeps growing. Glutton then scales the whole {@code 1 + f} time factor by 0.33, so the
     * two compose exactly as the sheet describes rather than fighting over the same term.
     */
    public static float adjustEffectiveness(Player player, float effectiveness) {
        float shaped = effectiveness <= 0.0F ? 0.0F : effectiveness / (1.0F + effectiveness);
        if (player instanceof ServerPlayer serverPlayer) {
            if (WendarrNodes.hasMajor(serverPlayer, WendarrNodes.LEGEND_OF_THE_PEAR)) {
                shaped = effectiveness <= 0.0F ? 0.0F : (float) Math.sqrt(effectiveness);
            }
            if (WendarrNodes.hasMinor(serverPlayer, WendarrNodes.GLUTTON)) {
                shaped = gluttonTimeMultiplier() * (1.0F + shaped) - 1.0F;
            }
        }
        return shaped;
    }

    /** Rot reductions compose multiplicatively, matching how the addon already stacks them. */
    public static float adjustRotChance(Player eater, float rotChance) {
        if (!(eater instanceof ServerPlayer player)) {
            return rotChance;
        }
        if (isFruitSaved(player)) {
            return 0.0F;
        }
        float adjusted = rotChance;
        if (WendarrNodes.hasMinor(player, WendarrNodes.PRISTINE_CONDITION)) {
            adjusted *= pristineRotMultiplier();
        }
        if (WendarrNodes.hasMinor(player, WendarrNodes.GLUTTON)) {
            adjusted *= gluttonRotMultiplier();
        }
        return adjusted;
    }

    /**
     * Per-fruit max-health retention. The addon compounds {@code 0.827} per fruit; Tough Stomach
     * raises it to {@code 0.86}, turning a ten-fruit stack from -85.3% max health into -78.4%.
     * A saved fruit (Expert Eater) skips the penalty entirely.
     */
    public static double healthScaling(ServerPlayer player) {
        if (player != null && WendarrNodes.hasMinor(player, WendarrNodes.TOUGH_STOMACH)) {
            return toughStomachHealthScaling();
        }
        return DEFAULT_HEALTH_SCALING;
    }

    /**
     * Rolls Expert Eater at the head of {@code onEaten}, before the rot roll and the max-health
     * penalty both run, so a saved fruit costs the player nothing at all. The refund itself lands
     * on Forge's use-finished event rather than a mixin on the vanilla consume path.
     */
    public static void rollFruitSave(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        SAVED_FRUIT.remove(serverPlayer.getUUID());
        if (!WendarrNodes.hasMinor(serverPlayer, WendarrNodes.EXPERT_EATER)) {
            return;
        }
        if (serverPlayer.getRandom().nextFloat() < expertEaterSaveChance()) {
            SAVED_FRUIT.add(serverPlayer.getUUID());
        }
    }

    public static boolean isFruitSaved(ServerPlayer player) {
        return player != null && SAVED_FRUIT.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void refundSavedFruit(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getItem().getItem() instanceof ItemVaultFruit)) {
            return;
        }
        if (!SAVED_FRUIT.remove(player.getUUID())) {
            return;
        }
        ItemStack result = event.getResultStack();
        if (result.isEmpty()) {
            event.setResultStack(new ItemStack(event.getItem().getItem(), 1));
        } else {
            result.grow(1);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SAVED_FRUIT.remove(event.getPlayer().getUUID());
    }
}
