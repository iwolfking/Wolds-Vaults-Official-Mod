package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.ability.effect.ArcanePrismAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// places a Prism once its seed has landed
@Mixin(value = ArcanePrismAbility.class, remap = false)
public interface ArcanePrismAbilityInvoker {
    @Invoker("createPrism")
    void woldsvaults$createPrism(ServerPlayer player, Vec3 position, Vec3 forward, float damageMultiplier);
}
