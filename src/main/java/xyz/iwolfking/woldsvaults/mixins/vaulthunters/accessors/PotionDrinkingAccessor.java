package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.bottle.PotionDrinking;
import iskallia.vault.item.bottle.PotionEffect;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PotionDrinking.class, remap = false)
public interface PotionDrinkingAccessor {
    @Invoker("applyEffect")
    void callTrigger(ServerPlayer player, @Nullable Vault vault, PotionEffect effect, int value);
}
