package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;

/**
 * Extraction Supervisor and Armored Extractors, read once from the player who places an extractor.
 * The extractor keeps both values for its whole run.
 */
public final class WendarrExtractors {
    private WendarrExtractors() {
    }

    /** The drill rate multiplier of an extractor placed by {@code placer}: 1 plus the bonus per point held. */
    public static float drillRateMultiplier(ServerPlayer placer) {
        int points = WendarrNodes.points(placer, WendarrNodes.EXTRACTION_SUPERVISER);
        if (points <= 0) {
            return 1.0F;
        }
        float scale = GodNodeGate.scale(placer, VaultGod.WENDARR, WendarrNodes.EXTRACTION_SUPERVISER);
        float bonus = WendarrNodeHandlers.params(WendarrNodes.EXTRACTION_SUPERVISER,
                WendarrNodeHandlers.ExtractionSuperviserParams.class).speed_bonus();
        return 1.0F + bonus * points * scale;
    }

    /** The share of incoming damage an extractor placed by {@code placer} still takes. */
    public static float damageTakenMultiplier(ServerPlayer placer) {
        if (!WendarrNodes.isActive(placer, WendarrNodes.ARMORED_EXTRACTORS)) {
            return 1.0F;
        }
        float reduction = WendarrNodeHandlers.params(WendarrNodes.ARMORED_EXTRACTORS,
                WendarrNodeHandlers.ArmoredExtractorsParams.class).damage_reduction();
        return 1.0F - reduction;
    }
}
