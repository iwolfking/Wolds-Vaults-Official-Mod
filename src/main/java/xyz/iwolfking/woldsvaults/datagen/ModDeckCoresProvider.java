package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.card.DeckModifiersConfig;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardNeighborType;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.card.modifier.deck.GlobalDeckModifier;
import iskallia.vault.core.card.modifier.deck.SlotDeckModifier;
import iskallia.vault.core.card.modifier.deck.StatEfficiencyDeckModifier;
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
import java.util.Set;

public class ModDeckCoresProvider extends AbstractDeckCoreProvider {
    protected ModDeckCoresProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("new_cores", builder -> {
            GlobalDeckModifier.Config arsenalModifierConfig = new GlobalDeckModifier.Config();
            arsenalModifierConfig.requiredGroups.add("Offensive");
            arsenalModifierConfig.modifierRoll = FloatRoll.ofUniform(0.4F, 0.6F);
            arsenalModifierConfig.modifierRolls.put("lesser", variant("Lesser Arsenal Core", FloatRoll.ofUniform(0.3F, 0.3F), 13618375, "woldsvaults:deck_cores/arsenal_deck_core_lesser#inventory"));
            arsenalModifierConfig.modifierRolls.put("greater", variant("Greater Arsenal Core", FloatRoll.ofUniform(0.75F, 1.25F), 16769382, "woldsvaults:deck_cores/arsenal_deck_core_greater#inventory"));

            GlobalDeckModifier.Config aegisModifierConfig = new GlobalDeckModifier.Config();
            aegisModifierConfig.requiredGroups.add("Defensive");
            aegisModifierConfig.modifierRoll = FloatRoll.ofUniform(0.3F, 0.6F);
            aegisModifierConfig.modifierRolls.put("lesser", variant("Lesser Aegis Core", FloatRoll.ofUniform(0.3F, 0.3F), 13618375, "woldsvaults:deck_cores/aegis_deck_core_lesser#inventory"));
            aegisModifierConfig.modifierRolls.put("greater", variant("Greater Aegis Core", FloatRoll.ofUniform(0.75F, 1.25F), 16769382, "woldsvaults:deck_cores/aegis_deck_core_greater#inventory"));

            GlobalDeckModifier.Config toolModifierConfig = new GlobalDeckModifier.Config();
            toolModifierConfig.requiredGroups.add("Utility");
            toolModifierConfig.modifierRoll = FloatRoll.ofUniform(0.35F, 0.6F);
            toolModifierConfig.modifierRolls.put("lesser", variant("Lesser Tool Core", FloatRoll.ofUniform(0.3F, 0.3F), 13618375, "woldsvaults:deck_cores/tool_deck_core_lesser#inventory"));
            toolModifierConfig.modifierRolls.put("greater", variant("Greater Tool Core", FloatRoll.ofUniform(0.75F, 1.25F), 16769382, "woldsvaults:deck_cores/tool_deck_core_greater#inventory"));

            GlobalDeckModifier.Config naturalModifierConfig = new GlobalDeckModifier.Config();
            naturalModifierConfig.requiredGroups.add("Physical");
            naturalModifierConfig.modifierRoll = FloatRoll.ofUniform(0.35F, 0.6F);
            naturalModifierConfig.modifierRolls.put("lesser", variant("Lesser Natural Core", FloatRoll.ofUniform(0.3F, 0.3F), 13618375, "woldsvaults:deck_cores/natural_deck_core_lesser#inventory"));
            naturalModifierConfig.modifierRolls.put("greater", variant("Greater Natural Core", FloatRoll.ofUniform(0.75F, 1.25F), 16769382, "woldsvaults:deck_cores/natural_deck_core_greater#inventory"));

            GlobalDeckModifier.Config faeModifierConfig = new GlobalDeckModifier.Config();
            faeModifierConfig.requiredGroups.add("Magical");
            faeModifierConfig.modifierRoll = FloatRoll.ofUniform(0.3F, 0.6F);
            faeModifierConfig.modifierRolls.put("lesser", variant("Lesser Fae Core", FloatRoll.ofUniform(0.3F, 0.3F), 13618375, "woldsvaults:deck_cores/fae_deck_core_lesser#inventory"));
            faeModifierConfig.modifierRolls.put("greater", variant("Greater Fae Core", FloatRoll.ofUniform(0.75F, 1.25F), 16769382, "woldsvaults:deck_cores/fae_deck_core_greater#inventory"));

            GlobalDeckModifier.Config sparklingDeckCore = new GlobalDeckModifier.Config();
            sparklingDeckCore.requiredGroups.add("Shiny");
            sparklingDeckCore.modifierRoll = FloatRoll.ofUniform(0.6F, 0.9F);
            sparklingDeckCore.modifierRolls.put("lesser", variant("Lesser Sparkling Core", FloatRoll.ofUniform(0.4F, 0.4F), 13618375, "woldsvaults:deck_cores/sparkling_deck_core_lesser#inventory"));
            sparklingDeckCore.modifierRolls.put("greater", variant("Greater Sparkling Core", FloatRoll.ofUniform(1.0F, 1.5F), 16769382, "woldsvaults:deck_cores/sparkling_deck_core_greater#inventory"));

            EmptySlotDeckModifier.Config voidModifierConfig = new EmptySlotDeckModifier.Config();
            voidModifierConfig.modifierRoll = FloatRoll.ofUniform(0.1F, 0.2F);
            voidModifierConfig.modifierRolls.put("lesser", variant("Lesser Void Core", FloatRoll.ofUniform(0.1F, 0.1F), 13618375, "woldsvaults:deck_cores/void_deck_core_lesser#inventory"));
            voidModifierConfig.modifierRolls.put("greater", variant("Greater Void Core", FloatRoll.ofUniform(0.2F, 0.3F), 16769382, "woldsvaults:deck_cores/void_deck_core_greater#inventory"));

            NitwitDeckModifier.Config nitwitModifierConfig = new NitwitDeckModifier.Config();
            nitwitModifierConfig.modifierRoll = FloatRoll.ofUniform(0.75F, 1.25F);
            nitwitModifierConfig.modifierRolls.put("lesser", variant("Lesser Nitwit Core", FloatRoll.ofUniform(0.45F, 0.45F), 13618375, "woldsvaults:deck_cores/nitwit_deck_core_lesser#inventory"));
            nitwitModifierConfig.modifierRolls.put("greater", variant("Greater Nitwit Core", FloatRoll.ofUniform(1.25F, 2.5F), 16769382, "woldsvaults:deck_cores/nitwit_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config bazaarDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.2F, 0.35F), List.of("Resource"), false, AdjacencyBonusDeckModifier.Type.SURROUNDING);
            bazaarDeckModifier.modifierRolls.put("lesser", variant("Lesser Bazaar Core", FloatRoll.ofUniform(0.15F, 0.15F), 13618375, "woldsvaults:deck_cores/bazaar_deck_core_lesser#inventory"));
            bazaarDeckModifier.modifierRolls.put("greater", variant("Greater Bazaar Core", FloatRoll.ofUniform(0.35F, 0.5F), 16769382, "woldsvaults:deck_cores/bazaar_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config temporalDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.7F, 1.0F), List.of("Temporal"), false, AdjacencyBonusDeckModifier.Type.STARCROSS);
            temporalDeckModifier.modifierRolls.put("lesser", variant("Lesser Temporal Core", FloatRoll.ofUniform(0.35F, 0.35F), 13618375, "woldsvaults:deck_cores/temporal_deck_core_lesser#inventory"));
            temporalDeckModifier.modifierRolls.put("greater", variant("Greater Temporal Core", FloatRoll.ofUniform(1.0F, 2.0F), 16769382, "woldsvaults:deck_cores/temporal_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config talentDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.2F, 0.3F), List.of("Knack"), true, AdjacencyBonusDeckModifier.Type.SURROUNDING);
            talentDeckModifier.modifierRolls.put("lesser", variant("Lesser Talent Core", FloatRoll.ofUniform(0.15F, 0.15F), 13618375, "woldsvaults:deck_cores/talent_deck_core_lesser#inventory"));
            talentDeckModifier.modifierRolls.put("greater", variant("Greater Talent Core", FloatRoll.ofUniform(0.3F, 0.5F), 16769382, "woldsvaults:deck_cores/talent_deck_core_greater#inventory"));

            CreateGroupSlotDeckModifier.Config arcaneDeckModifier = new CreateGroupSlotDeckModifier.Config("Arcane");
            arcaneDeckModifier.slotRoll = IntRoll.ofUniform(2, 3);
            arcaneDeckModifier.modifierRoll = FloatRoll.ofConstant(0);
            arcaneDeckModifier.slotRolls.put("lesser", slotVariant("Lesser Arcane Core", IntRoll.ofConstant(2), FloatRoll.ofConstant(0), 13618375, "woldsvaults:deck_cores/arcane_deck_core_lesser#inventory", Set.of(), Set.of()));
            arcaneDeckModifier.slotRolls.put("greater", slotVariant("Greater Arcane Core", IntRoll.ofUniform(4, 5), FloatRoll.ofConstant(0), 16769382, "woldsvaults:deck_cores/arcane_deck_core_greater#inventory", Set.of(), Set.of()));

            CreateSlotDeckModifier.Config constructionCoreModifier = new CreateSlotDeckModifier.Config("");
            constructionCoreModifier.slotRoll = IntRoll.ofUniform(2, 3);
            constructionCoreModifier.slotRolls.put("lesser", slotVariant("Lesser Construction Core", IntRoll.ofConstant(1), FloatRoll.ofConstant(0), 13618375, "woldsvaults:deck_cores/construction_deck_core_lesser#inventory", Set.of(), Set.of()));
            constructionCoreModifier.slotRolls.put("greater", slotVariant("Greater Construction Core", IntRoll.ofUniform(4, 5), FloatRoll.ofConstant(0), 13618375, "woldsvaults:deck_cores/construction_deck_core_greater#inventory", Set.of(), Set.of()));
            constructionCoreModifier.modifierRoll = FloatRoll.ofConstant(0);

            ArcaneSlotDeckModifier.Config adeptDeckModifier = new ArcaneSlotDeckModifier.Config();
            adeptDeckModifier.slotRoll = IntRoll.ofConstant(1);
            adeptDeckModifier.modifierRoll = FloatRoll.ofUniformedStep(2.0F, 4.0F, 1.0F);
            adeptDeckModifier.modifierRolls.put("lesser", variant("Lesser Adept Core", FloatRoll.ofUniformedStep(2.0F, 2.0F, 1.0F), 13618375, "woldsvaults:deck_cores/adept_deck_core_lesser#inventory"));
            adeptDeckModifier.modifierRolls.put("greater", variant("Greater Adept Core", FloatRoll.ofUniformedStep(4.0F, 6.0F, 1.0F), 16769382, "woldsvaults:deck_cores/adept_deck_core_greater#inventory"));

            DominanceDeckModifier.Config jupiterDeckModifier = new DominanceDeckModifier.Config(FloatRoll.ofUniform(0.3F, 0.5F), DominanceDeckModifier.Mode.DOMINANT, Set.of("Stat"));
            jupiterDeckModifier.modifierRolls.put("lesser", variant("Lesser Jupiter Core", FloatRoll.ofUniform(0.2F, 0.2F), 13618375, "woldsvaults:deck_cores/jupiter_deck_core_lesser#inventory"));
            jupiterDeckModifier.modifierRolls.put("greater", variant("Greater Jupiter Core", FloatRoll.ofUniform(0.5F, 0.75F), 16769382, "woldsvaults:deck_cores/jupiter_deck_core_greater#inventory"));

            DominanceDeckModifier.Config plutoDeckModifier = new DominanceDeckModifier.Config(FloatRoll.ofUniform(0.5F, 1.0F), DominanceDeckModifier.Mode.MINORITY, Set.of("Stat"));
            plutoDeckModifier.modifierRolls.put("lesser", variant("Lesser Pluto Core", FloatRoll.ofUniform(0.35F, 0.35F), 13618375, "woldsvaults:deck_cores/pluto_deck_core_lesser#inventory"));
            plutoDeckModifier.modifierRolls.put("greater", variant("Greater Pluto Core", FloatRoll.ofUniform(1.0F, 2.0F), 16769382, "woldsvaults:deck_cores/pluto_deck_core_greater#inventory"));

            GroupSynergyDeckModifier.Config premiumCoreModifier = new GroupSynergyDeckModifier.Config(FloatRoll.ofUniform(0.05F, 0.1F), List.of("Deluxe"), List.of(), true);
            premiumCoreModifier.modifierRolls.put("lesser", variant("Lesser Premium Core", FloatRoll.ofUniform(0.05F, 0.05F), 13618375, "woldsvaults:deck_cores/premium_deck_core_lesser#inventory"));
            premiumCoreModifier.modifierRolls.put("greater", variant("Greater Premium Core", FloatRoll.ofUniform(0.15F, 0.2F), 16769382, "woldsvaults:deck_cores/premium_deck_core_greater#inventory"));

            GroupSynergyMultiplierModifier.Config archiveCoreModifier = new GroupSynergyMultiplierModifier.Config(FloatRoll.ofUniform(0.075F, 0.125F), "Arcane");
            archiveCoreModifier.modifierRolls.put("lesser", variant("Lesser Archive Core", FloatRoll.ofUniform(0.05F, 0.05F), 13618375, "woldsvaults:deck_cores/archive_deck_core_lesser#inventory"));
            archiveCoreModifier.modifierRolls.put("greater", variant("Greater Archive Core", FloatRoll.ofUniform(0.15F, 0.2F), 16769382, "woldsvaults:deck_cores/archive_deck_core_greater#inventory"));


            //Implicit Deck Modifiers
            AdjacencyBonusDeckModifier.Config merchantDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofConstant(0.5F), List.of("Resource"), true, AdjacencyBonusDeckModifier.Type.COLUMN);
            merchantDeckModifier.modifierRoll = FloatRoll.ofConstant(0.5F);

            TemporalTimeDeckModifier.Config temporalDeckDeckModifier = new TemporalTimeDeckModifier.Config();
            ArcaneLevelDeckModifier.Config arcaneDeckDeckModifier = new ArcaneLevelDeckModifier.Config();
            arcaneDeckDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            GlobalDeckModifier.Config treasureDeckModifier = new GlobalDeckModifier.Config();
            treasureDeckModifier.requiredGroups.add("Stat");
            treasureDeckModifier.modifierRoll = FloatRoll.ofConstant(1.0F);

            GlobalDeckModifier.Config idonaDeckModifier = new GlobalDeckModifier.Config();
            idonaDeckModifier.requiredColors.add(CardEntry.Color.RED);
            idonaDeckModifier.modifierRoll = FloatRoll.ofConstant(1.25F);

            GlobalDeckModifier.Config tenosDeckModifier = new GlobalDeckModifier.Config();
            tenosDeckModifier.requiredColors.add(CardEntry.Color.BLUE);
            tenosDeckModifier.modifierRoll = FloatRoll.ofConstant(1.25F);

            GlobalDeckModifier.Config velaraDeckModifier = new GlobalDeckModifier.Config();
            velaraDeckModifier.requiredColors.add(CardEntry.Color.GREEN);
            velaraDeckModifier.modifierRoll = FloatRoll.ofConstant(1.25F);

            GlobalDeckModifier.Config wendarrDeckModifier = new GlobalDeckModifier.Config();
            wendarrDeckModifier.requiredColors.add(CardEntry.Color.YELLOW);
            wendarrDeckModifier.modifierRoll = FloatRoll.ofConstant(1.25F);

            GlobalDeckModifier.Config cactusDeckModifier = new GlobalDeckModifier.Config();
            cactusDeckModifier.requiredGroups.add("Offensive");
            cactusDeckModifier.requiredGroups.add("Defensive");
            cactusDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            GlobalDeckModifier.Config championDeckModifier = new GlobalDeckModifier.Config();
            championDeckModifier.requiredGroups.add("Physical");
            championDeckModifier.modifierRoll = FloatRoll.ofConstant(1.5F);

            GlobalDeckModifier.Config beltDeckModifier = new GlobalDeckModifier.Config();
            beltDeckModifier.requiredGroups.add("Utility");
            beltDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            GlobalDeckModifier.Config fairyDeckModifier = new GlobalDeckModifier.Config();
            fairyDeckModifier.requiredGroups.add("Magical");
            fairyDeckModifier.modifierRoll = FloatRoll.ofConstant(0.5F);


            CardNeighborTypeDeckModifier.Config rookDeckModifier = new CardNeighborTypeDeckModifier.Config(FloatRoll.ofConstant(2.0F), Set.of(CardNeighborType.COLUMN));
            rookDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            CardNeighborTypeDeckModifier.Config pillagerDeckModifier = new CardNeighborTypeDeckModifier.Config(FloatRoll.ofConstant(2.0F), Set.of(CardNeighborType.SURROUNDING));
            pillagerDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            CardNeighborTypeDeckModifier.Config bishopDeckModifier = new CardNeighborTypeDeckModifier.Config(FloatRoll.ofConstant(3.0F), Set.of(CardNeighborType.DIAGONAL));
            bishopDeckModifier.modifierRoll = FloatRoll.ofConstant(3.0F);

            RowPositionDeckModifier.Config cakeDeckModifier = new RowPositionDeckModifier.Config(FloatRoll.ofConstant(1.0F), true);
            cakeDeckModifier.modifierRoll = FloatRoll.ofConstant(1.0F);

            ColorMismatchAdjacencyModifier.Config puzzleDeckModifier = new ColorMismatchAdjacencyModifier.Config(FloatRoll.ofConstant(1.0F), true, AdjacencyBonusDeckModifier.Type.ADJACENT);
            puzzleDeckModifier.modifierRoll = FloatRoll.ofConstant(1.0F);

            UniqueGroupsDeckModifier.Config mutantDeckModifier = new UniqueGroupsDeckModifier.Config(FloatRoll.ofConstant(0.25F));

            NitwitDeckModifier.Config villagerDeckModifier = new NitwitDeckModifier.Config();
            villagerDeckModifier.modifierRoll = FloatRoll.ofConstant(3.0F);

            SymmetricBalanceDeckModifier.Config runicDeckModifier = new SymmetricBalanceDeckModifier.Config(FloatRoll.ofConstant(2.0F), SymmetricBalanceDeckModifier.Axis.HORIZONTAL);
            runicDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            ChainReactionDeckModifier.Config snakeDeckModifier = new ChainReactionDeckModifier.Config();
            snakeDeckModifier.modifierRoll = FloatRoll.ofConstant(0.1F);

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
            builder.addCore("premium", GroupSynergyDeckModifier::new, premiumCoreModifier,"Premium Core", 13618375,"woldsvaults:deck_cores/premium_deck_core#inventory");
            builder.addCore("sparkling", GlobalDeckModifier::new, sparklingDeckCore,"Sparkling Core", 13618375,"woldsvaults:deck_cores/sparkling_deck_core#inventory");
            builder.addCore("construction", CreateSlotDeckModifier::new, constructionCoreModifier,"Construction Core", 13618375,"woldsvaults:deck_cores/construction_deck_core#inventory");
            builder.addCore("archive", GroupSynergyMultiplierModifier::new, archiveCoreModifier,"Archive Core", 13618375,"woldsvaults:deck_cores/archive_deck_core#inventory");
            //Implicit Deck Mods
            builder.addCore("merchant_deck", AdjacencyBonusDeckModifier::new, merchantDeckModifier,"Merchant Deck Modifier", 13618375,"the_vault:deck/merchant_deck#inventory");
            builder.addCore("extended_deck", TemporalTimeDeckModifier::new, temporalDeckDeckModifier,"Extended Deck Modifier", 13618375,"the_vault:deck/expanded_deck#inventory");
            builder.addCore("treasure_deck", GlobalDeckModifier::new, treasureDeckModifier,"Treasure Deck Modifier", 13618375,"the_vault:deck/treasure_deck#inventory");
            builder.addCore("arcane_deck", ArcaneLevelDeckModifier::new, arcaneDeckDeckModifier,"Arcane Deck Modifier", 13618375,"the_vault:deck/arcane_deck#inventory");
            builder.addCore("idona_deck", GlobalDeckModifier::new, idonaDeckModifier,"Idona Deck Modifier", 13618375,"the_vault:deck/idona_deck#inventory");
            builder.addCore("velara_deck", GlobalDeckModifier::new, velaraDeckModifier,"Velara Deck Modifier", 13618375,"the_vault:deck/velara_deck#inventory");
            builder.addCore("tenos_deck", GlobalDeckModifier::new, tenosDeckModifier,"Tenos Deck Modifier", 13618375,"the_vault:deck/tenos_deck#inventory");
            builder.addCore("wendarr_deck", GlobalDeckModifier::new, wendarrDeckModifier,"Wendarr Deck Modifier", 13618375,"the_vault:deck/wendarr_deck#inventory");
            builder.addCore("cactus_deck", GlobalDeckModifier::new, cactusDeckModifier,"Cactus Deck Modifier", 13618375,"the_vault:deck/cactus_deck#inventory");
            builder.addCore("champion_deck", GlobalDeckModifier::new, championDeckModifier,"Champion Deck Modifier", 13618375,"the_vault:deck/champion_deck#inventory");
            builder.addCore("rook_deck", CardNeighborTypeDeckModifier::new, rookDeckModifier,"Rook Deck Modifier", 13618375,"woldsvaults:deck/wall#inventory");
            builder.addCore("bishop_deck", CardNeighborTypeDeckModifier::new, bishopDeckModifier,"Bishop Deck Modifier", 13618375,"the_vault:deck/bishop_deck#inventory");
            builder.addCore("pillager_deck", CardNeighborTypeDeckModifier::new, pillagerDeckModifier,"Pillager Deck Modifier", 13618375,"the_vault:deck/pillager_deck#inventory");
            builder.addCore("cake_deck", RowPositionDeckModifier::new, cakeDeckModifier,"Cake Deck Modifier", 13618375,"the_vault:deck/cake_deck#inventory");
            builder.addCore("puzzle_deck", ColorMismatchAdjacencyModifier::new, puzzleDeckModifier,"Puzzle Deck Modifier", 13618375,"the_vault:deck/puzzle_deck#inventory");
            builder.addCore("mutant_deck", UniqueGroupsDeckModifier::new, mutantDeckModifier,"Mutant Deck Modifier", 13618375,"the_vault:deck/mutant_deck#inventory");
            builder.addCore("belt_deck", GlobalDeckModifier::new, beltDeckModifier,"Belt Deck Modifier", 13618375,"the_vault:deck/belt_deck#inventory");
            builder.addCore("villager_deck", NitwitDeckModifier::new, villagerDeckModifier,"Villager Deck Modifier", 13618375,"the_vault:deck/villager_deck#inventory");
            builder.addCore("fairy_deck", GlobalDeckModifier::new, fairyDeckModifier,"Fairy Deck Modifier", 13618375,"woldsvaults:deck/fairy#inventory");
            builder.addCore("runic_deck", SymmetricBalanceDeckModifier::new, runicDeckModifier,"Runic Deck Modifier", 13618375,"the_vault:deck/runic_deck#inventory");
            builder.addCore("snake_deck", ChainReactionDeckModifier::new, snakeDeckModifier,"Snake Deck Modifier", 13618375,"woldsvaults:deck/snake#inventory");

            //Pools
            builder.addPool("default", stringWeightedListBuilder -> {
                stringWeightedListBuilder.add("arsenal", 1);
                stringWeightedListBuilder.add("aegis", 1);
                stringWeightedListBuilder.add("tool", 1);
                stringWeightedListBuilder.add("natural", 1);
                stringWeightedListBuilder.add("fae", 1);
                stringWeightedListBuilder.add("void", 1);
                stringWeightedListBuilder.add("nitwit", 1);
                stringWeightedListBuilder.add("bazaar", 1);
                stringWeightedListBuilder.add("arcane", 1);
                stringWeightedListBuilder.add("temporal", 1);
                stringWeightedListBuilder.add("adept", 1);
                stringWeightedListBuilder.add("jupiter", 1);
                stringWeightedListBuilder.add("pluto", 1);
                stringWeightedListBuilder.add("premium", 1);
                stringWeightedListBuilder.add("sparkling", 1);
                stringWeightedListBuilder.add("archive", 1);
            });
            builder.addPool("greed", stringWeightedListBuilder -> {
                stringWeightedListBuilder.add("arsenal", 1);
                stringWeightedListBuilder.add("aegis", 1);
                stringWeightedListBuilder.add("tool", 1);
                stringWeightedListBuilder.add("natural", 1);
                stringWeightedListBuilder.add("fae", 1);
                stringWeightedListBuilder.add("void", 1);
                stringWeightedListBuilder.add("nitwit", 1);
                stringWeightedListBuilder.add("bazaar", 1);
                stringWeightedListBuilder.add("arcane", 1);
                stringWeightedListBuilder.add("temporal", 1);
                stringWeightedListBuilder.add("adept", 1);
                stringWeightedListBuilder.add("jupiter", 1);
                stringWeightedListBuilder.add("pluto", 1);
                stringWeightedListBuilder.add("premium", 1);
                stringWeightedListBuilder.add("sparkling", 1);
                stringWeightedListBuilder.add("construction", 1);
                stringWeightedListBuilder.add("archive", 1);
            });
            builder.addPool("treasure_sand", stringWeightedListBuilder -> {
                stringWeightedListBuilder.add("temporal", 1);
                stringWeightedListBuilder.add("adept", 1);
                stringWeightedListBuilder.add("fae", 1);
                stringWeightedListBuilder.add("tool", 1);
                stringWeightedListBuilder.add("arcane", 1);
            });
            builder.addPool("champions", stringWeightedListBuilder -> {
                stringWeightedListBuilder.add("nitwit", 2);
                stringWeightedListBuilder.add("natural", 2);
                stringWeightedListBuilder.add("arsenal", 2);
                stringWeightedListBuilder.add("aegis", 2);
                stringWeightedListBuilder.add("sparkling", 2);
                stringWeightedListBuilder.add("pluto", 2);
                stringWeightedListBuilder.add("jupiter", 2);
            });
            builder.addPool("completion_crate", stringWeightedListBuilder -> {
                stringWeightedListBuilder.add("bazaar", 1);
                stringWeightedListBuilder.add("premium", 1);
                stringWeightedListBuilder.add("archive", 1);
            });
            builder.addPool("dungeon_boss", stringWeightedListBuilder -> {
                stringWeightedListBuilder.add("adept", 1);
                stringWeightedListBuilder.add("void", 1);
            });
            builder.addPool("factory", stringWeightedListBuilder -> {
                stringWeightedListBuilder.add("construction", 1);
                stringWeightedListBuilder.add("tool", 1);
            });
            builder.addPool("card_deck_implicits", stringWeightedListBuilder -> {
                stringWeightedListBuilder.add("merchant_deck", 1);
                stringWeightedListBuilder.add("extended_deck", 1);
                stringWeightedListBuilder.add("treasure_deck", 1);
                stringWeightedListBuilder.add("arcane_deck", 1);
                stringWeightedListBuilder.add("idona_deck", 1);
                stringWeightedListBuilder.add("velara_deck", 1);
                stringWeightedListBuilder.add("wendarr_deck", 1);
                stringWeightedListBuilder.add("tenos_deck", 1);
                stringWeightedListBuilder.add("cactus_deck", 1);
                stringWeightedListBuilder.add("champion_deck", 1);
                stringWeightedListBuilder.add("rook_deck", 1);
                stringWeightedListBuilder.add("bishop_deck", 1);
                stringWeightedListBuilder.add("pillager_deck", 1);
                stringWeightedListBuilder.add("cake_deck", 1);
                stringWeightedListBuilder.add("puzzle_deck", 1);
                stringWeightedListBuilder.add("mutant_deck", 1);
                stringWeightedListBuilder.add("villager_deck", 1);
                stringWeightedListBuilder.add("belt_deck", 1);
                stringWeightedListBuilder.add("fairy_deck", 1);
                stringWeightedListBuilder.add("runic_deck", 1);
                stringWeightedListBuilder.add("snake_deck", 1);
            });
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

    public SlotDeckModifier.Config.SlotVariant slotVariant(String name, IntRoll roll, FloatRoll modRoll, int colour, String modelId, Set<CardEntry.Color> requiredColors, Set<String> requiredGroups) {
        SlotDeckModifier.Config.SlotVariant variant = SlotDeckModifier.Config.SlotVariant.of(roll, requiredGroups, requiredColors);
        variant.roll = modRoll;
        variant.colour = colour;
        variant.modelId = modelId;
        variant.name = name;
        return variant;
    }
}
