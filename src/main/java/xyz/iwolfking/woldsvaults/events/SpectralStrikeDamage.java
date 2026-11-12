package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModEffects;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.init.ModSounds;
import iskallia.vault.network.message.BonkParticleMessage;
import iskallia.vault.skill.ability.effect.BonkSpectralStrikeAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.damage.AttackScaleHelper;
import iskallia.vault.util.damage.CritHelper;
import iskallia.vault.util.damage.ThornsReflectDamageSource;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Random;

/**
 * Battle Cry: Spectral Strike, moved off the base mod's NORMAL handler (cancelled by
 * {@code MixinBonkSpectralStrikeAbility}) into the HIGH pre-multiplier hook.
 */
public final class SpectralStrikeDamage {
    private static final Random RANDOM = new Random();

    private SpectralStrikeDamage() {
    }

    /**
     * Spends up to {@code maxStacksUsedPerHit} Battle Cry stacks on a qualifying hit. Swings and fang hits get
     * {@code + abilityPowerPerStack x base ability power x stacks} before the damage multipliers; ability hits
     * (ability power flag, Fireshot, Arcane Rail) get {@code x (1 + abilityPowerPerStack x stacks)}. AoE, chain,
     * totem, charmed, DoT, reflect, effect, Javelin, Smite, Storm Arrow, Glacial Shatter, shocking collision,
     * necromancy and unlucky (echo replay, proc fang) hits are excluded. Swings keep the base full-charge and
     * no-crit gates.
     */
    public static void apply(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        if (!attacker.hasEffect(ModEffects.BATTLE_CRY_SPECTRAL_STRIKE)) {
            return;
        }
        if (event.getSource() instanceof ThornsReflectDamageSource || isExcludedHit()) {
            return;
        }
        boolean fangHit = WoldActiveFlags.IS_FANG_ATTACKING.isSet();
        boolean abilityHit = !fangHit && (ActiveFlags.IS_AP_ATTACKING.isSet()
                || ActiveFlags.IS_FIRESHOT_ATTACKING.isSet()
                || ActiveFlags.IS_ARCANE_RAIL_ATTACKING.isSet());
        if (!fangHit && !abilityHit) {
            if (CritHelper.getCrit(attacker) || AttackScaleHelper.getLastAttackScale(attacker) < 1.0F) {
                return;
            }
        }
        MobEffectInstance battleCry = attacker.getEffect(ModEffects.BATTLE_CRY_SPECTRAL_STRIKE);
        if (battleCry == null) {
            return;
        }
        AbilityTree abilities = PlayerAbilitiesData.get(attacker.getLevel()).getAbilities(attacker);
        for (BonkSpectralStrikeAbility ability : abilities.getAll(BonkSpectralStrikeAbility.class, Skill::isUnlocked)) {
            int stacksUsed = ability.getMaxStacksUsedPerHit();
            MobEffectInstance newBattleCry = null;
            if (battleCry.getAmplifier() - stacksUsed >= 0) {
                newBattleCry = new MobEffectInstance(battleCry.getEffect(), battleCry.getDuration(), battleCry.getAmplifier() - stacksUsed, false, false, true);
            } else {
                stacksUsed = battleCry.getAmplifier() + 1;
            }
            float perStack = ability.getAbilityPowerPerStack();
            if (abilityHit) {
                event.setAmount(event.getAmount() * (1.0F + perStack * stacksUsed));
            } else {
                float baseAbilityPower = AttributeSnapshotHelper.getInstance().getSnapshot(attacker)
                        .getAttributeValue(ModGearAttributes.ABILITY_POWER, VaultGearAttributeTypeMerger.floatSum());
                event.setAmount(event.getAmount() + perStack * baseAbilityPower * stacksUsed);
            }
            attacker.level.playSound(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), ModSounds.BONK, SoundSource.PLAYERS, 1.0F, 0.7F);
            attacker.playNotifySound(ModSounds.BONK, SoundSource.PLAYERS, 1.0F, 0.7F);
            int particles = 5 * stacksUsed;
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> attacker),
                    new BonkParticleMessage(new Vec3(attacker.getX(), attacker.getY() + attacker.getBbHeight() / 3.0F, attacker.getZ()),
                            event.getEntity().getId(), 38860, particles, 5 + (int) (RANDOM.nextFloat() * 10.0F)));
            attacker.removeEffect(ModEffects.BATTLE_CRY_SPECTRAL_STRIKE);
            if (newBattleCry != null) {
                attacker.addEffect(newBattleCry);
            }
        }
    }

    private static boolean isExcludedHit() {
        return ActiveFlags.IS_CHAINING_ATTACKING.isSet()
                || ActiveFlags.IS_AOE_ATTACKING.isSet()
                || ActiveFlags.IS_TOTEM_ATTACKING.isSet()
                || ActiveFlags.IS_CHARMED_ATTACKING.isSet()
                || ActiveFlags.IS_DOT_ATTACKING.isSet()
                || ActiveFlags.IS_REFLECT_ATTACKING.isSet()
                || ActiveFlags.IS_EFFECT_ATTACKING.isSet()
                || ActiveFlags.IS_JAVELIN_ATTACKING.isSet()
                || ActiveFlags.IS_SMITE_ATTACKING.isSet()
                || ActiveFlags.IS_SMITE_BASE_ATTACKING.isSet()
                || ActiveFlags.IS_GLACIAL_SHATTER_ATTACKING.isSet()
                || ActiveFlags.IS_SHOCKING_COLLISION.isSet()
                || ActiveFlags.IS_NECROMANCY_MINION_ATTACKING.isSet()
                || WoldActiveFlags.IS_UNLUCKY_ATTACK.isSet();
    }
}
