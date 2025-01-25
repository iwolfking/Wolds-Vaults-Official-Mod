package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.artisan;

import iskallia.vault.block.entity.VaultArtisanStationTileEntity;
import iskallia.vault.container.VaultArtisanStationContainer;
import iskallia.vault.container.oversized.OverSizedTabSlot;
import iskallia.vault.gear.modification.GearModification;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModGearModifications;

@Mixin(value = VaultArtisanStationContainer.class, remap = false)
public abstract class MixinArtisanStationContainer {
    @Shadow @Final private VaultArtisanStationTileEntity tileEntity;

    @Inject(method = "initSlots", at = @At(value = "INVOKE", target = "Liskallia/vault/container/VaultArtisanStationContainer;addModSlot(Liskallia/vault/container/oversized/OverSizedTabSlot;Liskallia/vault/container/VaultArtisanStationContainer$Tab;Liskallia/vault/gear/modification/GearModification;Liskallia/vault/container/VaultArtisanStationContainer$ButtonSide;)V", ordinal = 13))
    private void addUnusualSlot(Inventory playerInventory, CallbackInfo ci) {
        this.addModSlot(new OverSizedTabSlot(this.tileEntity.getInventory(), 16, 150, 92), VaultArtisanStationContainer.Tab.EXOTIC, ModGearModifications.UNUSUAL_GEAR_MODIFICATION, VaultArtisanStationContainer.ButtonSide.LEFT);
        //this.addModSlot(new OverSizedTabSlot(this.tileEntity.getInventory(), 17, 8, 92), VaultArtisanStationContainer.Tab.EXOTIC, ModGearModifications.WEAPON_TYPE_MODIFICATION, VaultArtisanStationContainer.ButtonSide.RIGHT);
    }

    @Shadow protected abstract void addModSlot(OverSizedTabSlot slot, VaultArtisanStationContainer.Tab tab, GearModification modification, VaultArtisanStationContainer.ButtonSide side);
}
