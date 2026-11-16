package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractTemplatePoolProvider.TemplatePoolBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedVaultStagesConfig;
import xyz.iwolfking.woldsvaults.config.GreedVaultStagesConfig.Stage;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The greed stages: per stage, its unlock rank and its pairs of normal palette or pool id and stage id. Stage 1
 * (Looter 1) adds enigma chests to the normal chest placeholders. Stage 2 (Looter 3) swaps in the room base chain,
 * the treasure door, the vendoor pedestal tables (stock and Barter Expert) and the challenge and omega room pools.
 * Stage 3 (Hunter 2) swaps in the treasure rooms, their chests and the trove. Refuses a pair whose stage id the stage
 * content providers do not generate.
 */
public class ModGreedVaultStagesProvider extends AbstractWoldsTableProvider {

    public ModGreedVaultStagesProvider(DataGenerator generator) {
        super(generator, "greed/stages");
    }

    @Override
    public void registerConfigs() {
        GreedVaultStagesConfig table = new GreedVaultStagesConfig();
        table.stages = new LinkedHashMap<>();

        Stage enigmas = stage(MilestoneRankLadder.LOOTER_1);
        palette(enigmas, "generic/ornate_chest_placeholder", GreedStageIds.id(1, "ornate_chest_placeholder"));
        palette(enigmas, "generic/gilded_chest_placeholder", GreedStageIds.id(1, "gilded_chest_placeholder"));
        palette(enigmas, "generic/living_chest_placeholder", GreedStageIds.id(1, "living_chest_placeholder"));
        table.stages.put("1", enigmas);

        Stage rooms = stage(MilestoneRankLadder.LOOTER_3);
        palette(rooms, "generic/room_base", GreedStageIds.id(2, "room_base"));
        palette(rooms, "generic/treasure_door_placeholder", GreedStageIds.id(2, "treasure_door_placeholder"));
        palette(rooms, "vendor_rooms/vendor_rooms", GreedStageIds.id(2, "vendor_rooms/vendor_rooms"));
        palette(rooms, WoldsVaults.id("vendor_rooms/barter_expert"), GreedStageIds.id(2, "vendor_rooms/barter_expert"));
        pool(rooms, "vault/rooms/challenge_rooms", GreedStageIds.id(2, "rooms/challenge"));
        pool(rooms, "vault/rooms/omega_rooms", GreedStageIds.id(2, "rooms/omega"));
        table.stages.put("2", rooms);

        Stage treasure = stage(MilestoneRankLadder.HUNTER_2);
        palette(treasure, "treasure_rooms/treasure_rooms", GreedStageIds.id(3, "treasure_rooms"));
        palette(treasure, "treasure_rooms/treasure_chest", GreedStageIds.id(3, "treasure_chest"));
        pool(treasure, "vault/rooms/omega/trove", GreedStageIds.id(3, "rooms/trove"));
        table.stages.put("3", treasure);

        requireGenerated(table);
        table("vault_stages", table);
    }

    private static Stage stage(int unlockRank) {
        Stage stage = new Stage();
        stage.unlockRank = unlockRank;
        stage.palettes = new LinkedHashMap<>();
        stage.pools = new LinkedHashMap<>();
        return stage;
    }

    private static void palette(Stage stage, String normal, ResourceLocation stageId) {
        palette(stage, VaultMod.id(normal), stageId);
    }

    private static void palette(Stage stage, ResourceLocation normal, ResourceLocation stageId) {
        stage.palettes.put(normal.toString(), stageId.toString());
    }

    private static void pool(Stage stage, String normal, ResourceLocation stageId) {
        stage.pools.put(VaultMod.id(normal).toString(), stageId.toString());
    }

    private static void requireGenerated(GreedVaultStagesConfig table) {
        Set<String> palettes = new HashSet<>();
        GreedStagePalettes.register((id, builder, palette) -> palettes.add(id.toString()));
        Set<String> pools = new HashSet<>();
        GreedStagePools.register(pool -> {
            TemplatePoolBuilder builder = new TemplatePoolBuilder();
            pool.accept(builder);
            pools.add(builder.getId().toString());
        });
        for (Map.Entry<String, Stage> entry : table.stages.entrySet()) {
            requireIn(entry.getKey(), entry.getValue().palettes, palettes, "palette");
            requireIn(entry.getKey(), entry.getValue().pools, pools, "pool");
        }
    }

    private static void requireIn(String stage, Map<String, String> pairs, Set<String> generated, String kind) {
        for (String stageId : pairs.values()) {
            if (!generated.contains(stageId)) {
                throw new IllegalStateException("Greed stage " + stage + " pairs the " + kind + " " + stageId + ", which no stage provider generates");
            }
        }
    }
}
