package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.modifier.spi.predicate.IModifierImmunity;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.entity.entity.EternalEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.lib.EntityAttributeModifierSettable;

public class MobAttributeModifierSettable extends EntityAttributeModifierSettable<EntityAttributeModifierSettable.Properties> {
    public MobAttributeModifierSettable(ResourceLocation id, EntityAttributeModifierSettable.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter(properties.getType().getDescriptionFormatter());
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.ENTITY_SPAWN.register(context.getUUID(), (event) -> {
            Entity patt1150$temp = event.getEntity();
            if (patt1150$temp instanceof LivingEntity entity) {
                patt1150$temp = event.getEntity();
                if (!(patt1150$temp instanceof EternalEntity)) {
                    if (entity.level != world) {
                        return;
                    }

                    if (IModifierImmunity.of(entity).test(this)) {
                        return;
                    }

                    if (context.hasTarget() && !context.getTarget().equals(entity.getUUID())) {
                        return;
                    }

                    this.applyToEntity(entity, context.getUUID(), context);
                    //entity.setHealth(entity.getMaxHealth());
                    return;
                }

                EternalEntity var5 = (EternalEntity)patt1150$temp;
            }

        });
    }
}
