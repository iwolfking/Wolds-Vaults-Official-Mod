package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.container.inventory.CardDeckContainer;
import iskallia.vault.container.inventory.CardDeckContainerMenu;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.modifiers.deck.NitwitDeckModifier;

@Mixin(value = CardDeckContainerMenu.DeckSlot.class, remap = false)
public abstract class MixinCardDeckContainerMenu extends Slot {

    @Shadow
    public abstract boolean isArcaneOnly();

    public MixinCardDeckContainerMenu(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Redirect(method = "mayPlace", at = @At(value = "INVOKE", target = "Liskallia/vault/container/inventory/CardDeckContainerMenu$DeckSlot;isArcaneOnly()Z"))
    private boolean test(CardDeckContainerMenu.DeckSlot instance) {
        if(container instanceof CardDeckContainer cardDeckContainerMenu) {
            CardDeck deck = cardDeckContainerMenu.getDeck();
            for(DeckModifier<?> mod : deck.getModifiers()) {
                System.out.println(mod.getName());
                if(mod instanceof NitwitDeckModifier) {
                    System.out.println("Placing");
                    System.out.println("Allowing placement due to Nitwit modifier.");
                    return !instance.isArcaneOnly();
                }
            }
        }

        return isArcaneOnly();
    }
}
