package xyz.iwolfking.woldsvaults.gods.trees.velara;

/**
 * Every node of the Velara god tree, in sheet order (God Tree Nodes rows r34-r60).
 *
 * <p>{@link Kind} decides which {@link xyz.iwolfking.woldsvaults.gods.GodNodeGate} query a node
 * answers to: stat and major nodes are strictly bound to the active tree, minor nodes also run
 * when selected in the active god's minor-transfer slots.
 */
public enum VelaraNode {
    TOUGH("velara_tough", Kind.STAT),
    ARMORED("velara_armored", Kind.STAT),
    IMMUNE("velara_immune", Kind.STAT),
    HEALTHY("velara_healthy", Kind.STAT),
    FAST_REFLEXES("velara_fast_reflexes", Kind.STAT),
    GUARDED("velara_guarded", Kind.STAT),
    PIOUS_DEVOTION("velara_pious_devotion", Kind.STAT),
    COUNTERSTRIKE("velara_counterstrike", Kind.MINOR),
    MAGIC_ARMOR("velara_magic_armor", Kind.MINOR),
    DEFENDER_OF_THE_FAITH("velara_defender_of_the_faith", Kind.MINOR),
    SACRIFICE("velara_sacrifice", Kind.MAJOR),
    PERSERVERENCE("velara_perserverence", Kind.MINOR),
    ADAPTIVE_ARMOR("velara_adaptive_armor", Kind.MINOR),
    BOUNCE_BACK("velara_bounce_back", Kind.MINOR),
    INDOMITABLE("velara_indomitable", Kind.MINOR),
    FIELD_MEDIC("velara_field_medic", Kind.MINOR),
    THE_STONEWALL("velara_the_stonewall", Kind.MINOR),
    CACTUS("velara_cactus", Kind.MINOR),
    THORNY("velara_thorny", Kind.STAT),
    MALEDICTION("velara_malediction", Kind.MINOR),
    IMMORTAL("velara_immortal", Kind.MAJOR),
    FLEETING_PHYSICALITY("velara_fleeting_physicality", Kind.MINOR),
    STEADFAST("velara_steadfast", Kind.MINOR),
    SANITATION("velara_sanitation", Kind.MINOR),
    PRESENCE("velara_presence", Kind.MINOR),
    HEALING_FLOW("velara_healing_flow", Kind.MINOR),
    UTILIZED("velara_utilized", Kind.MINOR);

    public enum Kind {
        STAT,
        MINOR,
        MAJOR
    }

    private final String id;
    private final Kind kind;

    VelaraNode(String id, Kind kind) {
        this.id = id;
        this.kind = kind;
    }

    public String getId() {
        return this.id;
    }

    public Kind getKind() {
        return this.kind;
    }

    public static VelaraNode byId(String id) {
        for (VelaraNode node : values()) {
            if (node.id.equals(id)) {
                return node;
            }
        }
        return null;
    }
}
