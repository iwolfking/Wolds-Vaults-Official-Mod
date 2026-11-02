package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.modifications;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.ChargedBoltsAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.ability.effect.spi.core.Ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ChargedBoltsAbility.class, remap = false)
public class MixinChargedBoltsAbility implements IWoldAbilityEnhancements {

    @Unique
    private int woldsVaults$tier = -1;

    @Unique
    private List<ElementalType> woldsVaults$elementalTypes = new ArrayList<>();

    @Unique
    private static final Map<UUID, List<ElementalType>> woldsVaults$chargedBoltsElements = new ConcurrentHashMap<>();

    @Inject(method = "doAction", at = @At("HEAD"))
    private void woldsVaults$captureElements(SkillContext context, CallbackInfoReturnable<Ability.ActionResult> cir) {
        context.getSource().as(ServerPlayer.class).ifPresent(player -> {
            woldsVaults$chargedBoltsElements.put(player.getUUID(), new ArrayList<>(woldsVaults$elementalTypes));
        });
    }

    @WrapOperation(method = "lambda$applyLightningDamage$3", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), remap = true)
    private static boolean woldsVaults$wrapElementalFlags(LivingEntity instance, DamageSource source, float amount, Operation<Boolean> original) {
        Entity attacker = source.getEntity();
        List<ElementalType> types = List.of();
        if (attacker instanceof ServerPlayer player) {
            types = woldsVaults$chargedBoltsElements.getOrDefault(player.getUUID(), List.of());
        }
        boolean[] result = new boolean[]{false};
        ElementHelper.runWithElements(types, () -> result[0] = original.call(instance, source, amount));
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
        this.woldsVaults$elementalTypes = elementalTypes == null ? new ArrayList<>() : elementalTypes;
    }

    @Override
    public List<ElementalType> getElementTypes() {
        return woldsVaults$elementalTypes;
    }
}