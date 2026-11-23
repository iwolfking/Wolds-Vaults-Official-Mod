package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.vault_forge;

import iskallia.vault.container.VaultForgeContainer;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.mixins.accessors.SlotAccessor;

@Mixin(value = VaultForgeContainer.class, remap = false)
public class MixinVaultForgeContainer {

    @Unique
    private static final int WOLDSVAULTS$DELTA_Y = 24;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void woldsVaults$shiftInventorySlots(CallbackInfo ci) {
        VaultForgeContainer self = (VaultForgeContainer) (Object) this;
        int limit = Math.min(VaultForgeContainer.INVENTORY_SLOT_COUNT, self.slots.size());
        for (int i = 0; i < limit; i++) {
            Slot slot = self.slots.get(i);
            ((SlotAccessor)slot).setY(slot.y + WOLDSVAULTS$DELTA_Y);
        }
    }
}