package xyz.iwolfking.woldsvaults.builds;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Operator command for build snapshots: {@code /wvbuilds list|info|validate|load|save|restore|backups}. Build names
 * with folders ({@code ledger/max/fangs}) are typed in quotes.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class BuildsCommand {
    private static final List<String> LOAD_OPTIONS = List.of("keep_inventory", "no_backup");
    private static final List<String> SAVE_OPTIONS = List.of("with_inventory", "overwrite");

    private BuildsCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("wvbuilds")
                .requires(source -> source.hasPermission(2));

        root.then(Commands.literal("list")
                .executes(context -> list(context, ""))
                .then(Commands.argument("folder", StringArgumentType.string())
                        .executes(context -> list(context, StringArgumentType.getString(context, "folder")))));

        root.then(Commands.literal("info")
                .then(Commands.argument("build", StringArgumentType.string()).suggests(BUILDS)
                        .executes(BuildsCommand::info)));

        root.then(Commands.literal("validate")
                .then(Commands.argument("build", StringArgumentType.string()).suggests(BUILDS)
                        .executes(context -> load(context, self(context), "", true))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> load(context, EntityArgument.getPlayer(context, "player"), "", true)))));

        root.then(Commands.literal("load")
                .then(Commands.argument("build", StringArgumentType.string()).suggests(BUILDS)
                        .executes(context -> load(context, self(context), "", false))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> load(context, EntityArgument.getPlayer(context, "player"), "", false))
                                .then(Commands.argument("options", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(LOAD_OPTIONS, builder))
                                        .executes(context -> load(context, EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "options"), false))))));

        root.then(Commands.literal("save")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> save(context, self(context), ""))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> save(context, EntityArgument.getPlayer(context, "player"), ""))
                                .then(Commands.argument("options", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(SAVE_OPTIONS, builder))
                                        .executes(context -> save(context, EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "options")))))));

        root.then(Commands.literal("restore")
                .executes(context -> restore(context, self(context), null))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> restore(context, EntityArgument.getPlayer(context, "player"), null))
                        .then(Commands.argument("backup", StringArgumentType.string()).suggests(BACKUPS)
                                .executes(context -> restore(context, EntityArgument.getPlayer(context, "player"),
                                        StringArgumentType.getString(context, "backup"))))));

        root.then(Commands.literal("backups")
                .executes(context -> backups(context, self(context)))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> backups(context, EntityArgument.getPlayer(context, "player")))));

        event.getDispatcher().register(root);
    }

    private static final SuggestionProvider<CommandSourceStack> BUILDS = (context, builder) ->
            SharedSuggestionProvider.suggest(BuildStore.list("").stream().map(BuildsCommand::quoteIfNeeded), builder);

    private static final SuggestionProvider<CommandSourceStack> BACKUPS = (context, builder) -> {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            return SharedSuggestionProvider.suggest(BuildStore.listBackups(player.getServer(), player.getUUID()).stream()
                    .map(path -> quoteIfNeeded(path.getFileName().toString().replace(".json", ""))), builder);
        } catch (CommandSyntaxException e) {
            return builder.buildFuture();
        }
    };

    private static String quoteIfNeeded(String name) {
        return unquotedAllowed(name) ? name : "\"" + name + "\"";
    }

    private static ServerPlayer self(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getSource().getPlayerOrException();
    }

    private static Consumer<TextComponent> reply(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        return message -> {
            context.getSource().sendSuccess(message, true);
            if (context.getSource().getEntity() != target) {
                target.displayClientMessage(message, false);
            }
        };
    }

    private static int list(CommandContext<CommandSourceStack> context, String folder) {
        try {
            if (!folder.isEmpty()) {
                BuildStore.checkName(folder);
            }
            List<String> names = BuildStore.list(folder);
            if (names.isEmpty()) {
                context.getSource().sendSuccess(BuildService.message(ChatFormatting.YELLOW, "No builds"
                        + (folder.isEmpty() ? "" : " under '" + folder + "'") + " in " + BuildStore.buildsRoot()), false);
                return 0;
            }
            context.getSource().sendSuccess(BuildService.message(ChatFormatting.GREEN, names.size() + " build(s): "
                    + String.join(", ", names)), false);
            return names.size();
        } catch (BuildException e) {
            context.getSource().sendFailure(BuildService.message(ChatFormatting.RED, e.getMessage()));
            return 0;
        }
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "build");
        try {
            JsonObject build = BuildStore.readBuild(name);
            List<String> sections = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : build.entrySet()) {
                if (!entry.getKey().equals("schema") && !entry.getKey().equals("name") && !entry.getKey().equals("source")) {
                    sections.add(entry.getKey());
                }
            }
            StringBuilder text = new StringBuilder("Build '" + name + "'");
            if (build.has("name") && build.get("name").isJsonPrimitive()) {
                text.append(" (").append(build.get("name").getAsString()).append(")");
            }
            text.append(": sections ").append(String.join(", ", sections));
            JsonObject source = BuildJson.optObject(build, "source", "build");
            if (source != null) {
                text.append("; source ").append(source);
            }
            context.getSource().sendSuccess(BuildService.message(ChatFormatting.AQUA, text.toString()), false);
            return 1;
        } catch (BuildException e) {
            context.getSource().sendFailure(BuildService.message(ChatFormatting.RED, "Build '" + name + "': " + e.getMessage()));
            return 0;
        } catch (RuntimeException e) {
            WoldLogger.error("[wvbuilds] info {} failed", name, e);
            context.getSource().sendFailure(BuildService.message(ChatFormatting.RED, "Build '" + name
                    + "' could not be read: " + e.getClass().getSimpleName()));
            return 0;
        }
    }

    private static int load(CommandContext<CommandSourceStack> context, ServerPlayer player, String options, boolean validateOnly) {
        String name = StringArgumentType.getString(context, "build");
        boolean keep = false;
        boolean noBackup = false;
        for (String option : options.trim().split("\\s+")) {
            if (option.isEmpty()) {
                continue;
            }
            switch (option) {
                case "keep_inventory" -> keep = true;
                case "no_backup" -> noBackup = true;
                default -> {
                    context.getSource().sendFailure(BuildService.message(ChatFormatting.RED, "Unknown load option '" + option
                            + "' (options: " + String.join(", ", LOAD_OPTIONS) + ")"));
                    return 0;
                }
            }
        }
        BuildService.load(player, name, new BuildService.LoadOptions(keep, noBackup, validateOnly), reply(context, player));
        return 1;
    }

    private static int save(CommandContext<CommandSourceStack> context, ServerPlayer player, String options) {
        String name = StringArgumentType.getString(context, "name");
        boolean withInventory = false;
        boolean overwrite = false;
        for (String option : options.trim().split("\\s+")) {
            if (option.isEmpty()) {
                continue;
            }
            switch (option) {
                case "with_inventory" -> withInventory = true;
                case "overwrite" -> overwrite = true;
                default -> {
                    context.getSource().sendFailure(BuildService.message(ChatFormatting.RED, "Unknown save option '" + option
                            + "' (options: " + String.join(", ", SAVE_OPTIONS) + ")"));
                    return 0;
                }
            }
        }
        BuildService.save(player, name, withInventory, overwrite, message -> context.getSource().sendSuccess(message, true));
        return 1;
    }

    private static int restore(CommandContext<CommandSourceStack> context, ServerPlayer player, String backup) {
        BuildService.restore(player, backup, reply(context, player));
        return 1;
    }

    private static int backups(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        List<Path> backups = BuildStore.listBackups(player.getServer(), player.getUUID());
        if (backups.isEmpty()) {
            context.getSource().sendSuccess(BuildService.message(ChatFormatting.YELLOW, "No backups for "
                    + player.getGameProfile().getName()), false);
            return 0;
        }
        List<String> names = backups.stream().limit(10).map(path -> path.getFileName().toString().replace(".json", "")).toList();
        context.getSource().sendSuccess(BuildService.message(ChatFormatting.GREEN, backups.size() + " backup(s) for "
                + player.getGameProfile().getName() + ", newest first: " + String.join(", ", names)), false);
        return backups.size();
    }

    private static boolean unquotedAllowed(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!StringReader.isAllowedInUnquotedString(text.charAt(i))) {
                return false;
            }
        }
        return !text.isEmpty();
    }
}
