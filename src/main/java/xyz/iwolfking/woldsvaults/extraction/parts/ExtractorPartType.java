package xyz.iwolfking.woldsvaults.extraction.parts;

import java.util.Locale;
import javax.annotation.Nullable;

public enum ExtractorPartType {
    CHASSIS("chassis", "Chassis", true),
    POWER_CORE("power_core", "Power Core", false),
    DRILL("drill", "Drill", true),
    LEGS("legs", "Legs", true),
    SHIELD_ARRAY("shield_array", "Shield Array", false),
    RADIATOR("radiator", "Radiator", false),
    SCANNER("scanner", "Scanner", false),
    ATTACHMENT("attachment", "Attachment", false);

    public static final ExtractorPartType[] CORE_PARTS = {CHASSIS, POWER_CORE, DRILL, LEGS, SHIELD_ARRAY, RADIATOR, SCANNER};

    private final String id;
    private final String displayName;
    private final boolean structural;

    ExtractorPartType(String id, String displayName, boolean structural) {
        this.id = id;
        this.displayName = displayName;
        this.structural = structural;
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /** Structural parts roll from the mechanical affix pools, hardware parts from the hardware pools. */
    public boolean isStructural() {
        return this.structural;
    }

    public boolean isAttachment() {
        return this == ATTACHMENT;
    }

    @Nullable
    public static ExtractorPartType byId(String id) {
        if (id == null) {
            return null;
        }
        for (ExtractorPartType type : values()) {
            if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.id.toLowerCase(Locale.ROOT);
    }
}
