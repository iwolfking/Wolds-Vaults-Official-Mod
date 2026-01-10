package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.card.DeckModifiersConfig;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.card.modifier.deck.GlobalDeckModifier;
import iskallia.vault.core.card.modifier.deck.SlotDeckModifier;
import iskallia.vault.core.world.roll.FloatRoll;
import iskallia.vault.core.world.roll.IntRoll;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.card.AbstractDeckCoreProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.modifiers.deck.*;

import java.util.List;

public class ModDeckCoresProvider extends AbstractDeckCoreProvider {
    protected ModDeckCoresProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("new_cores", builder -> {
            GlobalDeckModifier.Config arsenalModifierConfig = new GlobalDeckModifier.Config();
            arsenalModifierConfig.requiredGroups.add("Offensive");
            arsenalModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            arsenalModifierConfig.modifierRolls.put("lesser", variant("Lesser Arsenal Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/arsenal_deck_core_lesser#inventory"));
            arsenalModifierConfig.modifierRolls.put("greater", variant("Greater Arsenal Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/arsenal_deck_core_greater#inventory"));

            GlobalDeckModifier.Config aegisModifierConfig = new GlobalDeckModifier.Config();
            aegisModifierConfig.requiredGroups.add("Defensive");
            aegisModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            aegisModifierConfig.modifierRolls.put("lesser", variant("Lesser Aegis Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/aegis_deck_core_lesser#inventory"));
            aegisModifierConfig.modifierRolls.put("greater", variant("Greater Aegis Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/aegis_deck_core_greater#inventory"));


            GlobalDeckModifier.Config toolModifierConfig = new GlobalDeckModifier.Config();
            toolModifierConfig.requiredGroups.add("Utility");
            toolModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            toolModifierConfig.modifierRolls.put("lesser", variant("Lesser Tool Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/tool_deck_core_lesser#inventory"));
            toolModifierConfig.modifierRolls.put("greater", variant("Greater Tool Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/tool_deck_core_greater#inventory"));

            GlobalDeckModifier.Config naturalModifierConfig = new GlobalDeckModifier.Config();
            naturalModifierConfig.requiredGroups.add("Physical");
            naturalModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            naturalModifierConfig.modifierRolls.put("lesser", variant("Lesser Natural Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/natural_deck_core_lesser#inventory"));
            naturalModifierConfig.modifierRolls.put("greater", variant("Greater Natural Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/natural_deck_core_greater#inventory"));

            GlobalDeckModifier.Config faeModifierConfig = new GlobalDeckModifier.Config();
            faeModifierConfig.requiredGroups.add("Magical");
            faeModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            faeModifierConfig.modifierRolls.put("lesser", variant("Lesser Fae Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/fae_deck_core_lesser#inventory"));
            faeModifierConfig.modifierRolls.put("greater", variant("Greater Fae Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/fae_deck_core_greater#inventory"));

            EmptySlotDeckModifier.Config voidModifierConfig = new EmptySlotDeckModifier.Config();
            voidModifierConfig.modifierRoll = FloatRoll.ofUniform(0.05F, 0.1F);
            voidModifierConfig.modifierRolls.put("lesser", variant("Lesser Void Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/void_deck_core_lesser#inventory"));
            voidModifierConfig.modifierRolls.put("greater", variant("Greater Void Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/void_deck_core_greater#inventory"));

            NitwitDeckModifier.Config nitwitModifierConfig = new NitwitDeckModifier.Config();
            nitwitModifierConfig.modifierRoll = FloatRoll.ofUniform(0.35F, 0.5F);
            nitwitModifierConfig.modifierRolls.put("lesser", variant("Lesser Nitwit Core", FloatRoll.ofUniform(0.02F, 0.35F), 13618375, "woldsvaults:deck_cores/nitwit_deck_core_lesser#inventory"));
            nitwitModifierConfig.modifierRolls.put("greater", variant("Greater Nitwit Core", FloatRoll.ofUniform(0.5F, 1.0F), 16769382, "woldsvaults:deck_cores/nitwit_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config bazaarDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.1F, 0.2F), List.of("Resource"), false, AdjacencyBonusDeckModifier.Type.SURROUNDING);
            bazaarDeckModifier.modifierRolls.put("lesser", variant("Lesser Bazaar Core", FloatRoll.ofUniform(0.02F, 0.35F), 13618375, "woldsvaults:deck_cores/bazaar_deck_core_lesser#inventory"));
            bazaarDeckModifier.modifierRolls.put("greater", variant("Greater Bazaar Core", FloatRoll.ofUniform(0.5F, 1.0F), 16769382, "woldsvaults:deck_cores/bazaar_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config temporalDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.3F, 0.5F), List.of("Temporal"), false, AdjacencyBonusDeckModifier.Type.DIAGONAL);
            temporalDeckModifier.modifierRolls.put("lesser", variant("Lesser Temporal Core", FloatRoll.ofUniform(0.15F, 0.3F), 13618375, "woldsvaults:deck_cores/temporal_deck_core_lesser#inventory"));
            temporalDeckModifier.modifierRolls.put("greater", variant("Greater Temporal Core", FloatRoll.ofUniform(0.5F, 1.0F), 16769382, "woldsvaults:deck_cores/temporal_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config talentDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.3F, 0.5F), List.of("Knack"), true, AdjacencyBonusDeckModifier.Type.SURROUNDING);
            talentDeckModifier.modifierRolls.put("lesser", variant("Lesser Talent Core", FloatRoll.ofUniform(0.15F, 0.3F), 13618375, "woldsvaults:deck_cores/talent_deck_core_lesser#inventory"));
            talentDeckModifier.modifierRolls.put("greater", variant("Greater Talent Core", FloatRoll.ofUniform(0.5F, 1.0F), 16769382, "woldsvaults:deck_cores/talent_deck_core_greater#inventory"));

            CreateGroupSlotDeckModifier.Config arcaneDeckModifier = new CreateGroupSlotDeckModifier.Config("Arcane");
            arcaneDeckModifier.slotRoll = IntRoll.ofUniform(1, 3);
            arcaneDeckModifier.modifierRoll = FloatRoll.ofConstant(0);
            arcaneDeckModifier.modifierRolls.put("lesser", variant("Lesser Arcane Core", FloatRoll.ofConstant(0), 13618375, "woldsvaults:deck_cores/arcane_deck_core_lesser#inventory"));
            arcaneDeckModifier.modifierRolls.put("greater", variant("Greater Arcane Core", FloatRoll.ofConstant(0), 16769382, "woldsvaults:deck_cores/arcane_deck_core_greater#inventory"));

            ArcaneSlotDeckModifier.Config adeptDeckModifier = new ArcaneSlotDeckModifier.Config();
            adeptDeckModifier.slotRoll = IntRoll.ofConstant(1);
            adeptDeckModifier.modifierRoll = FloatRoll.ofUniformedStep(1.0F, 4.0F, 1.0F);
            adeptDeckModifier.modifierRolls.put("lesser", variant("Lesser Adept Core", FloatRoll.ofUniformedStep(1.0F, 2.0F, 1.0F), 13618375, "woldsvaults:deck_cores/adept_deck_core_lesser#inventory"));
            adeptDeckModifier.modifierRolls.put("greater", variant("Greater Adept Core", FloatRoll.ofUniformedStep(3.0F, 5.0F, 1.0F), 16769382, "woldsvaults:deck_cores/adept_deck_core_greater#inventory"));

            DominanceDeckModifier.Config jupiterDeckModifier = new DominanceDeckModifier.Config(FloatRoll.ofUniform(0.1F, 0.2F), DominanceDeckModifier.Mode.DOMINANT);
            jupiterDeckModifier.modifierRolls.put("lesser", variant("Lesser Jupiter Core", FloatRoll.ofUniform(0.15F, 0.3F), 13618375, "woldsvaults:deck_cores/jupiter_deck_core_lesser#inventory"));
            jupiterDeckModifier.modifierRolls.put("greater", variant("Greater Jupiter Core", FloatRoll.ofUniform(0.5F, 1.0F), 16769382, "woldsvaults:deck_cores/jupiter_deck_core_greater#inventory"));

            DominanceDeckModifier.Config plutoDeckModifier = new DominanceDeckModifier.Config(FloatRoll.ofUniform(0.25F, 0.35F), DominanceDeckModifier.Mode.MINORITY);
            plutoDeckModifier.modifierRolls.put("lesser", variant("Lesser Pluto Core", FloatRoll.ofUniform(0.15F, 0.3F), 13618375, "woldsvaults:deck_cores/pluto_deck_core_lesser#inventory"));
            plutoDeckModifier.modifierRolls.put("greater", variant("Greater Pluto Core", FloatRoll.ofUniform(0.5F, 1.0F), 16769382, "woldsvaults:deck_cores/pluto_deck_core_greater#inventory"));


            builder.addCore("arsenal", GlobalDeckModifier::new, arsenalModifierConfig, "Arsenal Core", 13618375,"woldsvaults:deck_cores/arsenal_deck_core#inventory");
            builder.addCore("aegis", GlobalDeckModifier::new, aegisModifierConfig,"Aegis Core", 13618375,"woldsvaults:deck_cores/aegis_deck_core#inventory");
            builder.addCore("tool", GlobalDeckModifier::new, toolModifierConfig,"Tool Core", 13618375,"woldsvaults:deck_cores/tool_deck_core#inventory");
            builder.addCore("natural", GlobalDeckModifier::new, naturalModifierConfig,"Natural Core", 13618375,"woldsvaults:deck_cores/natural_deck_core#inventory");
            builder.addCore("fae", GlobalDeckModifier::new, faeModifierConfig,"Fae Core", 13618375,"woldsvaults:deck_cores/fae_deck_core#inventory");
            builder.addCore("void", EmptySlotDeckModifier::new, voidModifierConfig,"Void Core", 13618375,"woldsvaults:deck_cores/void_deck_core#inventory");
            builder.addCore("nitwit", NitwitDeckModifier::new, nitwitModifierConfig,"Nitwit Core", 13618375,"woldsvaults:deck_cores/nitwit_deck_core#inventory");
            builder.addCore("bazaar", AdjacencyBonusDeckModifier::new, bazaarDeckModifier,"Bazaar Core", 13618375,"woldsvaults:deck_cores/bazaar_deck_core#inventory");
            builder.addCore("temporal", AdjacencyBonusDeckModifier::new, temporalDeckModifier,"Temporal Core", 13618375,"woldsvaults:deck_cores/temporal_deck_core#inventory");
            builder.addCore("talent", AdjacencyBonusDeckModifier::new, talentDeckModifier,"Talent Core", 13618375,"woldsvaults:deck_cores/talent_deck_core#inventory");
            builder.addCore("arcane", CreateGroupSlotDeckModifier::new, arcaneDeckModifier,"Arcane Core", 13618375,"woldsvaults:deck_cores/arcane_deck_core#inventory");
            builder.addCore("adept", ArcaneSlotDeckModifier::new, adeptDeckModifier,"Adept Core", 13618375,"woldsvaults:deck_cores/adept_deck_core#inventory");
            builder.addCore("jupiter", DominanceDeckModifier::new, jupiterDeckModifier,"Jupiter Core", 13618375,"woldsvaults:deck_cores/jupiter_deck_core#inventory");
            builder.addCore("pluto", DominanceDeckModifier::new, plutoDeckModifier,"Pluto Core", 13618375,"woldsvaults:deck_cores/pluto_deck_core#inventory");
        });
    }

    public DeckModifier.Config.RollVariant variant(String name, FloatRoll roll, int colour, String modelId) {
        DeckModifier.Config.RollVariant variant = new DeckModifier.Config.RollVariant();
        variant.roll = roll;
        variant.colour = colour;
        variant.modelId = modelId;
        variant.name = name;
        return variant;
    }
}
