package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import iskallia.vault.container.inventory.CoinPouchContainer;
import iskallia.vault.container.oversized.OverSizedSlotContainer;
import iskallia.vault.container.slot.CompactingSlot;
import iskallia.vault.init.ModSlotIcons;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

@Mixin(value = CoinPouchContainer.class, remap = false)
public abstract class MixinCoinPouchContainer extends OverSizedSlotContainer {

    protected MixinCoinPouchContainer(MenuType<?> menuType, int id, Player player) {
        super(menuType, id, player);
    }

    @Inject(method = "initSlots", at = @At("RETURN"))
    private void woldsvaults$addPalladiumAndIridiumSlots(
            IItemHandler playerInvHandler,
            IItemHandler pouchHandler,
            CallbackInfo ci
    ) {
        if (pouchHandler.getSlots() <= 9) {
            return;
        }

        CompactingSlot goldSlot = null;
        CompactingSlot platSlot = null;
        for (Slot s : this.slots) {
            if (s instanceof CompactingSlot cs) {
                if (cs.getSlotIndex() == 2) {
                    goldSlot = cs;
                } else if (cs.getSlotIndex() == 3) {
                    platSlot = cs;
                }
            }
        }

        int coinRowY = 19;
        CompactingSlot palladiumSlot = (CompactingSlot) this.addSlot(
                new CompactingSlot(pouchHandler, 8, 140, coinRowY, (CoinPouchContainer) (Object) this,
                        ModBlocks.VAULT_PALLADIUM_PILE.asItem(), ModSlotIcons.COINS_NO_ITEM)
        );
        CompactingSlot iridiumSlot = (CompactingSlot) this.addSlot(
                new CompactingSlot(pouchHandler, 9, 164, coinRowY, (CoinPouchContainer) (Object) this,
                        ModBlocks.VAULT_IRIDIUM_PILE.asItem(), ModSlotIcons.COINS_NO_ITEM)
        );

        if (platSlot != null) {
            platSlot.setupSlots(goldSlot, palladiumSlot);
        }
        palladiumSlot.setupSlots(platSlot, iridiumSlot);
        iridiumSlot.setupSlots(palladiumSlot, null);
    }
}