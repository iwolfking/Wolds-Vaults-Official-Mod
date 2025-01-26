package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobFrenzyModifier;
import iskallia.vault.core.vault.modifier.spi.EntityAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.modifier.spi.predicate.IModifierImmunity;
import iskallia.vault.core.vault.objective.KillBossObjective;
import iskallia.vault.core.vault.objective.ObeliskObjective;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(value = MobFrenzyModifier.class, remap = false)
public class MixinFrenzyModifier extends VaultModifier<MobFrenzyModifier.Properties> {
    @Shadow @Final private EntityAttributeModifier<?> attackDamageAttributeModifier;
    @Shadow @Final private EntityAttributeModifier<?> movementSpeedAttributeModifier;

    public MixinFrenzyModifier(ResourceLocation id, MobFrenzyModifier.Properties properties, Display display) {
        super(id, properties, display);
    }

    /**
     * @author iwolfking
     * @reason Change frenzy to be health multiplier instead of setting value
     */
    @Overwrite
    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.ENTITY_SPAWN.register(context.getUUID(), event -> {
            if (event.getEntity() instanceof LivingEntity entity) {
                if (!IModifierImmunity.of(entity).test((MobFrenzyModifier)(Object)this)) {
                    if (entity.level == world && !(entity instanceof Player)) {
                        long upperBits = context.getUUID().getMostSignificantBits();
                        long lowerBits = context.getUUID().getLeastSignificantBits();
                        this.attackDamageAttributeModifier.applyToEntity(entity, new UUID(upperBits++, lowerBits), context);
                        this.movementSpeedAttributeModifier.applyToEntity(entity, new UUID(upperBits, lowerBits), context);

                        boolean isBoss = vault.map(Vault.OBJECTIVES, objectives -> objectives.forEach(KillBossObjective.class, objective -> {
                            UUID bossId = objective.get(KillBossObjective.BOSS_ID);
                            return event.getEntity().getUUID().equals(bossId);
                        }) || objectives.forEach(ObeliskObjective.class, objective -> {
                            ObeliskObjective.Wave[] waves = objective.get(ObeliskObjective.WAVES);

                            for (ObeliskObjective.Wave wave : waves) {
                                if ((wave.get(ObeliskObjective.Wave.MOBS)).contains(entity.getUUID())) {
                                    return true;
                                }
                            }

                            return false;
                        }), false);

                        if (!isBoss && entity.getHealth() > this.properties.getMaxHealth()) {
                            entity.setHealth(entity.getMaxHealth() * this.properties.getMaxHealth());
                        }
                    }
                }
            }
        });
    }
}
