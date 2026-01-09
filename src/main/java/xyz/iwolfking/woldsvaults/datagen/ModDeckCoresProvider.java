package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.card.DeckModifiersConfig;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.card.modifier.deck.GlobalDeckModifier;
import iskallia.vault.core.world.roll.FloatRoll;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.card.AbstractDeckCoreProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.modifiers.deck.AdjacencyBonusDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.EmptySlotDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.NitwitDeckModifier;

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
            arsenalModifierConfig.modifierRolls.put("lesser", variant("Lesser Arsenal Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/arsenal_deck_core#inventory"));
            arsenalModifierConfig.modifierRolls.put("greater", variant("Greater Arsenal Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/arsenal_deck_core#inventory"));

            GlobalDeckModifier.Config aegisModifierConfig = new GlobalDeckModifier.Config();
            aegisModifierConfig.requiredGroups.add("Defensive");
            aegisModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            aegisModifierConfig.modifierRolls.put("lesser", variant("Lesser Aegis Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/aegis_deck_core#inventory"));
            aegisModifierConfig.modifierRolls.put("greater", variant("Greater Aegis Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/aegis_deck_core#inventory"));


            GlobalDeckModifier.Config toolModifierConfig = new GlobalDeckModifier.Config();
            toolModifierConfig.requiredGroups.add("Utility");
            toolModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            toolModifierConfig.modifierRolls.put("lesser", variant("Lesser Tool Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/tool_deck_core#inventory"));
            toolModifierConfig.modifierRolls.put("greater", variant("Greater Tool Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/tool_deck_core#inventory"));

            GlobalDeckModifier.Config naturalModifierConfig = new GlobalDeckModifier.Config();
            naturalModifierConfig.requiredGroups.add("Physical");
            naturalModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            naturalModifierConfig.modifierRolls.put("lesser", variant("Lesser Natural Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/natural_deck_core#inventory"));
            naturalModifierConfig.modifierRolls.put("greater", variant("Greater Natural Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/natural_deck_core#inventory"));

            GlobalDeckModifier.Config faeModifierConfig = new GlobalDeckModifier.Config();
            faeModifierConfig.requiredGroups.add("Magical");
            faeModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.25F);
            faeModifierConfig.modifierRolls.put("lesser", variant("Lesser Fae Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/fae_deck_core#inventory"));
            faeModifierConfig.modifierRolls.put("greater", variant("Greater Fae Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/fae_deck_core#inventory"));

            EmptySlotDeckModifier.Config voidModifierConfig = new EmptySlotDeckModifier.Config();
            voidModifierConfig.modifierRoll = FloatRoll.ofUniform(0.05F, 0.1F);
            voidModifierConfig.modifierRolls.put("lesser", variant("Lesser Void Core", FloatRoll.ofUniform(0.05F, 0.1F), 13618375, "woldsvaults:deck_cores/void_deck_core#inventory"));
            voidModifierConfig.modifierRolls.put("greater", variant("Greater Void Core", FloatRoll.ofUniform(0.25F, 0.5F), 16769382, "woldsvaults:deck_cores/void_deck_core#inventory"));

            NitwitDeckModifier.Config nitwitModifierConfig = new NitwitDeckModifier.Config();
            nitwitModifierConfig.modifierRoll = FloatRoll.ofUniform(0.35F, 0.5F);
            nitwitModifierConfig.modifierRolls.put("lesser", variant("Lesser Nitwit Core", FloatRoll.ofUniform(0.02F, 0.35F), 13618375, "woldsvaults:deck_cores/nitwit_deck_core#inventory"));
            nitwitModifierConfig.modifierRolls.put("greater", variant("Greater Nitwit Core", FloatRoll.ofUniform(0.5F, 1.0F), 16769382, "woldsvaults:deck_cores/nitwit_deck_core#inventory"));

            AdjacencyBonusDeckModifier.Config bazaarDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.1F, 0.2F), List.of("Resource"), false, AdjacencyBonusDeckModifier.Type.SURROUNDING);
            bazaarDeckModifier.modifierRolls.put("lesser", variant("Lesser Bazaar Core", FloatRoll.ofUniform(0.02F, 0.35F), 13618375, "woldsvaults:deck_cores/bazaar_deck_core#inventory"));
            bazaarDeckModifier.modifierRolls.put("greater", variant("Greater Bazaar Core", FloatRoll.ofUniform(0.5F, 1.0F), 16769382, "woldsvaults:deck_cores/bazaar_deck_core#inventory"));


            builder.addCore("arsenal", GlobalDeckModifier::new, arsenalModifierConfig, "Arsenal Core", 13618375,"woldsvaults:deck_cores/arsenal_deck_core#inventory");
            builder.addCore("aegis", GlobalDeckModifier::new, aegisModifierConfig,"Aegis Core", 13618375,"woldsvaults:deck_cores/aegis_deck_core#inventory");
            builder.addCore("tool", GlobalDeckModifier::new, toolModifierConfig,"Tool Core", 13618375,"woldsvaults:deck_cores/tool_deck_core#inventory");
            builder.addCore("natural", GlobalDeckModifier::new, naturalModifierConfig,"Natural Core", 13618375,"woldsvaults:deck_cores/natural_deck_core#inventory");
            builder.addCore("fae", GlobalDeckModifier::new, faeModifierConfig,"Fae Core", 13618375,"woldsvaults:deck_cores/fae_deck_core#inventory");
            builder.addCore("void", EmptySlotDeckModifier::new, voidModifierConfig,"Void Core", 13618375,"woldsvaults:deck_cores/void_deck_core#inventory");
            builder.addCore("nitwit", NitwitDeckModifier::new, nitwitModifierConfig,"Nitwit Core", 13618375,"woldsvaults:deck_cores/nitwit_deck_core#inventory");
            builder.addCore("bazaar", AdjacencyBonusDeckModifier::new, bazaarDeckModifier,"Bazaar Core", 13618375,"woldsvaults:deck_cores/bazaar_deck_core#inventory");
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
