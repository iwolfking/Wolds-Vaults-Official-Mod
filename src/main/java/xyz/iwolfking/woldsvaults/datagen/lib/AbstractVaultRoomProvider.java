package xyz.iwolfking.woldsvaults.datagen.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import iskallia.vault.VaultMod;
import net.minecraft.core.BlockPos;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.vhapi.api.datagen.lib.gen.palette.PaletteBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.TallRooms;

public abstract class AbstractVaultRoomProvider implements DataProvider {
    public static final ResourceLocation CHALLENGE_ROOMS = VaultMod.id("vault/rooms/challenge_rooms");
    public static final ResourceLocation OMEGA_ROOMS = VaultMod.id("vault/rooms/omega_rooms");
    private static final ResourceLocation ROOM_BASE = VaultMod.id("generic/room_base");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int ROOM_SIZE = 47;
    private static final int MYTHIC_SIZE = 141;
    private static final int[] MYTHIC_DOORS = {23, 70, 117};
    private static final Set<String> OPEN_BLOCKS = Set.of("minecraft:air", "minecraft:cave_air", "minecraft:light", "minecraft:structure_void", "the_vault:placeholder");
    private static final Path PACK_GEN = Path.of("config", "the_vault", "gen");

    protected final DataGenerator generator;
    protected final ExistingFileHelper existingFileHelper;
    protected final String modId;
    private final Map<ResourceLocation, RoomBuilder> rooms = new LinkedHashMap<>();

    protected AbstractVaultRoomProvider(DataGenerator generator, ExistingFileHelper existingFileHelper, String modId) {
        this.generator = generator;
        this.existingFileHelper = existingFileHelper;
        this.modId = modId;
    }

    protected abstract void registerRooms();

    protected void add(ResourceLocation id, Consumer<RoomBuilder> consumer) {
        RoomBuilder builder = new RoomBuilder(id);
        consumer.accept(builder);
        rooms.put(id, builder);
    }

    @Override
    public void run(HashCache cache) throws IOException {
        rooms.clear();
        registerRooms();
        if (rooms.isEmpty()) {
            return;
        }

        Path output = generator.getOutputFolder();
        Set<ResourceLocation> palettes = readKeys(PACK_GEN.resolve("palettes.json"));
        palettes.addAll(readKeys(output.resolve("data/" + modId + "/vault_configs/palettes/palettes.json")));
        Map<ResourceLocation, JsonArray> pools = readPools(output);

        JsonArray templateKeys = new JsonArray();
        JsonArray paletteKeys = new JsonArray();
        JsonArray poolKeys = new JsonArray();
        Map<ResourceLocation, JsonArray> merges = new LinkedHashMap<>();
        JsonArray mythicPool = new JsonArray();

        for (RoomBuilder room : rooms.values()) {
            checkStructure(room);

            JsonObject template = new JsonObject();
            template.addProperty("type", "structure");
            template.addProperty("path", room.structureFile().toString());
            templateKeys.add(key(room.templateId(), room.name, template));

            List<ResourceLocation> ownPalettes = new ArrayList<>();
            if (room.palette != null) {
                save(cache, room.palette.build().toJson(), output, "palettes", room.paletteId());
                paletteKeys.add(key(room.paletteId(), room.name + " Base", room.paletteId()));
                palettes.add(room.paletteId());
                ownPalettes.add(room.paletteId());
            }
            for (ResourceLocation palette : room.palettes) {
                require(palettes.contains(palette), room, "uses palette " + palette + ", which no palette config defines");
                ownPalettes.add(palette);
            }
            List<ResourceLocation> entryPalettes = new ArrayList<>(ownPalettes);
            if (room.roomBase) {
                entryPalettes.add(ROOM_BASE);
            }

            JsonArray roomPool = new JsonArray();
            roomPool.add(entry(1, "value", value(room.templateId(), entryPalettes, -1)));
            save(cache, roomPool, output, "template_pools", room.templateId());
            poolKeys.add(key(room.templateId(), room.name, room.templateId()));

            if (room.mythicWeight > 0) {
                mythicPool.add(entry(room.mythicWeight, "reference", reference(room.templateId(), room.minLevel)));
            }

            for (Map.Entry<String, List<String>> slot : room.decor.entrySet()) {
                JsonArray decorPool = new JsonArray();
                for (String piece : slot.getValue()) {
                    ResourceLocation pieceId = room.decorId(piece);
                    ResourceLocation file = ResourceLocation.fromNamespaceAndPath(pieceId.getNamespace(), "structures/" + pieceId.getPath() + ".nbt");
                    require(existingFileHelper.exists(file, PackType.SERVER_DATA), room, "needs its " + slot.getKey() + " piece at data/" + file.getNamespace() + "/" + file.getPath());
                    JsonObject pieceTemplate = new JsonObject();
                    pieceTemplate.addProperty("type", "structure");
                    pieceTemplate.addProperty("path", file.toString());
                    templateKeys.add(key(pieceId, room.name + " " + readableName(piece), pieceTemplate));
                    decorPool.add(entry(1, "value", value(pieceId, List.of(), -1)));
                }
                ResourceLocation poolId = room.decorId(slot.getKey());
                save(cache, decorPool, output, "template_pools", poolId);
                poolKeys.add(key(poolId, room.name + " " + readableName(slot.getKey()), poolId));
            }

            for (Placement placement : room.placements) {
                require(pools.containsKey(placement.pool()), room, "is placed in " + placement.pool() + ", which is not a template pool");
                merges.computeIfAbsent(placement.pool(), id -> new JsonArray()).add(entry(placement.weight(), "reference", reference(room.templateId(), room.minLevel)));
            }

            for (ResourceLocation like : room.placedLike) {
                int copies = 0;
                for (Map.Entry<ResourceLocation, JsonArray> pool : pools.entrySet()) {
                    if (pool.getKey().equals(like)) {
                        continue;
                    }
                    for (JsonElement element : pool.getValue()) {
                        JsonObject likeEntry = element.getAsJsonObject();
                        JsonObject likeValue = likeEntry.getAsJsonObject("value");
                        if (likeValue == null || !like.toString().equals(likeValue.get("template").getAsString())) {
                            continue;
                        }
                        List<ResourceLocation> copiedPalettes = new ArrayList<>(ownPalettes);
                        if (likeValue.has("palettes")) {
                            likeValue.getAsJsonArray("palettes").forEach(palette -> copiedPalettes.add(ResourceLocation.parse(palette.getAsString())));
                        }
                        merges.computeIfAbsent(pool.getKey(), id -> new JsonArray()).add(entry(likeEntry.get("weight").getAsDouble(), "value", value(room.templateId(), copiedPalettes, room.minLevel)));
                        copies++;
                    }
                }
                require(copies > 0, room, "is placed like " + like + ", which no template pool lists");
            }
        }

        for (Map.Entry<ResourceLocation, JsonArray> merge : merges.entrySet()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(merge.getKey().getNamespace(), merge.getKey().getPath() + "_merge");
            save(cache, merge.getValue(), output, "template_pools", id);
            poolKeys.add(key(id, readableName(id.getPath()), id));
        }

        if (!mythicPool.isEmpty()) {
            ResourceLocation id = mythicRooms();
            save(cache, mythicPool, output, "template_pools", id);
            poolKeys.add(key(id, "Mythic Rooms", id));
        }

        saveKeys(cache, output, "templates", templateKeys);
        saveKeys(cache, output, "palettes", paletteKeys);
        saveKeys(cache, output, "template_pools", poolKeys);
    }

    public ResourceLocation mythicRooms() {
        return ResourceLocation.fromNamespaceAndPath(modId, "vault/rooms/mythic_rooms");
    }

    @Override
    public String getName() {
        return modId + " Vault Room Provider";
    }

    private void checkStructure(RoomBuilder room) throws IOException {
        ResourceLocation file = room.structureFile();
        require(existingFileHelper.exists(file, PackType.SERVER_DATA), room, "needs its structure at data/" + file.getNamespace() + "/" + file.getPath());

        CompoundTag structure;
        try (Resource nbt = existingFileHelper.getResource(file, PackType.SERVER_DATA); InputStream stream = nbt.getInputStream()) {
            structure = NbtIo.readCompressed(stream);
        }
        ListTag size = structure.getList("size", Tag.TAG_INT);
        boolean mythic = room.mythicWeight > 0;
        int width = mythic ? MYTHIC_SIZE : ROOM_SIZE;
        require(size.size() == 3 && size.getInt(0) == width && size.getInt(2) == width
                        && (mythic ? size.getInt(1) <= TallRooms.MAX_HEIGHT : size.getInt(1) == ROOM_SIZE), room,
                "is " + size.getInt(0) + "x" + size.getInt(1) + "x" + size.getInt(2) + ", but " + (mythic ? "mythic rooms must be " + MYTHIC_SIZE
                        + " wide and long and at most " + TallRooms.MAX_HEIGHT + " tall" : "vault rooms must be " + ROOM_SIZE + "x" + ROOM_SIZE + "x" + ROOM_SIZE));

        ListTag palette = structure.getList("palette", Tag.TAG_COMPOUND);
        Map<Long, String> blocks = new HashMap<>();
        for (Tag tag : structure.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag block = (CompoundTag) tag;
            ListTag pos = block.getList("pos", Tag.TAG_INT);
            blocks.put(BlockPos.asLong(pos.getInt(0), pos.getInt(1), pos.getInt(2)), palette.getCompound(block.getInt("state")).getString("Name"));
        }
        String[] sides = {"north", "south", "west", "east"};
        for (int side = 0; side < sides.length; side++) {
            for (int centre : mythic ? MYTHIC_DOORS : new int[] {ROOM_SIZE / 2}) {
                if (!hasDoorway(blocks, side, side % 2 == 0 ? 0 : width - 1, centre)) {
                    WoldLogger.error("Room {} has no doorway on its {} side at {}; tunnels meet it at {}-{}, y 22-26 and need at least a 1 wide, 2 high gap there",
                            room.id, sides[side], centre, centre - 2, centre + 2);
                }
            }
        }
    }

    /** Every side of all 118 pack rooms passes this, even the ones with decorated or uneven doorways */
    private static boolean hasDoorway(Map<Long, String> blocks, int side, int edge, int centre) {
        for (int across = centre - 2; across <= centre + 2; across++) {
            for (int y = 22; y <= 25; y++) {
                if (isOpen(blocks, side, edge, across, y) && isOpen(blocks, side, edge, across, y + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isOpen(Map<Long, String> blocks, int side, int edge, int across, int y) {
        long pos = side < 2 ? BlockPos.asLong(across, y, edge) : BlockPos.asLong(edge, y, across);
        String block = blocks.getOrDefault(pos, "minecraft:air");
        return OPEN_BLOCKS.contains(block) || block.startsWith("woldsvaults:mob_barrier");
    }

    private static Set<ResourceLocation> readKeys(Path index) throws IOException {
        Set<ResourceLocation> ids = new HashSet<>();
        if (Files.exists(index)) {
            for (JsonElement key : readJson(index).getAsJsonObject().getAsJsonArray("keys")) {
                ids.add(ResourceLocation.parse(key.getAsJsonObject().get("id").getAsString()));
            }
        }
        return ids;
    }

    private Map<ResourceLocation, JsonArray> readPools(Path output) throws IOException {
        Map<ResourceLocation, JsonArray> pools = new LinkedHashMap<>();
        Path packIndex = PACK_GEN.resolve("template_pools.json");
        if (!Files.exists(packIndex)) {
            throw new IllegalStateException("No pack configs at " + packIndex.toAbsolutePath() + "; runData needs seedRunDataConfigs to copy them in first");
        }
        for (JsonElement key : readJson(packIndex).getAsJsonObject().getAsJsonArray("keys")) {
            if (!key.getAsJsonObject().has("1.0")) {
                continue;
            }
            Path body = Path.of(key.getAsJsonObject().get("1.0").getAsString());
            if (Files.exists(body)) {
                JsonElement json = readJson(body);
                if (json.isJsonArray()) {
                    pools.put(ResourceLocation.parse(key.getAsJsonObject().get("id").getAsString()), json.getAsJsonArray());
                }
            }
        }
        Path modIndex = output.resolve("data/" + modId + "/vault_configs/template_pools/template_pools.json");
        if (Files.exists(modIndex)) {
            for (JsonElement key : readJson(modIndex).getAsJsonObject().getAsJsonArray("keys")) {
                ResourceLocation id = ResourceLocation.parse(key.getAsJsonObject().get("1.0").getAsString());
                Path body = output.resolve("data/" + id.getNamespace() + "/vault_configs/gen/template_pools/" + id.getPath() + ".json");
                if (Files.exists(body)) {
                    pools.put(ResourceLocation.parse(key.getAsJsonObject().get("id").getAsString()), readJson(body).getAsJsonArray());
                }
            }
        }
        return pools;
    }

    private static JsonElement readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        }
    }

    private static void save(HashCache cache, JsonElement json, Path output, String kind, ResourceLocation id) throws IOException {
        DataProvider.save(GSON, cache, json, output.resolve("data/" + id.getNamespace() + "/vault_configs/gen/" + kind + "/" + id.getPath() + ".json"));
    }

    private void saveKeys(HashCache cache, Path output, String kind, JsonArray keys) throws IOException {
        if (keys.isEmpty()) {
            return;
        }
        JsonObject index = new JsonObject();
        index.add("keys", keys);
        DataProvider.save(GSON, cache, index, output.resolve("data/" + modId + "/vault_configs/" + kind + "/rooms.json"));
    }

    private static JsonObject key(ResourceLocation id, String name, Object body) {
        JsonObject key = new JsonObject();
        key.addProperty("id", id.toString());
        key.addProperty("name", name);
        if (body instanceof JsonElement json) {
            key.add("1.0", json);
        } else {
            key.addProperty("1.0", body.toString());
        }
        return key;
    }

    private static JsonObject entry(double weight, String kind, JsonElement content) {
        JsonObject entry = new JsonObject();
        if (weight == Math.rint(weight)) {
            entry.addProperty("weight", (int) weight);
        } else {
            entry.addProperty("weight", weight);
        }
        entry.add(kind, content);
        return entry;
    }

    private static JsonObject value(ResourceLocation template, List<ResourceLocation> palettes, int minLevel) {
        JsonObject value = new JsonObject();
        value.addProperty("template", template.toString());
        JsonArray paletteIds = new JsonArray();
        palettes.forEach(palette -> paletteIds.add(palette.toString()));
        value.add("palettes", paletteIds);
        if (minLevel >= 0) {
            value.add("conditions", minLevelCondition(minLevel));
        }
        return value;
    }

    private static JsonElement reference(ResourceLocation pool, int minLevel) {
        if (minLevel < 0) {
            return new JsonPrimitive(pool.toString());
        }
        JsonObject reference = new JsonObject();
        reference.addProperty("id", pool.toString());
        reference.add("conditions", minLevelCondition(minLevel));
        return reference;
    }

    private static JsonArray minLevelCondition(int minLevel) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "level");
        condition.addProperty("min", minLevel);
        condition.addProperty("max", -1);
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        return conditions;
    }

    private static void require(boolean check, RoomBuilder room, String problem) {
        if (!check) {
            throw new IllegalStateException("Vault room " + room.id + " " + problem);
        }
    }

    private static String readableName(String path) {
        StringBuilder name = new StringBuilder();
        for (String word : path.substring(path.lastIndexOf('/') + 1).split("_")) {
            if (!word.isEmpty()) {
                name.append(name.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return name.toString();
    }

    private record Placement(ResourceLocation pool, double weight) {
    }

    public static class RoomBuilder {
        private final ResourceLocation id;
        private String name;
        private PaletteBuilder palette;
        private final List<ResourceLocation> palettes = new ArrayList<>();
        private final List<Placement> placements = new ArrayList<>();
        private final List<ResourceLocation> placedLike = new ArrayList<>();
        private final Map<String, List<String>> decor = new LinkedHashMap<>();
        private boolean roomBase = true;
        private int minLevel = -1;
        private double mythicWeight;

        private RoomBuilder(ResourceLocation id) {
            this.id = id;
            this.name = readableName(id.getPath());
        }

        public RoomBuilder name(String name) {
            this.name = name;
            return this;
        }

        /** The room's own palette, written as <namespace>:<room path>/base and applied first */
        public RoomBuilder palette(Consumer<PaletteBuilder> consumer) {
            this.palette = new PaletteBuilder();
            consumer.accept(this.palette);
            return this;
        }

        public RoomBuilder palette(ResourceLocation existingPalette) {
            this.palettes.add(existingPalette);
            return this;
        }

        /** Generic/room_base turns the chest, coin, ore, pylon and gate placeholders into loot, ores and gates */
        public RoomBuilder noRoomBase() {
            this.roomBase = false;
            return this;
        }

        public RoomBuilder challenge(double weight) {
            return pool(CHALLENGE_ROOMS, weight);
        }

        public RoomBuilder omega(double weight) {
            return pool(OMEGA_ROOMS, weight);
        }

        /**
         * A 143 wide room over a 3x3 block of cells, with doorways at 24, 71 and 118 along every side; it goes in the
         * mythic_rooms pool
         */
        public RoomBuilder mythic(double weight) {
            this.mythicWeight = weight;
            return this;
        }

        public RoomBuilder pool(ResourceLocation pool, double weight) {
            this.placements.add(new Placement(pool, weight));
            return this;
        }

        /** Adds the room to every pool that lists the given template, with that entry's weight and theme palettes */
        public RoomBuilder placedLike(ResourceLocation template) {
            this.placedLike.add(template);
            return this;
        }

        public RoomBuilder minLevel(int level) {
            this.minLevel = level;
            return this;
        }

        /**
         * A pool of add-on pieces for the room's own jigsaws, <namespace>:vault/decor/<room>/<slot>, read from
         * structures/vault/decor/<room>/<piece>.nbt
         */
        public RoomBuilder decor(String slot, String... pieces) {
            this.decor.put(slot, List.of(pieces));
            return this;
        }

        private ResourceLocation templateId() {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "vault/rooms/" + id.getPath());
        }

        private ResourceLocation structureFile() {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "structures/vault/rooms/" + id.getPath() + ".nbt");
        }

        private ResourceLocation decorId(String name) {
            String room = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "vault/decor/" + room + "/" + name);
        }

        private ResourceLocation paletteId() {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/base");
        }
    }
}
