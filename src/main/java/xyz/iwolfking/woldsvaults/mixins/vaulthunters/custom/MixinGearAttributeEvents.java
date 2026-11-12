package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.EntityChainAttackedEvent;
import iskallia.vault.core.event.common.EntityDamageBlockEvent;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.event.GearAttributeEvents;
import iskallia.vault.event.PlayerActiveFlags;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.talent.type.PuristTalent;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.Entropy;
import iskallia.vault.util.calc.BlockChanceHelper;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModSounds;
import xyz.iwolfking.woldsvaults.mixins.LivingEntityAccessor;
import xyz.iwolfking.woldsvaults.spellcraft.chain.ChainLinks;

import java.util.ArrayList;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.util.TrueDamageSource;

@Mixin (value = GearAttributeEvents.class, remap = false)
public class MixinGearAttributeEvents {

    @Redirect(method = "lambda$triggerEffectCloudsActive$17", at = @At(value = "INVOKE", target = "Liskallia/vault/util/Entropy;canExecute(Lnet/minecraft/world/entity/Entity;Liskallia/vault/util/Entropy$Stat;F)Z"))
    private static boolean addEffectCloudChanceFromAttributeActive(Entity entity, Entropy.Stat stat, float chance) {
        if(entity instanceof LivingEntity living && living instanceof Player) {
            float increasedEffectCloudChance = AttributeSnapshotHelper.getInstance().getSnapshot(living).getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, VaultGearAttributeTypeMerger.floatSum());
            return Entropy.canExecute(entity, stat, chance + increasedEffectCloudChance);
        }

        return Entropy.canExecute(entity, stat, chance);
    }

    @Redirect(method = "lambda$triggerEffectCloudsPassive$19", at = @At(value = "INVOKE", target = "Liskallia/vault/util/Entropy;canExecute(Lnet/minecraft/world/entity/Entity;Liskallia/vault/util/Entropy$Stat;F)Z"))
    private static boolean addEffectCloudChanceFromAttributePassive(Entity entity, Entropy.Stat stat, float chance) {
        if(entity instanceof LivingEntity living && living instanceof Player) {
            float increasedEffectCloudChance = AttributeSnapshotHelper.getInstance().getSnapshot(living).getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, VaultGearAttributeTypeMerger.floatSum());
            return Entropy.canExecute(entity, stat, chance + increasedEffectCloudChance);
        }

        return Entropy.canExecute(entity, stat, chance);
    }

    /**
     * @author aida
     * @reason make the purist talent multiplicative, and keep proc fangs from re-applying stored increases
     */
    @Inject(method = "increaseDamageDealt",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/world/data/PlayerTalentsData;getTalents(Lnet/minecraft/world/entity/player/Player;)Liskallia/vault/skill/tree/TalentTree;",
                    shift = At.Shift.BEFORE),
            cancellable = true)
    private static void increaseDamageDealt(LivingHurtEvent event, CallbackInfo ci, @Local PlayerTalentsData talents, @Local float increasedDamage, @Local Player player) {
        if(WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet()) {
            ci.cancel();
            return;
        }

        float puristMult = 1.0F;
        for(PuristTalent talent : talents.getTalents(player).getAll(PuristTalent.class, Skill::isUnlocked)) {
            puristMult += talent.getDamageIncrease() * (float)talent.getCount(player);
        }
        event.setAmount(event.getAmount() * (1.0F + increasedDamage) * puristMult);
        ci.cancel();
    }

    /**
     * @author PoorMansPhysicist
     * @reason keep proc fangs from re-applying fatal strike gear crits already in their stored damage
     */
    @Inject(method = "doFatalStrikeAttack", at = @At("HEAD"), cancellable = true)
    private static void doFatalStrikeAttack(LivingHurtEvent event, CallbackInfo ci) {
        if(WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet())
            ci.cancel();
    }

    /**
     * @author PoorMansPhysicist
     * @reason Lightning Fiend set hits carry no on-hit procs; the shocking hit roll skips the thorns flag they ride on
     */
    @Inject(method = "triggerKnockbackAttack", at = @At("HEAD"), cancellable = true)
    private static void skipLightningFiendShockingHits(LivingHurtEvent event, CallbackInfo ci) {
        if(WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING.isSet())
            ci.cancel();
    }


    /**
     * @author aida
     * @reason safer spaces + block w/o shield
     */
    @SubscribeEvent
    @Overwrite
    public static void blockAttack(LivingAttackEvent event) {
        LivingEntity attacked = event.getEntityLiving();
        DamageSource damageSource = event.getSource();
        if (!attacked.getLevel().isClientSide() && !damageSource.isBypassInvul() && !(damageSource instanceof TrueDamageSource)) {
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
                    ((LivingEntityAccessor)attacked).setLastHurt(Math.max(6.9f*currentAmp, event.getAmount()*(1+currentAmp/10.0f)));
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
     * @reason add activeflag checks to cleave, and keep proc fangs from re-triggering it
     */
    @Inject(method = "triggerAoEAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;", shift = At.Shift.AFTER), cancellable = true, remap = true)
    private static void triggerAoEAttack(LivingHurtEvent event, CallbackInfo ci) {
        if(ActiveFlags.IS_AOE_ATTACKING.isSet()
        || ActiveFlags.IS_SMITE_ATTACKING.isSet()
        || ActiveFlags.IS_THORNS_REFLECTING.isSet()
        || ActiveFlags.IS_EFFECT_ATTACKING.isSet()
        || ActiveFlags.IS_REFLECT_ATTACKING.isSet()
        || ActiveFlags.IS_DOT_ATTACKING.isSet()
        || ActiveFlags.IS_TOTEM_ATTACKING.isSet()
        || WoldActiveFlags.IS_AOE2_ATTACK.isSet()
        || WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet()
        || hasLuckyHitCleave(event)
        ) {
            ci.cancel();
        }
    }

    /** Whether the attacker carries {@code lucky_hit_aoe}, in which case the base cleave yields to it. */
    private static boolean hasLuckyHitCleave(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return false;
        }
        if (!AttributeSnapshotHelper.canHaveSnapshot(attacker)) {
            return false;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(attacker);
        return snapshot.getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.LUCKY_HIT_AOE, VaultGearAttributeTypeMerger.intSum()) > 0;
    }

    /**
     * @author aida
     * @reason block echoing and proc fangs from re-activating chaining
     */
    @Inject(method = "triggerChainAttack", at = @At(value = "INVOKE", target = "Liskallia/vault/event/ActiveFlags;isSet()Z", shift = At.Shift.BEFORE, ordinal = 0), cancellable = true)
    private static void triggerChainAttack(LivingHurtEvent event, CallbackInfo ci) {
        if(WoldActiveFlags.IS_ECHOING_ATTACKING.isSet() || ActiveFlags.IS_TOTEM_ATTACKING.isSet() || WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet())
            ci.cancel();
    }

    /**
     * @author aida
     * @reason chain falloff reduction stat; targets, range and step come from {@link ChainLinks}, shared with the
     * Chaining glyph, so Chainlash's chaining damage and the CHAINING_PENALTY etching both raise the step
     */
    @Inject(method = "lambda$triggerChainAttack$13", at = @At("HEAD"), cancellable = true)
    private static void lambda$triggerChainAttack$10(Level world, LivingEntity attacked, float chainRange, LivingEntity attacker, LivingHurtEvent event, int chainCount, float finalChainStep, CallbackInfo ci) {
        List<Mob> nearby = ChainLinks.targets(world, attacked, attacker, event.getSource(), chainRange, chainCount);
        List<Vec3> nearbyPos = new ArrayList<>();
        if (!nearby.isEmpty()) {
            float baseMult = ChainLinks.step(attacker);
            float multiplier = baseMult;
            nearbyPos.add(ChainLinks.linkPoint(attacked));

            for (Mob mob : nearby) {
                Vec3 movement = mob.getDeltaMovement();
                nearbyPos.add(ChainLinks.linkPoint(mob));
                mob.hurt(event.getSource(), event.getAmount() * multiplier);
                mob.setDeltaMovement(movement);
                multiplier *= baseMult;
            }

            CommonEvents.ENTITY_CHAIN_ATTACKED.invoke(new EntityChainAttackedEvent.Data(attacker, nearby));
        }
        ChainLinks.particles(attacker, nearbyPos);
        if (attacker instanceof Player player)
            PlayerActiveFlags.set(player, PlayerActiveFlags.Flag.CHAINING_AOE, 2);

        ci.cancel();
    }
}
