package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.item.ItemVaultFruit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.HealthReductionHelper;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

/** The Wendarr fruit family. A call that finds no active node returns its input unchanged. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class WendarrFruit {
    public static final double DEFAULT_HEALTH_SCALING = HealthReductionHelper.DEFAULT_MULT_SCALING;

    private WendarrFruit() {
    }

    /** The fruit-efficiency time term. Legend of the Pear reshapes it, Glutton then scales it. */
    public static float adjustEffectiveness(Player player, float effectiveness) {
        float shaped = effectiveness <= 0.0F ? 0.0F : effectiveness / (1.0F + effectiveness);
        if (player instanceof ServerPlayer serverPlayer) {
            if (WendarrNodes.isActive(serverPlayer, WendarrNodes.LEGEND_OF_THE_PEAR)) {
                shaped = effectiveness <= 0.0F ? 0.0F : (float) Math.sqrt(effectiveness);
            }
            if (WendarrNodes.isActive(serverPlayer, WendarrNodes.GLUTTON)) {
                shaped = gluttonParams().time_multiplier() * (1.0F + shaped) - 1.0F;
            }
        }
        return shaped;
    }

    /** The fruit rot chance. Reductions compose multiplicatively; a saved fruit never rots. */
    public static float adjustRotChance(Player eater, float rotChance) {
        if (!(eater instanceof ServerPlayer player)) {
            return rotChance;
        }
        if (isFruitSaved(player)) {
            return 0.0F;
        }
        float adjusted = rotChance;
        if (WendarrNodes.isActive(player, WendarrNodes.PRISTINE_CONDITION)) {
            adjusted *= WendarrNodeHandlers.params(WendarrNodes.PRISTINE_CONDITION,
                    WendarrNodeHandlers.PristineConditionParams.class).rot_multiplier();
        }
        if (WendarrNodes.isActive(player, WendarrNodes.GLUTTON)) {
            adjusted *= gluttonParams().rot_multiplier();
        }
        return adjusted;
    }

    /** Per-fruit max-health retention, compounded per fruit. Tough Stomach raises it. */
    public static double healthScaling(ServerPlayer player) {
        if (player != null && WendarrNodes.isActive(player, WendarrNodes.TOUGH_STOMACH)) {
            return WendarrNodeHandlers.params(WendarrNodes.TOUGH_STOMACH,
                    WendarrNodeHandlers.ToughStomachParams.class).health_scaling();
        }
        return DEFAULT_HEALTH_SCALING;
    }

    /** Rolls Expert Eater at the head of {@code onEaten}, ahead of rot and the max-health penalty. */
    public static void rollFruitSave(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        GodNodeState.clear(serverPlayer.getUUID(), WendarrNodes.EXPERT_EATER);
        if (!WendarrNodes.isActive(serverPlayer, WendarrNodes.EXPERT_EATER)) {
            return;
        }
        float chance = WendarrNodeHandlers.params(WendarrNodes.EXPERT_EATER,
                WendarrNodeHandlers.ExpertEaterParams.class).save_chance();
        if (serverPlayer.getRandom().nextFloat() < chance) {
            GodNodeState.put(serverPlayer.getUUID(), WendarrNodes.EXPERT_EATER, Boolean.TRUE);
        }
    }

    public static boolean isFruitSaved(ServerPlayer player) {
        return player != null && GodNodeState.peek(player.getUUID(), WendarrNodes.EXPERT_EATER).isPresent();
    }

    @SubscribeEvent
    public static void refundSavedFruit(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getItem().getItem() instanceof ItemVaultFruit)) {
            return;
        }
        if (!isFruitSaved(player)) {
            return;
        }
        GodNodeState.clear(player.getUUID(), WendarrNodes.EXPERT_EATER);
        ItemStack result = event.getResultStack();
        if (result.isEmpty()) {
            event.setResultStack(new ItemStack(event.getItem().getItem(), 1));
        } else {
            result.grow(1);
        }
    }

    private static WendarrNodeHandlers.GluttonParams gluttonParams() {
        return WendarrNodeHandlers.params(WendarrNodes.GLUTTON, WendarrNodeHandlers.GluttonParams.class);
    }
}
