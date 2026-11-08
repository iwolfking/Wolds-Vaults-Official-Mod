package xyz.iwolfking.woldsvaults.client.events;

import com.mojang.datafixers.util.Either;
import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.client.spellcraft.ClientSpellStats;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellcraftGui;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellcraftTooltipRow;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The spellcraft block at the bottom of a focus's or wand's tooltip. Normally four lines: arcana used of the budget with
 * a bar, a rune per glyph line (spent lines dimmed) and the depth, the spell names, and the Alt hint. With Alt held:
 * every glyph line (rune, name, potency, uses left of rolled, arcana cost, side effects) and every spell (trigger icon,
 * name, arcana, runes, chained abilities, mana and cooldown at the player's haste). Runes and icons are tooltip
 * components ({@link SpellcraftTooltipRow}).
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public class AugmentTooltipEvents {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null || !SpellcraftRoller.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return;
        }
        List<Either<FormattedText, TooltipComponent>> block = new ArrayList<>();
        int[] width = {0};
        AbilityTree tree = ClientAbilityData.getTree();
        Function<String, String> specOf = SpellcraftRules.specOf(tree);
        SpellcraftRules.ItemCheck check = SpellcraftRules.check(config, stack, specOf, false);
        List<SpellcraftItemData.Line> lines = SpellcraftItemData.lines(stack);
        List<SpellcraftItemData.Spell> spells = SpellcraftItemData.spells(stack);
        int budget = SpellcraftItemData.budget(stack);
        int depth = SpellcraftItemData.depth(stack);
        block.add(Either.left(TextComponent.EMPTY));
        block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.HEAD, SpellcraftGui.tr("tooltip.title"),
                Math.round(check.arcana()) + "/" + budget, "", List.of(), List.of(), SpellcraftGui.ARCANE_GLOW, false, check.arcana(), budget, width)));
        if (!Screen.hasAltDown()) {
            List<SpellcraftTooltipRow.Rune> runes = new ArrayList<>();
            for (SpellcraftItemData.Line line : lines) {
                runes.add(new SpellcraftTooltipRow.Rune(line.type(), check.uses().getOrDefault(line.type(), 0) >= line.uses()));
            }
            block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.RUNES, SpellcraftGui.tr("tooltip.depth", depth), "", "",
                    runes, List.of(), SpellcraftGui.GREY, false, 0, 0, width)));
            List<String> names = new ArrayList<>();
            spells.forEach(spell -> names.add(spell.name()));
            MutableComponent spellLine = colored(SpellcraftGui.tr("tooltip.spells"), SpellcraftGui.GREY)
                    .append(colored(names.isEmpty() ? SpellcraftGui.tr("tooltip.no_spells") : String.join(", ", names), SpellcraftGui.ARCANE_GLOW));
            block.add(Either.left(spellLine));
            block.add(Either.left(colored(SpellcraftGui.tr("tooltip.hold_alt"), SpellcraftGui.DARK)));
        } else {
            block.add(Either.left(colored(SpellcraftGui.tr("tooltip.legend", depth), SpellcraftGui.DARK)));
            block.add(Either.left(colored(SpellcraftGui.tr("tooltip.glyphs"), SpellcraftGui.WHITE)));
            for (SpellcraftItemData.Line line : lines) {
                int left = line.uses() - check.uses().getOrDefault(line.type(), 0);
                block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.GLYPH, line.type().displayName(),
                        Math.max(0, left) + "/" + line.uses(), SpellcraftGui.potency(config, line.type(), line.potency()),
                        List.of(new SpellcraftTooltipRow.Rune(line.type(), left <= 0)), List.of(), SpellcraftGui.categoryColor(config, line.type()),
                        left <= 0, line.cost(), 0, width)));
                String side = SpellcraftGui.sideEffects(config, line.type(), line.potency());
                if (!side.isEmpty()) {
                    block.add(Either.left(colored("     " + side, SpellcraftGui.DARK)));
                }
            }
            block.add(Either.left(colored(SpellcraftGui.tr("tooltip.spells_header"), SpellcraftGui.WHITE)));
            for (SpellcraftItemData.Spell spell : spells) {
                SpellModel model = check.models().get(spell.id());
                String trigger = spell.trigger();
                String spec = specOf.apply(trigger);
                String label = SpellcraftGui.tr("tooltip.spell", spell.name(), SpellcraftGui.groupName(tree, trigger));
                block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.SPELL, label, "", "", List.of(),
                        List.of(spec != null ? spec : trigger), SpellcraftGui.ARCANE_GLOW, false, model == null ? 0 : model.arcana(), 0, width)));
                List<SpellcraftTooltipRow.Rune> runes = new ArrayList<>();
                List<String> chained = new ArrayList<>();
                for (int i = 0; i < spell.tokens().size(); i++) {
                    String token = spell.tokens().get(i);
                    AugmentType type = AugmentType.byId(token);
                    if (type != null) {
                        runes.add(new SpellcraftTooltipRow.Rune(type, false));
                    } else if (i > 0) {
                        String chainedSpec = specOf.apply(token);
                        chained.add(chainedSpec != null ? chainedSpec : token);
                    }
                }
                String numbers = "";
                ClientSpellStats.Numbers stats = model == null ? null : ClientSpellStats.spell(config, tree, model);
                if (stats != null) {
                    String cooldown = SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(stats.cooldownAfter()));
                    numbers = stats.hasMana() ? SpellcraftGui.tr("tooltip.numbers", Math.round(stats.manaAfter()), cooldown) : cooldown;
                }
                block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.SPELL_ROW, numbers, "", "", runes, chained,
                        SpellcraftGui.GREY, false, 0, 0, width)));
            }
        }
        Font font = Minecraft.getInstance().font;
        for (Either<FormattedText, TooltipComponent> element : event.getTooltipElements()) {
            width[0] = Math.max(width[0], element.left().map(font::width).orElse(0));
        }
        for (Either<FormattedText, TooltipComponent> element : block) {
            int w = element.map(font::width, component -> component instanceof SpellcraftTooltipRow row ? row.naturalWidth(font) : 0);
            width[0] = Math.max(width[0], w);
        }
        event.getTooltipElements().addAll(block);
    }

    private static MutableComponent colored(String text, int color) {
        return new TextComponent(text).withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF)));
    }
}
