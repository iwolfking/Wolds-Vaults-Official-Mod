package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.UniqueCodexConfig;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.lib.core.datagen.AbstractUniqueGearProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.AbstractVaultModifierPoolsProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.gear.UniqueGearBuilder;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.gear.UniqueGearEntry;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.modifier_pools.ModifierPoolBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.function.Consumer;

public class ModUniqueGearProvider extends AbstractUniqueGearProvider {
    public ModUniqueGearProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void addGear(Consumer<GeneratedEntry> consumer) {
        consumer.accept(entry("woldsvaults:ocean_current", new UniqueGearBuilder("Ocean Current")
                .model("woldsvaults:gear/trident/ocean_current")
                .base("the_vault:base_durability", "the_vault:u_living")
                .implicit("the_vault:base_armor",
                        "the_vault:jester_lucky_hit",
                        "the_vault:base_lucky_hit_chance",
                        "the_vault:base_lucky_hit_chance")
                .suffix("the_vault:u_item_rarity")
                .dropLocation("Nowhere")
                .description("This is a test unique!!!", "$text")
                .description("\n\nIt is very testy", "$text")
                .modelType("TRIDENT")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));
    }

    private GeneratedEntry entry(String id, UniqueGearEntry data) {
        return new GeneratedEntry(new ResourceLocation(id), data);
    }
}
