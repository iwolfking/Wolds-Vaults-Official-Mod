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
import xyz.iwolfking.woldsvaults.WoldsVaults;
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
    /**
     * Float-overflow safety ceiling for the compounding multiplier: a mob's max health lives
     * in a float (max ~3.4e38) and an Infinity max health is the prerequisite for the
     * percent-damage NaN-health chain, so the multiplier stops ~10 orders of magnitude short
     * of overflow for any realistic base health. At factor 1.65 the clamp cannot engage
     * before cycle ~128 — pure safety, not balance.
     */
    private static final double MAX_COMPOUNDING = 1.0e28D;

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
            if (compounding > MAX_COMPOUNDING) {
                WoldsVaults.LOGGER.warn(
                        "HYPER overflow clamp: cycle {} stat multiplier x{} exceeds the float-safety ceiling x{} — clamped for {} (unclamped it overflows to Infinity and decays into the NaN-health bug).",
                        stacks, String.format("%.4g", compounding + 1.0D), String.format("%.1g", MAX_COMPOUNDING),
                        entity.getType().getRegistryName());
                compounding = MAX_COMPOUNDING;
            }
            applyOnce(entity, Attributes.MAX_HEALTH, HEALTH_UUID, compounding, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyOnce(entity, Attributes.ATTACK_DAMAGE, DAMAGE_UUID, compounding, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyOnce(entity, Attributes.MOVEMENT_SPEED, SPEED_UUID, this.properties.getSpeedPerStack() * stacks, AttributeModifier.Operation.MULTIPLY_BASE);
            entity.setHealth(entity.getMaxHealth());
        });
    }

    private static void applyOnce(LivingEntity entity, Attribute attribute, UUID uuid, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null || instance.getModifier(uuid) != null) {
            return;
        }
        instance.addPermanentModifier(new AttributeModifier(uuid, "HYPER", amount, operation));
    }

    /**
     * The datagen'd hyper def ships both properties as 0 — deliberate sentinels the getters
     * resolve from hyper_objective.json (hyperStatFactor / speedPerStack); a pack wanting a
     * different HYPER-style modifier can still override per-def with any positive value.
     */
    public static class Properties {
        @Expose
        private final float statFactor;
        @Expose
        private final float speedPerStack;

        public Properties(float statFactor, float speedPerStack) {
            this.statFactor = statFactor;
            this.speedPerStack = speedPerStack;
        }

        public float getStatFactor() {
            return this.statFactor > 0.0F ? this.statFactor : HyperVaultObjective.cfg().getHyperStatFactor();
        }

        public float getSpeedPerStack() {
            return this.speedPerStack > 0.0F ? this.speedPerStack : HyperVaultObjective.cfg().getSpeedPerStack();
        }
    }
}
