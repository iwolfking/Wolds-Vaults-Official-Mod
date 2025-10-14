package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.core.data.adapter.basic.TypeSupplierAdapter;
import iskallia.vault.skill.base.Skill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.*;
import xyz.iwolfking.woldsvaults.expertises.*;
import xyz.iwolfking.woldsvaults.prestige.CraftingRecipePower;
import xyz.iwolfking.woldsvaults.prestige.ReachPrestigePower;

@Mixin(value = Skill.Adapter.class, remap = false)
public class MixinSkill extends TypeSupplierAdapter<Skill> {


    public MixinSkill(String key, boolean nullable) {
        super(key, nullable);
    }

    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void addSkills(CallbackInfo ci) {
        this.register("craftsman", CraftsmanExpertise.class, CraftsmanExpertise::new);
        this.register("negotiator", ShopRerollExpertise.class, ShopRerollExpertise::new);
        this.register("pylon_pilferer", PylonPilfererExpertise.class, PylonPilfererExpertise::new);
        this.register("blessed", BlessedExpertise.class, BlessedExpertise::new);
        this.register("surprise_favors", SurpriseModifiersExpertise.class, SurpriseModifiersExpertise::new);
        this.register("colossus", ColossusAbility.class,ColossusAbility::new);
        this.register("sneaky_getaway", SneakyGetawayAbility.class,SneakyGetawayAbility::new);
        this.register("vein_miner_chain", VeinMinerChainAbility.class, VeinMinerChainAbility::new);
        this.register("levitate", LevitateAbility.class,LevitateAbility::new);

        this.register("reach_cap_power", ReachPrestigePower.class, ReachPrestigePower::new);
        this.register("crafting_recipe_power", CraftingRecipePower.class, CraftingRecipePower::new);
    }
}
