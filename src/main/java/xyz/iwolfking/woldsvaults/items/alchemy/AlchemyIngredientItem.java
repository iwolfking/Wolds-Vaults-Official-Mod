package xyz.iwolfking.woldsvaults.items.alchemy;

import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.util.ColorUtil;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    public static int getMixedColor(List<ItemStack> ingredients) {
        List<Integer> ingredientColors = new ArrayList<>();
        for (ItemStack i : ingredients) {
            if (i.getItem() instanceof AlchemyIngredientItem ingredient) {
                ingredientColors.add(ingredient.getType().getStyle().getColor().getValue());
            }
        }

        return ColorUtil.mixColors(ingredientColors);
    }

    public static List<ItemStack> filterForAlchemyIngredients(List<ItemStack> items) {
        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack stack : items) {
            if (stack.getItem() instanceof AlchemyIngredientItem) {
                filtered.add(stack);
            }
        }

        return filtered;
    }

    public static MutableComponent getEffectComponent(AlchemyIngredientType type, int percentage) {
        return switch(type) {
            case DEADLY -> new TextComponent(percentage + "% Chance ").withStyle(Style.EMPTY.withColor(0xF0E68C))
                        .append(new TextComponent("to apply a ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                        .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                        .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                        .append(new TextComponent("Modifier").withStyle(Style.EMPTY.withColor(0xFFFFFF)));
            case RUTHLESS -> new TextComponent(percentage + "% Chance ").withStyle(Style.EMPTY.withColor(0xF0E68C))
                    .append(new TextComponent("to apply a ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                    .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                    .append(new TextComponent("Modifier").withStyle(Style.EMPTY.withColor(0xFFFFFF)));
            case NEUTRAL -> new TextComponent("");
            case VOLATILE -> new TextComponent(percentage + "% Chance ").withStyle(Style.EMPTY.withColor(0xF0E68C))
                    .append(new TextComponent("to apply a ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                    .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                    .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                    .append(new TextComponent("and a ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                    .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                    .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                    .append(new TextComponent("Modifier").withStyle(Style.EMPTY.withColor(0xFFFFFF)));
            case REFINED -> new TextComponent(percentage + "% Chance ").withStyle(Style.EMPTY.withColor(0xF0E68C))
                    .append(new TextComponent("to apply a ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                    .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                    .append(new TextComponent("Modifier").withStyle(Style.EMPTY.withColor(0xFFFFFF)));
            case EMPOWERED -> new TextComponent(percentage + "% Chance ").withStyle(Style.EMPTY.withColor(0xF0E68C))
                    .append(new TextComponent("to apply a ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                    .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                    .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                    .append(new TextComponent("Modifier").withStyle(Style.EMPTY.withColor(0xFFFFFF)));
        };
    }


    public enum AlchemyIngredientType implements StringRepresentable {

        DEADLY(Style.EMPTY.withColor(0x8B0000), "Deadly", new String[]{"Vile", "Assasin", "Scourge"}),
        RUTHLESS(Style.EMPTY.withColor(0xDC143C), "Ruthless", new String[]{"Brutal", "Slayer", "Force"}),
        NEUTRAL(Style.EMPTY.withColor(0xF0E68C), "Neutral", new String[]{}),
        VOLATILE(Style.EMPTY.withColor(0xFF00FF), "Volatile", new String[]{"Wild", "Surge", "Burst"}),
        REFINED(Style.EMPTY.withColor(0x00CED1), "Refined", new String[]{"Pure", "Clarity", "Flow"}),
        EMPOWERED(Style.EMPTY.withColor(0xFFD700), "Empowered", new String[]{"Radiant", "Power", "Glory"});

        private final Style style;
        private final String name;
        private String[] words;

        AlchemyIngredientType(Style style, String name, String[] words) {
            this.style = style;
            this.name = name;
            this.words = words;
        }

        public Style getStyle() {
            return style;
        }


        public String getWord(int index) {
            return index < words.length ? words[index] : "";
        }

        public static MutableComponent generatePotionNameComponent(List<AlchemyIngredientType> types) {
            List<AlchemyIngredientType> filtered = types.stream()
                    .distinct()
                    .filter(t -> t != AlchemyIngredientType.NEUTRAL)
                    .limit(3)
                    .toList();

            MutableComponent component = new TextComponent("");

            if (!filtered.isEmpty()) {
                component.append(new TextComponent(filtered.get(0).getWord(0)).withStyle(filtered.get(0).getStyle()));
            }

            if (filtered.size() > 1) {
                component.append(" ");
                component.append(new TextComponent(filtered.get(1).getWord(1)).withStyle(filtered.get(1).getStyle()));
            }

            if (filtered.size() > 2) {
                component.append(" ");
                component.append(new TextComponent(filtered.get(2).getWord(2)).withStyle(filtered.get(2).getStyle()));
            }

            if (component.getString().isEmpty()) {
                component.append(new TextComponent("Plain").withStyle(Style.EMPTY.withColor(0xAAAAAA)));
            }

            component.append(new TextComponent(" Brew").withStyle(Style.EMPTY.withColor(0xCCCCCC)));

            return component;
        }


        public static List<AlchemyIngredientType> getTypes(List<ItemStack> ingredients) {
            return ingredients.stream()
                    .filter(stack -> stack.getItem() instanceof AlchemyIngredientItem)
                    .map(stack -> ((AlchemyIngredientItem) stack.getItem()).getType())
                    .collect(Collectors.toList());
        }



        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
