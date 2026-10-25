package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Daggers {
    public static final DynamicModelRegistry<HandHeldModel> REGISTRY = new DynamicModelRegistry<>();

    // Scrappy
    public static final HandHeldModel BACKSTABBER;          // Minecraft Dungeons
    public static final HandHeldModel BASIC_DAGGER;         // Minecraft Dungeons
    public static final HandHeldModel DAGGER_CHIPPED;       // Vault Hunters 1

    // Common
    public static final HandHeldModel DAGGER_FLAWED;        // Vault Hunters 1
    public static final HandHeldModel MOON_DAGGER;          // Minecraft Dungeons
    public static final HandHeldModel PRISTINE_BLADE;       // Rshtg7776 - Slay the Princess
    public static final HandHeldModel SHEER_DAGGER;         // Minecraft Dungeons
    public static final HandHeldModel SWIFT_STRIKER;        // Minecraft Dungeons
    public static final HandHeldModel TRUSTY_DAGGER;        // Breadcrumb5550

    // Rare
    public static final HandHeldModel CHORUS_SLICER;        // Breadcrumb5550
    public static final HandHeldModel CORAL_BLADE;          // Minecraft Dungeons
    public static final HandHeldModel DAGGER_FLAWLESS;      // Vault Hunters 1
    public static final HandHeldModel NAMELESS_BLADE;       // Minecraft Dungeons
    public static final HandHeldModel SICKLE;               // Minecraft Dungeons
    public static final HandHeldModel SPIRIT_KNIFE;         // Minecraft Dungeons

    // Epic
    public static final HandHeldModel CLAWS;                // EmeraldMC18
    public static final HandHeldModel DAGGER_IMPERFECT;     // Vault Hunters 1
    public static final HandHeldModel ETERNAL_KNIFE;        // Minecraft Dungeons
    public static final HandHeldModel FROST_KNIFE;          // Minecraft Dungeons
    public static final HandHeldModel SURVIVAL_KNIFE;       // Rshtg7776 - Subnautica

    // Omega
    public static final HandHeldModel DAGGER_PERFECT;       // Vault Hunters 1
    public static final HandHeldModel JUST_A_FORK;          // Breadcrumb5550
    public static final HandHeldModel NIGHTMARES_BITE;      // Minecraft Dungeons
    public static final HandHeldModel SPONGE_STRIKER;       // Minecraft Dungeons
    public static final HandHeldModel TRAITORS_RAZOR;       // Rshtg7776 - Cult of the Lamb

    // Unique
//    public static final HandHeldModel DAGGER_;

    public Daggers() {
    }

    static {
        // Scrappy
        BACKSTABBER     =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/scrappy/backstabber"),   "Backstabber")              .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        BASIC_DAGGER    =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/scrappy/basic_dagger"),  "Basic Dagger")             .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        DAGGER_CHIPPED  =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/scrappy/dagger_chipped"),"Chipped Dagger")           .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));

        // Common
        DAGGER_FLAWED   =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/common/dagger_flawed"), "Flawed Dagger")            .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        MOON_DAGGER     =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/common/moon_dagger"),   "Moon Dagger")              .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        PRISTINE_BLADE  =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/common/pristine_blade"),"Pristine Blade")           .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        SHEER_DAGGER    =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/common/sheer_dagger"),  "Sheer Dagger")             .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        SWIFT_STRIKER   =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/common/swift_striker"), "Swift Striker")            .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        TRUSTY_DAGGER   =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/common/trusty_dagger"), "Trusty Dagger")            .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));

        // Rare
        CHORUS_SLICER   =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/rare/chorus_slicer"),   "Chorus Slicer")            .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        CORAL_BLADE     =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/rare/coral_blade"),     "Coral Blade")              .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        DAGGER_FLAWLESS =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/rare/dagger_flawless"), "Flawless Dagger")          .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        NAMELESS_BLADE  =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/rare/nameless_blade"),  "Nameless Blade")           .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        SICKLE          =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/rare/sickle"),          "Sickle")                   .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        SPIRIT_KNIFE    =      (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/rare/spirit_knife"),    "Spirit Knife")             .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));

        // Epic
        CLAWS            =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/epic/claws"),           "Claws")                     .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        DAGGER_IMPERFECT =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/epic/dagger_imperfect"),"Imperfect Dagger")          .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        ETERNAL_KNIFE    =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/epic/eternal_knife"),   "Eternal Knife")             .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        FROST_KNIFE      =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/epic/frost_knife"),     "Frost Knife")               .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        SURVIVAL_KNIFE   =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/epic/survival_knife"),  "Survival Knife")            .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));


        // Omega
        DAGGER_PERFECT   =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/omega/dagger_perfect"), "Perfect Dagger")             .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        JUST_A_FORK      =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/omega/just_a_fork"),    "Just a fork")                .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        NIGHTMARES_BITE  =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/omega/nightmares_bite"),"Nightmare's Bite")           .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        SPONGE_STRIKER   =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/omega/sponge_striker"), "Sponge Striker")             .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        TRAITORS_RAZOR   =     (HandHeldModel) REGISTRY.register((HandHeldModel) new HandHeldModel(VaultMod.id("gear/dagger/omega/traitors_razor"), "Traitor's Razor")            .properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));

        //Unique
    }
}