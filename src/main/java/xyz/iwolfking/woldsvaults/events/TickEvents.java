package xyz.iwolfking.woldsvaults.events;

import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.data.FlightCancellationStrings;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.lib.network.PacketHandler;

import java.util.Random;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public class TickEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if((event.player.tickCount % 20) != 0) {
            return;
        }

        if(event.side.isClient())
            WoldActiveFlags.IS_USING_SAFER_SPACE.trySet(event.player.hasEffect(ModEffects.SAFER_SPACE));

        if(!WoldsVaultsConfig.COMMON.disableFlightInVaults.get()) {
            return;
        }

        if(event.player.isCreative() || event.player.isSpectator()) {
            return;
        }

        if(event.side.isClient()) {
            if(ClientVaults.getActive().isPresent() && (event.player.getAbilities().flying ||event.player.getAbilities().mayfly)) {
                stopFlying(event.player);
            }
        }

        if(event.side.isServer()) {
            if(ServerVaults.get(event.player.getLevel()).isPresent() && (event.player.getAbilities().flying || event.player.getAbilities().mayfly)) {
                stopFlying(event.player);
            }
        }
    }


    public static void stopFlying(Player player) {
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        //punish(player);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendStopFlightToPlayer(serverPlayer);
        }
        sendFlightNotice(player);
    }

    public static void punish(Player player) {
        Random random = new Random();

        if(random.nextBoolean()) {
            int r = random.nextInt(0, 101);
            if(r >= 90) {
                player.displayClientMessage(new TextComponent("You have been shrunk for trying to fly in the vault!"), true);
                player.addEffect(new MobEffectInstance(ModEffects.SHRINKING, 500, 2));
            }
            else if(r >= 75) {
                player.displayClientMessage(new TextComponent("You have been mining fatigue from trying to fly in the vault!"), true);
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 500, 3));
            }
            else if(r >= 60) {
                player.displayClientMessage(new TextComponent("You have been slowed from trying to fly in the vault!"), true);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 500, 3));
            }
            else if(r >= 40) {
                player.displayClientMessage(new TextComponent("You have been withered from trying to fly in the vault!"), true);
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 500, 1));
            }
            else if(r >= 26) {
                player.displayClientMessage(new TextComponent("You have been blinded from trying to fly in the vault!"), true);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 500, 1));
            }
            else if(r >= 10) {
                player.displayClientMessage(new TextComponent("You have been cursed from trying to fly in the vault!"), true);
                player.addEffect(new MobEffectInstance(AMEffectRegistry.SUNBIRD_CURSE, 700, 1));
            }
            else if(r >= 0) {
                player.displayClientMessage(new TextComponent("You have been weakened from trying to fly in the vault!"), true);
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 4));
            }
        }
    }

    public static void sendFlightNotice(Player player) {
        if (!player.level.isClientSide) {
            player.displayClientMessage(FlightCancellationStrings.getMessage(), false);
        }
    }
}
