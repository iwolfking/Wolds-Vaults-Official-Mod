package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.etching;

import com.google.common.collect.Lists;
import iskallia.vault.etching.EtchingSet;
import iskallia.vault.etching.set.AssassinSet;
import iskallia.vault.etching.set.GearAttributeSet;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(value = AssassinSet.class, remap = false)
public abstract class MixinAssassinSet  extends EtchingSet<AssassinSet.Config> implements GearAttributeSet {
    public MixinAssassinSet(ResourceLocation name) {
        super(name);
    }

    @Overwrite
    public List<VaultGearAttributeInstance<?>> getAttributes() {
        return Lists.newArrayList(new VaultGearAttributeInstance[]{new VaultGearAttributeInstance(ModGearAttributes.LUCKY_HIT_CHANCE_PERCENTILE, ((AssassinSet.Config)this.getConfig()).getIncreasedFatalStrikeChance())});
    }
}
