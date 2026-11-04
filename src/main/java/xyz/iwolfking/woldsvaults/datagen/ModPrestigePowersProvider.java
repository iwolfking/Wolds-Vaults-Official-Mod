package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.PrestigePowersConfig;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.PrestigeTree;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractPrestigePowerProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.SkillOverlay;
import xyz.iwolfking.woldsvaults.datagen.lib.SkillOverlay.Tier;

import java.util.ArrayList;
import java.util.List;

/**
 * The prestige powers the greed rework adds to the base mod's {@code prestige_powers.json}; VHAPI's prestige loader
 * appends them. The rework's edits to base powers are config patches in {@link ModConfigPatchesProvider}.
 */
public class ModPrestigePowersProvider extends AbstractPrestigePowerProvider {
    public static final String ADDITIONS_FILE = "greed_powers";

    protected ModPrestigePowersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        addConfig(ADDITIONS_FILE, config(additions()));
    }

    /** Prestige powers the greed rework adds, in tree order. */
    public static List<Skill> additions() {
        return List.of(
                power("ToolsmithII", "Echoing Toolsmith II", tier("tool_capacity_power").with("capacityIncrease", 75).costs(0, 1200, 1, 0).greedTier(8, 8).id("toolsmith_2")),
                power("SuperCrystalsII", "Crystalweaver's Gift II", tier("super_crystals_power").with("extraCapacity", 25).costs(0, 1400, 1, 0).greedTier(9, 9).id("super_crystals_2")),
                power("SuperCrystalsIII", "Crystalweaver's Gift III", tier("super_crystals_power").with("extraCapacity", 25).costs(0, 3200, 1, 0).greedTier(15, 15).id("super_crystals_3")),
                power("SpiritsHandII", "Spirit's Hand II", tier("reach_cap_power").with("reachIncrease", 1.0F).costs(0, 2800, 1, 0).greedTier(14, 14).id("spirits_hand_2")),
                power("FruityII", "Bloom of Time II", tier("gear_attribute_power").with("attribute", "the_vault:fruit_effectiveness").with("value", 0.5F).costs(0, 3200, 1, 0).greedTier(15, 15).id("fruity_2")),
                power("GodsDisciple", "God's Disciple", tier("god_experience_power").with("experienceIncrease", 0.2F).with("altarBaseMultiplier", 2.0F).costs(0, 800, 1, 0).greedTier(6, 6).id("gods_disciple_1")),
                power("GodsDiscipleII", "God's Disciple II", tier("god_experience_power").with("experienceIncrease", 0.15F).costs(0, 2000, 1, 0).greedTier(12, 12).id("gods_disciple_2")),
                power("RelicHunter", "Relic Hunter", tier("relic_hunter_power").with("chanceMultiplier", 0.5F).costs(0, 2000, 1, 0).greedTier(12, 12).id("relic_hunter_1")),
                power("RelicHunterII", "Relic Hunter II", tier("relic_hunter_power").with("chanceMultiplier", 0.5F).costs(0, 3200, 1, 0).greedTier(15, 15).id("relic_hunter_2")),
                power("LegendOfTime", "Legend of Time", tier("unreleased_power").costs(0, 25000, 1, 0).greedTier(16, 16).id("legend_of_time")),
                power("LegendOfPower", "Legend of Power", tier("unreleased_power").costs(0, 25000, 1, 0).greedTier(16, 16).id("legend_of_power")),
                power("LegendOfRiches", "Legend of Riches", tier("unreleased_power").costs(0, 25000, 1, 0).greedTier(16, 16).id("legend_of_riches"))
        );
    }

    /** The prestige config as VHAPI leaves it after appending this provider's powers to {@code base}. */
    public static PrestigePowersConfig withAdditions(PrestigePowersConfig base) {
        base.tree.skills.addAll(additions());
        return base;
    }

    private static PrestigePowersConfig config(List<Skill> skills) {
        PrestigePowersConfig config = new PrestigePowersConfig();
        config.tree = new PrestigeTree();
        config.tree.skills = new ArrayList<>(skills);
        return config;
    }

    private static Skill power(String id, String name, Tier... tiers) {
        return SkillOverlay.tiered(id, name, tiers);
    }

    private static Tier tier(String type) {
        return Tier.of(type);
    }
}
