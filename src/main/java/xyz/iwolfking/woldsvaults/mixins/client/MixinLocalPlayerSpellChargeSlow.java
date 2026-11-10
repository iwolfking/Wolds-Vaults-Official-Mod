package xyz.iwolfking.woldsvaults.mixins.client;

import iskallia.vault.item.gear.FocusItem;
import iskallia.vault.item.gear.WandItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayerSpellChargeSlow {
    @Unique
    private static boolean woldsvaults$loggedMissingTable;

    /**
     * While a spell charges (the server is using the off-hand focus or wand), movement is slowed by the table's
     * {@code tuning.chargeMoveFactor} instead of vanilla's item-use 0.2. Without the table the vanilla slow stays and an
     * error is logged once.
     */
    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F), require = 2)
    private float woldsvaults$spellChargeSlow(float slow) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (!player.isUsingItem() || player.getUsedItemHand() != InteractionHand.OFF_HAND) {
            return slow;
        }
        Item item = player.getUseItem().getItem();
        if (!(item instanceof FocusItem) && !(item instanceof WandItem)) {
            return slow;
        }
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            if (!woldsvaults$loggedMissingTable) {
                woldsvaults$loggedMissingTable = true;
                WoldsVaults.LOGGER.error("Spellcraft: no focus augments table on the client; charging a spell uses the vanilla item-use slow");
            }
            return slow;
        }
        return config.tuning.chargeMoveFactor;
    }
}
