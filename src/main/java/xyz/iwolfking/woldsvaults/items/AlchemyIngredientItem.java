package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;

import java.util.List;

public class AlchemyIngredientItem extends BasicItem {
    private final AlchemyIngredientType type;
    public AlchemyIngredientItem(ResourceLocation id, AlchemyIngredientType type) {
        super(id);
        this.type = type;
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (stack.getTag() == null || stack.getTag().isEmpty()) {
            tooltip.add(new TextComponent("Rotten Ingredient").withStyle(Style.EMPTY.withColor(0x00680A))
                    .append(new TextComponent(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(new TextComponent("Cannot be used in ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                    .append(new TextComponent("the Vault").withStyle(Style.EMPTY.withColor(0xF0E68C)))
            );
            return;
        }
        tooltip.add(getTypeComponent().append(new TextComponent(" Ingredient").withStyle(Style.EMPTY.withColor(0xFFFFFF).withBold(false))));

        //super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }


    public AlchemyIngredientType getType() {
        return type;
    }

    private MutableComponent getTypeComponent() {
        if (type == AlchemyIngredientType.VOLATILE) {
            return ComponentUtils.wavingComponent(new TextComponent(type.name).withStyle(type.style), type.style.getColor(), 0.3F, 0.5F);
        }

        return new TextComponent(type.name).withStyle(type.style);
    }

    public enum AlchemyIngredientType implements StringRepresentable {
        DEADLY(Style.EMPTY.withColor(0x8B0000).withBold(true), "Deadly"),
        RUTHLESS(Style.EMPTY.withColor(0xDC143C), "Ruthless"),
        NEUTRAL(Style.EMPTY.withColor(0xF0E68C), "Neutral"),
        VOLATILE(Style.EMPTY.withColor(0xFF00FF), "Volatile"),
        REFINED(Style.EMPTY.withColor(0x00CED1), "Refined"),
        EMPOWERED(Style.EMPTY.withColor(0xFFD700).withBold(true), "Empowered");

        private final Style style;
        private final String name;

        AlchemyIngredientType(Style style, String name) {
            this.style = style;
            this.name = name;
        }


        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
