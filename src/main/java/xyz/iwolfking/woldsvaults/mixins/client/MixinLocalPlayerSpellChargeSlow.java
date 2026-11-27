package xyz.iwolfking.woldsvaults.mixins.client;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayerSpellChargeSlow {
    @Unique
    private static boolean woldsvaults$loggedMissingTable;

    /**
     * While a spell charges, movement is slowed by the table's {@code tuning.chargeMoveFactor} instead of vanilla's
     * item-use 0.2. Without the table the vanilla slow stays and an error is logged once.
     */
    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F), require = 2)
    private float woldsvaults$spellChargeSlow(float slow) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (!player.isUsingItem() || !SpellcraftItemData.canCarry(player.getUseItem())) {
            return slow;
        }
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null) {
            if (!woldsvaults$loggedMissingTable) {
                woldsvaults$loggedMissingTable = true;
                WoldLogger.error("Spellcraft: no spellcraft table on the client; charging a spell uses the vanilla item-use slow");
            }
            return slow;
        }
        return config.tuning.chargeMoveFactor;
    }
}
