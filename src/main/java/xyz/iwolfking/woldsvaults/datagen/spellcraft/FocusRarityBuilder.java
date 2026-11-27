package xyz.iwolfking.woldsvaults.datagen.spellcraft;

import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;

/**
 * How items of one gear rarity roll. Unless given, the focus-only budget multiplier stays unset (no multiplier) and so
 * does the budget roll range (the shared range applies); {@link #register} refuses a rarity without a line count.
 */
public final class FocusRarityBuilder {
    private final String rarity;
    private final float budget;
    private Float focusBudgetMult;
    private Integer linesMin;
    private Integer linesMax;
    private Float budgetRollMin;
    private Float budgetRollMax;

    public FocusRarityBuilder(String rarity, float budget) {
        this.rarity = rarity;
        this.budget = budget;
    }

    public FocusRarityBuilder focusBudgetMult(float focusBudgetMult) {
        this.focusBudgetMult = focusBudgetMult;
        return this;
    }

    public FocusRarityBuilder lines(int linesMin, int linesMax) {
        this.linesMin = linesMin;
        this.linesMax = linesMax;
        return this;
    }

    public FocusRarityBuilder budgetRoll(float budgetRollMin, float budgetRollMax) {
        this.budgetRollMin = budgetRollMin;
        this.budgetRollMax = budgetRollMax;
        return this;
    }

    public void register(SpellcraftConfig.FocusRolls focus) {
        if (this.linesMin == null) {
            throw new IllegalStateException("Focus rarity " + this.rarity + " has no line count");
        }
        SpellcraftConfig.RarityRolls rolls = new SpellcraftConfig.RarityRolls();
        rolls.budget = this.budget;
        rolls.focusBudgetMult = this.focusBudgetMult;
        rolls.linesMin = this.linesMin;
        rolls.linesMax = this.linesMax;
        rolls.budgetRollMin = this.budgetRollMin;
        rolls.budgetRollMax = this.budgetRollMax;
        focus.rarities.put(this.rarity, rolls);
    }
}
