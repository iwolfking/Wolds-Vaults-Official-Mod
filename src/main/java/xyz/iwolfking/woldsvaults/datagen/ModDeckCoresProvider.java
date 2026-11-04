package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.card.DeckModifiersConfig;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardNeighborType;
import iskallia.vault.core.card.modifier.deck.*;
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

import java.util.LinkedHashSet;
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
            arsenalModifierConfig.modifierRoll = FloatRoll.ofUniform(0.3F, 0.4F);
            arsenalModifierConfig.modifierRolls.put("lesser", variant("Lesser Arsenal Core", FloatRoll.ofUniform(0.2F, 0.2F), 13618375, "woldsvaults:deck_cores/arsenal_deck_core_lesser#inventory"));
            arsenalModifierConfig.modifierRolls.put("greater", variant("Greater Arsenal Core", FloatRoll.ofUniform(0.4F, 0.5F), 16769382, "woldsvaults:deck_cores/arsenal_deck_core_greater#inventory"));

            GroupSynergyMultiplierModifier.Config aegisModifierConfig = new GroupSynergyMultiplierModifier.Config(FloatRoll.ofUniform(0.075F, 0.125F), "Defensive");
            aegisModifierConfig.modifierRolls.put("lesser", variant("Lesser Aegis Core", FloatRoll.ofUniform(0.05F, 0.05F), 13618375, "woldsvaults:deck_cores/aegis_deck_core_lesser#inventory"));
            aegisModifierConfig.modifierRolls.put("greater", variant("Greater Aegis Core", FloatRoll.ofUniform(0.125F, 0.175F), 16769382, "woldsvaults:deck_cores/aegis_deck_core_greater#inventory"));

            GroupSynergyMultiplierModifier.Config toolModifierConfig = new GroupSynergyMultiplierModifier.Config(FloatRoll.ofUniform(0.03F, 0.05F), "Utility");
            toolModifierConfig.modifierRolls.put("lesser", variant("Lesser Tool Core", FloatRoll.ofUniform(0.02F, 0.02F), 13618375, "woldsvaults:deck_cores/tool_deck_core_lesser#inventory"));
            toolModifierConfig.modifierRolls.put("greater", variant("Greater Tool Core", FloatRoll.ofUniform(0.05F, 0.08F), 16769382, "woldsvaults:deck_cores/tool_deck_core_greater#inventory"));

            GlobalDeckModifier.Config naturalModifierConfig = new GlobalDeckModifier.Config();
            naturalModifierConfig.requiredGroups.add("Physical");
            naturalModifierConfig.modifierRoll = FloatRoll.ofUniform(0.75F, 1.1F);
            naturalModifierConfig.modifierRolls.put("lesser", variant("Lesser Natural Core", FloatRoll.ofUniform(0.5F, 0.5F), 13618375, "woldsvaults:deck_cores/natural_deck_core_lesser#inventory"));
            naturalModifierConfig.modifierRolls.put("greater", variant("Greater Natural Core", FloatRoll.ofUniform(1.1F, 1.4F), 16769382, "woldsvaults:deck_cores/natural_deck_core_greater#inventory"));

            GlobalDeckModifier.Config faeModifierConfig = new GlobalDeckModifier.Config();
            faeModifierConfig.requiredGroups.add("Magical");
            faeModifierConfig.modifierRoll = FloatRoll.ofUniform(0.75F, 1.1F);
            faeModifierConfig.modifierRolls.put("lesser", variant("Lesser Fae Core", FloatRoll.ofUniform(0.5F, 0.5F), 13618375, "woldsvaults:deck_cores/fae_deck_core_lesser#inventory"));
            faeModifierConfig.modifierRolls.put("greater", variant("Greater Fae Core", FloatRoll.ofUniform(1.1F, 1.4F), 16769382, "woldsvaults:deck_cores/fae_deck_core_greater#inventory"));

            EmptySlotDeckModifier.Config voidModifierConfig = new EmptySlotDeckModifier.Config();
            voidModifierConfig.modifierRoll = FloatRoll.ofUniform(0.03F, 0.05F);
            voidModifierConfig.modifierRolls.put("lesser", variant("Lesser Void Core", FloatRoll.ofUniform(0.02F, 0.02F), 13618375, "woldsvaults:deck_cores/void_deck_core_lesser#inventory"));
            voidModifierConfig.modifierRolls.put("greater", variant("Greater Void Core", FloatRoll.ofUniform(0.06F, 0.08F), 16769382, "woldsvaults:deck_cores/void_deck_core_greater#inventory"));

            NitwitDeckModifier.Config nitwitModifierConfig = new NitwitDeckModifier.Config();
            nitwitModifierConfig.modifierRoll = FloatRoll.ofUniform(1.0F, 2.0F);
            nitwitModifierConfig.modifierRolls.put("lesser", variant("Lesser Nitwit Core", FloatRoll.ofUniform(0.5F, 0.5F), 13618375, "woldsvaults:deck_cores/nitwit_deck_core_lesser#inventory"));
            nitwitModifierConfig.modifierRolls.put("greater", variant("Greater Nitwit Core", FloatRoll.ofUniform(2.0F, 3.0F), 16769382, "woldsvaults:deck_cores/nitwit_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config bazaarDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.4F, 0.6F), List.of("Resource"), false, AdjacencyBonusDeckModifier.Type.SURROUNDING);
            bazaarDeckModifier.modifierRolls.put("lesser", variant("Lesser Bazaar Core", FloatRoll.ofUniform(0.3F, 0.3F), 13618375, "woldsvaults:deck_cores/bazaar_deck_core_lesser#inventory"));
            bazaarDeckModifier.modifierRolls.put("greater", variant("Greater Bazaar Core", FloatRoll.ofUniform(0.6F, 0.8F), 16769382, "woldsvaults:deck_cores/bazaar_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config temporalDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.7F, 1.0F), List.of("Temporal"), false, AdjacencyBonusDeckModifier.Type.STARCROSS);
            temporalDeckModifier.modifierRolls.put("lesser", variant("Lesser Temporal Core", FloatRoll.ofUniform(0.35F, 0.35F), 13618375, "woldsvaults:deck_cores/temporal_deck_core_lesser#inventory"));
            temporalDeckModifier.modifierRolls.put("greater", variant("Greater Temporal Core", FloatRoll.ofUniform(1.0F, 2.0F), 16769382, "woldsvaults:deck_cores/temporal_deck_core_greater#inventory"));

            AdjacencyBonusDeckModifier.Config talentDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofUniform(0.4F, 0.6F), List.of("Knack"), true, AdjacencyBonusDeckModifier.Type.SURROUNDING);
            talentDeckModifier.modifierRolls.put("lesser", variant("Lesser Talent Core", FloatRoll.ofUniform(0.3F, 0.3F), 13618375, "woldsvaults:deck_cores/talent_deck_core_lesser#inventory"));
            talentDeckModifier.modifierRolls.put("greater", variant("Greater Talent Core", FloatRoll.ofUniform(0.6F, 0.8F), 16769382, "woldsvaults:deck_cores/talent_deck_core_greater#inventory"));

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

            DominanceDeckModifier.Config jupiterDeckModifier = new DominanceDeckModifier.Config(FloatRoll.ofUniform(0.4F, 0.6F), DominanceDeckModifier.Mode.DOMINANT, Set.of("Stat"));
            jupiterDeckModifier.modifierRolls.put("lesser", variant("Lesser Jupiter Core", FloatRoll.ofUniform(0.3F, 0.3F), 13618375, "woldsvaults:deck_cores/jupiter_deck_core_lesser#inventory"));
            jupiterDeckModifier.modifierRolls.put("greater", variant("Greater Jupiter Core", FloatRoll.ofUniform(0.6F, 0.85F), 16769382, "woldsvaults:deck_cores/jupiter_deck_core_greater#inventory"));

            DominanceDeckModifier.Config plutoDeckModifier = new DominanceDeckModifier.Config(FloatRoll.ofUniform(0.25F, 0.4F), DominanceDeckModifier.Mode.MINORITY, Set.of());
            plutoDeckModifier.modifierRolls.put("lesser", variant("Lesser Pluto Core", FloatRoll.ofUniform(0.2F, 0.2F), 13618375, "woldsvaults:deck_cores/pluto_deck_core_lesser#inventory"));
            plutoDeckModifier.modifierRolls.put("greater", variant("Greater Pluto Core", FloatRoll.ofUniform(0.4F, 0.6F), 16769382, "woldsvaults:deck_cores/pluto_deck_core_greater#inventory"));

            GroupSynergyDeckModifier.Config premiumCoreModifier = new GroupSynergyDeckModifier.Config(FloatRoll.ofUniform(0.05F, 0.1F), List.of("Deluxe"), List.of(), true);
            premiumCoreModifier.modifierRolls.put("lesser", variant("Lesser Premium Core", FloatRoll.ofUniform(0.03F, 0.03F), 13618375, "woldsvaults:deck_cores/premium_deck_core_lesser#inventory"));
            premiumCoreModifier.modifierRolls.put("greater", variant("Greater Premium Core", FloatRoll.ofUniform(0.1F, 0.125F), 16769382, "woldsvaults:deck_cores/premium_deck_core_greater#inventory"));

            GroupSynergyMultiplierModifier.Config archiveCoreModifier = new GroupSynergyMultiplierModifier.Config(FloatRoll.ofUniform(0.075F, 0.125F), "Arcane");
            archiveCoreModifier.modifierRolls.put("lesser", variant("Lesser Archive Core", FloatRoll.ofUniform(0.05F, 0.05F), 13618375, "woldsvaults:deck_cores/archive_deck_core_lesser#inventory"));
            archiveCoreModifier.modifierRolls.put("greater", variant("Greater Archive Core", FloatRoll.ofUniform(0.15F, 0.225F), 16769382, "woldsvaults:deck_cores/archive_deck_core_greater#inventory"));

            GreedStackDeckModifier.Config greedCoreModifier = new GreedStackDeckModifier.Config(FloatRoll.ofUniform(0.1F, 0.15F));
            greedCoreModifier.modifierRolls.put("lesser", variant("Lesser Greed Core", FloatRoll.ofUniform(0.05F, 0.05F), 13618375, "woldsvaults:deck_cores/greed_deck_core_lesser#inventory"));
            greedCoreModifier.modifierRolls.put("greater", variant("Greater Greed Core", FloatRoll.ofUniform(0.15F, 0.25F), 16769382, "woldsvaults:deck_cores/greed_deck_core_greater#inventory"));


            //Implicit Deck Modifiers
            AdjacencyBonusDeckModifier.Config merchantDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofConstant(0.5F), List.of("Resource"), true, AdjacencyBonusDeckModifier.Type.COLUMN);
            merchantDeckModifier.modifierRoll = FloatRoll.ofConstant(1.0F);

            TemporalTimeDeckModifier.Config temporalDeckDeckModifier = new TemporalTimeDeckModifier.Config();
            ArcaneLevelDeckModifier.Config arcaneDeckDeckModifier = new ArcaneLevelDeckModifier.Config();
            arcaneDeckDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            GlobalDeckModifier.Config treasureDeckModifier = new GlobalDeckModifier.Config();
            treasureDeckModifier.requiredGroups.add("Stat");
            treasureDeckModifier.modifierRoll = FloatRoll.ofConstant(1.25F);

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
            cactusDeckModifier.modifierRoll = FloatRoll.ofConstant(2.5F);

            GlobalDeckModifier.Config championDeckModifier = new GlobalDeckModifier.Config();
            championDeckModifier.requiredGroups.add("Physical");
            championDeckModifier.modifierRoll = FloatRoll.ofConstant(1.5F);

            GlobalDeckModifier.Config anvilDeckModifier = new GlobalDeckModifier.Config();
            anvilDeckModifier.requiredGroups.add("Defensive");
            anvilDeckModifier.modifierRoll = FloatRoll.ofConstant(1.0F);

            GlobalDeckModifier.Config beltDeckModifier = new GlobalDeckModifier.Config();
            beltDeckModifier.requiredGroups.add("Utility");
            beltDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            GlobalDeckModifier.Config fairyDeckModifier = new GlobalDeckModifier.Config();
            fairyDeckModifier.requiredGroups.add("Magical");
            fairyDeckModifier.modifierRoll = FloatRoll.ofConstant(0.5F);

            GlobalDeckModifier.Config gildedDeckModifier = new GlobalDeckModifier.Config();
            gildedDeckModifier.requiredGroups.add("Foil");
            gildedDeckModifier.requiredColors.add(CardEntry.Color.YELLOW);
            gildedDeckModifier.modifierRoll = FloatRoll.ofConstant(1.5F);

            GlobalDeckModifier.Config ornateDeckModifier = new GlobalDeckModifier.Config();
            ornateDeckModifier.requiredGroups.add("Foil");
            ornateDeckModifier.requiredColors.add(CardEntry.Color.RED);
            ornateDeckModifier.modifierRoll = FloatRoll.ofConstant(1.75F);

            GlobalDeckModifier.Config livingDeckModifier = new GlobalDeckModifier.Config();
            livingDeckModifier.requiredGroups.add("Foil");
            livingDeckModifier.requiredColors.add(CardEntry.Color.GREEN);
            livingDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);


            CardNeighborTypeDeckModifier.Config rookDeckModifier = new CardNeighborTypeDeckModifier.Config(FloatRoll.ofConstant(2.0F), Set.of(CardNeighborType.COLUMN));
            rookDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            CardNeighborTypeDeckModifier.Config pillagerDeckModifier = new CardNeighborTypeDeckModifier.Config(FloatRoll.ofConstant(2.0F), Set.of(CardNeighborType.SURROUNDING));
            pillagerDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            CardNeighborTypeDeckModifier.Config bishopDeckModifier = new CardNeighborTypeDeckModifier.Config(FloatRoll.ofConstant(2.0F), Set.of(CardNeighborType.DIAGONAL));
            bishopDeckModifier.modifierRoll = FloatRoll.ofConstant(2.0F);

            RowPositionDeckModifier.Config cakeDeckModifier = new RowPositionDeckModifier.Config(FloatRoll.ofConstant(1.0F), true);
            cakeDeckModifier.modifierRoll = FloatRoll.ofConstant(1.0F);

            ColorMismatchAdjacencyModifier.Config puzzleDeckModifier = new ColorMismatchAdjacencyModifier.Config(FloatRoll.ofConstant(1.0F), true, AdjacencyBonusDeckModifier.Type.ADJACENT);
            puzzleDeckModifier.modifierRoll = FloatRoll.ofConstant(1.0F);

            UniqueGroupsDeckModifier.Config mutantDeckModifier = new UniqueGroupsDeckModifier.Config(FloatRoll.ofConstant(0.25F));

            NitwitDeckModifier.Config villagerDeckModifier = new NitwitDeckModifier.Config();
            villagerDeckModifier.modifierRoll = FloatRoll.ofConstant(3.0F);

            SymmetricBalanceDeckModifier.Config runicDeckModifier = new SymmetricBalanceDeckModifier.Config(FloatRoll.ofConstant(0.2F), SymmetricBalanceDeckModifier.Axis.HORIZONTAL);
            runicDeckModifier.modifierRoll = FloatRoll.ofConstant(0.2F);

            ChainReactionDeckModifier.Config snakeDeckModifier = new ChainReactionDeckModifier.Config();
            snakeDeckModifier.modifierRoll = FloatRoll.ofConstant(0.05F);

            AdjacencyBonusDeckModifier.Config skullDeckModifier = new AdjacencyBonusDeckModifier.Config(FloatRoll.ofConstant(0.5F), List.of("Knack"), true, AdjacencyBonusDeckModifier.Type.SURROUNDING);
            skullDeckModifier.modifierRoll = FloatRoll.ofConstant(0.5F);

            EmptySlotDeckModifier.Config shadowDeckModifier = new EmptySlotDeckModifier.Config();
            shadowDeckModifier.modifierRoll = FloatRoll.ofConstant(0.04F);

            BountyDeckModifier.Config relicDeckModifier = new BountyDeckModifier.Config(FloatRoll.ofConstant(2.0F), IntRoll.ofConstant(4));

            builder.addCore("arsenal", MultiplicativeGlobalDeckModifier::new, arsenalModifierConfig, "Arsenal Core", 13618375,"woldsvaults:deck_cores/arsenal_deck_core#inventory");
            builder.addCore("aegis", AdjacentGroupMultiplierDeckModifier::new, aegisModifierConfig,"Aegis Core", 13618375,"woldsvaults:deck_cores/aegis_deck_core#inventory");
            builder.addCore("tool", GroupCountMultiplierDeckModifier::new, toolModifierConfig,"Tool Core", 13618375,"woldsvaults:deck_cores/tool_deck_core#inventory");
            builder.addCore("natural", GlobalDeckModifier::new, naturalModifierConfig,"Natural Core", 13618375,"woldsvaults:deck_cores/natural_deck_core#inventory");
            builder.addCore("fae", GlobalDeckModifier::new, faeModifierConfig,"Fae Core", 13618375,"woldsvaults:deck_cores/fae_deck_core#inventory");
            builder.addCore("void", MultiplicativeEmptySlotDeckModifier::new, voidModifierConfig,"Void Core", 13618375,"woldsvaults:deck_cores/void_deck_core#inventory");
            builder.addCore("nitwit", NitwitDeckModifier::new, nitwitModifierConfig,"Nitwit Core", 13618375,"woldsvaults:deck_cores/nitwit_deck_core#inventory");
            builder.addCore("bazaar", AdjacencyBonusDeckModifier::new, bazaarDeckModifier,"Bazaar Core", 13618375,"woldsvaults:deck_cores/bazaar_deck_core#inventory");
            builder.addCore("temporal", AdjacencyBonusDeckModifier::new, temporalDeckModifier,"Temporal Core", 13618375,"woldsvaults:deck_cores/temporal_deck_core#inventory");
            builder.addCore("talent", AdjacencyBonusDeckModifier::new, talentDeckModifier,"Talent Core", 13618375,"woldsvaults:deck_cores/talent_deck_core#inventory");
            builder.addCore("arcane", CreateGroupSlotDeckModifier::new, arcaneDeckModifier,"Arcane Core", 13618375,"woldsvaults:deck_cores/arcane_deck_core#inventory");
            builder.addCore("adept", ArcaneSlotDeckModifier::new, adeptDeckModifier,"Adept Core", 13618375,"woldsvaults:deck_cores/adept_deck_core#inventory");
            builder.addCore("jupiter", DominanceDeckModifier::new, jupiterDeckModifier,"Jupiter Core", 13618375,"woldsvaults:deck_cores/jupiter_deck_core#inventory");
            builder.addCore("pluto", MultiplicativeDominanceDeckModifier::new, plutoDeckModifier,"Pluto Core", 13618375,"woldsvaults:deck_cores/pluto_deck_core#inventory");
            builder.addCore("premium", GroupSynergyDeckModifier::new, premiumCoreModifier,"Premium Core", 13618375,"woldsvaults:deck_cores/premium_deck_core#inventory");
            builder.addCore("construction", CreateSlotDeckModifier::new, constructionCoreModifier,"Construction Core", 13618375,"woldsvaults:deck_cores/construction_deck_core#inventory");
            builder.addCore("archive", GroupSynergyMultiplierModifier::new, archiveCoreModifier,"Archive Core", 13618375,"woldsvaults:deck_cores/archive_deck_core#inventory");
            builder.addCore("greed", GreedStackDeckModifier::new, greedCoreModifier,"Greed Core", 13618375,"woldsvaults:deck_cores/greed_deck_core#inventory");

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
            builder.addCore("runic_deck", TagMirrorDeckModifier::new, runicDeckModifier,"Runic Deck Modifier", 13618375,"the_vault:deck/runic_deck#inventory");
            builder.addCore("snake_deck", ChainReactionDeckModifier::new, snakeDeckModifier,"Snake Deck Modifier", 13618375,"woldsvaults:deck/snake#inventory");
            builder.addCore("gilded_deck", GlobalDeckModifier::new, gildedDeckModifier,"Gilded Deck Modifier", 13618375,"the_vault:deck/edge_deck#inventory");
            builder.addCore("living_deck", GlobalDeckModifier::new, livingDeckModifier,"Living Deck Modifier", 13618375,"the_vault:deck/mushroom_deck#inventory");
            builder.addCore("ornate_deck", GlobalDeckModifier::new, ornateDeckModifier,"Ornate Deck Modifier", 13618375,"the_vault:deck/ornate_deck#inventory");
            builder.addCore("anvil_deck", GlobalDeckModifier::new, anvilDeckModifier,"Anvil Deck Modifier", 13618375,"the_vault:deck/anvil_deck#inventory");
            builder.addCore("skull_deck", AdjacencyBonusDeckModifier::new, skullDeckModifier,"Skull Deck Modifier", 13618375,"the_vault:deck/skull_deck#inventory");
            builder.addCore("shadow_deck", MultiplicativeEmptySlotDeckModifier::new, shadowDeckModifier,"Shadow Deck Modifier", 13618375,"the_vault:deck/black_deck#inventory");
            builder.addCore("relic_deck", BountyDeckModifier::new, relicDeckModifier,"Relic Deck Modifier", 13618375,"the_vault:deck/lost_deck#inventory");

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
                stringWeightedListBuilder.add("archive", 1);
                stringWeightedListBuilder.add("talent", 1);
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
                stringWeightedListBuilder.add("construction", 1);
                stringWeightedListBuilder.add("archive", 1);
                stringWeightedListBuilder.add("greed", 1);
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
                stringWeightedListBuilder.add("ornate_deck", 1);
                stringWeightedListBuilder.add("living_deck", 1);
                stringWeightedListBuilder.add("gilded_deck", 1);
                stringWeightedListBuilder.add("anvil_deck", 1);
                stringWeightedListBuilder.add("skull_deck", 1);
                stringWeightedListBuilder.add("shadow_deck", 1);
                stringWeightedListBuilder.add("relic_deck", 1);
            });
        });

        add("greed_cores", builder -> {
            StatEfficiencyDeckModifier.Config equilibriumConfig = new StatEfficiencyDeckModifier.Config(FloatRoll.ofUniform(0.12F, 0.2F));
            equilibriumConfig.modifierRolls.put("lesser", variant("Lesser Equilibrium Core", FloatRoll.ofUniform(0.06F, 0.06F), 13618375, "the_vault:deck_cores/lesser_equilibrium_deck_core#inventory"));
            equilibriumConfig.modifierRolls.put("greater", variant("Greater Equilibrium Core", FloatRoll.ofUniform(0.2F, 0.36F), 16769382, "the_vault:deck_cores/greater_equilibrium_deck_core#inventory"));

            GlobalDeckModifier.Config shinyConfig = new GlobalDeckModifier.Config();
            shinyConfig.requiredGroups.add("Foil");
            shinyConfig.modifierRoll = FloatRoll.ofUniform(0.3F, 0.6F);
            shinyConfig.modifierRolls.put("lesser", variant("Lesser Shiny Core", FloatRoll.ofUniform(0.2F, 0.2F), 13618375, "the_vault:deck_cores/lesser_shiny_deck_core#inventory"));
            shinyConfig.modifierRolls.put("greater", variant("Greater Shiny Core", FloatRoll.ofUniform(0.6F, 0.9F), 16769382, "the_vault:deck_cores/greater_shiny_deck_core#inventory"));

            SlotDeckModifier.Config empyrealConfig = new SlotDeckModifier.Config();
            empyrealConfig.modifierRoll = FloatRoll.ofUniform(0.2F, 0.3F);
            empyrealConfig.requiredGroups.add("Stat");
            empyrealConfig.requiredGroups.add("Evolution");
            empyrealConfig.slotRoll = IntRoll.ofUniform(2, 2);
            empyrealConfig.slotRolls.put("lesser", slotVariant("Lesser Empyreal Core", IntRoll.ofUniform(1, 1), FloatRoll.ofUniform(0.2F, 0.2F), 13618375, "the_vault:deck_cores/lesser_empyreal_deck_core#inventory", Set.of(), new LinkedHashSet<>(List.of("Stat", "Evolution"))));
            empyrealConfig.slotRolls.put("greater", slotVariant("Greater Empyreal Core", IntRoll.ofUniform(3, 3), FloatRoll.ofUniform(0.3F, 0.4F), 16769382, "the_vault:deck_cores/greater_empyreal_deck_core#inventory", Set.of(), new LinkedHashSet<>(List.of("Stat", "Evolution"))));

            NonFoilEfficiencyDeckModifier.Config pureConfig = new NonFoilEfficiencyDeckModifier.Config(FloatRoll.ofUniform(0.02F, 0.03F));
            pureConfig.modifierRolls.put("lesser", variant("Lesser Pure Core", FloatRoll.ofUniform(0.01F, 0.01F), 13618375, "the_vault:deck_cores/lesser_pure_deck_core#inventory"));
            pureConfig.modifierRolls.put("greater", variant("Greater Pure Core", FloatRoll.ofUniform(0.03F, 0.05F), 16769382, "the_vault:deck_cores/greater_pure_deck_core#inventory"));

            builder.addCore("equilibrium", StatEfficiencyDeckModifier::new, equilibriumConfig, "Equilibrium Core", 13618375, "the_vault:deck_cores/equilibrium_deck_core#inventory");
            builder.addCore("shiny", GlobalDeckModifier::new, shinyConfig, "Shiny Core", 13618375, "the_vault:deck_cores/shiny_deck_core#inventory");
            colourCore(builder, "crimson", "Crimson", CardEntry.Color.RED);
            colourCore(builder, "azure", "Azure", CardEntry.Color.BLUE);
            colourCore(builder, "viridian", "Viridian", CardEntry.Color.GREEN);
            colourCore(builder, "golden", "Golden", CardEntry.Color.YELLOW);
            builder.addCore("empyreal", MultiplicativeSlotDeckModifier::new, empyrealConfig, "Empyreal Core", 13618375, "the_vault:deck_cores/empyreal_deck_core#inventory");
            builder.addCore("pure", NonFoilEfficiencyDeckModifier::new, pureConfig, "Pure Core", 13618375, "the_vault:deck_cores/pure_deck_core#inventory");

            builder.addPool("greed_basic", pool -> {
                pool.add("harvest", 4);
                pool.add("bounty", 4);
                pool.add("crimson", 4);
                pool.add("azure", 4);
                pool.add("viridian", 4);
                pool.add("golden", 4);
            });
        });
    }

    private void colourCore(Builder builder, String id, String label, CardEntry.Color color) {
        GlobalDeckModifier.Config config = new GlobalDeckModifier.Config();
        config.requiredColors.add(color);
        config.modifierRoll = FloatRoll.ofUniform(0.1F, 0.3F);
        config.modifierRolls.put("lesser", variant("Lesser " + label + " Core", FloatRoll.ofUniform(0.05F, 0.05F), 13618375, "the_vault:deck_cores/lesser_" + id + "_deck_core#inventory"));
        config.modifierRolls.put("greater", variant("Greater " + label + " Core", FloatRoll.ofUniform(0.4F, 0.6F), 16769382, "the_vault:deck_cores/greater_" + id + "_deck_core#inventory"));
        builder.addCore(id, GlobalDeckModifier::new, config, label + " Core", 13618375, "the_vault:deck_cores/" + id + "_deck_core#inventory");
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
