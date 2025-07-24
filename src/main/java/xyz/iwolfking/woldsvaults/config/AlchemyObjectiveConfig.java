package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.VaultMod;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.FloatRangeEntry;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;

public class AlchemyObjectiveConfig extends Config {

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
                VaultMod.id("alchemy_strong_negative"),
                VaultMod.id("alchemy_negative"),
                VaultMod.id("alchemy_positive"),
                VaultMod.id("alchemy_strong_positive"),
                0.5F,
                0.005F,
                0.005F,
                new WeightedList<ItemStack>()
                        .add(new ItemStack(ModItems.ROTTEN_HEART), 1) // DEADLY
                        .add(new ItemStack(ModItems.ROTTEN_APPLE), 4) // RUTHLESS
                        .add(new ItemStack(ModItems.VERDANT_GLOBULE), 8) // NEUTRAL
                        .add(new ItemStack(ModItems.ERRATIC_EMBER), 2) // VOLATILE
                        .add(new ItemStack(ModItems.REFINED_POWDER), 4) // REFINED
                        .add(new ItemStack(ModItems.AURIC_CRYSTAL), 1), // EMPOWERED
                new WeightedList<ItemStack>()
                        .add(new ItemStack(ModItems.ROTTEN_HEART), 1)
                        .add(new ItemStack(ModItems.ROTTEN_APPLE), 4)
                        .add(new ItemStack(ModItems.VERDANT_GLOBULE), 8)
                        .add(new ItemStack(ModItems.ERRATIC_EMBER), 2)
                        .add(new ItemStack(ModItems.REFINED_POWDER), 4)
                        .add(new ItemStack(ModItems.AURIC_CRYSTAL), 1),
                new WeightedList<ItemStack>()
                        .add(new ItemStack(ModItems.ROTTEN_HEART), 1)
                        .add(new ItemStack(ModItems.ROTTEN_APPLE), 4)
                        .add(new ItemStack(ModItems.VERDANT_GLOBULE), 8)
                        .add(new ItemStack(ModItems.ERRATIC_EMBER), 2)
                        .add(new ItemStack(ModItems.REFINED_POWDER), 4)
                        .add(new ItemStack(ModItems.AURIC_CRYSTAL), 1),
                0.30F,
                0.10F,
                0.05F,
                new FloatRangeEntry(-0.3F, 0.3F),
                -0.10F,
                -0.30F
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
        private final float oreProbabiltiy;

        @Expose
        private final WeightedList<ItemStack> chestIngredients;
        @Expose
        private final WeightedList<ItemStack> coinIngredients;
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
                     float oreProbabiltiy,
                     WeightedList<ItemStack> chestIngredients,
                     WeightedList<ItemStack> coinIngredients,
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
            this.oreProbabiltiy = oreProbabiltiy;
            this.chestIngredients = chestIngredients;
            this.coinIngredients = coinIngredients;
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

        public float getOreProbabiltiy() {
            return oreProbabiltiy;
        }

        public WeightedList<ItemStack> getChestIngredients() {
            return chestIngredients;
        }

        public WeightedList<ItemStack> getCoinIngredients() {
            return coinIngredients;
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

        public BrewingAltarTileEntity.PercentageResult getPercentageForType(AlchemyIngredientItem item) {
            return switch (item.getType()) {
                case DEADLY -> new BrewingAltarTileEntity.PercentageResult(percentDeadlyIngredient, percentDeadlyIngredient);
                case RUTHLESS -> new BrewingAltarTileEntity.PercentageResult(percentRuthlessIngredient, percentRuthlessIngredient);
                case NEUTRAL -> new BrewingAltarTileEntity.PercentageResult(percentNeutralIngredient, percentNeutralIngredient);
                case EMPOWERED -> new BrewingAltarTileEntity.PercentageResult(percentEmpoweredIngredient, percentEmpoweredIngredient);
                case REFINED -> new BrewingAltarTileEntity.PercentageResult(percentRefinedIngredient, percentRefinedIngredient);
                case VOLATILE -> new BrewingAltarTileEntity.PercentageResult(rangedPercentVolatileIngredient.getMin(), rangedPercentVolatileIngredient.getMax());
            };
        }
    }
}
