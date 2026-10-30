package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;

public class MM_Reward extends MobModifier {
    @Override
    public String getModName() {
        return "Reward";
    }

    @Override
    public boolean onDeath() {
        if(ServerVaults.get(this.getMobTarget().getLevel()).isPresent()) {
            Vault vault = ServerVaults.get(this.getMobTarget().getLevel()).get();
            return true;
        }
        return false;
    }
}
