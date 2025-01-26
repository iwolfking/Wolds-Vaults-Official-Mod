package xyz.iwolfking.woldsvaults.etching;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import iskallia.vault.etching.EtchingSet;
import iskallia.vault.etching.set.AssassinSet;
import iskallia.vault.etching.set.GearAttributeSet;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ZodSet extends EtchingSet<ZodSet.Config> implements GearAttributeSet {
    public ZodSet(ResourceLocation name) {
        super(name);
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getAttributes() {
        return Lists.newArrayList(new VaultGearAttributeInstance[]{new VaultGearAttributeInstance(ModGearAttributes.ABILITY_LEVEL, new AbilityLevelAttribute("all_abilities", this.getConfig().increasedAbilityLevels))});
    }

    @Override
    public Class<ZodSet.Config> getConfigClass() {
        return ZodSet.Config.class;
    }

    @Override
    public ZodSet.Config getDefaultConfig() {
        return new ZodSet.Config(2);
    }

    public static class Config {
        @Expose
        private int increasedAbilityLevels;

        public Config(int increasedAbilityLevels) {
            this.increasedAbilityLevels = increasedAbilityLevels;
        }

        public int getIncreasedAbilityLevels() {
            return this.increasedAbilityLevels;
        }
    }
}
