package xyz.iwolfking.woldsvaults.client.spellcraft.gui;

import com.mojang.datafixers.util.Either;
import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientGlyphViews;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.SlotCostStats;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.EtchingLines;
import xyz.iwolfking.woldsvaults.spellcraft.source.GearAffixLines;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Spellcraft additions to item tooltips: the rune beside each glyph-granting affix or etching line, and the spellcraft
 * block of an item that carries spells. The block is gathered at low priority, after the rune pass and other
 * normal-priority additions.
 */
public class SpellcraftTooltips {
    private static final Set<String> MISSING_AFFIX_LINES = new HashSet<>();

    /**
     * A gear affix or etching that grants a glyph keeps its own tooltip line, found by matching the text Vault Hunters'
     * tooltip shows, with the glyph's rune drawn to its right. A line that cannot be found is added at the end with an
     * error logged once.
     */
    public static void onGatherGrants(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        List<GearAffixLines.Grant> grants = GearAffixLines.grants(stack);
        GlyphType etchingGlyph = EtchingLines.glyphOf(stack);
        if (grants.isEmpty() && etchingGlyph == null) {
            return;
        }
        VaultGearData data = VaultGearData.read(stack);
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        for (GearAffixLines.Grant grant : grants) {
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
                                   GlyphType glyph, ItemStack stack) {
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
            WoldLogger.error("Spellcraft: no tooltip line matches the glyph-granting line '{}' on {}; the glyph line is added at the end",
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

    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        SpellcraftConfig config = xyz.iwolfking.woldsvaults.init.ModConfigs.SPELLCRAFT;
        if (config == null || !SpellcraftItemData.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return;
        }
        List<Either<FormattedText, TooltipComponent>> block = new ArrayList<>();
        int[] width = {0};
        AbilityTree tree = ClientAbilityData.getTree();
        Function<String, String> specOf = AbilityLookup.specOf(tree);
        SpellcraftRules.ItemCheck check = SpellcraftRules.check(config, stack, ClientGlyphViews.view(config, stack), specOf);
        List<GlyphLine> lines = SpellcraftItemData.lines(stack);
        List<StoredSpell> spells = SpellcraftItemData.spells(stack);
        int budget = SpellcraftItemData.budget(stack);
        int depth = SpellcraftItemData.depth(stack);
        block.add(Either.left(TextComponent.EMPTY));
        block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.HEAD, SpellcraftNames.tr("tooltip.title"),
                Math.round(check.arcana()) + "/" + budget, "", List.of(), List.of(), SpellcraftPalette.ARCANE_GLOW, check.arcana(), budget, width)));
        if (!Screen.hasAltDown()) {
            List<SpellcraftTooltipRow.Rune> runes = new ArrayList<>();
            for (GlyphLine line : lines) {
                runes.add(new SpellcraftTooltipRow.Rune(line.type()));
            }
            block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.RUNES, SpellcraftNames.tr("tooltip.depth", depth), "", "",
                    runes, List.of(), SpellcraftPalette.GREY, 0, 0, width)));
            List<String> names = new ArrayList<>();
            spells.forEach(spell -> names.add(spell.name()));
            MutableComponent spellLine = SpellcraftNames.colored(SpellcraftNames.tr("tooltip.spells"), SpellcraftPalette.GREY)
                    .append(SpellcraftNames.colored(names.isEmpty() ? SpellcraftNames.tr("tooltip.no_spells") : String.join(", ", names), SpellcraftPalette.ARCANE_GLOW));
            block.add(Either.left(spellLine));
            block.add(Either.left(SpellcraftNames.colored(SpellcraftNames.tr("tooltip.hold_alt"), SpellcraftPalette.DARK)));
        } else {
            block.add(Either.left(SpellcraftNames.colored(SpellcraftNames.tr("tooltip.legend", depth), SpellcraftPalette.DARK)));
            block.add(Either.left(SpellcraftNames.colored(SpellcraftNames.tr("tooltip.glyphs"), SpellcraftPalette.WHITE)));
            for (GlyphLine line : lines) {
                int left = GlyphView.ownLimit(config, line);
                block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.GLYPH, line.type().displayName(),
                        "x" + left, GlyphText.potency(config, line.type(), line.potency()),
                        List.of(new SpellcraftTooltipRow.Rune(line.type())), List.of(), SpellcraftPalette.categoryColor(config, line.type()),
                        line.cost(), 0, width)));
                String side = GlyphText.sideEffects(config, line.type(), line.potency());
                if (!side.isEmpty()) {
                    block.add(Either.left(SpellcraftNames.colored("     ", SpellcraftPalette.DARK).append(GlyphText.tagged(side))));
                }
            }
            block.add(Either.left(SpellcraftNames.colored(SpellcraftNames.tr("tooltip.spells_header"), SpellcraftPalette.WHITE)));
            for (StoredSpell spell : spells) {
                SpellModel model = check.models().get(spell.id());
                String trigger = spell.trigger();
                String spec = specOf.apply(trigger);
                String label = SpellcraftNames.tr("tooltip.spell", spell.name(), SpellcraftNames.groupName(tree, trigger));
                block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.SPELL, label, "", "", List.of(),
                        List.of(spec != null ? spec : trigger), SpellcraftPalette.ARCANE_GLOW, model == null ? 0 : model.arcana(), 0, width)));
                List<SpellcraftTooltipRow.Rune> runes = new ArrayList<>();
                List<String> chained = new ArrayList<>();
                for (int i = 0; i < spell.tokens().size(); i++) {
                    String token = spell.tokens().get(i);
                    GlyphType type = GlyphType.byId(token);
                    if (type != null) {
                        runes.add(new SpellcraftTooltipRow.Rune(type));
                    } else if (i > 0) {
                        String chainedSpec = specOf.apply(token);
                        chained.add(chainedSpec != null ? chainedSpec : token);
                    }
                }
                String numbers = "";
                SlotCostStats.Numbers stats = model == null ? null : SlotCostStats.spell(config, tree, model);
                if (stats != null) {
                    numbers = StatText.numbers("tooltip.numbers", stats);
                }
                block.add(Either.right(new SpellcraftTooltipRow(SpellcraftTooltipRow.Kind.SPELL_ROW, numbers, "", "", runes, chained,
                        SpellcraftPalette.GREY, 0, 0, width)));
            }
        }
        Font font = Minecraft.getInstance().font;
        for (Either<FormattedText, TooltipComponent> element : block) {
            int w = element.map(text -> 0, component -> component instanceof SpellcraftTooltipRow row ? row.naturalWidth(font) : 0);
            width[0] = Math.max(width[0], w);
        }
        event.getTooltipElements().addAll(block);
    }
}
