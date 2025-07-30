package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModSounds;

public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {

    protected ModSoundDefinitionsProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, WoldsVaults.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {

        add(ModSounds.WOLD_AMBIENT,
                SoundDefinition.definition().with(
                        sound(WoldsVaults.id("wold/ambient1")),
                        sound(WoldsVaults.id("wold/ambient2")),
                        sound(WoldsVaults.id("wold/ambient3")),
                        sound(WoldsVaults.id("wold/ambient4"))
                ).subtitle("woldsvaults.subtitle.wold.ambient")
        );

        add(ModSounds.WOLD_HURT,
                SoundDefinition.definition().with(
                        sound(WoldsVaults.id("wold/hurt1")),
                        sound(WoldsVaults.id("wold/hurt2")),
                        sound(WoldsVaults.id("wold/hurt3"))
                ).subtitle("woldsvaults.subtitle.wold.hurt")
        );

        add(ModSounds.WOLD_DEATH,
                SoundDefinition.definition().with(
                        sound(WoldsVaults.id("wold/death1")),
                        sound(WoldsVaults.id("wold/death2"))
                ).subtitle("woldsvaults.subtitle.wold.hurt")
        );

        add(ModSounds.SAFERSPACES_PROC,
                SoundDefinition.definition().with(
                        sound(WoldsVaults.id("saferspaces/bear_01")),
                        sound(WoldsVaults.id("saferspaces/bear_02")),
                        sound(WoldsVaults.id("saferspaces/bear_03"))
                ).subtitle("woldsvaults.subtitle.saferspaces_proc")
        );

        add(ModSounds.OMINOUS_AMBIENCE,
                SoundDefinition.definition().with(
                        sound(WoldsVaults.id("ominous_ambience_1")),
                        sound(WoldsVaults.id("ominous_ambience_2")),
                        sound(WoldsVaults.id("ominous_ambience_3"))
                ).subtitle("woldsvaults.subtitle.ambience")
        );

        add(ModSounds.DARK, SoundDefinition.definition().with(sound(WoldsVaults.id("dark")).stream()));

        add(ModSounds.BLENDER, SoundDefinition.definition().with(sound(WoldsVaults.id("blender")).stream()));

    }
}
