package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidget;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.AbilityTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.NoSuchElementException;
import java.util.Optional;

@Mixin(value = AbilityWidget.class, remap = false)
public abstract class MixinAbilityWidget {

    @Shadow @Final protected String abilityName;

    @WrapOperation(
        method = "updateTree",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Optional;orElseThrow()Ljava/lang/Object;"
        )
    )
    private Object wrapGetAbilityOrLog(Optional<Skill> instance, Operation<Object> original) {
        try {
            return original.call(instance);
        } catch (NoSuchElementException e) {
            WoldLogger.error("Failed to find ability ID '{}' in AbilityTree during widget update!", this.abilityName, e);
            return null;
        }
    }
}