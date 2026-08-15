package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.wendarr;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.greed.GreedNodeHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodCarryover;
import xyz.iwolfking.woldsvaults.gods.trees.wendarr.WendarrNodes;

/**
 * Master Imbuer (r66): imbuement chance from the Wendarr tree.
 *
 * <p>Imbuement chance is not a vault gear attribute, so it cannot ride the snapshot the way every
 * other stat node does. Its single consumption site reads
 * {@code GreedNodeHelper.getImbuementChanceBonus}, so the god contribution is added to that
 * helper's result -  the god tree becomes a second source alongside the greed tree rather than
 * replacing it.
 */
@Mixin(value = GreedNodeHelper.class, remap = false)
public abstract class MixinGreedNodeHelperImbuement {
    public static final float IMBUEMENT_CHANCE_PER_POINT = 0.05F;

    @ModifyReturnValue(method = "getImbuementChanceBonus", at = @At("RETURN"))
    private static float woldsvaults$addGodImbuementChance(float bonus, ServerPlayer player) {
        if (player == null || player.getServer() == null) {
            return bonus;
        }
        int points = GodAlignmentData.get(player.getServer())
                .getPointsIn(player.getUUID(), WendarrNodes.GOD, WendarrNodes.MASTER_IMBUER);
        if (points <= 0) {
            return bonus;
        }
        float scale = ActiveGodResolver.isActive(player, WendarrNodes.GOD) ? 1.0F : GodCarryover.FOREIGN_TREE_SCALE;
        return bonus + IMBUEMENT_CHANCE_PER_POINT * points * scale;
    }
}
