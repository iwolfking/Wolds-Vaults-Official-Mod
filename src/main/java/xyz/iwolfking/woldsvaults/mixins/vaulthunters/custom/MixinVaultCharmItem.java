package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.gear.VaultCharmItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Lets vault god charms scale past 50 reputation for God's Mastery players. The charm's own
 * follow-the-holder machinery (the 10-tick inventory update that rescales every prefix by
 * (current+1)/(stored+1) and re-snapshots) is untouched — only the two hardcoded 50-gates
 * that froze it are lifted. Charms still reset to the reputation of whichever player holds
 * them, in both directions.
 */
@Mixin(value = VaultCharmItem.class, remap = false)
public class MixinVaultCharmItem {
    /**
     * getGodReputation clamped every read of the charm's stored reputation snapshot at 50, so
     * a charm could never display or scale past 50 even after updateCharm stored a higher
     * value. The snapshot is only ever written from the holder's live reputation — itself
     * capped at 50 + mastery by MixinPlayerReputationData — so the read needs no bound.
     */
    @ModifyConstant(method = "getGodReputation", constant = @Constant(intValue = 50), require = 1)
    private static int woldsVaults$unclampStoredReputation(int legacyCap) {
        return Integer.MAX_VALUE;
    }

    /**
     * shouldUpdateItem early-outs when stored and current reputation are both >= 50 — with a
     * raised cap that froze charms at exactly 50 forever (stored 50, current 52, no update
     * ever). Lifting both constants reduces the check to stored != current, which also covers
     * handing a 52-rep charm to a lower-rep player: it rescales down to them, per the
     * follow-the-holder semantics.
     */
    @ModifyConstant(method = "shouldUpdateItem", constant = @Constant(intValue = 50), require = 2)
    private static int woldsVaults$neverFreezeAtLegacyCap(int legacyCap) {
        return Integer.MAX_VALUE;
    }
}
