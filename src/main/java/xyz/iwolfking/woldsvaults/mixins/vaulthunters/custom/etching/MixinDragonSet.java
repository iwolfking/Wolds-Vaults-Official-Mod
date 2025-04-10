package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.etching;

import com.google.common.collect.Lists;
import iskallia.vault.etching.EtchingSet;
import iskallia.vault.etching.set.AssassinSet;
import iskallia.vault.etching.set.DragonSet;
import iskallia.vault.etching.set.GearAttributeSet;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.etching.ZodSet;

import java.util.List;
import java.util.UUID;

@Mixin(value = DragonSet.class, remap = false)
public class MixinDragonSet extends EtchingSet<DragonSet.Config> implements GearAttributeSet {
    public MixinDragonSet(ResourceLocation name) {
        super(name);
    }

    @Override
    public Class<DragonSet.Config> getConfigClass() {
        return DragonSet.Config.class;
    }

    @Override
    public DragonSet.Config getDefaultConfig() {
        return new DragonSet.Config(0.5F);
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getAttributes() {
        return Lists.newArrayList(new VaultGearAttributeInstance[]{new VaultGearAttributeInstance(ModGearAttributes.AREA_OF_EFFECT, ((DragonSet.Config)this.getConfig()).getIncreasedDamage()), new VaultGearAttributeInstance(ModGearAttributes.ABILITY_POWER_PERCENTILE, ((DragonSet.Config)this.getConfig()).getIncreasedDamage())});
    }

    /**
     * @author iwolfking
     * @reason Disable existing functionality
     */
    @Overwrite
    public void tick(ServerPlayer player) {
        super.tick(player);
    }

    /**
     * @author iwolfking
     * @reason Disable existing functionality
     */
    @Overwrite
    public void remove(ServerPlayer player) {
        super.remove(player);
    }
}
