package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
import iskallia.vault.core.data.adapter.basic.TypeSupplierAdapter;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.CrystalEntry;
import iskallia.vault.item.crystal.data.serializable.ISerializable;
import iskallia.vault.item.crystal.model.CrystalModel;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.models.crystal.UnhingedCrystalModel;

@Mixin(value = CrystalData.class, remap = false)
public abstract class MixinCrystalData extends CrystalEntry implements ISerializable<CompoundTag, JsonObject>
{

    @Shadow public static TypeSupplierAdapter<CrystalModel> MODEL;

    @Shadow private CrystalModifiers modifiers;

    @Shadow private CrystalTheme theme;

    static {
        MODEL.register("unhinged", UnhingedCrystalModel.class, UnhingedCrystalModel::new);
    }

    /**
     * @author iwolfking
     * @reason Always allow catalyst fragments
     */
    @Overwrite
    public boolean canGenerateCatalystFragments() {
        boolean hasCatalystDenyingModifier = false;
        for(VaultModifierStack modStack : this.modifiers.getList()) {

            if(modStack.getModifierId().equals(VaultMod.id("gilded_cascade"))) {
                if(modStack.getSize() > 2) {
                    hasCatalystDenyingModifier = true;
                }
            }

            if(modStack.getModifierId().equals(VaultMod.id("prismatic"))) {
                hasCatalystDenyingModifier = false;
            }
            else if(modStack.getModifierId().equals(VaultMod.id("sparkling"))) {
                hasCatalystDenyingModifier = false;
            }
        }

        return !hasCatalystDenyingModifier;
    }

}
