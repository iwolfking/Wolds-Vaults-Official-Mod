package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.client.gui.helper.SkillFrame;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.datagen.lib.WoldsTalentStyleProvider;

import java.util.List;

public class ModTalentStyleProvider extends WoldsTalentStyleProvider {
    public ModTalentStyleProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    public void registerConfigs() {
        LayoutBuilder builder = new LayoutBuilder();

        int startX = -332;
        int coreY = 0;
        int advancedY = DEFAULT_Y_SPACING;
        int specialisationY = advancedY + DEFAULT_Y_SPACING;
        int greaterY = specialisationY + DEFAULT_Y_SPACING;

        //Core Row
        builder.addRow(startX, coreY, SkillFrame.RECTANGULAR, List.of(
                new TalentData("Strength", "the_vault:gui/skills/strength"),
                new TalentData("Speed", "the_vault:gui/skills/speed"),
                new TalentData("Haste", "the_vault:gui/skills/haste"),
                new TalentData("Trap_Disarm", "the_vault:gui/skills/trap_disarm"),
                new EmptySpace(DEFAULT_X_SPACING),
                new TalentData("Stoneskin", "the_vault:gui/skills/stone_skin"),
                new TalentData("Heart_Fragments", "the_vault:gui/skills/well_fit"),
                new TalentData("Medic", "woldsvaults:gui/skills/medic"),
                new TalentData("Farmer_Twerker", "the_vault:gui/skills/farmer"),
                new TalentData("Lingering_Fumes", "the_vault:gui/skills/lingering_fumes"),
                new TalentData("Intelligence", "the_vault:gui/skills/intelligence"),
                new TalentData("Ethereal", "the_vault:gui/skills/ethereal_ability"),
                new TalentData("Quickening", "the_vault:gui/skills/quickening"),
                new TalentData("Treasure_Seeker", "the_vault:gui/skills/treasure_seeker"),
                new TalentData("Bountiful_Harvest", "the_vault:gui/skills/bountiful_harvest")
        ));

        //Advanced Row
        builder.addRowBelow(startX, coreY, SkillFrame.RECTANGULAR, List.of(
                new EmptySpace(DEFAULT_X_SPACING),//
                new EmptySpace(DEFAULT_X_SPACING),//
                new TalentData("Ransack", "woldsvaults:gui/skills/ransack"),
                new TalentData("Depleted", "the_vault:gui/skills/depleted"),
                new TalentData("Daze", "the_vault:gui/skills/dazed"),
                new TalentData("Purist", "the_vault:gui/skills/purist"),
                new TalentData("Healthy_Harvest", "woldsvaults:gui/skills/supply_crates"),
                new TalentData("Mana_Harvest", "the_vault:gui/skills/discipline"),
                new TalentData("Executioner", "the_vault:gui/skills/executioner"),
                new TalentData("Voltaic_Impact", "the_vault:gui/skills/voltaic_impact"),
                new EmptySpace(DEFAULT_X_SPACING),//
                new TalentData("Prudent", "the_vault:gui/skills/potion_amount"),
                new TalentData("Witchery", "the_vault:gui/skills/witchery"),
                new TalentData("Sorcery", "the_vault:gui/skills/sorcery"),
                new TalentData("Cleave", "the_vault:gui/skills/cleave")

        ));

        //Specialisation Row
        builder.addRowBelow(startX, advancedY, SkillFrame.RECTANGULAR, List.of(
                new EmptySpace(DEFAULT_X_SPACING),//
                new TalentData("Momentum_Engine", "woldsvaults:gui/skills/momentum_engine"),
                new TalentData("Clean_Escape", "the_vault:gui/skills/clean_escape"),
                new TalentData("Methodical", "the_vault:gui/skills/methodical"),
                new TalentData("Nucleus", "the_vault:gui/skills/nucleus"),
                new TalentData("Fatal_Strike", "the_vault:gui/skills/fatal_strike_damage"),
                new TalentData("Life_Steal", "the_vault:gui/skills/life_steal"),
                new TalentData("Mana_Steal", "the_vault:gui/skills/mana_steal"),
                new TalentData("Blizzard", "the_vault:gui/skills/blizzard"),
                new TalentData("Lightning_Damage", "the_vault:gui/skills/lightning_damage"),
                new TalentData("Mind_Meld", "woldsvaults:gui/skills/mind_meld"),
                new ChoiceGroup(List.of(new TalentData("Potent_Elixir", "woldsvaults:gui/skills/potent_elixir"), new TalentData("Healthy_Elixir", "woldsvaults:gui/skills/healthy_elixir"))),
                new EmptySpace(DEFAULT_X_SPACING),
                new TalentData("Arcana", "the_vault:gui/skills/arcana"),
                new TalentData("Hexbreaker", "the_vault:gui/skills/hexbreaker")
        ));

        //Greater Row
        builder.addRowBelow(startX, specialisationY, SkillFrame.RECTANGULAR, List.of(
                new TalentData("Lunge", "woldsvaults:gui/skills/lunge"),
                new EmptySpace(DEFAULT_X_SPACING),//
                new TalentData("Frenzy", "the_vault:gui/skills/frenzy"),
                new TalentData("Blood_Rush", "the_vault:gui/skills/bloodrush"),
                new TalentData("Toxic_Reaction", "the_vault:gui/skills/toxic_reaction"),
                new EmptySpace(DEFAULT_X_SPACING),//
                new EmptySpace(DEFAULT_X_SPACING),//
                new EmptySpace(DEFAULT_X_SPACING),//
                new TalentData("Frostbite", "the_vault:gui/skills/frostbite"),
                new TalentData("Lightning_Stun", "the_vault:gui/skills/lightning_stunned"),
                new TalentData("Prime_Amplification", "the_vault:gui/skills/prime_amplification"),
                new TalentData("Dungeon_Mastery", "the_vault:gui/skills/dungeon_mastery"),
                new TalentData("Horde_Mastery", "the_vault:gui/skills/horde_mastery"),
                new TalentData("Assassin_Mastery", "the_vault:gui/skills/assassin_mastery"),
                new TalentData("Blood_Chakra", "woldsvaults:gui/skills/blood_chakra")
        ));

        //Finale Row
        builder.addRowBelow(startX, greaterY, SkillFrame.RECTANGULAR, List.of(
                new TalentData("Battle_Trance", "the_vault:gui/skills/battle_trance"),
                new TalentData("Blazing", "the_vault:gui/skills/blazing"),
                new TalentData("Berserking", "the_vault:gui/skills/berserking"),
                new TalentData("Last_Stand", "the_vault:gui/skills/last_stand"),
                new TalentData("Blight", "the_vault:gui/skills/blight"),
                new TalentData("Execution_Strike", "woldsvaults:gui/skills/execution_strike"),
                new TalentData("Fanged_Strike", "woldsvaults:gui/skills/fanged_strike"),
                new TalentData("Arcane_Strike", "woldsvaults:gui/skills/arcane_strike"),
                new TalentData("Lucky_Momentum", "the_vault:gui/skills/luck_momentum"),
                new TalentData("Hunters_Instinct", "the_vault:gui/skills/hunters_instinct"),
                new TalentData("Stack_Master", "woldsvaults:gui/skills/stack_master"),
                new EmptySpace(DEFAULT_X_SPACING),
                new TalentData("Champion_Mastery", "the_vault:gui/skills/champion_mastery"),
                new TalentData("Treasure_Sense", "the_vault:gui/skills/treasure_hunter")
        ));
        this.addConfig("overwrite/wolds_talents", builder.build());
    }
}
