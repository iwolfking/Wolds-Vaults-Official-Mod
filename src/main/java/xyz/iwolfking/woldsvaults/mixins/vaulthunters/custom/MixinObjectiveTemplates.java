package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.event.common.ObjectiveTemplateEvent;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.world.template.Template;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModObjectiveTemplates;
import xyz.iwolfking.woldsvaults.objectives.AlchemyObjective;
import xyz.iwolfking.woldsvaults.objectives.CorruptedObjective;

@Mixin(value = ObjectiveTemplateEvent.class, remap = false)
public class MixinObjectiveTemplates {
    @Inject(method = "getTemplateForObjective", at = @At("HEAD"), cancellable = true)
    private void getWoldsObjectiveTemplates(Objective objective, CallbackInfoReturnable<Template> cir) {
        if(objective instanceof CorruptedObjective) {
            cir.setReturnValue(ModObjectiveTemplates.CORRUPTED_OBJECTIVE_TEMPLATE);
        } else if (objective instanceof AlchemyObjective) {
            cir.setReturnValue(ModObjectiveTemplates.ALCHEMY_OBJECTIVE_TEMPLATE);
        }
    }
}
