package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.item.CardDeckItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.CardDeckAccessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(value = CardDeckItem.class, remap = false)
public class CardDeckItemMixin {

    /**
     * @author iwolfking
     * @reason Render live preview of card deck instead of just default layout.
     */
    @Overwrite(remap = true)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        Optional<CardDeck> deckOpt = CardDeckItem.getCardDeck(stack);
        
        if (deckOpt.isPresent()) {
            CardDeck deck = deckOpt.get();
            deck.addText(tooltip, tooltip.size(), flag, 0.0F);
            
            tooltip.add(TextComponent.EMPTY);
            tooltip.add(new TextComponent("Layout:").withStyle(ChatFormatting.GRAY));

            Map<CardPos, Card> liveCardsMap = ((CardDeckAccessor) deck).getCardsMap();
            
            if (liveCardsMap != null && !liveCardsMap.isEmpty()) {
                int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
                int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

                for (CardPos pos : liveCardsMap.keySet()) {
                    minX = Math.min(minX, pos.x);
                    maxX = Math.max(maxX, pos.x);
                    minY = Math.min(minY, pos.y);
                    maxY = Math.max(maxY, pos.y);
                }

                int normalSlotsCount = 0;
                int arcaneSlotsCount = 0;

                for (int y = minY; y <= maxY; y++) {
                    MutableComponent rowComponent = new TextComponent(" ");
                    
                    for (int x = minX; x <= maxX; x++) {
                        CardPos structuralMatch = null;
                        for (CardPos pos : liveCardsMap.keySet()) {
                            if (pos.x == x && pos.y == y) {
                                structuralMatch = pos;
                                break;
                            }
                        }

                        Optional<MutableComponent> cardComponent = woldsVaults$getCardIconComponent(deck, structuralMatch);

                        if(cardComponent.isPresent()) {
                            rowComponent.append(cardComponent.get());
                        }
                        else if (structuralMatch != null) {
                            if (structuralMatch.allowedGroups != null && structuralMatch.allowedGroups.contains("Arcane")) {
                                arcaneSlotsCount++;
                                rowComponent.append(new TextComponent("◆").withStyle(ChatFormatting.LIGHT_PURPLE));
                            } else {
                                normalSlotsCount++;
                                rowComponent.append(new TextComponent("■").withStyle(ChatFormatting.GOLD));
                            }
                        } else {
                            rowComponent.append(new TextComponent("□").withStyle(ChatFormatting.DARK_GRAY));
                        }
                        rowComponent.append(" ");
                    }
                    tooltip.add(rowComponent);
                }

                tooltip.add(TextComponent.EMPTY);
                if (arcaneSlotsCount > 0) {
                    tooltip.add(new TextComponent("◆").withStyle(ChatFormatting.LIGHT_PURPLE)
                            .append(new TextComponent(" = Arcane").withStyle(ChatFormatting.DARK_GRAY))
                            .append(new TextComponent(" (" + arcaneSlotsCount + ")").withStyle(ChatFormatting.LIGHT_PURPLE)));
                }
                tooltip.add(new TextComponent("■").withStyle(ChatFormatting.GOLD)
                        .append(new TextComponent(" = Empty Slot").withStyle(ChatFormatting.DARK_GRAY))
                        .append(new TextComponent(" (" + normalSlotsCount + ")").withStyle(ChatFormatting.GOLD)));
                tooltip.add(new TextComponent("□").withStyle(ChatFormatting.DARK_GRAY)
                        .append(new TextComponent(" = No Slot").withStyle(ChatFormatting.DARK_GRAY)));
            }
        } else {
            CardDeckItem.appendLayoutPreview(CardDeckItem.getId(stack), tooltip, false);
        }
    }

    @Unique
    private Optional<MutableComponent> woldsVaults$getCardIconComponent(CardDeck deck, CardPos pos) {
        if(deck.getCard(pos).isPresent()) {
            Card card = deck.getCard(pos).get();
            if(card.getColor() != null && card.getGroups() != null) {
                return Optional.of(new TextComponent(woldsVaults$getGroupIcon(card.getGroups())).withStyle(card.getColor().getColoredText().getStyle()));
            }
        }

        return Optional.empty();
    }

    @Unique
    private String woldsVaults$getGroupIcon(List<String> groups) {
        if(groups.contains("Arcane")) {
            return "A";
        }
        else if(groups.contains("Greed")) {
            return "G";
        }
        else if(groups.contains("Evolution")) {
            return "E";
        }
        else if(groups.contains("Resource")) {
            return "R";
        }
        else if(groups.contains("Knack")) {
            return "K";
        }
        else if(groups.contains("Temporal")) {
            return "T";
        }
        else if(groups.contains("Shiny")) {
            return "✮";
        }
        else if(groups.contains("Deluxe")) {
            return "D";
        }
        else if(groups.contains("Stat")) {
            return "S";
        }

        return "■";
    }
}