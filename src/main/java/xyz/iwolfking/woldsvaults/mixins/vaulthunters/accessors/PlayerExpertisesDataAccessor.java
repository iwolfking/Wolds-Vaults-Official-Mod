package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = PlayerExpertisesData.class, remap = false)
public interface PlayerExpertisesDataAccessor {
    @Accessor("playerMap")
    Map<UUID, ExpertiseTree> woldsvaults$getPlayerMap();
}
