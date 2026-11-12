package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Ability.class, remap = false)
public interface AbilityCooldownInvoker {
    @Invoker("setCooldown")
    void woldsvaults$setCooldown(int maxCooldownTicks, int cooldownTicks, int cooldownDelayTicks);

    @Invoker("trySkipCooldown")
    boolean woldsvaults$trySkipCooldown(ServerPlayer player);
}
