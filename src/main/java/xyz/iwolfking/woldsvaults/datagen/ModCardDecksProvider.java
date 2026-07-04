package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.entry.IntRangeEntry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.card.AbstractDeckProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.BasicListBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModCardDecksProvider extends AbstractDeckProvider {
    protected ModCardDecksProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_decks", builder -> {
            builder.addEntry("wold", "woldsvaults:deck/wold#inventory", "The Wold Deck", new IntRangeEntry(4, 4), deck(deckBuilder -> {
                deckBuilder.add("AXAAAXA");
                deckBuilder.add("OXOOOXO");
                deckBuilder.add("OXOOOXO");
                deckBuilder.add("OXOOOXO");
                deckBuilder.add("OOOOOOO");
            }), new IntRangeEntry(4, 4));

            builder.addEntry("snake", "woldsvaults:deck/snake#inventory", "The Snake Deck", new IntRangeEntry(4, 4), deck(deckBuilder -> {
                deckBuilder.add("XXXXXXXXA");
                deckBuilder.add("OOOOOOOOO");
                deckBuilder.add("OXXXXXXXX");
                deckBuilder.add("OOOOOOOOO");
                deckBuilder.add("XXXXXXXXO");
                deckBuilder.add("AOOOOOOOO");
            }), new IntRangeEntry(2, 2));

            builder.addEntry("fairy", "woldsvaults:deck/fairy#inventory", "The Fairy Deck", new IntRangeEntry(4, 4), deck(deckBuilder -> {
                deckBuilder.add("AXXOXOXXA");
                deckBuilder.add("XOXXAXXOX");
                deckBuilder.add("XXOOOOOXX");
                deckBuilder.add("XXAOOOAXX");
                deckBuilder.add("XOXOOOXOX");
                deckBuilder.add("AXXXOXXXA");
            }), new IntRangeEntry(3, 3));

            builder.addEntry("wall", "woldsvaults:deck/wall#inventory", "The Rook Deck", new IntRangeEntry(4, 4), deck(deckBuilder -> {
                deckBuilder.add("AXOXAXOXA");
                deckBuilder.add("OXOXOXOXO");
                deckBuilder.add("OXOXOXOXO");
                deckBuilder.add("OXOXOXOXO");
                deckBuilder.add("OXOXOXOXO");
                deckBuilder.add("OXOXOXOXO");
            }), new IntRangeEntry(2, 2));

            builder.addEntry("crate", "woldsvaults:deck/crate#inventory", "The Crate Deck", new IntRangeEntry(4, 4), deck(deckBuilder -> {
                deckBuilder.add("OOOOOOOO");
                deckBuilder.add("OXOXXOXO");
                deckBuilder.add("OAOOOOAO");
                deckBuilder.add("OXOXXOXO");
                deckBuilder.add("OOOOOOOO");
            }), new IntRangeEntry(2, 2));
        });


    }

    public static String[] deck(Consumer<BasicListBuilder<String>> linesBuilder) {
        BasicListBuilder<String> lines = new BasicListBuilder<>();
        linesBuilder.accept(lines);
        return lines.build().toArray(new String[0]);
    }
}
