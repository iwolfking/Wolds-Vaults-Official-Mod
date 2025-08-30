package xyz.iwolfking.woldsvaults.mixins.vaulthunters.performance.elixir;

import iskallia.vault.core.vault.objective.elixir.ElixirTask;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.ElixirParticleMessage;

@Mixin(value = ElixirTask.class, remap = false)
public class MixinElixirTask {

    /**
     * @author a1qs
     * @reason why are we making ENTITIES (replaces entities with particles)
     */
    @Overwrite
    public void summonOrbs(VirtualWorld world, Vec3 pos, int amount) {
        int size = (amount < 0 ? -1 : 1) * ModConfigs.ELIXIR.getSize(Math.abs(amount));

        for(int i = 0; i < Math.abs(size) / 2 + 3; i++) {
            ModNetwork.sendToAllClients(new ElixirParticleMessage(size));
        }

        world.playSound(null, new BlockPos(pos), SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.4f, 0.7f);
    }


}
