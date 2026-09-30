package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ClassicListenersLogic.class, remap = false)
public interface ClassicListenersLogicAccessor {
    @Invoker("shouldAddFree")
    boolean woldsvaults$shouldAddFree(ServerPlayer player, Vault vault, ItemStack stack);
}
