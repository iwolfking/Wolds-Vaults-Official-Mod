package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.SlotDeckModifier;
import iskallia.vault.core.random.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.SlotDeckModifierAccessor;

import java.util.ArrayList;
import java.util.List;

public class ArcaneSlotDeckModifier extends SlotDeckModifier {

    public ArcaneSlotDeckModifier(SlotDeckModifier.Config config) {
        super(config);
    }

    public ArcaneSlotDeckModifier() {
        super(new SlotDeckModifier.Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public void populateOnApply(CardDeck deck, RandomSource rand) {
        List<CardPos> availablePositions = new ArrayList<>(deck.getSlots());
        availablePositions.removeIf((pos) -> !pos.allowedGroups.contains("Arcane"));

        for(int i = 0; i < ((SlotDeckModifierAccessor)this).getSlotRoll(); ++i) {
            CardPos position = availablePositions.isEmpty() ? null : availablePositions.get(rand.nextInt(availablePositions.size()));
            if (position != null) {
                this.getAffectedSlots().add(position);
                availablePositions.remove(position);
            }
        }
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        tooltip.add(new TextComponent(((SlotDeckModifierAccessor)this).getSlotRoll() + " Arcane slots gains " + Math.round(this.getModifierValue()) + " additional levels"));
    }
}
