package xyz.iwolfking.woldsvaults.items;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.common.slottype.SlotType;

import java.util.*;

public class TrinketPouchItem extends BasicItem implements ICurioItem {
    private final Map<String, Integer> curioSlotsToAdd;

    public TrinketPouchItem(ResourceLocation id, Map<String, Integer> curioSlotsToAdd) {
        super(id, new Properties().stacksTo(1));
        this.curioSlotsToAdd = curioSlotsToAdd;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
       Multimap<Attribute, AttributeModifier> map = LinkedHashMultimap.create();
       for(String slotType : curioSlotsToAdd.keySet()) {
           CuriosApi.getCuriosHelper().addSlotModifier(map, slotType, uuid, curioSlotsToAdd.get(slotType), AttributeModifier.Operation.ADDITION);
       }
        return map;
    }

    @Override
    public BasicItem withTooltip(List<Component> tooltip) {
        for(String slotType : curioSlotsToAdd.keySet()) {
            ISlotType curioType = CuriosApi.getSlotHelper().getSlotType(slotType).orElse(null);
            if(curioType != null) {
                tooltip.add(new TextComponent("+" + curioSlotsToAdd.get(slotType) + " " + curioType.getIdentifier() + " slots").withStyle(slotTypeColor(slotType)));
            }

        }
        return super.withTooltip(tooltip);
    }

    private static Style slotTypeColor(String type) {
        return switch (type) {
            case "blue_trinket" -> Style.EMPTY.withColor(ChatFormatting.BLUE);
            case "red_trinket" -> Style.EMPTY.withColor(ChatFormatting.RED);
            case "green_trinket" -> Style.EMPTY.withColor(ChatFormatting.GREEN);
            default -> Style.EMPTY.withColor(ChatFormatting.WHITE);
        };
    }
}
