package xyz.iwolfking.woldsvaults.events;

import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import xyz.iwolfking.woldsvaults.init.ModGameRules;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.lib.network.packets.StopFlightMessage;

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

        if(event.player.getLevel().getGameRules().getBoolean(ModGameRules.ALLOW_FLIGHT_IN_VAULTS)) {
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
            ModNetwork.sendToClient(new StopFlightMessage(serverPlayer.getUUID()), serverPlayer);
        }
        sendFlightNotice(player);
    }

    public static void sendFlightNotice(Player player) {
        if (!player.level.isClientSide) {
            player.displayClientMessage(FlightCancellationStrings.getMessage(), false);
        }
    }
}
