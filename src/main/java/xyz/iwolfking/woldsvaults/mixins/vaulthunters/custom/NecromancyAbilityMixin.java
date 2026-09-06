package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.init.ModEffects;
import iskallia.vault.skill.ability.effect.NecromancyAbility;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = NecromancyAbility.class, remap = false)
public abstract class NecromancyAbilityMixin {

    @Shadow
    @Final
    public static String TAG;

    @Shadow
    @Final
    public static String TAG_OWNER;

    @Inject(
        method = "onLivingDeath",
        at = @At("HEAD")
    )
    private static void spreadCurseOnLivingDeath(LivingDeathEvent event, CallbackInfo ci) {
        if (!(event.getEntity() instanceof LivingEntity killed)) {
            return;
        }

        CompoundTag tag = killed.getPersistentData().getCompound(TAG);

        if (killed.hasEffect(ModEffects.NECROMANCY_CURSE) && tag.hasUUID(TAG_OWNER)) {
            if (killed.level instanceof ServerLevel serverLevel) {
                ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(tag.getUUID(TAG_OWNER));

                if (owner != null) {
                    MobEffectInstance curseEffect = killed.getEffect(ModEffects.NECROMANCY_CURSE);

                    if (curseEffect != null) {
                        NecromancyAbility.MinionKind kind = NecromancyAbility.MinionKind.byId(curseEffect.getAmplifier());
                        int remainingDuration = curseEffect.getDuration();
                        double spreadRadius = 3.0;
                        float aoeMult = 1.0F + AreaOfEffectHelper.getAreaOfEffectUnlimited(owner);
                        spreadRadius *= aoeMult;

                        AABB searchArea = killed.getBoundingBox().inflate(spreadRadius);
                        List<LivingEntity> nearbyTargets = serverLevel.getEntitiesOfClass(
                            LivingEntity.class,
                            searchArea,
                            target -> target != killed 
                                   && NecromancyAbility.isValidTarget(owner, target)
                                   && !target.hasEffect(ModEffects.NECROMANCY_CURSE)
                        );

                        for (LivingEntity target : nearbyTargets) {
                            NecromancyAbility.applyCurse(target, owner, kind, remainingDuration);
                        }
                    }
                }
            }
        }
    }
}