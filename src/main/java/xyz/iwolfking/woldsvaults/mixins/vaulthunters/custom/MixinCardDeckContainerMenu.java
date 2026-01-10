package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.container.inventory.CardDeckContainer;
import iskallia.vault.container.inventory.CardDeckContainerMenu;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.item.CardItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.modifiers.deck.NitwitDeckModifier;

@Mixin(value = CardDeckContainerMenu.DeckSlot.class, remap = false)
public abstract class MixinCardDeckContainerMenu extends Slot {

    @Shadow
    private boolean isActive;

    public MixinCardDeckContainerMenu(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Inject(method = "mayPlace", at = @At(value = "HEAD"), cancellable = true)
    private void specialArcanePlacementForNitwit(ItemStack stack, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) ItemStack heldStack) {
        if(container instanceof CardDeckContainer cardDeckContainerMenu) {
            CardDeck deck = cardDeckContainerMenu.getDeck();
            for(DeckModifier<?> mod : deck.getModifiers()) {
                if(mod instanceof NitwitDeckModifier) {
                    if(heldStack.getItem() instanceof CardItem) {
                       cir.setReturnValue(!CardItem.getCard(heldStack).getGroups().contains("Arcane") && isActive);
                    }
                }
            }
        }
    }
}
