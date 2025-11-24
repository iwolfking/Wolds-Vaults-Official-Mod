package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultModifierPoolsProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.modifier_pools.ModifierPoolBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.function.Consumer;

public class ModVaultModifierPoolsProvider extends AbstractVaultModifierPoolsProvider {
    public ModVaultModifierPoolsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void addFiles(Map<String, Consumer<ModifierPoolBuilder>> map) {
        map.put("test", b -> {

            b.pool("the_vault:example_1", pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:coin_cascade", 10);
                                e.add("the_vault:wooden_cascade", 10);
                                e.add("the_vault:ornate_cascade", 6);
                                e.add("the_vault:gilded_cascade", 6);
                                e.add("the_vault:living_cascade", 6);
                                e.add("the_vault:plentiful", 6);
                                e.add("the_vault:extended", 4);
                                e.add("the_vault:ornate", 2);
                                e.add("the_vault:gilded", 2);
                                e.add("the_vault:living", 2);
                                e.add("the_vault:phoenix", 1);
                                e.add("the_vault:extra_extension", 1);
                            })
                    )
            );

            b.pool("the_vault:example_2", pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:overpower", 1);
                                e.add("the_vault:champion_domain", 1);
                                e.add("the_vault:loot_goblin", 1);
                                e.add("the_vault:door_hunter", 1);
                                e.add("the_vault:ultimate_regeneration", 1);
                                e.add("the_vault:kill_nuke", 1);
                                e.add("the_vault:lunar", 1);
                                e.add("the_vault:kill_hunter", 1);
                                e.add("the_vault:kill_totem", 1);
                                e.add("the_vault:bronze_nuke", 1);
                                e.add("the_vault:glued_mobs", 1);
                                e.add("the_vault:rock_solid", 1);
                                e.add("the_vault:pylon_hunter", 1);
                                e.add("the_vault:soul_fest", 1);
                            })
                    )
            );

            b.pool("the_vault:example_3", pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:coin_cascade", 10);
                                e.add("the_vault:gilded_cascade", 10);
                                e.add("the_vault:living_cascade", 10);
                                e.add("the_vault:ornate_cascade", 10);
                                e.add("the_vault:wooden_cascade", 10);
                                e.add("the_vault:coin_cascade", 10);
                                e.add("the_vault:gilded", 10);
                                e.add("the_vault:living", 10);
                                e.add("the_vault:ornate", 10);
                                e.add("the_vault:wooden", 10);
                                e.add("the_vault:plentiful", 10);
                                e.add("the_vault:extended", 10);
                                e.add("the_vault:phoenix", 10);
                            })
                    )
            );
        });
    }
}
