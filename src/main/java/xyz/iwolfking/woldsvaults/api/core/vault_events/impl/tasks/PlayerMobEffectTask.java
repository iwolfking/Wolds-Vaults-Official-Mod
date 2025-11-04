package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.ref.Effect;

import java.util.ArrayList;
import java.util.List;

public class PlayerMobEffectTask implements VaultEventTask {

    private final List<Effect> effects;


    public PlayerMobEffectTask(List<Effect> effects) {
        this.effects = effects;
    }


    @Override
    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {
        for(Effect effect : effects) {
            effect.apply(player);
        }
    }

    public static class Builder {

        private final List<Effect> effects = new ArrayList<>();

        public PlayerMobEffectTask.Builder effect(MobEffect effect, int amplitude, int duration) {
            effects.add(new Effect(effect, amplitude, duration));
            return this;
        }

        public PlayerMobEffectTask build() {
            if(effects.isEmpty()) {
                effects.add(new Effect(MobEffects.HEAL, 0, 20));
            }

            return new PlayerMobEffectTask(effects);
        }

    }
}
