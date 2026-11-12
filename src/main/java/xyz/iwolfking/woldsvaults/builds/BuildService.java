package xyz.iwolfking.woldsvaults.builds;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.builds.state.EquipmentCodec;
import xyz.iwolfking.woldsvaults.builds.state.PlayerStateCodec;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchRuntime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Loads, validates, saves and restores builds. A load resolves everything before touching the player, takes a
 * backup, and restores that backup if applying fails part way. No exception leaves this class: every failure ends
 * in a chat message and a report file.
 */
public final class BuildService {
    private static final Set<String> BUILD_KEYS = Set.of("schema", "name", "source", "player", "abilities", "talents",
            "expertises", "prestige", "gods", "equipment", "hotbar", "inventory", "curios");

    private BuildService() {
    }

    /** Options of a load. */
    public record LoadOptions(boolean keepInventory, boolean noBackup, boolean validateOnly) {
    }

    /** Loads (or with {@code validateOnly} only checks) the named build onto {@code player}. */
    public static void load(ServerPlayer player, String buildName, LoadOptions options, Consumer<TextComponent> reply) {
        String action = options.validateOnly() ? "validate" : "load";
        BuildReport report = new BuildReport(action, buildName, player.getGameProfile().getName());
        boolean applied = false;
        try {
            JsonObject build = BuildStore.readBuild(buildName);
            source(build, report);
            if (!options.validateOnly() && ServerVaults.get(player.getLevel()).isPresent()) {
                throw new BuildException(player.getGameProfile().getName() + " is inside a vault; builds load outside vaults only");
            }
            Prepared prepared = prepare(build, player, options.keepInventory(), report);
            if (report.hasErrors() || options.validateOnly()) {
                finish(report, player, reply, false, null);
                return;
            }
            Prepared rollback;
            JsonObject backup;
            BuildReport backupReport = new BuildReport("backup", buildName, player.getGameProfile().getName());
            try {
                backup = snapshot(player, true, "backup before loading " + buildName, backupReport);
                rollback = prepare(backup, player, false, backupReport);
            } catch (RuntimeException e) {
                throw new BuildException("could not prepare a copy of the current state to restore on failure, so nothing "
                        + "was changed: " + e.getMessage(), e);
            }
            if (backupReport.hasErrors()) {
                throw new BuildException("could not prepare a copy of the current state to restore on failure, so nothing "
                        + "was changed: " + backupReport.getErrors().get(0));
            }
            backupReport.getFallbacks().forEach(fallback -> report.note("backup: " + fallback));
            Path backupFile = null;
            if (!options.noBackup()) {
                backupFile = BuildStore.writeBackup(player.getServer(), player.getUUID(), buildName, backup);
                report.note("Backup of the previous state: " + backupFile);
            }
            try {
                applied = true;
                prepared.apply(player, options.keepInventory(), report);
            } catch (RuntimeException | LinkageError e) {
                report.failure("applying the build failed part way", e);
                try {
                    rollback.apply(player, false, new BuildReport("rollback", buildName, player.getGameProfile().getName()));
                    report.note("Rolled back to the state before the load");
                } catch (RuntimeException | LinkageError rollbackFailure) {
                    report.failure("ROLLBACK FAILED; restore by hand with /wvbuilds restore"
                            + (backupFile == null ? " (no backup file was written: no_backup)" : ""), rollbackFailure);
                }
            }
            finish(report, player, reply, applied, backupFile);
        } catch (BuildException e) {
            report.error(e.getMessage());
            finish(report, player, reply, applied, null);
        } catch (RuntimeException | LinkageError e) {
            report.failure("unexpected error", e);
            finish(report, player, reply, applied, null);
        }
    }

    /** Restores {@code player} from a backup file (the newest when {@code backupName} is null). */
    public static void restore(ServerPlayer player, String backupName, Consumer<TextComponent> reply) {
        BuildReport report = new BuildReport("restore", backupName == null ? "latest backup" : backupName,
                player.getGameProfile().getName());
        try {
            if (ServerVaults.get(player.getLevel()).isPresent()) {
                throw new BuildException(player.getGameProfile().getName() + " is inside a vault; restore outside vaults");
            }
            Path file = null;
            for (Path candidate : BuildStore.listBackups(player.getServer(), player.getUUID())) {
                String name = candidate.getFileName().toString();
                if (backupName == null || name.equals(backupName) || name.equals(backupName + ".json")) {
                    file = candidate;
                    break;
                }
            }
            if (file == null) {
                throw new BuildException(backupName == null ? "no backups exist for " + player.getGameProfile().getName()
                        : "no backup '" + backupName + "' for " + player.getGameProfile().getName());
            }
            report.note("Restoring " + file);
            JsonObject build = BuildStore.read(file);
            Prepared prepared = prepare(build, player, false, report);
            if (report.hasErrors()) {
                finish(report, player, reply, false, null);
                return;
            }
            BuildReport currentReport = new BuildReport("restore-rollback", file.getFileName().toString(),
                    player.getGameProfile().getName());
            Prepared rollback = prepare(snapshot(player, true, "state before restore", currentReport), player, false, currentReport);
            try {
                prepared.apply(player, false, report);
            } catch (RuntimeException | LinkageError e) {
                report.failure("restoring failed part way", e);
                if (!currentReport.hasErrors()) {
                    rollback.apply(player, false, currentReport);
                    report.note("Rolled back to the state before the restore");
                }
            }
            finish(report, player, reply, true, null);
        } catch (BuildException e) {
            report.error(e.getMessage());
            finish(report, player, reply, false, null);
        } catch (RuntimeException | LinkageError e) {
            report.failure("unexpected error", e);
            finish(report, player, reply, false, null);
        }
    }

    /** Saves {@code player}'s current build under {@code buildName}. */
    public static void save(ServerPlayer player, String buildName, boolean withInventory, boolean overwrite,
                            Consumer<TextComponent> reply) {
        BuildReport report = new BuildReport("save", buildName, player.getGameProfile().getName());
        try {
            Path file = BuildStore.buildFile(buildName);
            if (Files.exists(file) && !overwrite) {
                throw new BuildException("a build named '" + buildName + "' already exists; add 'overwrite' to replace it");
            }
            JsonObject build = snapshot(player, withInventory, "saved from " + player.getGameProfile().getName(), report);
            build.addProperty("name", buildName);
            BuildStore.write(file, build);
            report.summary("Saved to " + file);
            Path reportFile = report.writeTo(BuildStore.reportFile("save", player.getGameProfile().getName(), buildName));
            reply.accept(message(ChatFormatting.GREEN, "Saved build '" + buildName + "' from "
                    + player.getGameProfile().getName() + (report.getFallbacks().isEmpty() ? "."
                    : " (" + report.getFallbacks().size() + " item(s) stored as raw NBT; see the report).")
                    + reportSuffix(reportFile)));
        } catch (BuildException e) {
            report.error(e.getMessage());
            Path reportFile = report.writeTo(BuildStore.reportFile("save", player.getGameProfile().getName(), buildName));
            reply.accept(message(ChatFormatting.RED, "Could not save build '" + buildName + "': " + e.getMessage()
                    + reportSuffix(reportFile)));
        } catch (RuntimeException | LinkageError e) {
            report.failure("unexpected error", e);
            Path reportFile = report.writeTo(BuildStore.reportFile("save", player.getGameProfile().getName(), buildName));
            reply.accept(message(ChatFormatting.RED, "Could not save build '" + buildName + "': unexpected error "
                    + e.getClass().getSimpleName() + reportSuffix(reportFile)));
        }
    }

    /** A complete build file of {@code player}'s current state. */
    static JsonObject snapshot(ServerPlayer player, boolean withInventory, String note, BuildReport report) {
        JsonObject build = new JsonObject();
        build.addProperty("schema", BuildStore.SCHEMA);
        build.addProperty("name", note);
        JsonObject source = new JsonObject();
        source.addProperty("kind", "save");
        source.addProperty("player", player.getGameProfile().getName());
        source.addProperty("uuid", player.getUUID().toString());
        source.addProperty("time", LocalDateTime.now().toString());
        source.addProperty("woldsvaults", modVersion(WoldsVaults.MOD_ID));
        source.addProperty("the_vault", modVersion("the_vault"));
        source.addProperty("notes", note);
        build.add("source", source);
        PlayerStateCodec.capture(player, build);
        EquipmentCodec.capture(player, withInventory, build, report);
        return build;
    }

    private static String modVersion(String modId) {
        return ModList.get().getModContainerById(modId).map(container -> container.getModInfo().getVersion().toString())
                .orElse("missing");
    }

    private static void source(JsonObject build, BuildReport report) {
        JsonObject source = Json.optObject(build, "source", "build");
        if (source == null) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            report.source(entry.getKey() + ": " + (entry.getValue().isJsonPrimitive()
                    ? entry.getValue().getAsString() : entry.getValue().toString()));
        }
    }

    private static Prepared prepare(JsonObject build, ServerPlayer player, boolean keepInventory, BuildReport report) {
        Json.onlyKeys(build, "build", BUILD_KEYS);
        PlayerStateCodec.Plan state = PlayerStateCodec.plan(build, player, report);
        int greedTier = PlayerGreedTreeData.get(player.getServer()).getGreedTier(player);
        JsonObject playerJson = Json.optObject(build, "player", "build");
        JsonObject greed = playerJson == null ? null : Json.optObject(playerJson, "greed", "player");
        Integer targetGreed = greed == null ? null : Json.optInt(greed, "tier", "player.greed");
        EquipmentCodec.Plan items = null;
        if (EquipmentCodec.hasItems(build)) {
            items = EquipmentCodec.plan(build, player, state.targetLevel(), targetGreed == null ? greedTier : targetGreed, report);
            if (keepInventory) {
                String noRoom = items.checkKeepRoom(player);
                if (noRoom != null) {
                    report.error(noRoom);
                }
            }
        }
        return new Prepared(state, items);
    }

    private static void finish(BuildReport report, ServerPlayer player, Consumer<TextComponent> reply, boolean applied,
                               Path backupFile) {
        if (applied) {
            refreshSoon(player);
        }
        String playerName = player.getGameProfile().getName();
        Path reportFile = report.writeTo(BuildStore.reportFile(report.getAction(), playerName, report.getBuildName()));
        if (report.hasErrors()) {
            String first = report.getErrors().get(0);
            int more = report.getErrors().size() - 1;
            String state = applied ? " The player was rolled back where possible; read the report."
                    : " Nothing was changed.";
            reply.accept(message(ChatFormatting.RED, "Build '" + report.getBuildName() + "' is incompatible with this game: "
                    + first + (more > 0 ? " (+" + more + " more)" : "") + "." + state + reportSuffix(reportFile)));
            return;
        }
        String verb = applied ? "Loaded" : "Validated";
        reply.accept(message(ChatFormatting.GREEN, verb + " build '" + report.getBuildName() + "' for " + playerName + ": "
                + report.getBypassed().size() + " bypassed game rule(s), " + report.getFallbacks().size() + " fallback(s)."
                + reportSuffix(reportFile)));
    }

    private static void refreshSoon(ServerPlayer player) {
        BuildTasks.later(2, () -> {
            if (player.isRemoved()) {
                return;
            }
            PouchRuntime.update(player);
            AttributeSnapshotHelper.getInstance().refreshSnapshot(player);
        });
    }

    private static String reportSuffix(Path reportFile) {
        return reportFile == null ? " (the report file could not be written; see the server log)"
                : " Report: logs/wvbuilds/" + reportFile.getFileName();
    }

    static TextComponent message(ChatFormatting color, String text) {
        TextComponent component = new TextComponent("[wvbuilds] " + text);
        component.withStyle(color);
        return component;
    }

    /** A resolved build: the player-state plan and the item plan, applied in that order. */
    private record Prepared(PlayerStateCodec.Plan state, EquipmentCodec.Plan items) {
        void apply(ServerPlayer player, boolean keepInventory, BuildReport report) {
            this.state.apply(player, report);
            if (this.items != null) {
                this.items.apply(player, keepInventory, report);
            }
            AttributeSnapshotHelper.getInstance().refreshSnapshot(player);
        }
    }
}
