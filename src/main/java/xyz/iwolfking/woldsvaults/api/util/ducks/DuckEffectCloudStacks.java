package xyz.iwolfking.woldsvaults.api.util.ducks;

import iskallia.vault.entity.entity.EffectCloudEntity;

import java.util.UUID;

public interface DuckEffectCloudStacks {

    UUID woldsvaults$getOwnerId();

    boolean woldsvaults$canAbsorb(EffectCloudEntity other);

    void woldsvaults$absorb(EffectCloudEntity other);
}
