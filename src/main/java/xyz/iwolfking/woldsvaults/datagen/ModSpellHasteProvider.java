package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.SpellHasteConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

/**
 * Spell haste: the base cap on positive haste (+400%) and the cap on cooldown skip chance (50%). Haste values on gear,
 * talents, cards and modifiers are converted where each source is defined.
 */
public class ModSpellHasteProvider extends AbstractWoldsTableProvider {

    public ModSpellHasteProvider(DataGenerator generator) {
        super(generator, "spell_haste");
    }

    @Override
    public void registerConfigs() {
        SpellHasteConfig table = new SpellHasteConfig();
        table.baseCap = 4.0F;
        table.skipChanceCap = 0.5F;
        table("spell_haste", table);
    }
}
