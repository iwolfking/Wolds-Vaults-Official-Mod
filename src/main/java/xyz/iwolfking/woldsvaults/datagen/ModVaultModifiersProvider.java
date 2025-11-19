package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.lib.core.datagen.AbstractVaultModifierPoolsProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.AbstractVaultModifierProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.modifier_pools.ModifierPoolBuilder;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.modifiers.ModifierBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.function.Consumer;

public class ModVaultModifiersProvider extends AbstractVaultModifierProvider {
    public ModVaultModifiersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }


    @Override
    public void addFiles(Map<String, Consumer<ModifierBuilder>> map) {
        map.put("example", b -> {

            // -------- greedy -----------
            b.type("the_vault:modifier_type/vault_time", t ->
                    t.modifier("the_vault:extra_extension", m ->
                            m.property("timeAddedTicks", 6000)
                                    .display("Extra Extension", "#EBFF8D",
                                            "Super extendo!", "Exteeension",
                                            "the_vault:gui/modifiers/impossible")

                    )
            );
        });
    }
}
