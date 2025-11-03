package xyz.iwolfking.woldsvaults.api.core.vault_events.legacy;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.api.core.vault_events.LegacyVaultEvent;

import java.util.List;
import java.util.Objects;

public class BuffEntityInAreaVaultEvent extends LegacyVaultEvent {
    @Expose
    private final WeightedList<MobEffectInstance> effects;
    @Expose
    private final boolean shouldGrantAllEffects;
    @Expose
    private final int randomEffectCount;
    @Expose
    private final double effectRadius;

    public BuffEntityInAreaVaultEvent(String eventName, String eventDescription, String primaryColor, WeightedList<MobEffectInstance> effects, boolean shouldGrantAllEffects, int randomEffectCount, double effectRadius) {
        super(eventName, eventDescription, primaryColor);
        this.effects = effects;
        this.shouldGrantAllEffects = shouldGrantAllEffects;
        this.randomEffectCount = randomEffectCount;
        this.effectRadius = effectRadius;
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        List<LivingEntity> nearbyEntities = player.level.getEntitiesOfClass(LivingEntity.class, Objects.requireNonNull(player).getBoundingBox().inflate(effectRadius));

        nearbyEntities.forEach(livingEntity -> {
            if(!(livingEntity instanceof Player) && shouldGrantAllEffects) {
                effects.forEach((mobEffectInstance, aDouble) -> livingEntity.addEffect(mobEffectInstance));
            }
            else {
                for(int i = 0; i < randomEffectCount; i++) {
                    if(!(livingEntity instanceof Player)) {
                        effects.getRandom().ifPresent(livingEntity::addEffect);
                    }

                }
            }

        });
        super.triggerEvent(pos, player, vault);
    }
}
