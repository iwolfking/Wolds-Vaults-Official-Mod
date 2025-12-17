package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultMapIconsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModVaultMapIconsProvider extends AbstractVaultMapIconsProvider {
    protected ModVaultMapIconsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_room_icons", builder -> {
            builder.addRoomIcons(VaultMod.id("gui/map/aquarium"), resourceLocations -> {
               resourceLocations.add(VaultMod.id("vault/rooms/challenge/aquarium"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/trove"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/omega/trove"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/woldian_garden"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/omega/iskallian_garden"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/pirate_cave"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/challenge/pirate_cave1"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/arcade"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/omega/arcade"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/hellish_digsite"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/omega/hellish_digsite"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/playzone"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/omega/playzone"));
                resourceLocations.add(VaultMod.id("vault/rooms/omega/playzone1"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/comet_observatory"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/omega/comet_observatory"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/wolds_dinner"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/omega/wolds_dinner"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/wardens_garden"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/omega/wardens_garden"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/labyrinth"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/challenge/labyrinth"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/gateway"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("map/rooms/gateway_channeling/gateway"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/graveyard"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/challenge/graveyard"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/vm_nether"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/raw/nether1"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/nether_raw"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/raw/nether1"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/vm_end"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/raw/end1"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/end_raw"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/raw/end1"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/vm_quarry"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/raw/quarry1"));
            });

            builder.addRoomIcons(VaultMod.id("gui/map/vm_cube"), resourceLocations -> {
                resourceLocations.add(VaultMod.id("vault/rooms/raw/cube1"));
            });


        });
    }
}
