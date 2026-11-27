package xyz.iwolfking.woldsvaults.client.spellcraft.keys;

import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.tree.AbilityTree;

final class ClientCooldownTicker {
    private ClientCooldownTicker() {
    }

    /** Counts the client's copy of ability cooldowns down every tick, between the server syncs that overwrite it. */
    static void tick() {
        AbilityTree tree = ClientAbilityData.getTree();
        if (tree == null) {
            return;
        }
        tree.iterate(Ability.class, ability -> ability.getCooldown().ifPresent(cooldown -> {
            cooldown.decrement();
            if (cooldown.getRemainingTicks() <= 0 && cooldown.getRemainingDelayTicks() <= 0) {
                ability.setCooldownFromSync(0, 0, 0);
            }
        }));
    }
}
