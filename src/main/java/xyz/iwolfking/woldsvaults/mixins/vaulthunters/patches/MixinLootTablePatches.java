package xyz.iwolfking.woldsvaults.mixins.vaulthunters.patches;

import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.world.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatch;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatches;

import java.io.Reader;
import java.util.Optional;

@Mixin(value = LootTable.class, remap = false)
public abstract class MixinLootTablePatches {

    /**
     * Applies Wold's Vaults' config patch, when one exists, to a generation loot table's disk JSON before it is read.
     */
    @WrapOperation(method = "fromPath", at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"))
    private static Object woldsvaults$patchLootTable(Gson gson, Reader reader, Class<?> type, Operation<Object> original, @Local(argsOnly = true) String filePath) {
        String path = ConfigPatches.relativePath(filePath);
        Optional<ConfigPatch> patch = ConfigPatches.find(path);
        if (patch.isEmpty()) {
            return original.call(gson, reader, type);
        }
        return ConfigPatches.deserializePatched(path, patch.get(), reader, patched -> original.call(gson, patched, type));
    }
}
