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
 * Master Imbuer (r66): adds the Wendarr tree's imbuement chance on top of the greed tree's, since it is
 * not a gear attribute. Chance per point comes from {@code god_node_effects_wendarr.json}.
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
