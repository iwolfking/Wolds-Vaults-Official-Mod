package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.MobAttributeModifierSettable;

/**
 * Hands a greed assassin or Vault Champion the vault's mob attribute modifiers, which an entity spawned by hand never
 * receives. {@code applyToEntity} is keyed by the modifier's UUID, so a second call cannot apply one twice.
 */
public final class GreedModifierInheritance {
    private GreedModifierInheritance() {
    }

    /** Applies every mob attribute modifier on the vault to {@code entity} and returns how many there were. */
    public static int apply(Vault vault, LivingEntity entity) {
        if (vault == null || !vault.has(Vault.MODIFIERS)) {
            WoldLogger.error("{} cannot inherit vault modifiers: the vault carries no modifier stack.",
                    entity.getName().getString());
            return 0;
        }
        Modifiers modifiers = vault.get(Vault.MODIFIERS);
        int applied = 0;
        for (Modifiers.Entry entry : modifiers.getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            ModifierContext context = modifiers.getContext(entry);
            if (modifier instanceof MobAttributeModifier mob) {
                mob.applyToEntity(entity, context.getUUID(), context);
                applied++;
            } else if (modifier instanceof MobAttributeModifierSettable settable) {
                settable.applyToEntity(entity, context.getUUID(), context);
                applied++;
            }
        }
        return applied;
    }
}
