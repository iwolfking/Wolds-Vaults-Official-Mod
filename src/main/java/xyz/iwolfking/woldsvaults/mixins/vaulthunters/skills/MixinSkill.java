package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.core.data.adapter.basic.TypeSupplierAdapter;
import iskallia.vault.skill.base.Skill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.*;
import xyz.iwolfking.woldsvaults.divinities.*;
import xyz.iwolfking.woldsvaults.expertises.*;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;

@Mixin(value = Skill.Adapter.class, remap = false)
public class MixinSkill extends TypeSupplierAdapter<Skill> {


    public MixinSkill(String key, boolean nullable) {
        super(key, nullable);
    }

    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void addSkills(CallbackInfo ci) {
        /* Divinity */
        this.register("divinity", DivinityTree.class, DivinityTree::new);

        this.register("divinity_vanilla_attribute", VanillaAttributeDivinity.class, VanillaAttributeDivinity::new);
        this.register("divinity_vanilla_attribute_2", VanillaAttributeDivinitySecondary.class, VanillaAttributeDivinitySecondary::new);
        this.register("divinity_gear_attribute", GearAttributeDivinity.class, GearAttributeDivinity::new);
        this.register("divinity_gear_attribute_2", GearAttributeDivinitySecondary.class, GearAttributeDivinitySecondary::new);
        this.register("divinity_skill_point_increase", SkillPointIncreaseDivinity.class, SkillPointIncreaseDivinity::new);
        this.register("divinity_expertise_point_increase", ExpertisePointIncreaseDivinity.class, ExpertisePointIncreaseDivinity::new);
        this.register("divinity_fall_reduction", FallReductionDivinity.class, FallReductionDivinity::new);
        this.register("divinity_kinetic_reduction", KineticReductionDivinity.class, KineticReductionDivinity::new);

        /* ---- */

        this.register("craftsman", CraftsmanExpertise.class, CraftsmanExpertise::new);
        this.register("negotiator", ShopRerollExpertise.class, ShopRerollExpertise::new);
        this.register("pylon_pilferer", PylonPilfererExpertise.class, PylonPilfererExpertise::new);
        this.register("blessed", BlessedExpertise.class, BlessedExpertise::new);
        this.register("surprise_favors", SurpriseModifiersExpertise.class, SurpriseModifiersExpertise::new);
        this.register("colossus", ColossusAbility.class,ColossusAbility::new);
        this.register("sneaky_getaway", SneakyGetawayAbility.class,SneakyGetawayAbility::new);
        this.register("vein_miner_chain", VeinMinerChainAbility.class, VeinMinerChainAbility::new);
        this.register("levitate", LevitateAbility.class,LevitateAbility::new);
    }
}
