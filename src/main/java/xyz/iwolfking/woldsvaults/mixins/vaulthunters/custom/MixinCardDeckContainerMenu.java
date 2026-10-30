package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.container.CardBinderContainer;
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
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.DeckContainerWrapperAccessor;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.NitwitDeckModifier;

@Mixin(value = CardDeckContainerMenu.DeckSlot.class, remap = false)
public abstract class MixinCardDeckContainerMenu extends Slot {

    @Shadow
    private boolean isActive;

    public MixinCardDeckContainerMenu(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Inject(method = "mayPlace", at = @At(value = "HEAD"), cancellable = true, remap = true)
    private void specialArcanePlacementForNitwit(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(container instanceof CardDeckContainer || container.getClass().getName().contains("DeckContainerWrapper")) {
            CardDeck deck;
            if(container instanceof CardDeckContainer cardDeckContainer) {
                deck = cardDeckContainer.getDeck();
            }
            else {
                Container delegateContainer = ((DeckContainerWrapperAccessor)container).getDelegate();
                if(delegateContainer instanceof CardDeckContainer cardDeckContainer) {
                    deck = cardDeckContainer.getDeck();
                }
                else {
                    return;
                }
            }

            for(DeckModifier<?> mod : deck.getModifiers()) {
                if(mod instanceof NitwitDeckModifier || (mod instanceof ImplicitDeckModifier implicitDeckModifier && implicitDeckModifier.getModifier() instanceof NitwitDeckModifier)) {
                    if(stack.getItem() instanceof CardItem) {
                        cir.setReturnValue(!CardItem.getCard(stack).getGroups().contains("Arcane") && isActive);
                    }
                }
            }
        }
    }
}
