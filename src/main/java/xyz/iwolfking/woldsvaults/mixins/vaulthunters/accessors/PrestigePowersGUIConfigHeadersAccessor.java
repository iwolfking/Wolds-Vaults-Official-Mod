package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.PrestigePowersGUIConfig;
import iskallia.vault.config.entry.PrestigeHeaderStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = PrestigePowersGUIConfig.class, remap = false)
public interface PrestigePowersGUIConfigHeadersAccessor {
    @Accessor("headers")
    void setHeaders(List<PrestigeHeaderStyle> headers);
}
