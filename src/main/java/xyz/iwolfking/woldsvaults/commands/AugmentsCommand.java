package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBars;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellCooldowns;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphSources;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.pool.ViewLine;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.world.data.DiscoveredModelsData;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import iskallia.vault.init.ModItems;
import java.util.Locale;
import net.minecraft.commands.SharedSuggestionProvider;

/**
 * {@code /wvspellcraft}: test tools for the spell bar and focus spellcraft, next to the screens. {@code show} prints the
 * off-hand item's implicits and spells with their glyph states; {@code view} prints the off-hand item's glyph view and
 * the auto lines; {@code bar <slot> <ability|clear>} sets a default bar slot; {@code spell add <tokens>} and
 * {@code spell remove <id>} edit the off-hand item's spells; {@code slot <spell id> <slot|0>} puts a spell on a bar slot
 * (0 takes it off) and {@code prio <spell id> <priority>} sets its priority. Operators: {@code reroll} rerolls the held
 * focus or wand; {@code givefocus <level> <rarity> [wand]} gives an identified focus or wand of that level and rarity.
 */
public final class AugmentsCommand {
    private static final int OFFHAND_SLOT = 40;

    private AugmentsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvspellcraft")
                .then(Commands.literal("show").executes(context -> show(context.getSource())))
                .then(Commands.literal("bar")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, SpellcraftItemData.SLOTS))
                                .then(Commands.argument("ability", StringArgumentType.word())
                                        .executes(AugmentsCommand::setDefault))))
                .then(Commands.literal("view").executes(context -> view(context.getSource())))
                .then(Commands.literal("slot")
                        .then(Commands.argument("spell", IntegerArgumentType.integer(1))
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, SpellcraftItemData.SLOTS))
                                        .executes(AugmentsCommand::setSlot))))
                .then(Commands.literal("prio")
                        .then(Commands.argument("spell", IntegerArgumentType.integer(1))
                                .then(Commands.argument("priority", IntegerArgumentType.integer(1, 99))
                                        .executes(AugmentsCommand::setPrio))))
                .then(Commands.literal("spell")
                        .then(Commands.literal("add").then(Commands.argument("tokens", StringArgumentType.greedyString())
                                .executes(AugmentsCommand::addSpell)))
                        .then(Commands.literal("remove").then(Commands.argument("id", IntegerArgumentType.integer(1))
                                .executes(AugmentsCommand::removeSpell))))
                .then(Commands.literal("reroll").requires(source -> source.hasPermission(2))
                        .executes(context -> reroll(context.getSource())))
                .then(Commands.literal("givefocus").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 100))
                                .then(Commands.argument("rarity", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(VaultGearRarity.values()).map(Enum::name), builder))
                                        .executes(context -> giveFocus(context, false))
                                        .then(Commands.literal("wand").executes(context -> giveFocus(context, true)))))));
    }

    private static int show(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        FocusAugmentsConfig config = FocusAugments.config();
        ItemStack stack = player.getOffhandItem();
        if (config == null || !SpellcraftItemData.has(stack)) {
            source.sendFailure(new TextComponent("Hold a focus or wand with spellcraft in your off hand"));
            return 0;
        }
        GlyphView view = GlyphSources.get(player).view(config, stack);
        SpellcraftRules.ItemCheck check = SpellcraftRules.check(config, stack, view, SpellcraftRules.specOf(SpellCooldowns.tree(player)));
        line(source, "Arcana " + Math.round(check.arcana()) + "/" + SpellcraftItemData.budget(stack) + ", depth "
                + SpellcraftItemData.depth(stack) + (check.budgetInactive() > 0 ? ", over budget: " + check.budgetInactive() + " glyphs inactive" : ""),
                ChatFormatting.LIGHT_PURPLE);
        for (SpellcraftItemData.Line line : SpellcraftItemData.lines(stack)) {
            line(source, "  " + line.type().id() + "  potency " + String.format("%.3f", line.potency()) + "  cost " + line.cost()
                    + "  up to " + SpellModel.perSpellLimit(config, line) + " per spell" + (line.transferable() ? "  transferable" : ""),
                    ChatFormatting.GRAY);
        }
        for (SpellcraftItemData.Spell spell : SpellcraftItemData.spells(stack)) {
            SpellModel model = check.models().get(spell.id());
            String slot = spell.assigned() ? "slot " + (spell.slot() + 1) : "off the bar";
            line(source, "  #" + spell.id() + " " + spell.name() + " [" + slot + ", priority " + SpellcraftRules.priority(config, spell) + "]: "
                    + String.join(" ", spell.tokens()) + "  (" + Math.round(model.arcana()) + " arcana, mana x" + String.format("%.2f", model.manaFactor())
                    + ", cd base x" + String.format("%.2f", model.cooldownBase()) + ", glyph haste x" + String.format("%.2f", model.glyphHaste()) + ", "
                    + model.casts() + " casts)", ChatFormatting.DARK_PURPLE);
            for (SpellModel.Glyph glyph : model.glyphs()) {
                if (!glyph.active()) {
                    line(source, "     token " + (glyph.token() + 1) + " " + glyph.type().id() + ": "
                            + glyph.state().name().toLowerCase(Locale.ROOT) + (glyph.dependent() ? " (rides on an inactive Bundled Spell)" : ""),
                            ChatFormatting.DARK_GRAY);
                }
            }
        }
        check.errors().forEach(error -> line(source, "  ! " + error, ChatFormatting.RED));
        return 1;
    }

    private static int view(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            source.sendFailure(new TextComponent("The focus augments table is not loaded"));
            return 0;
        }
        GlyphSources.Snapshot snapshot = GlyphSources.get(player);
        ItemStack stack = player.getOffhandItem();
        if (SpellcraftItemData.has(stack)) {
            line(source, "View of " + stack.getHoverName().getString() + ":", ChatFormatting.LIGHT_PURPLE);
            for (ViewLine line : snapshot.view(config, stack).lines()) {
                line(source, "  " + line.type().id() + "  potency " + String.format("%.3f", line.potency()) + " from " + line.potencySource()
                        + ", cost " + line.cost() + ", " + line.limit() + " per spell from " + line.usesSource()
                        + (line.borrowed() ? "  (borrowed)" : "") + (line.transferable() ? "  (transferable)" : ""), ChatFormatting.GRAY);
            }
        } else {
            line(source, "No spellcraft item in the off hand", ChatFormatting.GRAY);
        }
        line(source, "Equipped arcana items: " + snapshot.arcanaItems().size() + ", glyph sources: " + snapshot.sources().size(), ChatFormatting.GRAY);
        GlyphView auto = snapshot.autoLines(config);
        if (auto.isEmpty()) {
            line(source, "Auto lines: none", ChatFormatting.GRAY);
        }
        for (ViewLine line : auto.lines()) {
            line(source, "  auto " + line.type().id() + "  potency " + String.format("%.3f", line.potency()) + " from " + line.potencySource(),
                    ChatFormatting.GOLD);
        }
        return 1;
    }

    private static int setSlot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int slot = IntegerArgumentType.getInteger(context, "slot");
        FocusAugments.placeSpell(player, OFFHAND_SLOT, IntegerArgumentType.getInteger(context, "spell"), slot - 1, 0);
        return show(context.getSource());
    }

    private static int setPrio(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int spellId = IntegerArgumentType.getInteger(context, "spell");
        SpellcraftItemData.Spell spell = SpellcraftItemData.spell(player.getOffhandItem(), spellId).orElse(null);
        if (spell == null) {
            context.getSource().sendFailure(new TextComponent("No spell #" + spellId + " on the off-hand item"));
            return 0;
        }
        FocusAugments.placeSpell(player, OFFHAND_SLOT, spellId, spell.slot(), IntegerArgumentType.getInteger(context, "priority"));
        return show(context.getSource());
    }

    private static int setDefault(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String ability = StringArgumentType.getString(context, "ability");
        String error = SpellBars.editDefault(player, IntegerArgumentType.getInteger(context, "slot") - 1,
                ability.equalsIgnoreCase("clear") ? "" : ability);
        return report(context.getSource(), error);
    }

    private static int addSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<String> tokens = Arrays.asList(StringArgumentType.getString(context, "tokens").trim().split("\\s+"));
        FocusAugments.editSpell(player, OFFHAND_SLOT, 0, tokens.get(0).replace('_', ' '), tokens, false);
        return show(context.getSource());
    }

    private static int removeSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        FocusAugments.editSpell(player, OFFHAND_SLOT, IntegerArgumentType.getInteger(context, "id"), "", List.of(), true);
        return show(context.getSource());
    }

    private static int reroll(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = SpellcraftRoller.canCarry(player.getMainHandItem()) ? player.getMainHandItem() : player.getOffhandItem();
        if (!SpellcraftRoller.canCarry(stack)) {
            source.sendFailure(new TextComponent("Hold a focus or wand"));
            return 0;
        }
        SpellcraftRoller.roll(stack, new Random());
        SpellBars.refresh(player);
        line(source, "Rerolled spellcraft on " + stack.getHoverName().getString(), ChatFormatting.LIGHT_PURPLE);
        return 1;
    }

    /**
     * Gives a focus (or wand) of the given level and rarity, identified on the spot so the rarity sticks (identifying
     * an unidentified item re-rolls its rarity), with its affixes and spellcraft rolled.
     */
    private static int giveFocus(CommandContext<CommandSourceStack> context, boolean wand) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "rarity").toUpperCase(Locale.ROOT);
        VaultGearRarity rarity;
        try {
            rarity = VaultGearRarity.valueOf(name);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(new TextComponent("Unknown rarity " + name));
            return 0;
        }
        ItemStack stack = new ItemStack(wand ? ModItems.WAND : ModItems.FOCUS);
        VaultGearData data = VaultGearData.read(stack);
        data.setItemLevel(IntegerArgumentType.getInteger(context, "level"));
        data.setRarity(rarity);
        data.setState(VaultGearState.UNIDENTIFIED);
        data.write(stack);
        GearRollHelper.initializeGear(stack, player);
        rollModel(stack, player);
        if (!SpellcraftItemData.has(stack)) {
            SpellcraftRoller.roll(stack, player.getRandom());
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        line(context.getSource(), "Gave a level " + data.getItemLevel() + " " + name + (wand ? " wand" : " focus"), ChatFormatting.LIGHT_PURPLE);
        return 1;
    }

    /**
     * Rolls the gear model the identification roll would have picked. {@link GearRollHelper#initializeGear} never sets
     * one (only the roll tick does), and gear without a model renders with the unidentified texture.
     */
    private static void rollModel(ItemStack stack, ServerPlayer player) {
        VaultGearData data = VaultGearData.read(stack);
        if (data.getFirstValue(ModGearAttributes.GEAR_MODEL).isPresent() || !(stack.getItem() instanceof VaultGearItem gearItem)) {
            return;
        }
        ResourceLocation model = gearItem.getRandomModel(stack, player.getRandom(), player, DiscoveredModelsData.get(player.getServer()));
        if (model == null) {
            WoldLogger.error("givefocus: no model rolled for a {} {}; it renders with the unidentified texture",
                    data.getRarity(), stack.getItem().getRegistryName());
            return;
        }
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, model);
        data.write(stack);
    }

    private static int report(CommandSourceStack source, String error) {
        if (error != null) {
            source.sendFailure(new TextComponent(error));
            return 0;
        }
        line(source, "Done", ChatFormatting.GRAY);
        return 1;
    }

    private static void line(CommandSourceStack source, String text, ChatFormatting colour) {
        source.sendSuccess(new TextComponent(text).withStyle(colour), false);
    }
}
