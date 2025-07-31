package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.init.ModBlocks;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.vhapi.api.registry.objective.CustomObjectiveRegistryEntry;
import xyz.iwolfking.woldsvaults.objectives.*;

public class ModCustomVaultObjectiveEntries {

    public static final CustomObjectiveRegistryEntry BRUTAL_BOSSES = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("brutal_bosses", "Brutal Bosses", BrutalBossesCrystalObjective.class, BrutalBossesCrystalObjective::new, BrutalBossesObjective.E_KEY, BrutalBossesObjective.class).setCrateItem(ModBlocks.VAULT_CRATE_MONOLITH).build();
    public static final CustomObjectiveRegistryEntry ENCHANTED_ELIXIR = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("enchanted_elixir", "Enchanted Elixir", EnchantedElixirCrystalObjective.class, EnchantedElixirCrystalObjective::new, EnchantedElixirObjective.E_KEY, EnchantedElixirObjective.class).setCrateItem(ModBlocks.VAULT_CRATE_ELIXIR).build();
    public static final CustomObjectiveRegistryEntry UNHINGED_SCAVENGER_HUNT = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("unhinged_scavenger", "Unhinged Scavenger Hunt", UnhingedScavengerCrystalObjective.class, UnhingedScavengerCrystalObjective::new, UnhingedScavengerObjective.E_KEY, UnhingedScavengerObjective.class).setCrateItem(ModBlocks.VAULT_CRATE_SCAVENGER).build();
    public static final CustomObjectiveRegistryEntry HAUNTED_BRAZIERS = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("haunted_braziers", "Haunted Braziers", HauntedBraziersCrystalObjective.class, HauntedBraziersCrystalObjective::new, HauntedBraziersObjective.E_KEY, HauntedBraziersObjective.class).setCrateItem(ModBlocks.VAULT_CRATE_MONOLITH).build();
    public static final CustomObjectiveRegistryEntry BALLISTIC_BINGO = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("ballistic_bingo", "Ballistic Bingo", BallisticBingoCrystalObjective.class, BallisticBingoCrystalObjective::new, BallisticBingoObjective.KEY, BallisticBingoObjective.class).setCrateItem(ModBlocks.VAULT_CRATE_BINGO).build();
    public static final CustomObjectiveRegistryEntry ZEALOT = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("zealot", "Zealot", ZealotCrystalObjective.class, ZealotCrystalObjective::new, ZealotObjective.KEY, ZealotObjective.class).setCrateItem(ModBlocks.VAULT_CRATE_ELIXIR).build();
    public static final CustomObjectiveRegistryEntry CORRUPTED = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("corrupted", "Corrupted", CorruptedCrystalObjective.class, CorruptedCrystalObjective::new, CorruptedObjective.S_KEY, CorruptedObjective.class).setCrateItem(xyz.iwolfking.woldsvaults.init.ModBlocks.VAULT_CRATE_CORRUPTED).build();
    public static final CustomObjectiveRegistryEntry ALCHEMY = new CustomObjectiveRegistryEntry.CustomObjectiveBuilder("alchemy", "Alchemy", AlchemyCrystalObjective.class, AlchemyCrystalObjective::new, AlchemyObjective.KEY, AlchemyObjective.class).setCrateItem(ModBlocks.VAULT_CRATE_ELIXIR).build();


    public static void registerCustomObjectives(RegistryEvent.Register<CustomObjectiveRegistryEntry> event) {
        event.getRegistry().register(BRUTAL_BOSSES);
        event.getRegistry().register(ENCHANTED_ELIXIR);
        event.getRegistry().register(UNHINGED_SCAVENGER_HUNT);
        event.getRegistry().register(HAUNTED_BRAZIERS);
        event.getRegistry().register(BALLISTIC_BINGO);
        event.getRegistry().register(ZEALOT);
        event.getRegistry().register(CORRUPTED);
        event.getRegistry().register(ALCHEMY);
    }
}
