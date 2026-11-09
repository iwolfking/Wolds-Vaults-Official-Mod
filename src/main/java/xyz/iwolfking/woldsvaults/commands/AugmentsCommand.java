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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * {@code /wvspellcraft}: test tools for the spell bar and focus spellcraft, next to the screens. {@code show} prints the
 * off-hand item's implicits and spells; {@code bar <slot> <ability|clear>} sets a default bar slot; {@code spell add
 * <tokens>} and {@code spell remove <id>} edit the off-hand item's spells; {@code itembar <slot> <entry>} sets its bar
 * slot (inherit, blank, a:Ability or s:id). Operators: {@code reroll} rerolls the held focus or wand; {@code givefocus
 * <level> <rarity> [wand]} gives an identified focus or wand of that level and rarity.
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
                .then(Commands.literal("itembar")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, SpellcraftItemData.SLOTS))
                                .then(Commands.argument("entry", StringArgumentType.word())
                                        .executes(AugmentsCommand::setItemBar))))
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
                                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                                Arrays.stream(iskallia.vault.gear.VaultGearRarity.values()).map(Enum::name), builder))
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
        SpellcraftRules.ItemCheck check = SpellcraftRules.check(config, stack, SpellcraftRules.specOf(SpellCooldowns.tree(player)), false);
        line(source, "Arcana " + Math.round(check.arcana()) + "/" + SpellcraftItemData.budget(stack) + ", depth "
                + SpellcraftItemData.depth(stack), ChatFormatting.LIGHT_PURPLE);
        for (SpellcraftItemData.Line line : SpellcraftItemData.lines(stack)) {
            line(source, "  " + line.type().id() + "  potency " + String.format("%.3f", line.potency()) + "  cost " + line.cost()
                    + "  up to " + Math.min(line.uses(), config.glyph(line.type()).capPerSpell) + " per spell", ChatFormatting.GRAY);
        }
        for (SpellcraftItemData.Spell spell : SpellcraftItemData.spells(stack)) {
            SpellModel model = check.models().get(spell.id());
            line(source, "  #" + spell.id() + " " + spell.name() + ": " + String.join(" ", spell.tokens()) + "  (" + Math.round(model.arcana())
                    + " arcana, mana x" + String.format("%.2f", model.manaFactor()) + ", cd x" + String.format("%.2f", model.cooldownFactor())
                    + ", haste x" + String.format("%.2f", model.hasteMultiplier()) + ", " + model.casts() + " casts)", ChatFormatting.DARK_PURPLE);
        }
        check.errors().forEach(error -> line(source, "  ! " + error, ChatFormatting.RED));
        line(source, "Bar: " + String.join(" | ", SpellcraftItemData.bar(stack)), ChatFormatting.GRAY);
        return 1;
    }

    private static int setDefault(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String ability = StringArgumentType.getString(context, "ability");
        String error = SpellBars.editDefault(player, IntegerArgumentType.getInteger(context, "slot") - 1,
                ability.equalsIgnoreCase("clear") ? "" : ability);
        return report(context.getSource(), error);
    }

    private static int setItemBar(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String entry = StringArgumentType.getString(context, "entry");
        String value = entry.equalsIgnoreCase("inherit") ? SpellcraftItemData.INHERIT
                : entry.equalsIgnoreCase("blank") ? SpellcraftItemData.BLANK : entry;
        String error = SpellBars.editItem(player, OFFHAND_SLOT, IntegerArgumentType.getInteger(context, "slot") - 1, value);
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
        String name = StringArgumentType.getString(context, "rarity").toUpperCase(java.util.Locale.ROOT);
        iskallia.vault.gear.VaultGearRarity rarity;
        try {
            rarity = iskallia.vault.gear.VaultGearRarity.valueOf(name);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(new TextComponent("Unknown rarity " + name));
            return 0;
        }
        ItemStack stack = new ItemStack(wand ? iskallia.vault.init.ModItems.WAND : iskallia.vault.init.ModItems.FOCUS);
        iskallia.vault.gear.data.VaultGearData data = iskallia.vault.gear.data.VaultGearData.read(stack);
        data.setItemLevel(IntegerArgumentType.getInteger(context, "level"));
        data.setRarity(rarity);
        data.setState(iskallia.vault.gear.VaultGearState.UNIDENTIFIED);
        data.write(stack);
        iskallia.vault.gear.GearRollHelper.initializeGear(stack, player);
        if (!SpellcraftItemData.has(stack)) {
            SpellcraftRoller.roll(stack, player.getRandom());
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        line(context.getSource(), "Gave a level " + data.getItemLevel() + " " + name + (wand ? " wand" : " focus"), ChatFormatting.LIGHT_PURPLE);
        return 1;
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
