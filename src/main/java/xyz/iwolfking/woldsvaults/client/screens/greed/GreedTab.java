package xyz.iwolfking.woldsvaults.client.screens.greed;

import net.minecraft.network.chat.Component;
import xyz.iwolfking.woldsvaults.milestones.MilestoneCategory;

/**
 * The six sub-tabs shared by the player greed screen and Mr. Greedy's achievements screen. The
 * display order is the design sheet's; {@link #getCategory()} is null only for {@link #MAIN},
 * which renders the rank summary instead of an achievement list.
 */
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

    public Component getTitle() {
        return GreedTheme.lang(this.langSuffix);
    }
}
