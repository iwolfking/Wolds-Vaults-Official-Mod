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

    @Shadow private boolean expired;
    @Shadow private VaultGod god;

    @Shadow public abstract void doCompletionEffects(Vault vault, TaskContext context, boolean failure);

    @Shadow private BlockPos pos;

    /**
     * @author iwolfking
     * @reason Modify what happens when failing God Altar
     */
    @Overwrite
    public void onFail(Vault vault, TaskContext context) {
        this.expired = true;
        TaskSource var4 = context.getSource();
        if (var4 instanceof EntityTaskSource entitySource) {
            for (Player player : entitySource.getEntities(Player.class)) {
                if (PlayerReputationData.getReputation(player.getUUID(), this.god) > 0) {
                    PlayerReputationData.addReputation(player.getUUID(), this.god, -1);
                }
            }
        }

        this.doCompletionEffects(vault, context, true);
        CommonEvents.GOD_ALTAR_EVENT.invoke((GodAltarTask) (Object)this, vault, this.pos, context, false);
    }

    @Unique
    private static void woldsVaults$doFailureEffect(Player player, VaultGod god) {
        float affinity = VaultGodAffinityHelper.getAffinityPercent(player, god);

        if(player.getRandom().nextFloat() <= affinity) {
            player.displayClientMessage(new TextComponent(god.getName()).withStyle(god.getChatColor()).append(" has forgiven you for failing this time due to your affinity..."), true);
        }
        else {
            float rand = player.getRandom().nextFloat();

            if(rand <= 0.15) {
                if (PlayerReputationData.getReputation(player.getUUID(), god) > 0) {
                    PlayerReputationData.addReputation(player.getUUID(), god, -1);
                    player.displayClientMessage(new TextComponent("You have lost a reputation with ").append(new TextComponent(god.getName()).withStyle(god.getChatColor())), true);
                }
            }
            else if(rand <= 0.45)  {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, 1));
                player.displayClientMessage(new TextComponent("You have been punished for your insolence by ").append(new TextComponent(god.getName()).withStyle(god.getChatColor())), true);
            }
            else if(rand <= 0.75){
                player.hurt(DamageSource.MAGIC, player.getMaxHealth() * 0.5F);
                player.displayClientMessage(new TextComponent("You have been punished for your insolence by ").append(new TextComponent(god.getName()).withStyle(god.getChatColor())), true);
            }
            else {
                player.displayClientMessage(new TextComponent(god.getName()).withStyle(god.getChatColor()).append(" is disappointed in you."), true);
            }
        }
    }
}
