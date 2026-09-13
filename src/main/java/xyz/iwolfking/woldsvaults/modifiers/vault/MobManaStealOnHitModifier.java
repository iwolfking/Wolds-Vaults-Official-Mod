package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.modifier.spi.predicate.IModifierImmunity;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.mana.Mana;
import iskallia.vault.mana.ManaAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MobManaStealOnHitModifier extends VaultModifier<MobManaStealOnHitModifier.Properties> {
    public MobManaStealOnHitModifier(ResourceLocation id, MobManaStealOnHitModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter((t, p, s) -> t.formatted((int)Math.abs(p.onHitApplyChance * (float)s * 100.0F)));
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.ENTITY_DAMAGE.register(context.getUUID(), (event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                Entity sourceEntity = event.getSource().getEntity();
                if (sourceEntity instanceof LivingEntity livingEntity) {
                    if (!IModifierImmunity.of(livingEntity).test(this)) {
                        if (vault.get(Vault.LISTENERS).contains(player.getUUID())) {
                            if (!context.hasTarget() || context.getTarget().equals(player.getUUID())) {
                                if (!(world.random.nextFloat() >= this.properties.onHitApplyChance)) {
                                    if(this.properties.shouldDrainPercentage()) {
                                        Mana.decrease(player, ManaAction.NEGATIVE_GAME_MECHANIC, Mana.getMax(player) * this.properties.getAmount());
                                    }
                                    else {
                                        Mana.decrease(player, ManaAction.NEGATIVE_GAME_MECHANIC, this.properties.getAmount());
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
        private final float amount;
        @Expose
        private final boolean drainsPercentage;
        @Expose
        private final float onHitApplyChance;

        public Properties(float amount, boolean drainsPercentage, float onHitApplyChance) {
            this.amount = amount;
            this.drainsPercentage = drainsPercentage;
            this.onHitApplyChance = onHitApplyChance;
        }

        public float getAmount() {
            return this.amount;
        }

        public boolean shouldDrainPercentage() {
            return this.drainsPercentage;
        }

        public float getOnHitApplyChance() {
            return this.onHitApplyChance;
        }
    }
}
