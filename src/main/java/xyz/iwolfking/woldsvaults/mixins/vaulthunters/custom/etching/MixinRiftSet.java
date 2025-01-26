package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.etching;

import com.google.common.collect.Lists;
import iskallia.vault.etching.EtchingSet;
import iskallia.vault.etching.set.GearAttributeSet;
import iskallia.vault.etching.set.GolemSet;
import iskallia.vault.etching.set.RiftSet;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(value = RiftSet.class, remap = false)
public abstract class MixinRiftSet extends EtchingSet<RiftSet.Config> implements GearAttributeSet {
    public MixinRiftSet(ResourceLocation name) {
        super(name);
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getAttributes() {
        return Lists.newArrayList(new VaultGearAttributeInstance[]{new VaultGearAttributeInstance(ModGearAttributes.COOLDOWN_REDUCTION_CAP, ((RiftSet.Config)this.getConfig()).getCooldownMultiplier())});
    }
}
