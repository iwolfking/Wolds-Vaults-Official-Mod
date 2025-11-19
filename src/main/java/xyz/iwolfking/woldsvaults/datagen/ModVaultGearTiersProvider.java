package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.lib.core.datagen.AbstractGearModifierProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.gear.ModifierGroupBuilder;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.gear.RangeValue;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.function.Consumer;

public class ModVaultGearTiersProvider extends AbstractGearModifierProvider {
    public ModVaultGearTiersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void addModifierGroups(Map<String, Consumer<ModifierGroupBuilder>> map) {
        map.put("sword", modifierGroupBuilder -> {
            modifierGroupBuilder.category("IMPLICIT", categoryBuilder -> {
                categoryBuilder.entry("the_vault:damage_dweller", "ModDamageDweller", "the_vault:damage_dweller_implicit", entryBuilder -> {
                    entryBuilder.tier(0, -1, 1000, new RangeValue(3.0, 9.0, 2.0)).build();
                });
            }).build();
        });
    }
}
