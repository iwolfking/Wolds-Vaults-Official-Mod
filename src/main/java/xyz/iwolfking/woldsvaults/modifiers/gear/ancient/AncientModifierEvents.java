package xyz.iwolfking.woldsvaults.modifiers.gear.ancient;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.EntityStunnedEvent;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.event.PlayerActiveFlags;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.skill.ability.effect.EarthquakeAbility;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.talent.type.JavelinConductTalent;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.EntityHelper;
import iskallia.vault.util.damage.AttackScaleHelper;
import iskallia.vault.util.damage.CritHelper;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaState;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.List;

public class AncientModifierEvents {
    private static final float CLEAVE_DAMAGE_PERCENT = 0.6F;
    private static final String EARTHQUAKE_ABILITY_ID = "Earthquake_Base";

    /**
     * Registers the ancient-unique handlers. The lucky cleave is bound at setup, not from an annotated
     * subscriber, so it runs after the_vault's LOWEST-priority {@code doLuckyHit} has scaled the hit.
     */
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, AncientModifierEvents::luckyHitCleave);
        CommonEvents.ENTITY_STUNNED.register(AncientModifierEvents.class, AncientModifierEvents::quakingHit);
    }

    private static void luckyHitCleave(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (attacker.level.isClientSide || !AttributeSnapshotHelper.canHaveSnapshot(attacker)) {
            return;
        }
        if (isCleaveSuppressed()) {
            return;
        }

        boolean hasConduct = false;
        if (attacker instanceof Player player) {
            if (ActiveFlags.IS_JAVELIN_ATTACKING.isSet()) {
                if (attacker instanceof ServerPlayer serverPlayer) {
                    TalentTree talents = PlayerTalentsData.get(serverPlayer.getLevel()).getTalents(serverPlayer);
                    hasConduct = !talents.getAll(JavelinConductTalent.class, Skill::isUnlocked).isEmpty();
                }
                if (!hasConduct) {
                    return;
                }
            }
            if (!hasConduct) {
                if (CritHelper.getCrit(player)) {
                    return;
                }
                if (PlayerActiveFlags.isSet(player, PlayerActiveFlags.Flag.ATTACK_AOE)) {
                    return;
                }
                if (AttackScaleHelper.getLastAttackScale(player) < 1.0F) {
                    return;
                }
            }
        }

        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(attacker);
        int aoeSize = snapshot.getAttributeValue(ModGearAttributes.LUCKY_HIT_AOE, VaultGearAttributeTypeMerger.intSum());
        if (aoeSize <= 0) {
            return;
        }

        LivingEntity attacked = event.getEntityLiving();
        Level world = attacker.level;
        float cleaveDamage = event.getAmount() * CLEAVE_DAMAGE_PERCENT;

        ActiveFlags.IS_AOE_ATTACKING.runIfNotSet(() -> {
            List<Mob> nearby = EntityHelper.getNearby(world, attacked.blockPosition(), aoeSize, Mob.class);
            nearby.remove(attacked);
            nearby.remove(attacker);
            nearby.removeIf(mob -> (attacker instanceof EternalEntity || attacker instanceof Player) && mob instanceof EternalEntity || mob instanceof PetEntity);
            nearby.removeIf(mob -> mob.isInvulnerableTo(event.getSource()));
            ServerPlayer cleaver = attacker instanceof ServerPlayer serverPlayer ? serverPlayer : null;
            ServerPlayer previous = cleaver == null ? null : IdonaState.pushCleave(cleaver);
            try {
                nearby.forEach(mob -> {
                    Vec3 movement = mob.getDeltaMovement();
                    mob.hurt(event.getSource(), cleaveDamage);
                    mob.setDeltaMovement(movement);
                });
            } finally {
                if (cleaver != null) {
                    IdonaState.popCleave(previous);
                }
            }
            if (attacker instanceof Player player) {
                PlayerActiveFlags.set(player, PlayerActiveFlags.Flag.ATTACK_AOE, 2);
            }
        });
    }

    private static boolean isCleaveSuppressed() {
        return ActiveFlags.IS_AOE_ATTACKING.isSet()
                || ActiveFlags.IS_SMITE_ATTACKING.isSet()
                || ActiveFlags.IS_THORNS_REFLECTING.isSet()
                || ActiveFlags.IS_EFFECT_ATTACKING.isSet()
                || ActiveFlags.IS_REFLECT_ATTACKING.isSet()
                || ActiveFlags.IS_DOT_ATTACKING.isSet()
                || ActiveFlags.IS_TOTEM_ATTACKING.isSet()
                || WoldActiveFlags.IS_AOE2_ATTACK.isSet();
    }

    private static void quakingHit(EntityStunnedEvent.Data data) {
        if (!(data.getSource() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity target = data.getTarget();
        if (target == null || player.level.isClientSide || !AttributeSnapshotHelper.canHaveSnapshot(player)) {
            return;
        }

        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        int quakeLevel = snapshot.getAttributeValue(ModGearAttributes.QUAKING_HIT, VaultGearAttributeTypeMerger.intSum());
        if (quakeLevel <= 0) {
            return;
        }

        EarthquakeAbility quake = resolveEarthquake(quakeLevel);
        if (quake == null) {
            WoldsVaults.LOGGER.error("quaking_hit could not resolve ability {} tier {}; no earthquake was cast", EARTHQUAKE_ABILITY_ID, quakeLevel);
            return;
        }
        quake.triggerExplosion(player, target.position(), true);
    }

    /**
     * A shared {@code EarthquakeAbility} for the requested tier, from {@code ModConfigs.ABILITIES} rather
     * than the player's tree. Only {@code triggerExplosion} is safe; {@code onAction} would put the
     * shared instance on cooldown.
     */
    private static EarthquakeAbility resolveEarthquake(int level) {
        Skill skill = ModConfigs.ABILITIES.getAbilityById(EARTHQUAKE_ABILITY_ID).orElse(null);
        if (!(skill instanceof TieredSkill tiered)) {
            return null;
        }
        List<LearnableSkill> tiers = tiered.getTiers();
        if (tiers.isEmpty()) {
            return null;
        }
        int index = Math.max(1, Math.min(level, tiers.size()));
        LearnableSkill child = tiered.getChild(index);
        return child instanceof EarthquakeAbility earthquake ? earthquake : null;
    }
}
