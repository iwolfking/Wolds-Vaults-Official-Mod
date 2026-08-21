package xyz.iwolfking.woldsvaults.gods.trees.velara;

/**
 * Every node of the Velara god tree, in sheet order (God Tree Nodes rows r34-r60).
 *
 * <p>{@link Kind} decides which {@link xyz.iwolfking.woldsvaults.gods.GodNodeGate} query a node
 * answers to: stat and major nodes are strictly bound to the active tree, minor nodes also run
 * when selected in the active god's minor-transfer slots.
 *
 * <p>A stat the sheet lists as a pair ({@code 25%+, 50%+}) is two ids, not one node with two
 * ranks: the shallow placements in the tree are the base id and the deep ones are the
 * {@code _ii} id, each paying its own value per star with no ceiling. Which placement is which
 * is decided by depth in {@code tree-drafts/export_velara_wiring.py}, not here.
 */
public enum VelaraNode {
    TOUGH("velara_tough", Kind.STAT),
    TOUGH_II("velara_tough_ii", Kind.STAT),
    ARMORED("velara_armored", Kind.STAT),
    ARMORED_II("velara_armored_ii", Kind.STAT),
    IMMUNE("velara_immune", Kind.STAT),
    IMMUNE_II("velara_immune_ii", Kind.STAT),
    HEALTHY("velara_healthy", Kind.STAT),
    HEALTHY_II("velara_healthy_ii", Kind.STAT),
    FAST_REFLEXES("velara_fast_reflexes", Kind.STAT),
    FAST_REFLEXES_II("velara_fast_reflexes_ii", Kind.STAT),
    GUARDED("velara_guarded", Kind.STAT),
    GUARDED_II("velara_guarded_ii", Kind.STAT),
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
    THORNY_II("velara_thorny_ii", Kind.STAT),
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
