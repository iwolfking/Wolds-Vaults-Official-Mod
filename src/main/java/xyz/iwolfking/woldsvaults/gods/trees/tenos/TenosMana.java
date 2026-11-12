package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.init.ModAttributes;
import iskallia.vault.mana.Mana;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import xyz.iwolfking.woldsvaults.gods.GodVanillaAttributes;

import java.util.UUID;

/** Mana Starved and Deep Reserves, the latter a transient {@code MANA_MAX} modifier. */
public final class TenosMana {
    private static final UUID DEEP_RESERVES_UUID = GodVanillaAttributes.modifierId(
            "tenos_deep_reserves", ModAttributes.MANA_MAX, AttributeModifier.Operation.MULTIPLY_BASE);

    private TenosMana() {
    }

    /** Mana Starved: 1x at {@code threshold} of maximum mana, {@code 1 + max_bonus} at empty. */
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

    /** Adds Deep Reserves' modifier. {@code multiplier} is the increment, so 0.5 is 1.5x max mana. */
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
