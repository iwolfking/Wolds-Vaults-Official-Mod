package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.block.VaultCrateBlock.Type;
import xyz.iwolfking.vhapi.api.extensions.general.VaultCrateTypeFactory;

public class ModVaultCrateTypes {
    private static final Class<?>[] SWITCH_USERS = { VaultCrateBlock.class };

    public static final Type CORRUPTED = VaultCrateTypeFactory.INSTANCE.create("CORRUPTED", ModBlocks.getCrateFor("corrupted"), SWITCH_USERS);
    public static final Type ALCHEMY = VaultCrateTypeFactory.INSTANCE.create("ALCHEMY", ModBlocks.getCrateFor("alchemy"), SWITCH_USERS);
    public static final Type BRUTAL_BOSSES = VaultCrateTypeFactory.INSTANCE.create("BRUTAL_BOSSES", ModBlocks.getCrateFor("brutal_bosses"), SWITCH_USERS);
    public static final Type ENCHANTED_ELIXIR = VaultCrateTypeFactory.INSTANCE.create("ENCHANTED_ELIXIR", ModBlocks.getCrateFor("enchanted_elixir"), SWITCH_USERS);
    public static final Type UNHINGED_SCAVENGER = VaultCrateTypeFactory.INSTANCE.create("UNHINGED_SCAVENGER", ModBlocks.getCrateFor("unhinged_scavenger"), SWITCH_USERS);
    public static final Type HAUNTED_BRAZIERS = VaultCrateTypeFactory.INSTANCE.create("HAUNTED_BRAZIERS", ModBlocks.getCrateFor("haunted_braziers"), SWITCH_USERS);
    public static final Type BALLISTIC_BINGO = VaultCrateTypeFactory.INSTANCE.create("BALLISTIC_BINGO", ModBlocks.getCrateFor("ballistic_bingo"), SWITCH_USERS);
    public static final Type ZEALOT = VaultCrateTypeFactory.INSTANCE.create("ZEALOT", ModBlocks.getCrateFor("zealot"), SWITCH_USERS);
    public static final Type SURVIVAL = VaultCrateTypeFactory.INSTANCE.create("SURVIVAL", ModBlocks.getCrateFor("survival"), SWITCH_USERS);
    public static final Type TIME_TRIAL_REWARD = VaultCrateTypeFactory.INSTANCE.create("TIME_TRIAL_REWARD", ModBlocks.getCrateFor("time_trial"), SWITCH_USERS);
    public static final Type HYPER = VaultCrateTypeFactory.INSTANCE.create("HYPER", ModBlocks.getCrateFor("hyper"), SWITCH_USERS);

    public static void init() {
    }
}