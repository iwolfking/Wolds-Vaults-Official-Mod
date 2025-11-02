package xyz.iwolfking.woldsvaults.entities.ghosts.lib;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import vazkii.quark.content.mobs.entity.Wraith;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.Optional;

public class VaultModifierWraith extends Wraith {

    private final ResourceLocation vaultModifierPool;
    public VaultModifierWraith(EntityType<? extends Wraith> type, Level worldIn, ResourceLocation vaultModifierPool) {
        super(type, worldIn);
        this.vaultModifierPool = vaultModifierPool;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entityIn) {
        boolean did = super.doHurtTarget(entityIn);
        if (did) {
            if (entityIn instanceof Player player) {
                if(ServerVaults.get(player.level).isPresent()) {
                    Optional<Vault> vaultOpt = ServerVaults.get(player.level);
                    if(vaultOpt.isPresent()) {
                        Vault vault = vaultOpt.get();
                        VaultModifierUtils.addModifierFromPool(vault, vaultModifierPool);
                    }
                }

            }
        }

        return did;
    }
}
