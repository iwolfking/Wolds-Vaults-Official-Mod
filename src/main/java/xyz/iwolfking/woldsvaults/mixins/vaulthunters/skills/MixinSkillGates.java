package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.skill.SkillGates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.List;

@Mixin(value = SkillGates.class, remap = false)
public class MixinSkillGates {

    @ModifyVariable(
            method = "getAllSkillIds",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private List<String> addDivinitySkills(List<String> allSkillIds) {
        ModConfigs.DIVINITY_CONFIG.getAll().skills.forEach((s) -> {
            allSkillIds.add(s.getId());
        });

        return allSkillIds;
    }
}