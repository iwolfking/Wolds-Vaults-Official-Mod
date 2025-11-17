package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.lib.core.datagen.gen.AbstractPaletteProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.gen.AbstractThemeProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultThemesProvider extends AbstractThemeProvider {
    public ModVaultThemesProvider(DataGenerator generator, String modid) {
        super(generator, modid);
    }

    @Override
    protected void registerThemes() {
        add(new ResourceLocation("woldsvaults", "eclipse"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("eclipse_starts").toString())
                    .rooms(WoldsVaults.id("eclipse_rooms").toString())
                    .tunnels(WoldsVaults.id("eclipse_tunnels").toString())

                    .ambientLight(0.2f)
                    .fogColor(12358351)
                    .grassColor(8041299)
                    .foliageColor(8041299)
                    .waterColor(3112412)
                    .waterFogColor(3112412)
                    .themeColor(3112412)
                    .particle("minecraft:ambient_entity_effect")
                    .particleProbability(0.002f);
        });
    }
}
