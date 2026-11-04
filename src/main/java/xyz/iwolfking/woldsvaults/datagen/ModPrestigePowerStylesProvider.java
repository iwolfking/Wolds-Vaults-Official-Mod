package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.client.gui.helper.SkillFrame;
import iskallia.vault.config.PrestigePowersGUIConfig;
import iskallia.vault.config.entry.PrestigeHeaderStyle;
import iskallia.vault.config.entry.SkillStyle;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractPrestigePowerStyleProvider;
import xyz.iwolfking.vhapi.mixin.accessors.PrestigePowersGUIConfigAccessor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PrestigePowersGUIConfigHeadersAccessor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The full prestige power screen layout for the greed rework. The greed ladder has one header per greed rank, and
 * headers can only change through VHAPI's "overwrite" path, so this file replaces the base mod's
 * {@code prestige_powers_gui_styles.json} whole, including the base powers' styles.
 */
public class ModPrestigePowerStylesProvider extends AbstractPrestigePowerStyleProvider {
    public static final String OVERWRITE_FILE = "overwrite/greed_prestige_powers";

    private static final int HEADER_COLUMNS = 3;
    private static final int HEADER_LEFT = -150;
    private static final int HEADER_TOP = -22;
    private static final int HEADER_COLUMN_STEP = 130;
    private static final int HEADER_ROW_STEP = 72;
    private static final int HEADER_WIDTH = 100;
    private static final int HEADER_HEIGHT = 16;
    private static final int HEADER_COLOR = 0xFF808080;
    private static final int HEADER_TEXT_COLOR = 0xFFFFFFFF;
    private static final int HEADER_CONTAINER_HEIGHT = 50;

    protected ModPrestigePowerStylesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        PrestigePowersGUIConfig config = new PrestigePowersGUIConfig();
        ((PrestigePowersGUIConfigAccessor) config).setStyles(styles());
        ((PrestigePowersGUIConfigHeadersAccessor) config).setHeaders(headers());
        addConfig(OVERWRITE_FILE, config);
    }

    /** One header per greed rank, named from the rank ladder and laid out three ranks to a row. */
    private static List<PrestigeHeaderStyle> headers() {
        List<PrestigeHeaderStyle> headers = new ArrayList<>();
        for (int rank = MilestoneRankLadder.FIRST_RANK; rank <= MilestoneRankLadder.LEGEND_RANK; rank++) {
            int index = rank - MilestoneRankLadder.FIRST_RANK;
            headers.add(new PrestigeHeaderStyle(ModLanguageProvider.rankName(rank), HEADER_LEFT + HEADER_COLUMN_STEP * (index % HEADER_COLUMNS),
                    HEADER_TOP + HEADER_ROW_STEP * (index / HEADER_COLUMNS), HEADER_WIDTH, HEADER_HEIGHT, HEADER_COLOR,
                    HEADER_TEXT_COLOR, HEADER_CONTAINER_HEIGHT));
        }
        return headers;
    }

    private static LinkedHashMap<String, SkillStyle> styles() {
        LinkedHashMap<String, SkillStyle> styles = new LinkedHashMap<>();
        style(styles, "BountyCooldownReduction", -130, 10, "bountymaster");
        style(styles, "EnderAnchorPower", -100, 10, "the_anchor");
        style(styles, "FallImmunity", -70, 10, "the_elvish");
        style(styles, "Toolsmith", 0, 10, "toolsmith");
        style(styles, "KineticImmunity", 30, 10, "no_kinetic_damage");
        style(styles, "Shielded", 60, 10, "shielded");
        style(styles, "PickupRange", 130, 10, "magnet_range");
        style(styles, "StepUpPower", 160, 10, "the_frog");
        style(styles, "GluttonyPower", 190, 10, "the_gluttony");
        style(styles, "GearDurabilityPower", -130, 82, "no_gear_durability");
        style(styles, "NoVoidUses", -100, 82, "necklace");
        style(styles, "BlackMarketRerolls", -70, 82, "blackmarket");
        style(styles, "NoCharmsUses", 0, 82, "no_charm_use");
        style(styles, "CrateTier", 30, 82, "bountiful_crates");
        style(styles, "Artificer", 60, 82, "artificer");
        style(styles, "SuperCrystals", 130, 82, "super_crystals");
        style(styles, "GodsDisciple", 160, 82, "the_spark");
        style(styles, "SoulFetcher", 190, 82, "soul_fetcher");
        style(styles, "MythicBounty", -130, 154, "mythic_bounty_hunter");
        style(styles, "TreasureHunterPower", -100, 154, "treasure");
        style(styles, "SpiritsHand", -70, 154, "spirits_hand");
        style(styles, "ToolsmithII", 0, 154, "toolsmith");
        style(styles, "NoTrinketUses", 30, 154, "no_trinket_use");
        style(styles, "TemporalShardChance", 60, 154, "temporal");
        style(styles, "SuperCrystalsII", 130, 154, "super_crystals");
        style(styles, "ChampionsDamage", 160, 154, "champion");
        style(styles, "CommanderPower", 190, 154, "commander");
        style(styles, "BlackMarketer", -130, 226, "black_marketer");
        style(styles, "ToolDurabilityPower", -100, 226, "no_tools_durability");
        style(styles, "MovementSpeedMultiplier", -70, 226, "swiftness_amplified");
        style(styles, "OriginalWardPower", 0, 226, "ward");
        style(styles, "BerserkPower", 30, 226, "berserk");
        style(styles, "HealthIncrease", 60, 226, "dryad");
        style(styles, "Fruity", 130, 226, "fruity");
        style(styles, "GodsDiscipleII", 160, 226, "the_petal");
        style(styles, "RelicHunter", 190, 226, "the_carapace");
        style(styles, "PrismaticPouch", -130, 298, "prism_possessor");
        style(styles, "BarrierOfResilience", -100, 298, "barrier_of_resilience");
        style(styles, "MasterfulPower", -70, 298, "masterful");
        style(styles, "WeaverOfTime", 0, 298, "weaver_of_time");
        style(styles, "SpiritsHandII", 30, 298, "spirits_hand");
        style(styles, "ShieldOfLastingGuard", 60, 298, "shield_of_lasting_guard");
        style(styles, "FruityII", 130, 298, "fruity");
        style(styles, "SuperCrystalsIII", 160, 298, "super_crystals");
        style(styles, "RelicHunterII", 190, 298, "the_shadow");
        style(styles, "LegendOfTime", -130, 370, "the_wings");
        style(styles, "LegendOfPower", -100, 370, "triple_jump");
        style(styles, "LegendOfRiches", -70, 370, "chain_miner");
        return styles;
    }

    private static void style(LinkedHashMap<String, SkillStyle> styles, String power, int x, int y, String icon) {
        styles.put(power, new SkillStyle(x, y, VaultMod.id("gui/prestige/" + icon), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/" + icon + "_inactive")));
    }
}
