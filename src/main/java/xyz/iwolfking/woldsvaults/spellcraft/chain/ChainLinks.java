package xyz.iwolfking.woldsvaults.spellcraft.chain;

import iskallia.vault.entity.IPlayerAttackIgnore;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.etching.EtchingGearAttributes;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ChainingParticleMessage;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.EntityHelper;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * VH's on-hit chain, shared by its melee chain and the Chaining glyph: the range (5 blocks scaled by the attacker's
 * area of effect), the targets and the damage step per link. The step is VH's 0.5, raised by the CHAINING_PENALTY
 * etching to {@code 0.5 + 0.5 x reduction} and multiplied by {@code 1 + value} of the {@code chaining_damage} affix,
 * never above 1, so a link never deals more than the one before it.
 */
public final class ChainLinks {
    private static final float BASE_RANGE = 5.0F;
    private static final float BASE_STEP = 0.5F;

    private ChainLinks() {
    }

    public static float range(LivingEntity attacker) {
        return AreaOfEffectHelper.adjustAreaOfEffect(attacker, null, BASE_RANGE);
    }

    /** The damage factor from one link to the next. */
    public static float step(LivingEntity attacker) {
        float step = BASE_STEP;
        if (attacker instanceof Player player) {
            float reduction = EtchingHelper.getEtchings(player, EtchingGearAttributes.CHAINING_PENALTY).stream().findFirst()
                    .map(stack -> stack.getValue()).orElse(0.0F);
            step = BASE_STEP + BASE_STEP * reduction;
        }
        if (AttributeSnapshotHelper.canHaveSnapshot(attacker)) {
            step *= 1.0F + AttributeSnapshotHelper.getInstance().getSnapshot(attacker)
                    .getAttributeValue(ModGearAttributes.CHAINING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
        }
        return Math.min(1.0F, step);
    }

    /** The mobs a chain from {@code attacked} reaches, nearest first, at most {@code count}. */
    public static List<Mob> targets(Level world, LivingEntity attacked, LivingEntity attacker, DamageSource source, float range, int count) {
        List<Mob> nearby = EntityHelper.getNearby(world, attacked.blockPosition(), range, Mob.class);
        nearby.remove(attacked);
        nearby.remove(attacker);
        nearby.removeIf(mob -> (attacker instanceof EternalEntity || attacker instanceof Player) && mob instanceof EternalEntity
                || mob instanceof PetEntity || IPlayerAttackIgnore.isIgnoredByPlayerAttack(source, mob));
        nearby.removeIf(mob -> mob.isInvulnerableTo(source));
        nearby.sort(Comparator.comparing(mob -> mob.distanceTo(attacked)));
        return nearby.subList(0, Math.min(Math.max(0, count), nearby.size()));
    }

    /** Where VH draws a chain link: a third of the way up the mob. */
    public static Vec3 linkPoint(LivingEntity entity) {
        return entity.position().add(0.0, entity.getBbHeight() / 3.0F, 0.0);
    }

    public static void particles(LivingEntity attacker, List<Vec3> points) {
        ModNetwork.sendTrackingAndSelf(new ChainingParticleMessage(points), attacker);
    }
}
