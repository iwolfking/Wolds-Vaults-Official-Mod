package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.SpellHasteConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

/** Generates the spell haste table: the base cap on positive haste and the cap on cooldown skip chance. */
public class ModSpellHasteProvider extends AbstractWoldsTableProvider {

    public ModSpellHasteProvider(DataGenerator generator) {
        super(generator, "spell_haste");
    }

    @Override
    public void registerConfigs() {
        table("spell_haste", spellHaste());
    }

    /** The spell haste table; text that quotes its numbers (the statistics tab description) reads it from here. */
    public static SpellHasteConfig spellHaste() {
        SpellHasteConfig table = new SpellHasteConfig();
        table.baseCap = 4.0F;
        table.skipChanceCap = 0.5F;
        return table;
    }
}
