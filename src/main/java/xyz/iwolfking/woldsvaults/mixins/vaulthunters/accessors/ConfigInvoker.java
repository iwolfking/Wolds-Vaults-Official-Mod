package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(value = Config.class, remap = false)
public interface ConfigInvoker {
    @Invoker("getConfigFile")
    File woldsvaults$getConfigFile();

    @Invoker("onLoad")
    void woldsvaults$onLoad(Config oldConfigInstance);

    @Invoker("isValid")
    boolean woldsvaults$isValid();

    @Invoker("reset")
    void woldsvaults$reset();
}
