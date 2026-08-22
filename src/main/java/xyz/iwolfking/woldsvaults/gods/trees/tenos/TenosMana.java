package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.init.ModAttributes;
import iskallia.vault.mana.Mana;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import xyz.iwolfking.woldsvaults.gods.GodVanillaAttributes;

import java.util.UUID;

/**
 * Mana Starved (r100) and Deep Reserves (r101).
 *
 * <p>Deep Reserves is a transient attribute modifier rather than a gear attribute, because the
 * sheet asks for a 1.5x multiplier and gear attributes contribute additively. Applying and removing
 * it is driven by {@link TenosVaultHandlers.DeepReservesHandler} on the shared ticker: the base mod
 * rewrites {@code MANA_MAX}'s <em>base</em> value every single tick, so a modifier is the only safe
 * place to put a multiplier, and the ticker's own deactivation pass is what takes it back on a
 * charm swap or a refund.
 */
public final class TenosMana {
    private static final UUID DEEP_RESERVES_UUID = GodVanillaAttributes.modifierId(
            "tenos_deep_reserves", ModAttributes.MANA_MAX, AttributeModifier.Operation.MULTIPLY_BASE);

    private TenosMana() {
    }

    /**
     * Mana Starved (r100): mana regeneration scales from 1x at half mana up to 2x at empty. The
     * regen tick is the only writable point - {@code CommonEvents.MANA_MODIFY} has no setters -
     * so the multiplier is applied to the amount handed to {@code Mana.increase}.
     */
    public static float manaStarvedMultiplier(ServerPlayer player) {
        if (!TenosNodes.isActive(player, TenosNodes.MANA_STARVED)) {
            return 1.0F;
        }
        float max = Mana.getMax(player);
        if (max <= 0.0F) {
            return 1.0F;
        }
        TenosNodeHandlers.ManaStarvedParams params = TenosNodeHandlers.params(TenosNodes.MANA_STARVED,
                TenosNodeHandlers.ManaStarvedParams.class);
        float fraction = Mana.get(player) / max;
        if (fraction >= params.threshold()) {
            return 1.0F;
        }
        float depth = (params.threshold() - fraction) / params.threshold();
        return 1.0F + params.max_bonus() * depth;
    }

    /** Adds Deep Reserves' maximum mana multiplier if it is not already on the player. */
    public static void applyDeepReserves(ServerPlayer player, float multiplier) {
        AttributeInstance manaMax = player.getAttribute(ModAttributes.MANA_MAX);
        if (manaMax == null || manaMax.getModifier(DEEP_RESERVES_UUID) != null) {
            return;
        }
        manaMax.addTransientModifier(new AttributeModifier(DEEP_RESERVES_UUID, "TenosDeepReserves",
                multiplier, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    public static void removeDeepReserves(ServerPlayer player) {
        AttributeInstance manaMax = player.getAttribute(ModAttributes.MANA_MAX);
        if (manaMax != null && manaMax.getModifier(DEEP_RESERVES_UUID) != null) {
            manaMax.removeModifier(DEEP_RESERVES_UUID);
        }
    }
}
