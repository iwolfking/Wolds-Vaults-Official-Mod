package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.EntityDamageBlockEvent;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.event.GearAttributeEvents;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.Entropy;
import iskallia.vault.util.calc.BlockChanceHelper;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModSounds;
import xyz.iwolfking.woldsvaults.mixins.LivingEntityAccessor;

@Mixin (value = GearAttributeEvents.class, remap = false)
public class MixinGearAttributeEvents {

    /**
     * @author aida
     * @reason safer spaces + block w/o shield
     */
    @SubscribeEvent
    @Overwrite
    public static void blockAttack(LivingAttackEvent event) {
        LivingEntity attacked = event.getEntityLiving();
        DamageSource damageSource = event.getSource();
        if (!attacked.getLevel().isClientSide() && !damageSource.isBypassInvul()) {
            float blockChance = 0.0f;
            if (AttributeSnapshotHelper.canHaveSnapshot(attacked))
                blockChance = BlockChanceHelper.getBlockChance(attacked);

            if (attacked.hasEffect(ModEffects.SAFER_SPACE)) {
                MobEffectInstance currentSpace = attacked.getEffect(ModEffects.SAFER_SPACE);
                int currentAmp = currentSpace.getAmplifier();
                if (currentAmp >= 255)
                    CommonEvents.ENTITY_DAMAGE_BLOCK.invoke(new EntityDamageBlockEvent.Data(true, damageSource, attacked));
                else {
                    event.setCanceled(true);
                    ((LivingEntityAccessor)attacked).setLastHurt(6.9f*currentAmp);
                    attacked.invulnerableTime = attacked.invulnerableDuration;

                    CommonEvents.ENTITY_DAMAGE_BLOCK.invoke(new EntityDamageBlockEvent.Data(false, damageSource, attacked));
                    int unsafeDuration = (int) (200*(1.0f-blockChance));
                    int safeDuration = currentSpace.getDuration();

                    attacked.removeEffect(ModEffects.SAFER_SPACE);
                    if(currentAmp > 0)
                        attacked.addEffect(new MobEffectInstance(ModEffects.SAFER_SPACE, safeDuration+unsafeDuration, currentAmp-1, true, true, true));
                    attacked.addEffect(new MobEffectInstance(ModEffects.SAFER_SPACE, unsafeDuration, 255, true, false, false));

                    if(attacked instanceof Player player)
                        player.playNotifySound(ModSounds.SAFERSPACES_PROC, SoundSource.PLAYERS, 0.4f, 1.0f);
                    attacked.playSound(ModSounds.SAFERSPACES_PROC, 0.4f, 1.0f);
                }
            }
            else if (!damageSource.isFall() && AttributeSnapshotHelper.canHaveSnapshot(attacked)) {
                if (!Entropy.canExecute(attacked, Entropy.Stat.BLOCK, blockChance)) {
                    CommonEvents.ENTITY_DAMAGE_BLOCK.invoke(new EntityDamageBlockEvent.Data(true, damageSource, attacked));
                } else {
                    event.setCanceled(true);
                    CommonEvents.ENTITY_DAMAGE_BLOCK.invoke(new EntityDamageBlockEvent.Data(false, damageSource, attacked));

                    ItemStack mainHandStack = attacked.getItemInHand(InteractionHand.MAIN_HAND);
                    ItemStack offHandStack = attacked.getItemInHand(InteractionHand.OFF_HAND);
                    ItemStack shieldStack = mainHandStack.getItem() instanceof ShieldItem
                            ? mainHandStack
                            : (offHandStack.getItem() instanceof ShieldItem ? offHandStack : null);
                    if (shieldStack != null && shieldStack.getItem() instanceof VaultGearItem) {
                        VaultGearData gearData = VaultGearData.read(shieldStack);
                        gearData.getFirstValue(ModGearAttributes.GEAR_MODEL)
                                .flatMap(ModDynamicModels.Shields.REGISTRY::get)
                                .ifPresent(shieldModel -> shieldModel.onBlocked(attacked, damageSource));
                    }

                    attacked.getLevel().broadcastEntityEvent(attacked, (byte) 29);
                    if (attacked instanceof Player player) {
                        BlockChanceHelper.setPlayerBlocking(player);
                    }
                }
            }
        }
    }


    /**
     * @author aida
     * @reason add activeflag checks to cleave
     */
    @Inject(method = "triggerAoEAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;", shift = At.Shift.AFTER), cancellable = true, remap = true)
    private static void triggerAoEAttack(LivingHurtEvent event, CallbackInfo ci) {
        if(ActiveFlags.IS_SMITE_ATTACKING.isSet() && !ActiveFlags.IS_SMITE_BASE_ATTACKING.isSet())
            ci.cancel();
    }


//    /**
//     * @return
//     * @author aida
//     * @reason chain falloff reduction stat
//     */
//    @Overwrite
//    private static float lambda$triggerChainAttack$10(Level level, LivingEntity attacked, LivingEntity attacker, LivingHurtEvent event, CallbackInfo ci) {
//        return value;
//    }
}
