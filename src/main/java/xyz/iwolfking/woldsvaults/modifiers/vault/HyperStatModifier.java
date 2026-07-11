package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.modifier.spi.predicate.IModifierImmunity;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The HYPER escalation modifier: one vault-modifier entry whose strength is derived from the
 * Hyper objective's persisted cycle count at each mob's spawn, so it needs no mutable state and
 * survives restarts for free. Health and damage compound (×factor^n via MULTIPLY_TOTAL); speed
 * is additive per stack. The hyperboss is skipped — its scaling is folded into the arm-time
 * BossRuneModifiers instead, so it must not double-dip here.
 */
public class HyperStatModifier extends VaultModifier<HyperStatModifier.Properties> {
    private static final UUID HEALTH_UUID = UUID.nameUUIDFromBytes("woldsvaults:hyper_health".getBytes(StandardCharsets.UTF_8));
    private static final UUID DAMAGE_UUID = UUID.nameUUIDFromBytes("woldsvaults:hyper_damage".getBytes(StandardCharsets.UTF_8));
    private static final UUID SPEED_UUID = UUID.nameUUIDFromBytes("woldsvaults:hyper_speed".getBytes(StandardCharsets.UTF_8));

    public HyperStatModifier(ResourceLocation id, Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.ENTITY_SPAWN.register(context.getUUID(), event -> {
            if (!(event.getEntity() instanceof LivingEntity entity) || entity instanceof Player) {
                return;
            }
            if (entity.level != world || entity instanceof EternalEntity || entity instanceof PetEntity) {
                return;
            }
            if (entity instanceof VaultBossEntity) {
                return;
            }
            if (IModifierImmunity.of(entity).test(this)) {
                return;
            }
            int stacks = HyperVaultObjective.getCycleCount(vault);
            if (stacks <= 0) {
                return;
            }
            double compounding = Math.pow(this.properties.getStatFactor(), stacks) - 1.0D;
            applyOnce(entity, Attributes.MAX_HEALTH, HEALTH_UUID, compounding, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyOnce(entity, Attributes.ATTACK_DAMAGE, DAMAGE_UUID, compounding, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyOnce(entity, Attributes.MOVEMENT_SPEED, SPEED_UUID, this.properties.getSpeedPerStack() * stacks, AttributeModifier.Operation.MULTIPLY_BASE);
            entity.setHealth(entity.getMaxHealth());
        });
    }

    private static void applyOnce(LivingEntity entity, Attribute attribute, UUID uuid, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        // Some entity types simply lack an attribute (e.g. no attack damage) — that's normal, skip.
        if (instance == null || instance.getModifier(uuid) != null) {
            return;
        }
        instance.addPermanentModifier(new AttributeModifier(uuid, "HYPER", amount, operation));
    }

    public static class Properties {
        @Expose
        private final float statFactor;
        @Expose
        private final float speedPerStack;

        public Properties(float statFactor, float speedPerStack) {
            this.statFactor = statFactor;
            this.speedPerStack = speedPerStack;
        }

        // The datagen'd modifier def carries no numbers: the live knobs are
        // hyper_objective.json (hyperStatFactor / speedPerStack). A pack that wants a
        // different HYPER-style modifier can still override per-def by setting these
        // properties to a positive value.
        public float getStatFactor() {
            return this.statFactor > 0.0F ? this.statFactor : HyperVaultObjective.cfg().getHyperStatFactor();
        }

        public float getSpeedPerStack() {
            return this.speedPerStack > 0.0F ? this.speedPerStack : HyperVaultObjective.cfg().getSpeedPerStack();
        }
    }
}
