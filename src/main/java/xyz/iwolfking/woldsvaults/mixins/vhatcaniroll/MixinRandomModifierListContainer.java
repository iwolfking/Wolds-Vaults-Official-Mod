package xyz.iwolfking.woldsvaults.mixins.vhatcaniroll;

import com.llamalad7.mixinextras.sugar.Local;
import com.radimous.vhatcaniroll.ui.gear.inner.RandomModifierListContainer;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.integration.vhatcaniroll.VHCIRMythicCharms;

import java.util.List;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "vhatcaniroll", versionPredicates = "<=1.16.2"),
    }
)
@Mixin(value = RandomModifierListContainer.class, remap = false)
public class MixinRandomModifierListContainer {

    /** On the god charm page in Mythic mode, lists the mythic charm's own implicits, prefixes and suffixes. */
    @ModifyVariable(method = "<init>", at = @At(value = "LOAD", ordinal = 0), ordinal = 0)
    private List<Component> woldsvaults$mythicCharmRolls(List<Component> modifiers,
                                                         @Local(argsOnly = true) ItemStack gearPiece,
                                                         @Local(argsOnly = true) boolean mythic) {
        if (!mythic || gearPiece.getItem() != ModItems.VAULT_GOD_CHARM) {
            return modifiers;
        }
        VaultGod god = VaultGearData.read(gearPiece).getFirstValue(ModGearAttributes.CHARM_VAULT_GOD).orElse(null);
        if (god == null) {
            WoldLogger.errorOnce("vhcir-mythic-charm-no-god",
                    "Vhat Can I Roll's god charm page has no god; showing its stock list instead of mythic charm rolls.");
            return modifiers;
        }
        return VHCIRMythicCharms.lines(god);
    }
}
