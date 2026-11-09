package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.utils.AbilityHelper;
import com.infamous.dungeons_libraries.utils.PetHelper;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.mixins.accessors.NearestAttackableTargetGoalAccessor;
import xyz.iwolfking.woldsvaults.mixins.accessors.TargetingConditionsAccessor;

import java.util.function.Predicate;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "dungeons_libraries")
        }
)
@Mixin(value = AbilityHelper.class, remap = false)
public class MixinAbilityHelper {

    @Shadow
    private static boolean isBothPlayerAndNoPvP(LivingEntity attacker, LivingEntity nearbyEntity) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static boolean isEntityBlacklisted(LivingEntity origin, LivingEntity target) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    /**
     * @author iwolfking
     * @reason Fix mobs being unable to attack each other, Patrigan.... (backported fix from 1.19.2 version of Dungeons Libraries
     */
    @Overwrite
    public static boolean isAlly(LivingEntity origin, LivingEntity target) {
        LivingEntity originOwner = PetHelper.getOwner(origin);
        LivingEntity targetOwner = PetHelper.getOwner(target);

        if (originOwner != null || targetOwner != null) {
            return isAlly(originOwner != null ? originOwner : origin, targetOwner != null ? targetOwner : target);
        }

        boolean isPetOrColleague = PetHelper.isPetOrColleagueRelation(origin, target);
        boolean isAllied = origin.isAlliedTo(target);
        boolean isNoPvP = isBothPlayerAndNoPvP(origin, target);
        boolean isBlacklisted = isEntityBlacklisted(origin, target);

        boolean isMonsterAlly = origin.getClassification(false).equals(MobCategory.MONSTER)
                && target.getClassification(false).equals(MobCategory.MONSTER)
                && !woldsVaults$matchesAITargets(origin, target);

        return isPetOrColleague || isAllied || isNoPvP || isMonsterAlly || isBlacklisted;
    }

    @Unique
    private static boolean woldsVaults$matchesAITargets(LivingEntity origin, LivingEntity target) {
        if (origin instanceof Mob originMob) {
            return woldsVaults$checkTargetGoals(originMob, target);
        }
        return true;
    }

    @Unique
    private static boolean woldsVaults$checkTargetGoals(Mob originMob, LivingEntity target) {
        return originMob.targetSelector.getAvailableGoals().stream()
                .map(WrappedGoal::getGoal)
                .filter(goal -> goal instanceof NearestAttackableTargetGoal<?>)
                .map(goal -> woldsVaults$matchesPredicate(target, ((NearestAttackableTargetGoalAccessor<?>) goal).getTargetType(), ((NearestAttackableTargetGoalAccessor<?>) goal).getTargetingConditions()))
                .reduce(false, (o, o2) -> o || o2);
    }

    @Unique
    private static boolean woldsVaults$matchesPredicate(LivingEntity target, Class targetType, TargetingConditions targetConditions) {
        Predicate<LivingEntity> selector = ((TargetingConditionsAccessor) targetConditions).getSelector();
        return (targetType == null || targetType.isInstance(target)) && (selector == null || selector.test(target));
    }
}
