package xyz.iwolfking.woldsvaults.mixins.client;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayerSpellChargeSlow {
    @Unique
    private static boolean woldsvaults$loggedMissingTable;

    /**
     * While a spell charges (the server is using the spell's focus or wand), movement is slowed by the table's
     * {@code tuning.chargeMoveFactor} instead of vanilla's item-use 0.2. Without the table the vanilla slow stays and an
     * error is logged once.
     */
    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F), require = 2)
    private float woldsvaults$spellChargeSlow(float slow) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (!player.isUsingItem() || !SpellcraftRoller.canCarry(player.getUseItem())) {
            return slow;
        }
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            if (!woldsvaults$loggedMissingTable) {
                woldsvaults$loggedMissingTable = true;
                WoldLogger.error("Spellcraft: no focus augments table on the client; charging a spell uses the vanilla item-use slow");
            }
            return slow;
        }
        return config.tuning.chargeMoveFactor;
    }
}
