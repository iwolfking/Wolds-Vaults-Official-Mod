package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.FloatRangeEntry;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.objectives.data.alchemy.AlchemyTasks;

import java.util.List;

public class AlchemyObjectiveConfig extends Config {
    @Expose
    private List<AlchemyTask> tasks;

    @Expose
    private LevelEntryList<AlchemyObjectiveConfig.Entry> levels;

    @Override
    public String getName() {
        return "alchemy_objective";
    }


    @Override
    protected void reset() {
        levels = new LevelEntryList<>();

        levels.put(new Entry(
                0,
                WoldsVaults.id("empty"),
                WoldsVaults.id("empty"),
                WoldsVaults.id("empty"),
                WoldsVaults.id("empty"),
                0.5F,
                0.5F,
                0.5F,
                0.5F,
                new WeightedList<ItemStack>()
                        .add(new ItemStack(ModItems.ROTTEN_HEART), 1)
                        .add(new ItemStack(ModItems.ROTTEN_APPLE), 2)
                        .add(new ItemStack(ModItems.VERDANT_GLOBULE), 3)
                        .add(new ItemStack(ModItems.ERRATIC_EMBER), 2)
                        .add(new ItemStack(ModItems.REFINED_POWDER), 2)
                        .add(new ItemStack(ModItems.AURIC_CRYSTAL), 1),
                new WeightedList<ItemStack>()
                        .add(new ItemStack(ModItems.ROTTEN_HEART), 1)
                        .add(new ItemStack(ModItems.ROTTEN_APPLE), 2)
                        .add(new ItemStack(ModItems.VERDANT_GLOBULE), 3)
                        .add(new ItemStack(ModItems.ERRATIC_EMBER), 2)
                        .add(new ItemStack(ModItems.REFINED_POWDER), 2)
                        .add(new ItemStack(ModItems.AURIC_CRYSTAL), 1),
                List.of(new AlchemyTasks.MobEntry(new ItemStack(ModItems.SKILL_ORB_ITEM), EntityPredicate.TRUE)), // lol todo
                new WeightedList<ItemStack>()
                        .add(new ItemStack(ModItems.ROTTEN_HEART), 1)
                        .add(new ItemStack(ModItems.ROTTEN_APPLE), 2)
                        .add(new ItemStack(ModItems.VERDANT_GLOBULE), 3)
                        .add(new ItemStack(ModItems.ERRATIC_EMBER), 2)
                        .add(new ItemStack(ModItems.REFINED_POWDER), 2)
                        .add(new ItemStack(ModItems.AURIC_CRYSTAL), 1),
                0.1F,
                0.075F,
                0.05F,
                new FloatRangeEntry(-0.1F, 0.1F),
                -0.05F,
                -0.1F
                )
        );
    }

    public AlchemyObjectiveConfig.Entry getConfig(int vaultLevel) {
        return levels.getForLevel(vaultLevel).orElse(null);
    }

    public class Entry implements LevelEntryList.ILevelEntry {
        @Expose
        private final int level;

        @Expose
        private final ResourceLocation strongNegativeModifierPool;
        @Expose
        private final ResourceLocation negativeModifierPool;
        @Expose
        private final ResourceLocation positiveModifierPool;
        @Expose
        private final ResourceLocation strongPositiveModifierPool;

        @Expose
        private final float chestProbability;
        @Expose
        private final float coinProbability;
        @Expose
        private final float mobProbability;
        @Expose
        private final float oreProbabiltiy;

        @Expose
        private final WeightedList<ItemStack> chestIngredients;
        @Expose
        private final WeightedList<ItemStack> coinIngredients;
        @Expose
        private final List<AlchemyTasks.MobEntry> mobIngredientEntries;
        @Expose
        private final WeightedList<ItemStack> oreIngredients;

        @Expose
        private final float percentDeadlyIngredient;
        @Expose
        private final float percentRuthlessIngredient;
        @Expose
        private final float percentNeutralIngredient;
        @Expose
        private final FloatRangeEntry rangedPercentVolatileIngredient;
        @Expose
        private final float percentRefinedIngredient;
        @Expose
        private final float percentEmpoweredIngredient;





        public Entry(int level,
                     ResourceLocation strongNegativeModifierPool,
                     ResourceLocation negativeModifierPool,
                     ResourceLocation positiveModifierPool,
                     ResourceLocation strongPositiveModifierPool,
                     float chestProbability,
                     float coinProbability,
                     float mobProbability,
                     float oreProbabiltiy,
                     WeightedList<ItemStack> chestIngredients,
                     WeightedList<ItemStack> coinIngredients,
                     List<AlchemyTasks.MobEntry> mobIngredientEntries,
                     WeightedList<ItemStack> oreIngredients,
                     float percentDeadlyIngredient,
                     float percentRuthlessIngredient,
                     float percentNeutralIngredient,
                     FloatRangeEntry rangedPercentVolatileIngredient,
                     float percentRefinedIngredient,
                     float percentEmpoweredIngredient
        ) {
            this.level = level;
            this.strongNegativeModifierPool = strongNegativeModifierPool;
            this.negativeModifierPool = negativeModifierPool;
            this.positiveModifierPool = positiveModifierPool;
            this.strongPositiveModifierPool = strongPositiveModifierPool;
            this.chestProbability = chestProbability;
            this.coinProbability = coinProbability;
            this.mobProbability = mobProbability;
            this.oreProbabiltiy = oreProbabiltiy;
            this.chestIngredients = chestIngredients;
            this.coinIngredients = coinIngredients;
            this.mobIngredientEntries = mobIngredientEntries;
            this.oreIngredients = oreIngredients;
            this.percentDeadlyIngredient = percentDeadlyIngredient;
            this.percentRuthlessIngredient = percentRuthlessIngredient;
            this.percentNeutralIngredient = percentNeutralIngredient;
            this.rangedPercentVolatileIngredient = rangedPercentVolatileIngredient;
            this.percentRefinedIngredient = percentRefinedIngredient;
            this.percentEmpoweredIngredient = percentEmpoweredIngredient;
        }

        @Override
        public int getLevel() {
            return this.level;
        }


        public ResourceLocation getStrongPositiveModifierPool() {
            return strongPositiveModifierPool;
        }

        public ResourceLocation getPositiveModifierPool() {
            return positiveModifierPool;
        }

        public ResourceLocation getNegativeModifierPool() {
            return negativeModifierPool;
        }

        public ResourceLocation getStrongNegativeModifierPool() {
            return strongNegativeModifierPool;
        }

        public float getChestProbability() {
            return chestProbability;
        }

        public float getCoinProbability() {
            return coinProbability;
        }

        public float getMobProbability() {
            return mobProbability;
        }

        public float getOreProbabiltiy() {
            return oreProbabiltiy;
        }

        public WeightedList<ItemStack> getChestIngredients() {
            return chestIngredients;
        }

        public WeightedList<ItemStack> getCoinIngredients() {
            return coinIngredients;
        }

        public List<AlchemyTasks.MobEntry> getMobIngredientEntries() {
            return mobIngredientEntries;
        }

        public WeightedList<ItemStack> getOreIngredients() {
            return oreIngredients;
        }


        public String getFormattedPercentIngredient(AlchemyIngredientItem.AlchemyIngredientType type) {
            switch (type) {
                case DEADLY -> {
                    return String.format("%.1f", percentDeadlyIngredient * 100) + "%";
                }
                case RUTHLESS -> {
                    return String.format("%.1f", percentRuthlessIngredient * 100) + "%";
                }
                case NEUTRAL -> {
                    return String.format("%.1f", percentNeutralIngredient * 100) + "%";
                }
                case VOLATILE -> {
                    return "between " + String.format("%.1f", rangedPercentVolatileIngredient.getMin() * 100) + "% and " + String.format("%.1f", rangedPercentVolatileIngredient.getMax() * 100) + "%";
                }
                case EMPOWERED -> {
                    return String.format("%.1f", percentEmpoweredIngredient * 100) + "%";
                }
                case REFINED ->  {
                    return String.format("%.1f", percentRefinedIngredient * 100) + "%";
                }
            }
            return "What the hell did you do";
        }
    }
}
