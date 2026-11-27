package xyz.iwolfking.woldsvaults.mixins.vaultmapper;

import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.integration.vaultmapper.VaultMapperMultiCell;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "vaultmapper"),
    }
)
@Mixin(value = VaultMap.class, remap = false)
public class MixinVaultMapMultiCell {

    /** Fills the rest of a multi-cell room's footprint once its anchor cell is on the map. */
    @Inject(method = "addOrReplaceCell", at = @At("RETURN"))
    private static void woldsvaults$expandMultiCellRoom(VaultCell cell, CallbackInfo ci) {
        VaultMapperMultiCell.expand(cell);
    }

    @Inject(method = "resetMap", at = @At("HEAD"))
    private static void woldsvaults$clearMultiCellRooms(CallbackInfo ci) {
        VaultMapperMultiCell.clear();
    }
}
