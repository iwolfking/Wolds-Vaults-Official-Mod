package xyz.iwolfking.woldsvaults.etching;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import iskallia.vault.etching.EtchingSet;
import iskallia.vault.etching.set.GearAttributeSet;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class IngeniumSet extends EtchingSet<IngeniumSet.Config> implements GearAttributeSet {
    public IngeniumSet(ResourceLocation name) {
        super(name);
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getAttributes() {
        return Lists.newArrayList(new VaultGearAttributeInstance[]{new VaultGearAttributeInstance(ModGearAttributes.TALENT_LEVEL, new TalentLevelAttribute("all_talents", this.getConfig().increasedTalentLevels))});
    }

    @Override
    public Class<IngeniumSet.Config> getConfigClass() {
        return IngeniumSet.Config.class;
    }

    @Override
    public IngeniumSet.Config getDefaultConfig() {
        return new IngeniumSet.Config(1);
    }

    public static class Config {
        @Expose
        private int increasedTalentLevels;

        public Config(int increasedTalentLevels) {
            this.increasedTalentLevels = increasedTalentLevels;
        }

        public int getIncreasedTalentLevels() {
            return this.increasedTalentLevels;
        }
    }
}
