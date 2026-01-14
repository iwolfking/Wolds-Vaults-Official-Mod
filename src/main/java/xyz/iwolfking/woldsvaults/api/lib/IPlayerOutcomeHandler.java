package xyz.iwolfking.woldsvaults.api.lib;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.random.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface IPlayerOutcomeHandler {
    List<Card> woldsVaults$getOutcomes(Player player, String id, RandomSource random);
}

