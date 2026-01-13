package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.data.adapter.basic.TypeSupplierAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.deck.*;

@Mixin(value = DeckModifier.Adapter.class, remap = false)
public class MixinDeckModifierAdapter extends TypeSupplierAdapter<DeckModifier<?>> {
    public MixinDeckModifierAdapter(String key, boolean nullable) {
        super(key, nullable);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void registerNewCores(CallbackInfo ci) {
        this.register("empty_slot_efficiency", EmptySlotDeckModifier.class, EmptySlotDeckModifier::new);
        this.register("no_arcane_efficiency", NitwitDeckModifier.class, NitwitDeckModifier::new);
        this.register("group_adjacency_efficiency", AdjacencyBonusDeckModifier.class, AdjacencyBonusDeckModifier::new);
        this.register("create_group_slots", CreateGroupSlotDeckModifier.class, CreateGroupSlotDeckModifier::new);
        this.register("arcane_slot_boost", ArcaneSlotDeckModifier.class, ArcaneSlotDeckModifier::new);
        this.register("dominance_boost", DominanceDeckModifier.class, DominanceDeckModifier::new);
        this.register("group_synergy", GroupSynergyDeckModifier.class, GroupSynergyDeckModifier::new);
    }
}
