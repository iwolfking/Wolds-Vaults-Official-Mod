package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.card.BoosterPackConfig;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.lib.IPlayerOutcomeHandler;
import xyz.iwolfking.woldsvaults.expertises.GamblerExpertise;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = BoosterPackConfig.class, remap = false)
public abstract class MixinBoosterPackConfig implements IPlayerOutcomeHandler {

    @Shadow
    public abstract List<Card> getOutcomes(String id, RandomSource random);

    @Unique
    Player woldsVaults$playerRef = null;

    @Override
    public List<Card> woldsVaults$getOutcomes(Player player, String id, RandomSource random) {
       if(player != null) {
           woldsVaults$playerRef = player;
       }
       return getOutcomes(id, random);
    }

    @Redirect( method = "getOutcomes", at = @At(value = "INVOKE", target = "Liskallia/vault/core/util/WeightedList;getRandom(Liskallia/vault/core/random/RandomSource;)Ljava/util/Optional;"))
    private Optional<Integer> addGamblerExpertise(WeightedList<Integer> instance, RandomSource random) {
        if(woldsVaults$playerRef == null) {
            return instance.getRandom(random);
        }

        int value = instance.getRandom(random).orElse(0);
        AtomicInteger addedRolls = new AtomicInteger(0);
        if(woldsVaults$playerRef.getServer() != null) {
            ExpertiseTree tree = PlayerExpertisesData.get(woldsVaults$playerRef.getServer()).getExpertises(woldsVaults$playerRef);
            tree.getAll(GamblerExpertise.class, Skill::isUnlocked).forEach(
                    gamblerExpertise -> {
                        addedRolls.getAndAdd(gamblerExpertise.getAdditionalChoices());
                    }
            );
            value += addedRolls.get();
        }

        woldsVaults$playerRef = null;
        return Optional.of(value);
    }
}
