package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.init.ModBlocks;
import iskallia.vault.util.data.LazySet;
import net.minecraftforge.event.RegistryEvent;
import org.lwjgl.openal.ALC;
import xyz.iwolfking.vhapi.api.registry.objective.CustomObjectiveRegistryEntry;
import xyz.iwolfking.woldsvaults.objectives.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ModCustomVaultObjectiveEntries {
    public static LazySet<CustomObjectiveRegistryEntry> ENTRIES = new LazySet<>(ModCustomVaultObjectiveEntries::getEntries);
    public static final CustomObjectiveRegistryEntry BRUTAL_BOSSES = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("brutal_bosses", "Brutal Bosses", BrutalBossesCrystalObjective.class, BrutalBossesCrystalObjective::new, BrutalBossesObjective.E_KEY, BrutalBossesObjective.class).setCrateItem(() -> xyz.iwolfking.woldsvaults.init.ModBlocks.CUSTOM_VAULT_CRATES.get("brutal_bosses").asItem()).build();
    public static final CustomObjectiveRegistryEntry ENCHANTED_ELIXIR = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("enchanted_elixir", "Enchanted Elixir", EnchantedElixirCrystalObjective.class, EnchantedElixirCrystalObjective::new, EnchantedElixirObjective.E_KEY, EnchantedElixirObjective.class).setCrateItem(() -> xyz.iwolfking.woldsvaults.init.ModBlocks.CUSTOM_VAULT_CRATES.get("enchanted_elixir").asItem()).build();
    public static final CustomObjectiveRegistryEntry UNHINGED_SCAVENGER_HUNT = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("unhinged_scavenger", "Unhinged Scavenger Hunt", UnhingedScavengerCrystalObjective.class, UnhingedScavengerCrystalObjective::new, UnhingedScavengerObjective.E_KEY, UnhingedScavengerObjective.class).setCrateItem(() -> xyz.iwolfking.woldsvaults.init.ModBlocks.CUSTOM_VAULT_CRATES.get("unhinged_scavenger").asItem()).build();
    public static final CustomObjectiveRegistryEntry HAUNTED_BRAZIERS = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("haunted_braziers", "Haunted Braziers", HauntedBraziersCrystalObjective.class, HauntedBraziersCrystalObjective::new, HauntedBraziersObjective.E_KEY, HauntedBraziersObjective.class).setCrateItem(() -> xyz.iwolfking.woldsvaults.init.ModBlocks.CUSTOM_VAULT_CRATES.get("haunted_braziers").asItem()).build();
    public static final CustomObjectiveRegistryEntry BALLISTIC_BINGO = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("ballistic_bingo", "Ballistic Bingo", BallisticBingoCrystalObjective.class, BallisticBingoCrystalObjective::new, BallisticBingoObjective.KEY, BallisticBingoObjective.class).setCrateItem(() -> xyz.iwolfking.woldsvaults.init.ModBlocks.CUSTOM_VAULT_CRATES.get("ballistic_bingo").asItem()).build();
    public static final CustomObjectiveRegistryEntry ZEALOT = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("zealot", "Zealot", ZealotCrystalObjective.class, ZealotCrystalObjective::new, ZealotObjective.KEY, ZealotObjective.class).setCrateItem(() -> xyz.iwolfking.woldsvaults.init.ModBlocks.CUSTOM_VAULT_CRATES.get("zealot").asItem()).build();
    public static final CustomObjectiveRegistryEntry CORRUPTED = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("corrupted", "Corrupted", CorruptedCrystalObjective.class, CorruptedCrystalObjective::new, CorruptedObjective.S_KEY, CorruptedObjective.class).setCrateItem(() -> xyz.iwolfking.woldsvaults.init.ModBlocks.CUSTOM_VAULT_CRATES.get("corrupt").asItem()).build();
    public static final CustomObjectiveRegistryEntry ALCHEMY = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("alchemy", "Alchemy", AlchemyCrystalObjective.class, AlchemyCrystalObjective::new, AlchemyObjective.KEY, AlchemyObjective.class).setCrateItem(() -> xyz.iwolfking.woldsvaults.init.ModBlocks.CUSTOM_VAULT_CRATES.get("alchemy").asItem()).build();


    public static void registerCustomObjectives(RegistryEvent.Register<CustomObjectiveRegistryEntry> event) {
        getEntries().forEach(customObjectiveRegistryEntry -> {
            event.getRegistry().register(customObjectiveRegistryEntry);
        });
    }

    public static List<CustomObjectiveRegistryEntry> getEntries() {
        if(!ENTRIES.isEmpty()) {
            return ENTRIES.stream().toList();
        }

        ENTRIES.add(BRUTAL_BOSSES);
        ENTRIES.add(ENCHANTED_ELIXIR);
        ENTRIES.add(UNHINGED_SCAVENGER_HUNT);
        ENTRIES.add(HAUNTED_BRAZIERS);
        ENTRIES.add(BALLISTIC_BINGO);
        ENTRIES.add(ZEALOT);
        ENTRIES.add(CORRUPTED);
        ENTRIES.add(ALCHEMY);
        return ENTRIES.stream().toList();
    }
}
