package xyz.iwolfking.woldsvaults.mixins.infernalmobs;


import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.integration.infernalmobs.ModInfernalMobModifiers;

import java.util.ArrayList;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "infernalmobs")
        }
)
@Mixin(value = InfernalMobsCore.class, remap = false)
public abstract class MixinInfernalMobsCore {


    @Shadow
    private ArrayList<Class<? extends MobModifier>> mobMods;

    /**
     * @author iwolfking
     * @reason Disable Infernal Mob spawns
     */
    @Overwrite
    public void processEntitySpawn(LivingEntity entity) {
        return;
    }

    @Inject(method = "prepareModList", at = @At("TAIL"))
    private void addNewModifiers(CallbackInfo ci) {
        this.mobMods.addAll(ModInfernalMobModifiers.MOB_MODIFIERS);
    }
}
