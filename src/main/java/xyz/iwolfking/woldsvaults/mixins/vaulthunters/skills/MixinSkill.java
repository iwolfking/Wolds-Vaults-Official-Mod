package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.core.data.adapter.basic.TypeSupplierAdapter;
import iskallia.vault.skill.base.Skill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.*;
import xyz.iwolfking.woldsvaults.expertises.*;
import xyz.iwolfking.woldsvaults.gods.ultimates.*;
import xyz.iwolfking.woldsvaults.prestige.*;
import xyz.iwolfking.woldsvaults.talent.bottle.HealthyElixirTalent;
import xyz.iwolfking.woldsvaults.talent.bottle.PotentElixirTalent;
import xyz.iwolfking.woldsvaults.talent.lib.StackOnHitAttributeTalent;
import xyz.iwolfking.woldsvaults.talent.loot_trigger.HeartFragmentPerLootedContainerTalent;
import xyz.iwolfking.woldsvaults.talent.lucky_hit.CooldownReductionLuckyHitTalent;
import xyz.iwolfking.woldsvaults.talent.lucky_hit.ExecutionDamageLuckyHitTalent;
import xyz.iwolfking.woldsvaults.talent.lucky_hit.FangedStrikeLuckyHitTalent;
import xyz.iwolfking.woldsvaults.talent.special.MomentumEngineTalent;
import xyz.iwolfking.woldsvaults.talent.special.WoldsAxeSpecializationTalent;
import xyz.iwolfking.woldsvaults.talent.stat_conversion.MindMeldTalent;

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

        //God ultimates
        this.register("stirrings_of_power", StirringsOfPowerAbility.class, StirringsOfPowerAbility::new);
        this.register("cope_de_grace", CopeDeGraceAbility.class, CopeDeGraceAbility::new);
        this.register("savior", SaviorAbility.class, SaviorAbility::new);
        this.register("eyes_of_god", EyesOfGodAbility.class, EyesOfGodAbility::new);
        this.register("bullet_time", BulletTimeAbility.class, BulletTimeAbility::new);

        this.register("reach_cap_power", ReachPrestigePower.class, ReachPrestigePower::new);
        this.register("crafting_recipe_power", CraftingRecipePower.class, CraftingRecipePower::new);
        this.register("tool_capacity_power", ToolCapacityPrestigePower.class, ToolCapacityPrestigePower::new);
        this.register("crafting_potential_power", CraftingPotentialPrestigePower.class, CraftingPotentialPrestigePower::new);
        this.register("legendary_bounty_power", LegendaryBountyPower.class, LegendaryBountyPower::new);
        this.register("god_experience_power", GodExperiencePrestigePower.class, GodExperiencePrestigePower::new);
        this.register("relic_hunter_power", RelicHunterPrestigePower.class, RelicHunterPrestigePower::new);
        this.register("unreleased_power", UnreleasedPrestigePower.class, UnreleasedPrestigePower::new);

        //Talents
        this.register("wold_axe_talent", WoldsAxeSpecializationTalent.class, WoldsAxeSpecializationTalent::new);
        this.register("stack_on_hit_talent", StackOnHitAttributeTalent.class, StackOnHitAttributeTalent::new);
        this.register("execution_lucky_hit", ExecutionDamageLuckyHitTalent.class, ExecutionDamageLuckyHitTalent::new);
        this.register("cooldown_lucky_hit", CooldownReductionLuckyHitTalent.class, CooldownReductionLuckyHitTalent::new);
        this.register("fanged_lucky_hit", FangedStrikeLuckyHitTalent.class, FangedStrikeLuckyHitTalent::new);
        this.register("heart_fragment_on_loot", HeartFragmentPerLootedContainerTalent.class, HeartFragmentPerLootedContainerTalent::new);
        this.register("mind_meld", MindMeldTalent.class, MindMeldTalent::new);
        this.register("momentum_engine", MomentumEngineTalent.class, MomentumEngineTalent::new);
        this.register("potent_elixir", PotentElixirTalent.class, PotentElixirTalent::new);
        this.register("healthy_elixir", HealthyElixirTalent.class, HealthyElixirTalent::new);
    }
}
