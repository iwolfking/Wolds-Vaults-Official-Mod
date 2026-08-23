package xyz.iwolfking.woldsvaults.prestige;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.prestige.core.PrestigePower;

/** A prestige power that is visible in the tree but can never be learned. */
public class UnreleasedPrestigePower extends PrestigePower {
    public UnreleasedPrestigePower() {
    }

    @Override
    public boolean canLearn(SkillContext context) {
        return false;
    }
}
