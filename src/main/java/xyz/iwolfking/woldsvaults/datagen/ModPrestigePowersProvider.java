package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.prestige.GearAttributePrestigePower;
import iskallia.vault.skill.prestige.core.PrestigePower;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractPrestigePowerProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.prestige.CraftingRecipePower;
import xyz.iwolfking.woldsvaults.prestige.ReachPrestigePower;

import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class ModPrestigePowersProvider extends AbstractPrestigePowerProvider {
    protected ModPrestigePowersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
//        add("wolds_powers", builder -> {
//            builder.add("PrismaticPouch", "Prism Possessor", 25, 1250, 100, 1, 7, 6, 1, tier -> {
//               return new CraftingRecipePower(WoldsVaults.id("prismatic_trinket_pouch"));
//            });
//
//            builder.add("SpiritsHand", "Spirit's Hand", 25, 1250, 100, 1,7, 6, 3, tier -> {
//               return new ReachPrestigePower(0.5F + (0.25F * tier));
//            });
//
//            builder.add("BarrierOfResilience", "Barrier of Resilience", 25, 1250, 100, 1,7, 6, 3, tier -> {
//                return new GearAttributePrestigePower(100, 25, 1250, 1, ModGearAttributes.RESISTANCE_CAP, 0.05F + (0.025F * tier));
//            });
//
//            builder.add("ShieldOfLastingGuard", "Shield of Lasting Guard", 25, 1250, 100, 1,7, 6, 3, tier -> {
//                return new GearAttributePrestigePower(100, 25, 1250, 1, ModGearAttributes.BLOCK_CAP, 0.05F + (0.025F * tier));
//            });
//
//            builder.add("WeaverOfTime", "Weaver of Time", 25, 1250, 100, 1,7, 6, 3, tier -> {
//                return new GearAttributePrestigePower(100, 25, 1250, 1, ModGearAttributes.COOLDOWN_REDUCTION_CAP, 0.05F + (0.025F * tier));
//            });
//        });
    }

    public TieredSkill createTieredSkill(int unlockLevel, int learnPointCost, int regretPointCost, int numberOfTiers, IntFunction<? extends PrestigePower> tiers) {
        return new TieredSkill(unlockLevel, learnPointCost, 1, IntStream.range(0, numberOfTiers).mapToObj(tiers));
    }
}
