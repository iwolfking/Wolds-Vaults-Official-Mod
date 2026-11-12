package xyz.iwolfking.woldsvaults.mixins.vaulthunters.patches;

import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.VaultMod;
import iskallia.vault.config.Config;
import iskallia.vault.init.ModConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatch;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatches;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ConfigInvoker;

import java.io.File;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Optional;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Mixin(value = Config.class, remap = false)
public abstract class MixinConfigPatches {

    /**
     * Applies Wold's Vaults' config patch, when one exists, to the disk JSON before the base mod deserializes it. A
     * patched tree that fails to deserialize falls back to the unpatched file, so the base mod never replaces the
     * disk file with its Java defaults because of a patch.
     */
    @WrapOperation(method = "readConfig", at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/reflect/Type;)Ljava/lang/Object;"))
    private Object woldsvaults$patchConfig(Gson gson, Reader reader, Type type, Operation<Object> original) {
        String path = ConfigPatches.relativePath(((ConfigInvoker) this).woldsvaults$getConfigFile());
        Optional<ConfigPatch> patch = ConfigPatches.find(path);
        if (patch.isEmpty()) {
            return original.call(gson, reader, type);
        }
        return ConfigPatches.deserializePatched(path, patch.get(), reader, patched -> original.call(gson, patched, type));
    }

    /**
     * Loads a config from the patch that creates it. The patch wins over any file already on disk (a stale copy or
     * one the base mod wrote with its defaults); a create that fails falls through to the base mod's own read.
     */
    @Inject(method = "readConfig", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$createConfig(CallbackInfoReturnable<Config> cir) {
        Config self = (Config) (Object) this;
        File file = ((ConfigInvoker) self).woldsvaults$getConfigFile();
        String path = ConfigPatches.relativePath(file);
        Optional<ConfigPatch> patch = ConfigPatches.find(path);
        if (patch.isEmpty() || patch.get().create() == null) {
            return;
        }
        try {
            if (file.exists()) {
                WoldLogger.warn("Config {} is created by a Wold's Vaults config patch; ignoring the file on disk", path);
            }
            VaultMod.LOGGER.info("Reading config: " + self.getName() + " (created by a Wold's Vaults config patch)");
            Config config = self.getGson().fromJson(ConfigPatches.created(path, patch.get()), self.getClass());
            ConfigInvoker invoker = (ConfigInvoker) config;
            invoker.woldsvaults$onLoad(self);
            if (!invoker.woldsvaults$isValid()) {
                VaultMod.LOGGER.error("Invalid config {}, using defaults", self);
                ModConfigs.INVALID_CONFIGS.add(file.getName() + " - There was an invalid setting in this config.");
                invoker.woldsvaults$reset();
            }
            ModConfigs.CONFIGS.add(config);
            cir.setReturnValue(config);
        } catch (Exception e) {
            WoldLogger.error("Config patch for {} could not create the config; falling back to the base mod's read", path, e);
        }
    }
}
