package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.container.oversized.OverSizedSlotContainer;
import iskallia.vault.container.spi.AbstractElementContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OverSizedSlotContainer.class, remap = false)
public abstract class MixinOversizedSlotContainer extends AbstractElementContainer {
    public MixinOversizedSlotContainer(MenuType<?> menuType, int id, Player player) {
        super(menuType, id, player);
    }

    @Inject(method = "setSynchronizer", at = @At("HEAD"), cancellable = true, remap = true)
    private void preventFakePlayerLeak(ContainerSynchronizer sync, CallbackInfo ci) {
        if(this.getPlayer() instanceof FakePlayer) {
            ci.cancel();
        }
    }
}
