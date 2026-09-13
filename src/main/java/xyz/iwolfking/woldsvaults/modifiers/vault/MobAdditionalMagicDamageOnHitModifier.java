package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.modifier.spi.predicate.IModifierImmunity;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MobAdditionalMagicDamageOnHitModifier extends VaultModifier<MobAdditionalMagicDamageOnHitModifier.Properties> {
    public MobAdditionalMagicDamageOnHitModifier(ResourceLocation id, MobAdditionalMagicDamageOnHitModifier.Properties properties, Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter((t, p, s) -> t.formatted((int)Math.abs(p.onHitApplyChance * (float)s * 100.0F)));
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.ENTITY_DAMAGE.register(context.getUUID(), (event) -> {
            if (event.getSource() == DamageSource.MAGIC) {
                return;
            }

            if (event.getEntity() instanceof ServerPlayer player) {
                Entity sourceEntity = event.getSource().getEntity();
                if (sourceEntity instanceof LivingEntity livingEntity) {
                    if (!IModifierImmunity.of(livingEntity).test(this)) {
                        if (vault.get(Vault.LISTENERS).contains(player.getUUID())) {
                            if (!context.hasTarget() || context.getTarget().equals(player.getUUID())) {
                                if (!(world.random.nextFloat() >= this.properties.onHitApplyChance)) {
                                    if(this.properties.isPercentageOfAttack()) {
                                        player.hurt(DamageSource.MAGIC, event.getAmount() * this.properties.getAmount());
                                    }
                                    else {
                                        player.hurt(DamageSource.MAGIC, this.properties.getAmount());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public static class Properties {
        @Expose
        private final boolean isPercentageOfAttack;

        @Expose
        private final float amount;

        @Expose
        private final float onHitApplyChance;

        public Properties(float amount, boolean isPercentageOfAttack, float onHitApplyChance) {
            this.amount = amount;
            this.isPercentageOfAttack = isPercentageOfAttack;
            this.onHitApplyChance = onHitApplyChance;
        }

        public float getAmount() {
            return this.amount;
        }

        public boolean isPercentageOfAttack() {
            return this.isPercentageOfAttack;
        }

        public float getOnHitApplyChance() {
            return this.onHitApplyChance;
        }
    }
}
