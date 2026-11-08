package xyz.iwolfking.woldsvaults.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentAbilities;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.item.AugmentItemData;
import xyz.iwolfking.woldsvaults.augments.item.AugmentRoller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The augment block at the bottom of a wand's or focus's tooltip: its rolled augment tokens, then each stored recipe.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AugmentTooltipEvents {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAugmentTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!AugmentRoller.canCarry(stack) || !AugmentItemData.hasRolled(stack)) {
            return;
        }
        Map<AugmentType, Integer> rolled = AugmentItemData.rolled(stack);
        List<Component> block = new ArrayList<>();
        block.add(TextComponent.EMPTY);
        block.add(new TextComponent("Augments").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (rolled.isEmpty()) {
            block.add(new TextComponent("  none").withStyle(ChatFormatting.DARK_GRAY));
        }
        rolled.forEach((type, count) -> block.add(new TextComponent("  " + type.displayName() + (count > 1 ? " x" + count : ""))
                .withStyle(ChatFormatting.GRAY)));
        AugmentItemData.recipes(stack).forEach((ability, glyphs) -> block.add(new TextComponent("  " + AugmentAbilities.displayName(ability)
                + ": " + describe(glyphs)).withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().addAll(block);
    }

    private static String describe(List<String> glyphs) {
        List<String> names = new ArrayList<>();
        for (String glyph : glyphs) {
            AugmentType type = AugmentType.byId(glyph);
            names.add(type != null ? type.displayName() : AugmentAbilities.displayName(glyph));
        }
        return String.join(", ", names);
    }
}
