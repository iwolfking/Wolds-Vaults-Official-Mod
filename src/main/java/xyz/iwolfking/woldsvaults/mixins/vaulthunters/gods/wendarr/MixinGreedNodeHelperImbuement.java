package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.wendarr;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.greed.GreedNodeHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.GodNodeCache;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.trees.wendarr.WendarrNodes;

/**
 * Master Imbuer (r66): imbuement chance from the Wendarr tree.
 *
 * <p>Imbuement chance is not a vault gear attribute, so it cannot ride the snapshot the way every
 * other stat node does. Its single consumption site reads
 * {@code GreedNodeHelper.getImbuementChanceBonus}, so the god contribution is added to that
 * helper's result -  the god tree becomes a second source alongside the greed tree rather than
 * replacing it. The chance per point is the effect's configured table
 * ({@code god_node_effects_wendarr.json}, {@code values[0]}) and the gate is the shared one, so the
 * node pays exactly what every other stat node pays on an active, foreign or absent charm.
 */
@Mixin(value = GreedNodeHelper.class, remap = false)
public abstract class MixinGreedNodeHelperImbuement {
    @ModifyReturnValue(method = "getImbuementChanceBonus", at = @At("RETURN"))
    private static float woldsvaults$addGodImbuementChance(float bonus, ServerPlayer player) {
        if (player == null || player.getServer() == null) {
            return bonus;
        }
        GodNodeCache.Gated gated = GodNodeGate.gate(player, WendarrNodes.GOD, WendarrNodes.MASTER_IMBUER);
        if (!gated.isActive()) {
            return bonus;
        }
        GodEffect effect = GodNodeRegistry.effect(WendarrNodes.MASTER_IMBUER).orElse(null);
        if (effect == null) {
            return bonus;
        }
        return bonus + effect.value(0) * gated.points() * gated.scale();
    }
}
