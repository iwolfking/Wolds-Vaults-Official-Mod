package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.PlainItemModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class MythicCharms {
    public static final DynamicModelRegistry<PlainItemModel> REGISTRY = new DynamicModelRegistry<>();
    public static final PlainItemModel IDONA = REGISTRY.register((PlainItemModel) new PlainItemModel(VaultMod.id("gear/charm/idona/mythic"), "Idona's Charm (Mythic)").properties(new DynamicModelProperties()));
    public static final PlainItemModel WENDARR = REGISTRY.register((PlainItemModel) new PlainItemModel(VaultMod.id("gear/charm/wendarr/mythic"), "Wendarr's Charm (Mythic)").properties(new DynamicModelProperties()));
    public static final PlainItemModel VELARA = REGISTRY.register((PlainItemModel) new PlainItemModel(VaultMod.id("gear/charm/velara/mythic"), "Velara's Charm (Mythic)").properties(new DynamicModelProperties()));
    public static final PlainItemModel TENOS = REGISTRY.register((PlainItemModel) new PlainItemModel(VaultMod.id("gear/charm/tenos/mythic"), "Tenos' Charm (Mythic)").properties(new DynamicModelProperties()));

    public static PlainItemModel of(VaultGod god) {
        return switch (god) {
            case IDONA -> IDONA;
            case WENDARR -> WENDARR;
            case VELARA -> VELARA;
            case TENOS -> TENOS;
        };
    }
}
