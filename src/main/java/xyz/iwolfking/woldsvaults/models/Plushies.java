package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Plushies {
    public static final DynamicModelRegistry<HandHeldModel> REGISTRY = new DynamicModelRegistry();

    public static final HandHeldModel AXOLOTL;
    public static final HandHeldModel WOLD;
    public static final HandHeldModel IWK;
    public static final HandHeldModel JOSEPH;
    public static final HandHeldModel CAIGAN;
    public static final HandHeldModel SQUISH;
    public static final HandHeldModel JACKAL;
    public static final HandHeldModel DWELLER;
    public static final HandHeldModel HRRY;
    public static final HandHeldModel SPARKLEZ;
    public static final HandHeldModel ARTIC_FOX;
    public static final HandHeldModel BEE;
    public static final HandHeldModel CAT;
    public static final HandHeldModel COW;
    public static final HandHeldModel DOLPHIN;
    public static final HandHeldModel DRAGON;
    public static final HandHeldModel FROG;
    public static final HandHeldModel LLAMA;
    public static final HandHeldModel PANDA;
    public static final HandHeldModel PARROT;
    public static final HandHeldModel SLIME;
    public static final HandHeldModel TURTLE;
    public static final HandHeldModel WARDEN;
    public static final HandHeldModel ARAEVIN;
    public static final HandHeldModel BLAHAJ;
    public static final HandHeldModel WITCH;
    public static final HandHeldModel SAFER_SPACES;

    public Plushies() {
    }

    static {
        AXOLOTL = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/axolotl"), "Aquee the Axolotl")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        WOLD = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/wold"), "Wold the Tyrant")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        IWK = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/wolf"), "Wolf the Creator")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        JOSEPH = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/joseph"), "Joseph the Filterer")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        CAIGAN = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/caigan"), "Caigan the Mouse")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        SQUISH = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/dearliest"), "Dearliest the Squishiest")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        JACKAL = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/jackal"), "Jackal the Legend")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        DWELLER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/dweller"), "Dave the Dweller")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        HRRY = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/hrry"), "Hrry the Hermit")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        SPARKLEZ = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/sparklez"), "Sparklez the Captain")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        ARTIC_FOX = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/artic_fox"), "Snowflake the Artic Fox")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BEE = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/bee"), "Billy the Bee")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        CAT = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/cat"), "Carl the Cat")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        COW = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/cow"), "Caroline the Cow")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        DOLPHIN = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/dolphin"), "Daphne the Dolphin")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        DRAGON = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/dragon"), "Drew the Dragon")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        FROG = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/frog"), "Fred the Frog")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        LLAMA = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/llama"), "Larry the Llama")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        PANDA = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/panda"), "Petey the Panda")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        PARROT = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/parrot"), "Petroclus the Parrot")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        SLIME = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/slime"), "Stevey the Slime")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        TURTLE = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/turtle"), "Tim the Turtle")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        WARDEN = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/warden"), "Wally the Warden")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        ARAEVIN = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/araevin"), "Araevin the Heathen")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BLAHAJ = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/blahaj"), "Blåhaj the Shark")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        WITCH = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/witch"), "Walda the Witch")).properties(new DynamicModelProperties());
        SAFER_SPACES = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/plushie/safer_spaces"), "Terry the Tardigrade")).properties(new DynamicModelProperties());
    }
}
