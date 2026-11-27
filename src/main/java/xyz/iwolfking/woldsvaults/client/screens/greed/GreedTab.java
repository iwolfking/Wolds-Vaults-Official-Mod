package xyz.iwolfking.woldsvaults.client.screens.greed;

import net.minecraft.network.chat.Component;
import xyz.iwolfking.woldsvaults.milestones.MilestoneCategory;

/** The six greed sub-tabs; {@link #getCategory()} is null only for {@link #MAIN}, the rank summary. */
public enum GreedTab {
    MAIN(null, "tab.main"),
    OFFENSE(MilestoneCategory.COMBAT, "tab.offense"),
    LOOT(MilestoneCategory.LOOTING, "tab.loot"),
    VAULT(MilestoneCategory.THEME, "tab.vault"),
    MISC(MilestoneCategory.MISC, "tab.misc"),
    CHALLENGE(MilestoneCategory.CHALLENGE, "tab.challenge");

    private final MilestoneCategory category;
    private final String langSuffix;

    GreedTab(MilestoneCategory category, String langSuffix) {
        this.category = category;
        this.langSuffix = langSuffix;
    }

    public MilestoneCategory getCategory() {
        return this.category;
    }

    /** Whether this tab is a milestone list, with the search box above it. */
    public boolean isList() {
        return this.category != null;
    }

    public Component getTitle() {
        return GreedTheme.lang(this.langSuffix);
    }
}
