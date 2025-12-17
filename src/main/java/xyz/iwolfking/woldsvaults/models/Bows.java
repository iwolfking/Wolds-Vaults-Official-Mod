package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.lib.models.BowModel;

public class Bows {
    public static final DynamicModelRegistry<BowModel> REGISTRY = new DynamicModelRegistry<>();
    //public static final BowModel BOW_0;


    public Bows() {
    }

    static {
      // BOW_0 = (BowModel)REGISTRY.register((BowModel) (new BowModel(WoldsVaults.id("gear/bow/bow_0"), "Bow_0")).properties((new DynamicModelProperties()).allowTransmogrification().discoverOnRoll()));

    }
}
