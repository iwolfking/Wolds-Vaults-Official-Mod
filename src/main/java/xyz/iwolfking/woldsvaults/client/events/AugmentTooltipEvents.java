package xyz.iwolfking.woldsvaults.client.events;

import java.util.Set;
import java.util.HashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.data.VaultGearData;
import xyz.iwolfking.woldsvaults.client.spellcraft.GlyphAffixLine;
import com.mojang.datafixers.util.Either;
import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
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
import xyz.iwolfking.woldsvaults.client.spellcraft.ClientGlyphViews;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.gear.EtchingGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GearGlyphLines;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import iskallia.vault.util.TextUtil;
import javax.annotation.Nullable;

/**
 * The spellcraft block at the bottom of a focus's or wand's tooltip. Normally four lines: arcana used of the budget with
 * a bar, a rune per glyph line (spent lines dimmed) and the depth, the spell names, and the Alt hint. With Alt held:
 * every glyph line (rune, name, potency, uses left of rolled, arcana cost, side effects) and every spell (trigger icon,
 * name, arcana, runes, chained abilities, mana and cooldown at the player's haste). Runes and icons are tooltip
 * components ({@link SpellcraftTooltipRow}).
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public class AugmentTooltipEvents {
    private static final Set<String> MISSING_AFFIX_LINES = new HashSet<>();

    /**
     * A gear affix or etching that grants a glyph keeps its own tooltip line and gets the glyph's rune drawn to its
     * right. The line is found by its text (the same calls VH's tooltip makes: an affix's {@code getDisplay}, the
     * etching's formatted description); if it can't be found, the line is added at the end and a warning is logged once.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onGatherGrants(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        List<GearGlyphLines.Grant> grants = GearGlyphLines.grants(stack);
        AugmentType etchingGlyph = EtchingGlyphs.glyphOf(stack);
        if (grants.isEmpty() && etchingGlyph == null) {
            return;
        }
        VaultGearData data = VaultGearData.read(stack);
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        for (GearGlyphLines.Grant grant : grants) {
            Component plain = grant.modifier().getDisplay(data, grant.affixType(), stack, false).orElse(null);
            Component detailed = grant.modifier().getDisplay(data, grant.affixType(), stack, true).orElse(null);
            if (plain != null) {
                attachRune(elements, plain, detailed, grant.glyph(), stack);
            }
        }
        if (etchingGlyph != null) {
            Component description = etchingDescription(stack, data);
            if (description != null) {
                attachRune(elements, description, null, etchingGlyph, stack);
            }
        }
    }

    private static void attachRune(List<Either<FormattedText, TooltipComponent>> elements, Component plain, @Nullable Component detailed,
                                   AugmentType glyph, ItemStack stack) {
        String plainText = plain.getString();
        String detailedText = detailed == null ? null : detailed.getString();
        for (int i = 0; i < elements.size(); i++) {
            FormattedText text = elements.get(i).left().orElse(null);
            if (text == null) {
                continue;
            }
            String shown = text.getString();
            if (shown.equals(plainText) || shown.equals(detailedText)) {
                Component line = text instanceof Component component ? component : new TextComponent(shown);
                elements.set(i, Either.right(new GlyphAffixLine(line, glyph)));
                return;
            }
        }
        if (MISSING_AFFIX_LINES.add(glyph.id() + "|" + plainText)) {
            WoldsVaults.LOGGER.warn("Spellcraft: no tooltip line matches the glyph-granting line '{}' on {}; the glyph line is added at the end",
                    plainText, stack.getItem());
        }
        elements.add(Either.right(new GlyphAffixLine(plain, glyph)));
    }

    @Nullable
    private static Component etchingDescription(ItemStack stack, VaultGearData data) {
        ResourceLocation id = data.getFirstValue(ModGearAttributes.ETCHING).orElse(null);
        VaultEtchingConfig.EtchingEntry config = id == null || ModConfigs.ETCHINGS == null ? null : ModConfigs.ETCHINGS.getEtchingConfig(id);
        if (config == null) {
            return null;
        }
        return TextUtil.applyColorTags(EtchingHelper.formatDescription(config.getDescription(), EtchingHelper.getEtching(stack)))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

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
        SpellcraftRules.ItemCheck check = SpellcraftRules.check(config, stack, ClientGlyphViews.view(config, stack), specOf);
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
                runes.add(new SpellcraftTooltipRow.Rune(line.type(), false));
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
                int left = SpellModel.perSpellLimit(config, line);
                block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.GLYPH, line.type().displayName(),
                        "x" + left, SpellcraftGui.potency(config, line.type(), line.potency()),
                        List.of(new SpellcraftTooltipRow.Rune(line.type(), false)), List.of(), SpellcraftGui.categoryColor(config, line.type()),
                        false, line.cost(), 0, width)));
                String side = SpellcraftGui.sideEffects(config, line.type(), line.potency());
                if (!side.isEmpty()) {
                    block.add(Either.left(colored("     ", SpellcraftGui.DARK).append(SpellcraftGui.tagged(side))));
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
