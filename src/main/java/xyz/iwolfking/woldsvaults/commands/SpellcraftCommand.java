package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.world.data.DiscoveredModelsData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.play.DefaultBar;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellBars;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellEdits;
import xyz.iwolfking.woldsvaults.spellcraft.roll.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.spellcraft.source.EquippedSources;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphViewLine;
import xyz.iwolfking.woldsvaults.spellcraft.spell.PlacedGlyph;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * {@code /wvspellcraft}: test tools for the spell bar and focus spellcraft. Player subcommands inspect and edit the
 * off-hand item's spells and the default bar. The operator subcommands {@code reroll} and {@code givefocus} reroll the
 * held focus or wand and give an identified one of a chosen level and rarity.
 */
public final class SpellcraftCommand {
    private static final int OFFHAND_SLOT = 40;

    private SpellcraftCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvspellcraft")
                .then(Commands.literal("show").executes(context -> show(context.getSource())))
                .then(Commands.literal("bar")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, SpellcraftItemData.SLOTS))
                                .then(Commands.argument("ability", StringArgumentType.word())
                                        .executes(SpellcraftCommand::setDefault))))
                .then(Commands.literal("view").executes(context -> view(context.getSource())))
                .then(Commands.literal("slot")
                        .then(Commands.argument("spell", IntegerArgumentType.integer(1))
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, SpellcraftItemData.SLOTS))
                                        .executes(SpellcraftCommand::setSlot))))
                .then(Commands.literal("prio")
                        .then(Commands.argument("spell", IntegerArgumentType.integer(1))
                                .then(Commands.argument("priority", IntegerArgumentType.integer(1, 99))
                                        .executes(SpellcraftCommand::setPrio))))
                .then(Commands.literal("spell")
                        .then(Commands.literal("add").then(Commands.argument("tokens", StringArgumentType.greedyString())
                                .executes(SpellcraftCommand::addSpell)))
                        .then(Commands.literal("remove").then(Commands.argument("id", IntegerArgumentType.integer(1))
                                .executes(SpellcraftCommand::removeSpell))))
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
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        ItemStack stack = player.getOffhandItem();
        if (config == null || !SpellcraftItemData.has(stack)) {
            source.sendFailure(new TextComponent("Hold a focus or wand with spellcraft in your off hand"));
            return 0;
        }
        GlyphView view = EquippedSources.get(player).view(config, stack);
        SpellcraftRules.ItemCheck check = SpellcraftRules.check(config, stack, view, AbilityLookup.specOf(AbilityLookup.tree(player)));
        line(source, "Arcana " + Math.round(check.arcana()) + "/" + SpellcraftItemData.budget(stack) + ", depth "
                + SpellcraftItemData.depth(stack) + (check.budgetInactive() > 0 ? ", over budget: " + check.budgetInactive() + " glyphs inactive" : ""),
                ChatFormatting.LIGHT_PURPLE);
        for (GlyphLine line : SpellcraftItemData.lines(stack)) {
            line(source, "  " + line.type().id() + "  potency " + String.format("%.3f", line.potency()) + "  cost " + line.cost()
                    + "  up to " + GlyphView.ownLimit(config, line) + " per spell" + (line.transferable() ? "  transferable" : ""),
                    ChatFormatting.GRAY);
        }
        for (StoredSpell spell : SpellcraftItemData.spells(stack)) {
            SpellModel model = check.models().get(spell.id());
            String slot = spell.assigned() ? "slot " + (spell.slot() + 1) : "off the bar";
            line(source, "  #" + spell.id() + " " + spell.name() + " [" + slot + ", priority " + SpellcraftRules.priority(config, spell) + "]: "
                    + String.join(" ", spell.tokens()) + "  (" + Math.round(model.arcana()) + " arcana, mana x" + String.format("%.2f", model.manaFactor())
                    + ", cd base x" + String.format("%.2f", model.cooldownBase()) + ", glyph haste x" + String.format("%.2f", model.glyphHaste()) + ", "
                    + model.casts() + " casts)", ChatFormatting.DARK_PURPLE);
            for (PlacedGlyph glyph : model.glyphs()) {
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
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null) {
            source.sendFailure(new TextComponent("The spellcraft table is not loaded"));
            return 0;
        }
        EquippedSources.Snapshot snapshot = EquippedSources.get(player);
        ItemStack stack = player.getOffhandItem();
        if (SpellcraftItemData.has(stack)) {
            line(source, "View of " + stack.getHoverName().getString() + ":", ChatFormatting.LIGHT_PURPLE);
            for (GlyphViewLine line : snapshot.view(config, stack).lines()) {
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
        for (GlyphViewLine line : auto.lines()) {
            line(source, "  auto " + line.type().id() + "  potency " + String.format("%.3f", line.potency()) + " from " + line.potencySource(),
                    ChatFormatting.GOLD);
        }
        return 1;
    }

    private static int setSlot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int slot = IntegerArgumentType.getInteger(context, "slot");
        SpellEdits.placeSpell(player, OFFHAND_SLOT, IntegerArgumentType.getInteger(context, "spell"), slot - 1, 0);
        return show(context.getSource());
    }

    private static int setPrio(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int spellId = IntegerArgumentType.getInteger(context, "spell");
        StoredSpell spell = SpellcraftItemData.spell(player.getOffhandItem(), spellId).orElse(null);
        if (spell == null) {
            context.getSource().sendFailure(new TextComponent("No spell #" + spellId + " on the off-hand item"));
            return 0;
        }
        SpellEdits.placeSpell(player, OFFHAND_SLOT, spellId, spell.slot(), IntegerArgumentType.getInteger(context, "priority"));
        return show(context.getSource());
    }

    private static int setDefault(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String ability = StringArgumentType.getString(context, "ability");
        String error = DefaultBar.editDefault(player, IntegerArgumentType.getInteger(context, "slot") - 1,
                ability.equalsIgnoreCase("clear") ? "" : ability);
        return report(context.getSource(), error);
    }

    private static int addSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<String> tokens = Arrays.asList(StringArgumentType.getString(context, "tokens").trim().split("\\s+"));
        SpellEdits.editSpell(player, OFFHAND_SLOT, 0, tokens.get(0).replace('_', ' '), tokens, false);
        return show(context.getSource());
    }

    private static int removeSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SpellEdits.editSpell(player, OFFHAND_SLOT, IntegerArgumentType.getInteger(context, "id"), "", List.of(), true);
        return show(context.getSource());
    }

    private static int reroll(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = SpellcraftItemData.canCarry(player.getMainHandItem()) ? player.getMainHandItem() : player.getOffhandItem();
        if (!SpellcraftItemData.canCarry(stack)) {
            source.sendFailure(new TextComponent("Hold a focus or wand"));
            return 0;
        }
        SpellcraftRoller.roll(stack, new Random());
        SpellBars.refresh(player);
        line(source, "Rerolled spellcraft on " + stack.getHoverName().getString(), ChatFormatting.LIGHT_PURPLE);
        return 1;
    }

    /**
     * The focus is identified on the spot so its rarity sticks, since identifying an unidentified item re-rolls its
     * rarity.
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
     * {@link GearRollHelper#initializeGear} never sets a gear model, and gear without one renders with the unidentified
     * texture.
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
