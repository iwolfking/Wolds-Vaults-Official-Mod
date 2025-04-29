package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.ObeliskObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.item.gear.CharmItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ObeliskObjective.class, remap = false)
public abstract class MixinObeliskObjective extends Objective {
    @Shadow @Final public static FieldKey<ObeliskObjective.Wave[]> WAVES;
//    @Unique
//    private boolean woldsVaults$hasScaledObjective = false;
//
//    @Inject(method = "tickServer", at = @At("TAIL"))
//    private void addObjectiveDifficultyHandling(VirtualWorld world, Vault vault, CallbackInfo ci) {
//        if(woldsVaults$hasScaledObjective) {
//            return;
//        }
//        this.ifPresent(WAVES, (value) -> {
//            double increase = CommonEvents.OBJECTIVE_TARGET.invoke(world, vault, (double)0.0F).getIncrease();
//            for(int i = 0; i < (this.get(WAVES)).length; ++i) {
//                (this.get(WAVES))[i] = new ObeliskObjective.Wave((int) (this.get(WAVES)[i].get(ObeliskObjective.Wave.TARGET) * (1.0F + increase)));
//            }
//        });
//
//        woldsVaults$hasScaledObjective = true;
//    }
}
