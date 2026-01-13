package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.objective.ElixirCrystalObjective;

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


    @Override
    public String getName() {
        return "time_trial_competition";
    }

    @Override
    protected void reset() {
        RESET_DAY_OF_WEEK = DayOfWeek.MONDAY;
        OBJECTIVE_WEIGHTS.add("elixir", 1);
        OBJECTIVE_ENTRIES.put("elixir", new ElixirCrystalObjective());
    }

    public String getRandomObjective() {
        return OBJECTIVE_WEIGHTS.getRandom().orElse("elixir");
    }
}
