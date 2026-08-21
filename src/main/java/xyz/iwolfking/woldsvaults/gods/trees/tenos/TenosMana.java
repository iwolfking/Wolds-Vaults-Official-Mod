package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.init.ModAttributes;
import iskallia.vault.mana.Mana;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.UUID;
import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

/**
 * Mana Starved (r100) and Deep Reserves (r101).
 *
 * <p>Deep Reserves is a transient attribute modifier rather than a gear attribute, because the
 * sheet asks for a 1.5x multiplier and gear attributes contribute additively. It is reapplied on a
 * one second cadence: the base mod rewrites {@code MANA_MAX}'s <em>base</em> value every single
 * tick, so a modifier is the only safe place to put a multiplier, and a periodic reconcile keeps
 * it correct across charm swaps and node refunds.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class TenosMana {
    public static float deepReservesMultiplier() {
        return GodNodeValues.number(TenosNodes.DEEP_RESERVES, "multiplier");
    }
    public static float manaStarvedMaxBonus() {
        return GodNodeValues.number(TenosNodes.MANA_STARVED, "max_bonus");
    }
    public static float manaStarvedThreshold() {
        return GodNodeValues.number(TenosNodes.MANA_STARVED, "threshold");
    }

    private static final UUID DEEP_RESERVES_UUID = UUID.fromString("1f2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d");
    private static final int RECONCILE_INTERVAL_TICKS = 20;

    private static int tickCounter;

    private TenosMana() {
    }

    /**
     * Mana Starved (r100): mana regeneration scales from 1x at half mana up to 2x at empty. The
     * regen tick is the only writable point - {@code CommonEvents.MANA_MODIFY} has no setters -
     * so the multiplier is applied to the amount handed to {@code Mana.increase}.
     */
    public static float manaStarvedMultiplier(ServerPlayer player) {
        if (!TenosNodes.hasMinor(player, TenosNodes.MANA_STARVED)) {
            return 1.0F;
        }
        float max = Mana.getMax(player);
        if (max <= 0.0F) {
            return 1.0F;
        }
        float fraction = Mana.get(player) / max;
        if (fraction >= manaStarvedThreshold()) {
            return 1.0F;
        }
        float depth = (manaStarvedThreshold() - fraction) / manaStarvedThreshold();
        return 1.0F + manaStarvedMaxBonus() * depth;
    }

    @SubscribeEvent
    public static void reconcileDeepReserves(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++tickCounter < RECONCILE_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            AttributeInstance manaMax = player.getAttribute(ModAttributes.MANA_MAX);
            if (manaMax == null) {
                continue;
            }
            boolean wanted = TenosNodes.hasMinor(player, TenosNodes.DEEP_RESERVES);
            boolean present = manaMax.getModifier(DEEP_RESERVES_UUID) != null;
            if (wanted == present) {
                continue;
            }
            if (wanted) {
                manaMax.addTransientModifier(new AttributeModifier(DEEP_RESERVES_UUID, "TenosDeepReserves",
                        deepReservesMultiplier(), AttributeModifier.Operation.MULTIPLY_BASE));
            } else {
                manaMax.removeModifier(DEEP_RESERVES_UUID);
            }
        }
    }
}
