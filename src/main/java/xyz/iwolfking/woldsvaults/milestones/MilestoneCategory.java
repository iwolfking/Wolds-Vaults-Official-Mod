package xyz.iwolfking.woldsvaults.milestones;

import java.util.Locale;

public enum MilestoneCategory {
    LOOTING,
    COMBAT,
    THEME,
    MISC,
    CHALLENGE;

    private final String id = this.name().toLowerCase(Locale.ROOT);

    public String getId() {
        return this.id;
    }

    public static MilestoneCategory byId(String id) {
        for (MilestoneCategory category : values()) {
            if (category.id.equalsIgnoreCase(id)) {
                return category;
            }
        }
        return null;
    }
}
