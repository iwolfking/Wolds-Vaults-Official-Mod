package xyz.iwolfking.woldsvaults.mixins.bettercombat;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.PlayerStat;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.client.collision.OrientedBoundingBox;
import net.bettercombat.client.collision.TargetFinder;
import net.bettercombat.client.collision.WeaponHitBoxes;
import net.bettercombat.compatibility.CompatibilityFlags;
import net.bettercombat.compatibility.PehkuiHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

import static net.bettercombat.client.collision.TargetFinder.getInitialTargets;
import static net.bettercombat.client.collision.TargetFinder.getInitialTracingPoint;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "bettercombat")
        }
)
@Mixin(value = TargetFinder.class, remap = false)
public abstract class MixinBetterCombatAttackRange {
    /**
     * @author iwolfking
     * @reason Add vault hunters attack range support
     */
    @Overwrite
    public static TargetFinder.TargetResult findAttackTargetResult(Player player, Entity cursorTarget, WeaponAttributes.Attack attack, double attackRange) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        double range = snapshot.getAttributeValue(ModGearAttributes.ATTACK_RANGE, VaultGearAttributeTypeMerger.doubleSum());
        attackRange += range;
        Vec3 origin = getInitialTracingPoint(player);
        List<Entity> entities = getInitialTargets(player, cursorTarget, attackRange);
        if (CompatibilityFlags.usePehkui) {
            attackRange *= PehkuiHelper.getScale(player);
        }

        boolean isSpinAttack = attack.angle() > 180.0;
        Vec3 size = WeaponHitBoxes.createHitbox(attack.hitbox(), attackRange, isSpinAttack);
        OrientedBoundingBox obb = new OrientedBoundingBox(origin, size, player.getXRot(), player.getYRot());
        if (!isSpinAttack) {
            obb = obb.offsetAlongAxisZ(size.z / 2.0);
        }

        obb.updateVertex();
        TargetFinder.CollisionFilter collisionFilter = new TargetFinder.CollisionFilter(obb);
        entities = collisionFilter.filter(entities);
        TargetFinder.RadialFilter radialFilter = new TargetFinder.RadialFilter(origin, obb.axisZ, attackRange, attack.angle());
        entities = radialFilter.filter(entities);
        return new TargetFinder.TargetResult(entities, obb);
    }


}
