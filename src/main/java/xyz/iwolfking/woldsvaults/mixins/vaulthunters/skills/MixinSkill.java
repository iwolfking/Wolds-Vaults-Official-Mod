package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.core.data.adapter.basic.TypeSupplierAdapter;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.Skill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.*;
import xyz.iwolfking.woldsvaults.expertises.*;
import xyz.iwolfking.woldsvaults.prestige.*;
import xyz.iwolfking.woldsvaults.talent.*;

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
        this.register("grave_insurance", GraveInsurance.class, GraveInsurance::new);
        this.register("augmentation_luck", EclecticGearExpertise.class, EclecticGearExpertise::new);
        this.register("surprise_favors", SurpriseModifiersExpertise.class, SurpriseModifiersExpertise::new);
        this.register("gambler", GamblerExpertise.class, GamblerExpertise::new);
        this.register("deck_master", DeckMasterExpertise.class, DeckMasterExpertise::new);
        this.register("colossus", ColossusAbility.class,ColossusAbility::new);
        this.register("sneaky_getaway", SneakyGetawayAbility.class,SneakyGetawayAbility::new);
        this.register("vein_miner_chain", VeinMinerChainAbility.class, VeinMinerChainAbility::new);
        this.register("levitate", LevitateAbility.class,LevitateAbility::new);
        this.register("expunge", ExpungeAbility.class,ExpungeAbility::new);
        this.register("concentrate", ConcentrateAbility.class,ConcentrateAbility::new);
        this.register("fangs", EvokerFangsAbility.class, EvokerFangsAbility::new);
        this.register("fangs_maw", EvokerFangsMawAbility.class, EvokerFangsMawAbility::new);
        this.register("meteor_storm", MeteorStormAbility.class, MeteorStormAbility::new);

        this.register("reach_cap_power", ReachPrestigePower.class, ReachPrestigePower::new);
        this.register("crafting_recipe_power", CraftingRecipePower.class, CraftingRecipePower::new);
        this.register("tool_capacity_power", ToolCapacityPrestigePower.class, ToolCapacityPrestigePower::new);
        this.register("crafting_potential_power", CraftingPotentialPrestigePower.class, CraftingPotentialPrestigePower::new);
        this.register("legendary_bounty_power", LegendaryBountyPower.class, LegendaryBountyPower::new);

        //Talents
        this.register("wold_axe_talent", WoldsAxeSpecializationTalent.class, WoldsAxeSpecializationTalent::new);
        this.register("stacking_weapon_attribute_talent", StackingWeaponAttributeTalent.class, StackingWeaponAttributeTalent::new);
        this.register("execution_lucky_hit", ExecutionDamageLuckyHitTalent.class, ExecutionDamageLuckyHitTalent::new);
        this.register("cooldown_lucky_hit", CooldownReductionLuckyHitTalent.class, CooldownReductionLuckyHitTalent::new);
        this.register("fanged_lucky_hit", FangedStrikeLuckyHitTalent.class, FangedStrikeLuckyHitTalent::new);
        this.register("heart_fragment_on_loot", HeartFragmentPerLootedContainerTalent.class, HeartFragmentPerLootedContainerTalent::new);
        this.register("mind_meld", MindMeldTalent.class, MindMeldTalent::new);
        this.register("momentum_engine", MomentumEngineTalent.class, MomentumEngineTalent::new);
    }
}
