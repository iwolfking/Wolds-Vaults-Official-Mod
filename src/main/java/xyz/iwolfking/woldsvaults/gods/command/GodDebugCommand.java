package xyz.iwolfking.woldsvaults.gods.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.util.LootInitialization;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.GodPiety;
import xyz.iwolfking.woldsvaults.gods.MinorTransferSlots;
import xyz.iwolfking.woldsvaults.gods.charms.MythicCharmRolls;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Operator debug access to god alignment state: {@code /wvgods}. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GodDebugCommand {
    private GodDebugCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("wvgods")
                .requires(source -> source.hasPermission(2));

        root.then(Commands.literal("xp")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(godArgument()
                                .then(Commands.argument("amount", LongArgumentType.longArg())
                                        .executes(GodDebugCommand::addXp)))));

        root.then(Commands.literal("setlevel")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(godArgument()
                                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                        .executes(GodDebugCommand::setLevel)))));

        root.then(Commands.literal("points")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(godArgument()
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(GodDebugCommand::addPoints)))));

        root.then(Commands.literal("info")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(GodDebugCommand::info)));

        root.then(Commands.literal("activegod")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(GodDebugCommand::activeGod)));

        root.then(Commands.literal("charm")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("unidentified")
                                .executes(GodDebugCommand::giveUnidentifiedMythicCharm))
                        .then(godArgument()
                                .executes(context -> giveMythicCharm(context, -1, false))
                                .then(Commands.literal("legendary")
                                        .executes(context -> giveMythicCharm(context, -1, true)))
                                .then(Commands.argument("piety", IntegerArgumentType.integer(0))
                                        .executes(context -> giveMythicCharm(context,
                                                IntegerArgumentType.getInteger(context, "piety"), false))
                                        .then(Commands.literal("legendary")
                                                .executes(context -> giveMythicCharm(context,
                                                        IntegerArgumentType.getInteger(context, "piety"), true)))))));

        root.then(Commands.literal("sacrifice")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(godArgument().executes(GodDebugCommand::grantSacrifice))));

        root.then(Commands.literal("mts")
                .then(Commands.literal("list")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(godArgument().executes(GodDebugCommand::listMinorTransfers))))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(godArgument()
                                        .then(Commands.argument("slot", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("node", StringArgumentType.string())
                                                        .executes(GodDebugCommand::setMinorTransfer))))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(godArgument()
                                        .then(Commands.argument("slot", IntegerArgumentType.integer(1))
                                                .executes(GodDebugCommand::clearMinorTransfer))))));

        event.getDispatcher().register(root);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> godArgument() {
        return Commands.argument("god", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(VaultGod.values()).map(VaultGod::getName), builder));
    }

    private static VaultGod god(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "god");
        VaultGod god = VaultGod.fromName(name);
        if (god == null) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                    new TextComponent("Unknown vault god: " + name)).create();
        }
        return god;
    }

    private static GodAlignmentData data(CommandContext<CommandSourceStack> context) {
        return GodAlignmentData.get(context.getSource().getServer());
    }

    private static int addXp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        VaultGod god = god(context);
        long amount = LongArgumentType.getLong(context, "amount");
        GodAlignmentData data = data(context);
        int gained = data.grantGodXp(player, god, amount);
        context.getSource().sendSuccess(new TextComponent("Gave %s %d %s XP (%d level(s) gained, now level %d with %d XP)."
                .formatted(player.getGameProfile().getName(), amount, god.getName(), gained,
                        data.getLevel(player.getUUID(), god), data.getXp(player.getUUID(), god))), true);
        return gained;
    }

    private static int setLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        VaultGod god = god(context);
        int level = IntegerArgumentType.getInteger(context, "level");
        GodAlignmentData data = data(context);
        data.setLevel(player, god, level);
        context.getSource().sendSuccess(new TextComponent("Set %s to %s level %d (%d XP, %d point(s), %d MTS slot(s))."
                .formatted(player.getGameProfile().getName(), god.getName(), level,
                        data.getXp(player.getUUID(), god), data.getTotalPoints(player.getUUID(), god),
                        data.getMinorTransferSlots(player.getUUID(), god))), true);
        return level;
    }

    private static int addPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        VaultGod god = god(context);
        int amount = IntegerArgumentType.getInteger(context, "amount");
        GodAlignmentData data = data(context);
        data.addBonusPoints(player, god, amount);
        context.getSource().sendSuccess(new TextComponent("%s now has %d unspent %s point(s)."
                .formatted(player.getGameProfile().getName(), data.getUnspentPoints(player.getUUID(), god), god.getName())), true);
        return data.getUnspentPoints(player.getUUID(), god);
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        GodAlignmentData data = data(context);
        context.getSource().sendSuccess(new TextComponent("God alignment for " + player.getGameProfile().getName()
                + " (active: " + ActiveGodResolver.getActiveGod(player).map(VaultGod::getName).orElse("none") + ")"), false);
        for (VaultGod god : VaultGod.values()) {
            int level = data.getLevel(player.getUUID(), god);
            Map<String, Integer> ledger = data.getSpentLedger(player.getUUID(), god);
            context.getSource().sendSuccess(new TextComponent(
                    "  %s: level %d, %d/%d XP to next, %d/%d points spent, piety %d, altars %d, MTS %d/%d %s"
                            .formatted(god.getName(), level, data.getXp(player.getUUID(), god), GodLevels.xpForLevel(level + 1),
                                    data.getSpentPoints(player.getUUID(), god), data.getTotalPoints(player.getUUID(), god),
                                    GodAlignmentData.piety(player, god), data.getAltarCompletions(player.getUUID(), god),
                                    data.getMinorTransfers(player.getUUID(), god).size(),
                                    data.getMinorTransferSlots(player.getUUID(), god), ledger)), false);
        }
        return VaultGod.values().length;
    }

    private static int activeGod(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ActiveGodResolver.invalidate(player);
        String god = ActiveGodResolver.getActiveGod(player).map(VaultGod::getName).orElse("none");
        context.getSource().sendSuccess(new TextComponent(player.getGameProfile().getName() + "'s active god tree: " + god), false);
        return god.equals("none") ? 0 : 1;
    }

    private static int listMinorTransfers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        VaultGod god = god(context);
        GodAlignmentData data = data(context);
        int capacity = data.getMinorTransferSlots(player.getUUID(), god);
        StringBuilder slots = new StringBuilder();
        for (int slot = 0; slot < GodLevels.maxMinorTransferSlots(); slot++) {
            if (slot > 0) {
                slots.append(" | ");
            }
            slots.append("slot ").append(slot + 1).append(": ");
            if (slot >= capacity) {
                slots.append("locked (level ").append(GodLevels.minorTransferSlotUnlockLevel(slot)).append(')');
            } else {
                slots.append(data.getMinorTransferSlot(player.getUUID(), god, slot).orElse("-"));
            }
        }
        List<String> live = data.getMinorTransfers(player.getUUID(), god);
        context.getSource().sendSuccess(new TextComponent("%s %s transfer slots (%d live of %d unlocked): %s"
                .formatted(player.getGameProfile().getName(), god.getName(), live.size(), capacity, slots)), false);
        return live.size();
    }

    private static int setMinorTransfer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        VaultGod god = god(context);
        int slot = IntegerArgumentType.getInteger(context, "slot") - 1;
        String node = StringArgumentType.getString(context, "node");
        MinorTransferSlots.Result result = MinorTransferSlots.assign(player, god, slot, node);
        context.getSource().sendSuccess(new TextComponent(result == MinorTransferSlots.Result.OK
                ? "Put " + node + " in " + god.getName() + " transfer slot " + (slot + 1) + "."
                : "Could not put " + node + " in " + god.getName() + " transfer slot " + (slot + 1) + ": " + result + "."), true);
        return result == MinorTransferSlots.Result.OK ? 1 : 0;
    }

    private static int clearMinorTransfer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        VaultGod god = god(context);
        int slot = IntegerArgumentType.getInteger(context, "slot") - 1;
        MinorTransferSlots.Result result = MinorTransferSlots.assign(player, god, slot, "");
        context.getSource().sendSuccess(new TextComponent(result == MinorTransferSlots.Result.OK
                ? "Cleared " + god.getName() + " transfer slot " + (slot + 1) + "."
                : "Could not clear " + god.getName() + " transfer slot " + (slot + 1) + ": " + result + "."), true);
        return result == MinorTransferSlots.Result.OK ? 1 : 0;
    }

    /**
     * Grants one cauldron sacrifice gate outright: clears item progress, advances the completed
     * count and promotes any levels the accumulated XP already paid for.
     */
    private static int grantSacrifice(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        VaultGod god = god(context);
        xyz.iwolfking.woldsvaults.gods.sacrifice.GodSacrificeData.get(player.getLevel())
                .clearProgress(player.getUUID(), god);
        GodAlignmentData alignment = GodAlignmentData.get(player.getServer());
        alignment.completeSacrifice(player, god);
        context.getSource().sendSuccess(new TextComponent("Granted %s a %s sacrifice (%d completed)."
                .formatted(player.getGameProfile().getName(), god.getName(),
                        alignment.getSacrifices(player.getUUID(), god))), true);
        return 1;
    }

    /** Gives an identified mythic charm, rolled at the player's piety or at {@code pietyOverride}. */
    private static int giveMythicCharm(CommandContext<CommandSourceStack> context, int pietyOverride,
                                       boolean forceLegendary) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        VaultGod god = god(context);
        ItemStack stack = new ItemStack(ModItems.MYTHIC_VAULT_CHARM);
        VaultGearData data = VaultGearData.read(stack);
        data.setState(VaultGearState.IDENTIFIED);
        data.setItemLevel(0);
        data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.CHARM_VAULT_GOD, god);
        data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.IS_LOOT, Boolean.TRUE);
        if (forceLegendary) {
            data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.IS_LEGENDARY, Boolean.TRUE);
        }
        data.write(stack);
        int unitsOverride = pietyOverride >= 0 ? pietyOverride / GodPiety.PIETY_PER_UNIT : -1;
        MythicCharmRolls.initialize(stack, player, unitsOverride);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        context.getSource().sendSuccess(new TextComponent("Gave %s an identified mythic %s charm (%s piety)."
                .formatted(player.getGameProfile().getName(), god.getName(),
                        pietyOverride >= 0 ? String.valueOf(pietyOverride) : "current")), true);
        return 1;
    }

    /**
     * Gives the unidentified mythic charm exactly as the mapped living chest tables drop it: the
     * loot entry's raw nbt run through the vault loot initialization, so name, tooltip, tint and
     * the identify flow match a real drop. Inside a vault the stack is initialized as that vault's
     * loot; outside it is level 100 and not loot, so it cannot roll legendary.
     */
    private static int giveUnidentifiedMythicCharm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ItemStack stack = new ItemStack(ModItems.MYTHIC_VAULT_CHARM);
        stack.getOrCreateTag().putString("the_vault:gear_roll_type", "Mythic");
        Vault vault = ServerVaults.get(player.getLevel()).orElse(null);
        stack = vault != null
                ? LootInitialization.initializeVaultLoot(stack, vault, player.blockPosition())
                : LootInitialization.initializeVaultLoot(stack, 100);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        context.getSource().sendSuccess(new TextComponent("Gave %s an unidentified mythic charm (%s)."
                .formatted(player.getGameProfile().getName(),
                        vault != null ? "initialized as loot of the current vault" : "level 100, not vault loot")), true);
        return 1;
    }
}
