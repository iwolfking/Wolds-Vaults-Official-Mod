package xyz.iwolfking.woldsvaults.builds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Where build files, backups and reports live: builds in {@code config/woldsvaults/builds/}, backups per player in
 * the world folder under {@code woldsvaults_builds/backups/}, reports in {@code logs/wvbuilds/}.
 */
public final class BuildStore {
    public static final int SCHEMA = 1;
    private static final int BACKUPS_KEPT = 20;
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+)*");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();

    private BuildStore() {
    }

    public static Path buildsRoot() {
        return FMLPaths.CONFIGDIR.get().resolve("woldsvaults").resolve("builds");
    }

    public static Path backupsRoot(MinecraftServer server, UUID player) {
        return server.getWorldPath(LevelResource.ROOT).resolve("woldsvaults_builds").resolve("backups").resolve(player.toString());
    }

    public static Path reportFile(String action, String playerName, String buildName) {
        String safe = buildName.replaceAll("[^A-Za-z0-9_\\-]", "_");
        return FMLPaths.GAMEDIR.get().resolve("logs").resolve("wvbuilds")
                .resolve(LocalDateTime.now().format(STAMP) + "_" + action + "_" + playerName + "_" + safe + ".txt");
    }

    /** Checks a build name: letters, digits, '_' and '-', with '/' between folders. */
    public static void checkName(String name) {
        if (name.length() > 120 || !NAME.matcher(name).matches()) {
            throw new BuildException("'" + name + "' is not a valid build name: use letters, digits, '_' and '-', "
                    + "with '/' between folders");
        }
    }

    public static Path buildFile(String name) {
        checkName(name);
        return buildsRoot().resolve(name + ".json");
    }

    /** All build names under {@code folder} (or every build), sorted. */
    public static List<String> list(String folder) {
        Path root = buildsRoot();
        Path start = folder.isEmpty() ? root : root.resolve(folder);
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(start)) {
            return names;
        }
        try (Stream<Path> files = Files.walk(start)) {
            files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        String relative = root.relativize(path).toString().replace('\\', '/');
                        names.add(relative.substring(0, relative.length() - ".json".length()));
                    });
        } catch (IOException e) {
            WoldLogger.error("[wvbuilds] Could not list builds under {}", start, e);
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    public static JsonObject readBuild(String name) {
        Path file = buildFile(name);
        if (!Files.isRegularFile(file)) {
            throw new BuildException("no build '" + name + "' (looked for " + file + ")");
        }
        return read(file);
    }

    /** Reads a build file with a strict JSON parser, so a trailing comma or comment is an error, not a null. */
    public static JsonObject read(Path file) {
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BuildException("could not read " + file + ": " + e.getMessage());
        }
        JsonObject build = parseStrict(text, file.getFileName().toString());
        JsonElement schema = build.get("schema");
        if (schema == null || !schema.isJsonPrimitive() || !schema.getAsJsonPrimitive().isNumber()) {
            throw new BuildException("schema: missing; this file is not a build file");
        }
        if (schema.getAsInt() != SCHEMA) {
            throw new BuildException("schema: this game reads build schema " + SCHEMA + ", the file is schema " + schema.getAsString());
        }
        return build;
    }

    static JsonObject parseStrict(String text, String what) {
        try (Reader in = new StringReader(text)) {
            JsonReader reader = new JsonReader(in);
            reader.setLenient(false);
            JsonElement element = new Gson().getAdapter(JsonElement.class).read(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new BuildException(what + ": text after the end of the JSON");
            }
            if (element == null || !element.isJsonObject()) {
                throw new BuildException(what + ": the file must hold one JSON object");
            }
            return element.getAsJsonObject();
        } catch (IOException | IllegalStateException | NumberFormatException e) {
            throw new BuildException(what + ": invalid JSON (" + e.getMessage() + ")");
        }
    }

    /** Writes a build file through a temporary file, so a crash mid-write never leaves half a build. */
    public static void write(Path file, JsonObject build) {
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, GSON.toJson(build), StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                WoldLogger.warn("[wvbuilds] Atomic move unsupported for {}; replacing the file directly", file);
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BuildException("could not write " + file + ": " + e.getMessage(), e);
        }
    }

    /** Writes a backup for {@code player} and trims old ones; returns the file. */
    public static Path writeBackup(MinecraftServer server, UUID player, String label, JsonObject build) {
        Path dir = backupsRoot(server, player);
        Path file = dir.resolve(LocalDateTime.now().format(STAMP) + "_" + label.replaceAll("[^A-Za-z0-9_\\-]", "_") + ".json");
        write(file, build);
        List<Path> backups = listBackups(server, player);
        for (int i = BACKUPS_KEPT; i < backups.size(); i++) {
            try {
                Files.deleteIfExists(backups.get(i));
            } catch (IOException e) {
                WoldLogger.warn("[wvbuilds] Could not trim the old backup {}", backups.get(i), e);
            }
        }
        return file;
    }

    /** The player's backups, newest first. */
    public static List<Path> listBackups(MinecraftServer server, UUID player) {
        Path dir = backupsRoot(server, player);
        List<Path> backups = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return backups;
        }
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json")).forEach(backups::add);
        } catch (IOException e) {
            WoldLogger.error("[wvbuilds] Could not list backups in {}", dir, e);
        }
        backups.sort(Comparator.comparing((Path path) -> path.getFileName().toString()).reversed());
        return backups;
    }

    public static String toJson(JsonObject build) {
        return GSON.toJson(build);
    }
}
