package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.item.VaultModifierItem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = VaultModifierItem.class, remap = false)
public class MixinVaultModifierItemFix {

    /**
     * @author iwolfking
     * @reason Fix race condition with ModernFix Dynamic Resources
     */
    @Overwrite
    public void loadModels(Consumer<ModelResourceLocation> consumer) {
        List<VaultModifier<?>> modifiers;
        synchronized (VaultModifierRegistry.class) {
            modifiers = new ArrayList<>();
            VaultModifierRegistry.getAll().forEach(modifiers::add);
        }

        for (VaultModifier<?> modifier : modifiers) {
            modifier.getIcon().ifPresent((id) -> {
                String[] parts = id.getPath().split("/");
                String last = parts[parts.length - 1];
                consumer.accept(new ModelResourceLocation("the_vault:modifiers/" + last + "#inventory"));
            });
        }
    }
}