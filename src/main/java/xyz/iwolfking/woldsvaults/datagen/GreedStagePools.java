package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractTemplatePoolProvider.TemplatePoolBuilder;

import java.util.function.Consumer;

/**
 * The room pools of the greed stages, registered into {@link ModVaultTemplatePoolsProvider}. Stage 2 holds the
 * challenge and omega room pools, the rooms whose palettes differ from the normal ones and the vendor and etching
 * vendor rooms; rooms that equal their normal pool apart from the room base are listed by their normal id. Stage 3
 * holds the trove. The stage-2 omega pool lists the normal trove, which stage 3 takes over.
 */
public final class GreedStagePools {
    private GreedStagePools() {
    }

    public static void register(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        poolRoomsChallenge(pools);
        poolRoomsOmega(pools);
        poolVendorRoomsVendorRooms(pools);
        poolVendorRoomsEtchingRooms(pools);
        poolRoomsTrove(pools);
        poolRoomsChallengeCrystalCaves(pools);
        poolRoomsChallengeDragon(pools);
        poolRoomsChallengeGraveyard(pools);
        poolRoomsChallengeLaboratory(pools);
        poolRoomsChallengeLabyrinth(pools);
        poolRoomsChallengeRaidBaseRooms(pools);
        poolRoomsChallengeRaidEasterRooms(pools);
        poolRoomsChallengeRaidOrcishRooms(pools);
        poolRoomsChallengeRaidRooms(pools);
        poolRoomsChallengeWildwest(pools);
        poolRoomsChallengeXMark(pools);
        poolRoomsOmegaBlacksmith(pools);
        poolRoomsOmegaCometObservatory(pools);
        poolRoomsOmegaCove(pools);
        poolRoomsOmegaMushRoom(pools);
        poolRoomsOmegaVendor(pools);
        poolRoomsOmegaWardensGarden(pools);
        poolRoomsOmegaWoldsDinner(pools);
    }

    private static void poolRoomsChallenge(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge"))
                .addReference(3, GreedStageIds.id(2, "rooms/challenge/labyrinth"))
                .addReference(5, GreedStageIds.id(2, "rooms/challenge/graveyard"))
                .addReference(5, VaultMod.id("vault/rooms/challenge/aquarium"))
                .addReference(5, GreedStageIds.id(2, "rooms/challenge/crystal_caves"))
                .addReference(2, GreedStageIds.id(2, "rooms/challenge/dragon"))
                .addReference(10, VaultMod.id("vault/rooms/challenge/village"))
                .addReference(5, GreedStageIds.id(2, "rooms/challenge/wildwest"))
                .addReference(6, VaultMod.id("vault/rooms/challenge/factory"))
                .addReference(10, GreedStageIds.id(2, "rooms/challenge/x-mark"))
                .addReference(5, VaultMod.id("vault/rooms/challenge/pirate_cave"))
                .addReference(4, GreedStageIds.id(2, "rooms/challenge/raid/rooms"))
                .addReference(3, GreedStageIds.id(2, "rooms/challenge/laboratory")));
    }

    private static void poolRoomsOmega(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/omega"))
                .addReference(7, VaultMod.id("vault/rooms/omega/cube"))
                .addReference(1, VaultMod.id("vault/rooms/omega/digsite2"))
                .addReference(7, VaultMod.id("vault/rooms/omega/playzone"))
                .addReference(2, VaultMod.id("vault/rooms/omega/iskallian_garden"))
                .addReference(3, GreedStageIds.id(2, "rooms/omega/comet_observatory"))
                .addReference(7, VaultMod.id("vault/rooms/omega/painting"))
                .addReference(3, VaultMod.id("vault/rooms/omega/mine"))
                .addReference(1, VaultMod.id("vault/rooms/omega/trove"))
                .addReference(3, GreedStageIds.id(2, "rooms/omega/vendor"))
                .addReference(7, GreedStageIds.id(2, "rooms/omega/blacksmith"))
                .addReference(3, GreedStageIds.id(2, "rooms/omega/cove"))
                .addReference(7, GreedStageIds.id(2, "rooms/omega/mush_room"))
                .addReference(7, VaultMod.id("vault/rooms/omega/library"))
                .addReference(4, VaultMod.id("vault/rooms/omega/arcade"))
                .addReference(1, VaultMod.id("vault/rooms/hellish/hellish_digsite"))
                .addReference(7, VaultMod.id("vault/rooms/omega/pixel"))
                .addReference(5, GreedStageIds.id(2, "rooms/omega/wolds_dinner"))
                .addReference(3, GreedStageIds.id(2, "rooms/omega/wardens_garden"))
                .addReference(4, VaultMod.id("vault/rooms/challenge/temple")));
    }

    private static void poolVendorRoomsVendorRooms(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "vendor_rooms/vendor_rooms"))
                .addEntry(25, VaultMod.id("vault/vendor_rooms/vendor1"), palettes -> { })
                .addEntry(12, VaultMod.id("vault/vendor_rooms/vendor2"), palettes -> { })
                .addEntry(25, VaultMod.id("vault/vendor_rooms/vendor3"), palettes -> { })
                .addEntry(25, VaultMod.id("vault/vendor_rooms/vendor4"), palettes -> { })
                .addEntry(12, VaultMod.id("vault/vendor_rooms/vendor5"), palettes -> { })
                .addEntry(5, VaultMod.id("vault/vendor_rooms/vendor6"), palettes -> { })
                .addEntry(12, VaultMod.id("vault/vendor_rooms/forge"), palettes -> palettes.add(VaultMod.id("vendor_rooms/blacksmith_rooms")))
                .addEntry(16, VaultMod.id("vault/vendor_rooms/vendor_blacksmith_shop"), palettes -> palettes.add(VaultMod.id("vendor_rooms/blacksmith_rooms")))
                .addEntry(10, VaultMod.id("vault/vendor_rooms/card_shop"), palettes -> palettes.add(VaultMod.id("vendor_rooms/card_rooms"))));
    }

    private static void poolVendorRoomsEtchingRooms(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "vendor_rooms/etching_rooms"))
                .addEntry(1, VaultMod.id("vault/vendor_rooms/etching_vendor1"), palettes -> { }));
    }

    private static void poolRoomsTrove(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(3, "rooms/trove"))
                .addEntry(1, VaultMod.id("vault/rooms/omega/trove"), palettes -> palettes.add(GreedStageIds.id(3, "trove/base"))));
    }

    private static void poolRoomsChallengeCrystalCaves(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/crystal_caves"))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/crystal_caves1"), palettes -> palettes.add(VaultMod.id("crystal_caves/prismatic")))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/crystal_caves2"), palettes -> palettes.add(VaultMod.id("crystal_caves/prismatic")))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/crystal_caves3"), palettes -> palettes.add(VaultMod.id("crystal_caves/prismatic")))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/crystal_caves4"), palettes -> palettes.add(VaultMod.id("crystal_caves/prismatic")))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/crystal_caves5"), palettes -> palettes.add(VaultMod.id("crystal_caves/prismatic"))));
    }

    private static void poolRoomsChallengeDragon(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/dragon"))
                .addEntry(3, VaultMod.id("vault/rooms/challenge/dragon1"), palettes -> palettes.add(VaultMod.id("dragon/base")))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/dragon_og"), palettes -> palettes.add(VaultMod.id("dragon/base_og"))));
    }

    private static void poolRoomsChallengeGraveyard(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/graveyard"))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/graveyard"), palettes -> palettes.add(GreedStageIds.id(2, "graveyard/base")).add(VaultMod.id("graveyard/spawner_settings"))));
    }

    private static void poolRoomsChallengeLaboratory(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/laboratory"))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/laboratory1"), palettes -> palettes.add(GreedStageIds.id(2, "laboratory/base")))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/laboratory2"), palettes -> palettes.add(GreedStageIds.id(2, "laboratory/base"))));
    }

    private static void poolRoomsChallengeLabyrinth(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/labyrinth"))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/labyrinth"), palettes -> palettes.add(GreedStageIds.id(2, "labyrinth/base")).add(VaultMod.id("labyrinth/spawner_settings"))));
    }

    private static void poolRoomsChallengeRaidBaseRooms(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/raid/base_rooms"))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/raid1"), palettes -> palettes.add(GreedStageIds.id(2, "raid/base"))));
    }

    private static void poolRoomsChallengeRaidEasterRooms(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/raid/easter_rooms"))
                .addReference(1, GreedStageIds.id(2, "rooms/challenge/raid/base_rooms"), palettes -> palettes.add(VaultMod.id("raid/easter"))));
    }

    private static void poolRoomsChallengeRaidOrcishRooms(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/raid/orcish_rooms"))
                .addReference(1, GreedStageIds.id(2, "rooms/challenge/raid/base_rooms"), palettes -> palettes.add(VaultMod.id("raid/orcish"))));
    }

    private static void poolRoomsChallengeRaidRooms(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/raid/rooms"))
                .addReference(1, GreedStageIds.id(2, "rooms/challenge/raid/orcish_rooms"))
                .addReference(1, GreedStageIds.id(2, "rooms/challenge/raid/easter_rooms")));
    }

    private static void poolRoomsChallengeWildwest(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/wildwest"))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/wildwest1"), palettes -> palettes.add(GreedStageIds.id(2, "wildwest/base")))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/wildwest2"), palettes -> palettes.add(GreedStageIds.id(2, "wildwest/base"))));
    }

    private static void poolRoomsChallengeXMark(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/challenge/x-mark"))
                .addEntry(1, VaultMod.id("vault/rooms/challenge/x-mark1"), palettes -> palettes.add(GreedStageIds.id(2, "x-mark/base"))));
    }

    private static void poolRoomsOmegaBlacksmith(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/omega/blacksmith"))
                .addEntry(1, VaultMod.id("vault/rooms/omega/blacksmith1"), palettes -> palettes.add(VaultMod.id("blacksmith/base"))));
    }

    private static void poolRoomsOmegaCometObservatory(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/omega/comet_observatory"))
                .addEntry(1, VaultMod.id("vault/rooms/omega/comet_observatory"), palettes -> palettes.add(GreedStageIds.id(2, "comet_observatory/base"))));
    }

    private static void poolRoomsOmegaCove(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/omega/cove"))
                .addEntry(1, VaultMod.id("vault/rooms/omega/cove1"), palettes -> palettes.add(GreedStageIds.id(2, "cove/base"))));
    }

    private static void poolRoomsOmegaMushRoom(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/omega/mush_room"))
                .addEntry(1, VaultMod.id("vault/rooms/omega/mush_room1"), palettes -> palettes.add(VaultMod.id("mush_room/base")))
                .addEntry(1, VaultMod.id("vault/rooms/omega/mush_room2"), palettes -> palettes.add(VaultMod.id("mush_room/base"))));
    }

    private static void poolRoomsOmegaVendor(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/omega/vendor"))
                .addEntry(1, VaultMod.id("vault/rooms/omega/vendor1"), palettes -> palettes.add(GreedStageIds.id(2, "vendor/base"))));
    }

    private static void poolRoomsOmegaWardensGarden(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/omega/wardens_garden"))
                .addEntry(1, VaultMod.id("vault/rooms/omega/wardens_garden"), palettes -> palettes.add(GreedStageIds.id(2, "wardens_garden/base"))));
    }

    private static void poolRoomsOmegaWoldsDinner(Consumer<Consumer<TemplatePoolBuilder>> pools) {
        pools.accept(pool -> pool.id(GreedStageIds.id(2, "rooms/omega/wolds_dinner"))
                .addEntry(1, VaultMod.id("vault/rooms/omega/wolds_dinner"), palettes -> palettes.add(GreedStageIds.id(2, "wolds_dinner/base"))));
    }
}
