package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.modifier.spi.predicate.IModifierImmunity;
import iskallia.vault.core.vault.objective.KillBossObjective;
import iskallia.vault.core.vault.objective.ObeliskObjective;
import iskallia.vault.core.vault.objective.ObeliskObjective.Wave;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.entity.Ignores;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class OverpowerModifier extends VaultModifier<OverpowerModifier.Properties> {

    public OverpowerModifier(ResourceLocation id, Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.ENTITY_TICK.register(context.getUUID(), (event) -> {
            LivingEntity entity = event.getEntityLiving();
            if (entity.level == world && !(entity instanceof Player)) {
                if (!IModifierImmunity.of(entity).test(this)) {
                    boolean isBoss = vault.map(Vault.OBJECTIVES, (objectives) -> objectives.forEach(KillBossObjective.class, (objective) -> {
                            UUID bossId = objective.get(KillBossObjective.BOSS_ID);
                            return event.getEntity().getUUID().equals(bossId);
                        }) || objectives.forEach(ObeliskObjective.class, (objective) -> {
                            Wave[] waves = objective.get(ObeliskObjective.WAVES);

                            for(Wave wave : waves) {
                                if (wave.get(Wave.MOBS).contains(entity.getUUID())) {
                                    return true;
                                }
                            }

                            return false;
                        }), false);
                    if (!isBoss && entity.getHealth() > (this.properties).maxHealth && !(entity instanceof Ignores.IOverpowerIgnore)) {
                        entity.setHealth((this.properties).maxHealth);
                    }

                }
            }
        });
    }

    public static class Properties {
        @Expose
        private final float maxHealth;

        public Properties(float maxHealth) {
            this.maxHealth = maxHealth;
        }

        public float getMaxHealth() {
            return this.maxHealth;
        }
    }
}
