package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.ObjectiveShuffleModifier;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.scavenger.ScavengeTask;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Optional;

@Mixin(value = ScavengerBingoObjective.class, remap = false)
public interface ScavengerBingoObjectiveAccessor {
    @Invoker
    void callCountItemFromWorld(ItemStack stack);

    @Accessor
    static SupplierKey<Objective> getKEY() {throw new UnsupportedOperationException();}

    @Accessor
    static FieldRegistry getFIELDS() {throw new UnsupportedOperationException();}
    
  
    @Accessor
    int getLastScaledJoined();

    @Accessor
    void setLastScaledJoined(int lastScaledJoined);
    

    @Invoker
    String callGetItemKey(ItemStack stack);
    

    @Invoker
    void callCollectCandidatesFromTask(ScavengeTask task, List candidates);

    @Invoker
    void callCountItem(ItemStack stack, Vault vault);

    @Invoker
    void callCountItems(List<ItemStack> items, Vault vault);

    @Invoker
    void callCountItemsFromWorld(List<ItemStack> items);

    @Invoker
    void callOnTileComplete(VirtualWorld world, Vault vault, int index);

    @Invoker
    void callCheckBingoCompletion(VirtualWorld world, Vault vault, ScavengerBingoObjective.BoolList settledTiles,
                                  ScavengerBingoObjective.BoolList settledBingos);

    @Invoker
    Optional<ObjectiveShuffleModifier> callGetBingoShuffleModifier(Vault vault);

    @Invoker
    void callShuffleIncompleteTiles(ScavengerBingoObjective.BoolList settledTiles);
}
