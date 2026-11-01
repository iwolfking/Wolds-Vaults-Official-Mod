package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.modifications;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.ArcanePrismAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ArcanePrismAbility.class, remap = false)
public class MixinArcanePrismAbility implements IWoldAbilityEnhancements {

    @Unique
    private static int woldsVaults$tier = -1;

    @Unique
    private static List<ElementalType> woldsVaults$elementalTypes = new ArrayList<>();

    @WrapOperation(method = "lambda$pulse$6", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), remap = true)
    private static boolean woldsVaults$wrapElementalFlags(LivingEntity instance, DamageSource source, float amount, Operation<Boolean> original) {
        boolean[] result = new boolean[]{false};
        ElementHelper.runWithElements(woldsVaults$elementalTypes, () -> result[0] = original.call(instance, source, amount));
        return result[0];
    }

    @Override
    public void setTierLevel(int level) {
        this.woldsVaults$tier = level;
    }

    @Override
    public int getTierLevel() {
        return woldsVaults$tier;
    }

    @Override
    public void setElementTypes(List<ElementalType> elementalTypes) {
        woldsVaults$elementalTypes = elementalTypes == null ? new ArrayList<>() : elementalTypes;
    }

    @Override
    public List<ElementalType> getElementTypes() {
        return woldsVaults$elementalTypes;
    }
}