package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.entity.entity.pet.PetHelper;
import iskallia.vault.entity.entity.pet.PetModelRegistry;
import iskallia.vault.entity.entity.pet.PetModelType;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModPetModels {
    public static PetModelType BMO;
    public static void register() {
        BMO = PetModelRegistry.register("bmo", new PetHelper.PetVariant("bmo", "BMO", false, false, PetHelper.PetTrait.Builder.create().addBlink().addSleep().build(), new PetHelper.PetRenderData(WoldsVaults.id("geo/companion/bmo.geo.json"), WoldsVaults.id("textures/entity/bmo.png"), WoldsVaults.id("animations/bmo.animation.json"))));
    }
}
