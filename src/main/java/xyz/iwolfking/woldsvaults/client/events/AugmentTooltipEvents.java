package xyz.iwolfking.woldsvaults.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * The spellcraft block at the bottom of a focus's or wand's tooltip: arcana and depth, then (with Alt held) each glyph
 * line with its roll, uses and cost, and each spell.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public class AugmentTooltipEvents {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSpellcraftTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null || !SpellcraftRoller.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return;
        }
        List<Component> block = new ArrayList<>();
        block.add(TextComponent.EMPTY);
        block.add(new TextComponent("Spellcraft: " + SpellcraftItemData.budget(stack) + " arcana, depth " + SpellcraftItemData.depth(stack))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!Screen.hasAltDown()) {
            block.add(new TextComponent("Hold Alt for glyphs and spells").withStyle(ChatFormatting.DARK_GRAY));
            event.getToolTip().addAll(block);
            return;
        }
        for (SpellcraftItemData.Line line : SpellcraftItemData.lines(stack)) {
            block.add(new TextComponent("  " + line.type().displayName() + " " + potency(config, line) + "  x" + line.uses()
                    + "  cost " + line.cost()).withStyle(ChatFormatting.GRAY));
        }
        for (SpellcraftItemData.Spell spell : SpellcraftItemData.spells(stack)) {
            block.add(new TextComponent("  " + spell.name() + ": " + String.join(" ", spell.tokens())).withStyle(ChatFormatting.DARK_PURPLE));
        }
        event.getToolTip().addAll(block);
    }

    /** A line's potency in its table format (percent, seconds, blocks). */
    public static String potency(FocusAugmentsConfig config, SpellcraftItemData.Line line) {
        FocusAugmentsConfig.Potency potency = config.glyph(line.type()).potency;
        float p = line.potency();
        return switch (potency.format) {
            case "pct" -> "+" + Math.round(p * 100) + "% " + potency.label;
            case "neg_pct" -> "-" + Math.round(p * 100) + "% " + potency.label;
            case "pct_plain" -> Math.round(p * 100) + "% " + potency.label;
            case "seconds" -> potency.label + " " + String.format("%.2fs", p);
            case "blocks" -> potency.label + " " + Math.round(p);
            default -> potency.label + " " + p;
        };
    }
}
