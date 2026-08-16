package xyz.iwolfking.woldsvaults.prestige;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.prestige.core.PrestigePower;

/**
 * A prestige power that is visible in the tree but can never be learned. Used for the Legend rank
 * placeholders, which the design sheet lists with a knowledge price but no effect yet; keeping them
 * in the config reserves their ids and their slots so shipping the real effect later is a pure
 * type swap rather than a tree migration.
 */
public class UnreleasedPrestigePower extends PrestigePower {
    public UnreleasedPrestigePower() {
    }

    @Override
    public boolean canLearn(SkillContext context) {
        return false;
    }
}
