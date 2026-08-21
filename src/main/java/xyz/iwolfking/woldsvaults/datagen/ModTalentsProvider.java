package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractTalentProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The talent-content leg of the node datagen pipeline, the counterpart to
 * {@link ModTalentStyleProvider}'s layout leg. It declared the style base class until now, so the
 * whole addon's talent-content datagen resolved to a second styles provider and emitted nothing.
 *
 * <p>Registering no talents is a legitimate state: the pack currently overrides talent content in
 * {@code config/the_vault/talents.json} directly. Talents declared here through
 * {@code add(name, builder -> ...)} would overlay the base mod's config at load.
 */
public class ModTalentsProvider extends AbstractTalentProvider {
    protected ModTalentsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
    }
}
