package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.task.GodAltarTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.TaskContext;
import iskallia.vault.task.source.EntityTaskSource;
import iskallia.vault.task.source.TaskSource;
import iskallia.vault.world.data.PlayerReputationData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.api.helper.VaultGodAffinityHelper;

@Mixin(value = GodAltarTask.class, remap = false)
public abstract class MixinGodAltarTask extends Task {

}
