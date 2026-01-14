package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.VaultMod;
import iskallia.vault.config.Config;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.item.crystal.objective.*;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.objectives.*;

import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimeTrialCompetitionConfig extends Config {
    @Expose
    public WeightedList<String> OBJECTIVE_WEIGHTS = new WeightedList<>();

    @Expose
    public Map<String, CrystalObjective> OBJECTIVE_ENTRIES = new LinkedHashMap<>();

    @Expose
    public DayOfWeek RESET_DAY_OF_WEEK;

    @Expose
    public ResourceLocation REWARD_CRATE_LOOT_TABLE;

    @Expose
    public boolean enabled;


    @Override
    public String getName() {
        return "time_trial_competition";
    }

    @Override
    protected void reset() {
        RESET_DAY_OF_WEEK = DayOfWeek.MONDAY;
        OBJECTIVE_WEIGHTS.add("elixir", 15);
        OBJECTIVE_WEIGHTS.add("enchanted_elixir", 5);
        OBJECTIVE_WEIGHTS.add("monolith", 10);
        OBJECTIVE_WEIGHTS.add("haunted_braziers", 5);
        OBJECTIVE_WEIGHTS.add("bingo", 10);
        OBJECTIVE_WEIGHTS.add("ballistic_bingo", 5);
        OBJECTIVE_WEIGHTS.add("brutal_bosses", 5);
        OBJECTIVE_WEIGHTS.add("zealot", 10);
        OBJECTIVE_WEIGHTS.add("alchemy", 10);
        OBJECTIVE_WEIGHTS.add("scavenger", 10);
        OBJECTIVE_WEIGHTS.add("unhinged_scavenger", 5);
        OBJECTIVE_WEIGHTS.add("rune_boss", 10);
        OBJECTIVE_ENTRIES.put("elixir", new ElixirCrystalObjective());
        OBJECTIVE_ENTRIES.put("enchanted_elixir", new EnchantedElixirCrystalObjective());
        OBJECTIVE_ENTRIES.put("monolith", new MonolithCrystalObjective());
        OBJECTIVE_ENTRIES.put("haunted_braziers", new HauntedBraziersCrystalObjective());
        OBJECTIVE_ENTRIES.put("bingo", new BingoCrystalObjective());
        OBJECTIVE_ENTRIES.put("ballistic_bingo", new BallisticBingoCrystalObjective());
        OBJECTIVE_ENTRIES.put("brutal_bosses", new BallisticBingoCrystalObjective());
        OBJECTIVE_ENTRIES.put("zealot", new ZealotCrystalObjective());
        OBJECTIVE_ENTRIES.put("alchemy", new AlchemyCrystalObjective());
        OBJECTIVE_ENTRIES.put("scavenger", new ScavengerCrystalObjective());
        OBJECTIVE_ENTRIES.put("unhinged_scavenger", new UnhingedScavengerCrystalObjective());
        OBJECTIVE_ENTRIES.put("rune_boss", new VaultRuneBossCrystalObjective());
        REWARD_CRATE_LOOT_TABLE = VaultMod.id("time_trial_reward_crate");
        this.enabled = true;
    }

    public String getRandomObjective() {
        return OBJECTIVE_WEIGHTS.getRandom().orElse("elixir");
    }
}
