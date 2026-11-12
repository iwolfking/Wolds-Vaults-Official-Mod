package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * A Vault Fabricator pattern. The blank card is crafted; placing it in a Fabricator and choosing a component
 * imprints it into the encoded card, which names the component (a key of {@code extractor_costs.json} COMPONENTS)
 * the Fabricator then builds. Patterns are never consumed.
 */
public class FabricationPatternItem extends BasicItem {
    public static final String TAG_COMPONENT = "Component";

    private final boolean encoded;

    public FabricationPatternItem(ResourceLocation id, Properties properties, boolean encoded) {
        super(id, properties);
        this.encoded = encoded;
    }

    public boolean isEncoded() {
        return this.encoded;
    }

    public static ItemStack encode(String componentKey) {
        ItemStack stack = new ItemStack(ModItems.FABRICATION_PATTERN);
        stack.getOrCreateTag().putString(TAG_COMPONENT, componentKey);
        return stack;
    }

    @Nullable
    public static String componentKey(ItemStack stack) {
        if (!(stack.getItem() instanceof FabricationPatternItem pattern) || !pattern.encoded || !stack.hasTag()) {
            return null;
        }
        String key = stack.getTag().getString(TAG_COMPONENT);
        return key.isEmpty() ? null : key;
    }

    @Nullable
    public static ExtractorCostsConfig.ComponentEntry recipe(ItemStack stack) {
        String key = componentKey(stack);
        return key == null || ModConfigs.EXTRACTOR_COSTS == null ? null : ModConfigs.EXTRACTOR_COSTS.componentFor(key);
    }

    /** The display name of what a component recipe makes. */
    public static Component productName(ExtractorCostsConfig.ComponentEntry recipe) {
        Item item = ExtractorCosts.resolveItem(recipe.item);
        return item == null ? new TextComponent(recipe.item) : new ItemStack(item).getHoverName();
    }

    @Override
    public Component getName(ItemStack stack) {
        ExtractorCostsConfig.ComponentEntry recipe = recipe(stack);
        if (recipe == null) {
            return super.getName(stack);
        }
        return new TextComponent("Fabrication Pattern: ").append(productName(recipe));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (!this.encoded) {
            tooltip.add(new TextComponent("Place in a Vault Fabricator and choose a component to imprint it.").withStyle(ChatFormatting.GRAY));
            return;
        }
        ExtractorCostsConfig.ComponentEntry recipe = recipe(stack);
        if (recipe == null) {
            tooltip.add(new TextComponent("Unknown component; re-imprint it in a Vault Fabricator.").withStyle(ChatFormatting.RED));
            return;
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int cell = 0; cell < 25; cell++) {
            String id = recipe.cell(cell);
            if (id != null) {
                counts.merge(id, 1, Integer::sum);
            }
        }
        counts.forEach((id, count) -> {
            Item item = ExtractorCosts.resolveItem(id);
            Component name = item == null ? new TextComponent(id) : new ItemStack(item).getHoverName();
            tooltip.add(new TextComponent("  " + count + "x ").append(name).withStyle(ChatFormatting.GRAY));
        });
        if (!recipe.fluid.isEmpty() && recipe.fluidAmount > 0) {
            ExtractorCosts.CostLine fluid = new ExtractorCosts.CostLine(recipe.fluid, true, recipe.fluidAmount);
            tooltip.add(new TextComponent("  " + recipe.fluidAmount + " mB ").append(fluid.displayName()).withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(new TextComponent(String.format("  %,d FE over %.1f s", recipe.fe, recipe.ticks / 20.0F)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
