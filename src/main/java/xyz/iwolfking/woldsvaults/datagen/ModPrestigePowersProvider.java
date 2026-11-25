package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.prestige.*;
import iskallia.vault.skill.prestige.core.PrestigePower;
import iskallia.vault.skill.prestige.core.TrinketPrestigePower;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.effect.MobEffects;
import xyz.iwolfking.vhapi.api.datagen.AbstractPrestigePowerProvider;
import xyz.iwolfking.vhapi.api.loaders.skills.PrestigePowersConfigLoader;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.prestige.CraftingPotentialPrestigePower;
import xyz.iwolfking.woldsvaults.prestige.CraftingRecipePower;
import xyz.iwolfking.woldsvaults.prestige.ReachPrestigePower;
import xyz.iwolfking.woldsvaults.prestige.ToolCapacityPrestigePower;

import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class ModPrestigePowersProvider extends AbstractPrestigePowerProvider {
    protected ModPrestigePowersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
//        add("replace/vanilla_tier_moves", builder -> {
//           builder.add()
//        });
        add("remove/vanilla_removals", builder -> {
            builder.add("TheCarapace", "Shell of the Carapace", 0, 0, 0, 1, 2, 1, 1, tier -> {
                return new TrinketPrestigePower(VaultMod.id("carapace"));
            });
            builder.add("AetherSparkTrinket", "Burst of the Aether", 0, 0, 0, 1, 2, 1, 1, tier -> {
                return new TrinketPrestigePower(VaultMod.id("aether"));
            });
            builder.add("ShadowCloakPower", "Veil of the Shadow Cloak", 0, 0, 0, 1, 2, 1, 1, tier -> {
                return new TrinketPrestigePower(VaultMod.id("shadow_cloak"));
            });
            builder.add("TemporalShardChance", "Trace of Eternal", 0, 0, 0, 1, 3, 1, 1, tier -> {
                return new TemporalShardChancePrestigePower(0.3F);
            });
            builder.add("CatDodge", "Grace of the Cat", 0, 0, 0, 1, 4, 1, 1, tier -> {
                return new TrinketPrestigePower(VaultMod.id("cat_in_a_jar"));
            });
            builder.add("RegenerationPower", "Gift of the Petal", 0, 0, 0, 1, 5, 1, 1, tier -> {
                return new PotionEffectPrestigePower(MobEffects.REGENERATION.getRegistryName(), 0);
            });
            builder.add("ElytraWings", "Wings of Ascension", 0, 0, 0, 1, 4, 1, 1, tier -> {
                return new WingsActivationPrestigePower();
            });
            builder.add("ChainMiner", "Chain Miner", 0, 0, 0, 1, 5, 1, 1, tier -> {
                return new ChainMinerPrestigePower();
            });
        });

        //TODO: Adding Prestige Powers like this causes them to reset on reload
//        add("wolds_powers", builder -> {
//            builder.add("PrismaticPouch", "Prism Possessor", 0, 0, 0, 1, 4, 0, 1, tier -> {
//               return new CraftingRecipePower(WoldsVaults.id("prismatic_trinket_pouch"));
//            });
//
//            builder.add("BarrierOfResilience", "Barrier of Resilience", 0, 0, 0, 1,5, 0, 1, tier -> {
//                return new GearAttributePrestigePower(0, 0, 0, 1, ModGearAttributes.RESISTANCE_CAP, 0.05F + (0.025F * tier));
//            });
//
//            builder.add("ShieldOfLastingGuard", "Shield of Lasting Guard", 0, 0, 0, 1,4, 0, 1, tier -> {
//                return new GearAttributePrestigePower(0, 0, 0, 1, ModGearAttributes.BLOCK_CAP, 0.05F + (0.025F * tier));
//            });
//
//            builder.add("WeaverOfTime", "Weaver of Time", 0, 0, 0, 1,5, 0, 1, tier -> {
//                return new GearAttributePrestigePower(0, 0, 0, 1, ModGearAttributes.COOLDOWN_REDUCTION_CAP, 0.05F + (0.025F * tier));
//            });
//
//            builder.add("Artificer", "Artificer of Ages", 0, 0, 0, 1,3, 0, 1, tier -> {
//                return new CraftingPotentialPrestigePower(100);
//            });
//
//            builder.add("Toolsmith", "Echoing Hammersmith", 0, 0, 0, 1,2, 0, 1, tier -> {
//                return new ToolCapacityPrestigePower(15);
//            });
//
//            builder.add("SoulFetcher", "Fetcher of Souls", 0, 0, 0, 1,2, 0, 1, tier -> {
//                return new GearAttributePrestigePower(0, 0, 0, 1, ModGearAttributes.SOUL_QUANTITY, 1.0F);
//            });
//
//            builder.add("HealthIncrease", "Blessing of the Dryad", 0, 0, 0, 1,2, 0, 1, tier -> {
//                return new GearAttributePrestigePower(0, 0, 0, 1, ModGearAttributes.HEALTH, 30);
//            });
//        });
    }

    public TieredSkill createTieredSkill(int unlockLevel, int learnPointCost, int regretPointCost, int numberOfTiers, IntFunction<? extends PrestigePower> tiers) {
        return new TieredSkill(unlockLevel, learnPointCost, 1, IntStream.range(0, numberOfTiers).mapToObj(tiers));
    }
}
