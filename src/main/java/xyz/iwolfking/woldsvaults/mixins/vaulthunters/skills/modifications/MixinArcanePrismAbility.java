package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.modifications;

import iskallia.vault.skill.ability.effect.ArcanePrismAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.arcane.ArcanePrisms;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;

import java.util.ArrayList;
import java.util.List;

/**
 * Arcane: Prism as a seed and a spinning glass pyramid ({@link ArcanePrisms}). Casting launches a seed that homes in
 * on the target; when it lands, it has Vault Hunters place the Prism there. Placing a Prism starts its turning beams
 * and tells nearby clients to draw it. Vault Hunters' pulses and their particles are switched off.
 */
@Mixin(value = ArcanePrismAbility.class, remap = false)
public class MixinArcanePrismAbility implements IWoldAbilityEnhancements {

    @Unique
    private static int woldsVaults$tier = -1;

    @Unique
    private static List<ElementalType> woldsVaults$elementalTypes = new ArrayList<>();

    @Inject(method = "createPrism", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$placePrism(ServerPlayer player, Vec3 position, Vec3 forward, float damageMultiplier, CallbackInfo ci) {
        ArcanePrismAbility self = (ArcanePrismAbility) (Object) this;
        if (ArcanePrisms.launch(self, player, position, forward, damageMultiplier)) {
            ci.cancel();
            return;
        }
        ArcanePrisms.created(player, position, forward, self.getDurationTicks(), self.getIntervalTicks(), self.getRadius(),
                self.getPercentAbilityPowerDealt() * damageMultiplier, self.getBeamWidth(), self.getMaxTargets());
    }

    @Inject(method = "pulse", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$noPulse(ServerPlayer player, Vec3 position, Vec3 forward, @Coerce Object pattern, float damageMultiplier, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "spawnPrismBurst", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$noBurstParticles(ServerLevel level, Vec3 position, float radius, CallbackInfo ci) {
        ci.cancel();
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
