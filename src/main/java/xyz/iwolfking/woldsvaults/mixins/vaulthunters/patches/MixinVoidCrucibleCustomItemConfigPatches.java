package xyz.iwolfking.woldsvaults.mixins.vaulthunters.patches;

import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.crucible.VoidCrucibleCustomItemConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatch;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatches;

import java.io.Reader;
import java.util.Optional;

@Mixin(value = VoidCrucibleCustomItemConfig.class, remap = false)
public abstract class MixinVoidCrucibleCustomItemConfigPatches {

    /**
     * Applies Wold's Vaults' config patch, when one exists, to each void crucible item file the folder scan reads.
     */
    @WrapOperation(method = "readConfig", at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"))
    private Object woldsvaults$patchCrucibleItem(Gson gson, Reader reader, Class<?> type, Operation<Object> original, @Local String id) {
        String path = "void_crucible/" + id + ".json";
        Optional<ConfigPatch> patch = ConfigPatches.find(path);
        if (patch.isEmpty()) {
            return original.call(gson, reader, type);
        }
        return ConfigPatches.deserializePatched(path, patch.get(), reader, patched -> original.call(gson, patched, type));
    }
}
