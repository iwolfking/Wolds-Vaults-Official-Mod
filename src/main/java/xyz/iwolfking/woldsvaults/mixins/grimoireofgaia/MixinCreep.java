package xyz.iwolfking.woldsvaults.mixins.grimoireofgaia;

import gaia.entity.AbstractGaiaEntity;
import gaia.entity.Creep;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "grimoireofgaia")
        }
)
@Mixin(value = Creep.class, remap = false)
public abstract class MixinCreep extends AbstractGaiaEntity implements PowerableMob {
    public MixinCreep(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "explodeCreep", at = @At(value = "INVOKE", target = "Lgaia/entity/Creep;spawnLingeringCloud(Ljava/util/List;)V"))
    private void fixLingeringCloud(Creep instance, List<MobEffectInstance> list) {
    }
}
