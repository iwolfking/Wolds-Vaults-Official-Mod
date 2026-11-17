package xyz.iwolfking.woldsvaults.init;

import xyz.iwolfking.woldsvaults.stats.ClientboundStatSyncMessage;
import xyz.iwolfking.woldsvaults.stats.ServerboundStatSyncRequestMessage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.network.message.*;
import xyz.iwolfking.woldsvaults.network.packets.OpenFloatingTextScreenPacket;
import xyz.iwolfking.woldsvaults.network.packets.OrbSteerMessage;
import xyz.iwolfking.woldsvaults.network.packets.ProjectileScaleMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarEditMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellEditMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellSlotKeyMessage;
import xyz.iwolfking.woldsvaults.network.packets.StopFlightMessage;
import xyz.iwolfking.woldsvaults.network.message.BrewingAltarParticleMessage;
import xyz.iwolfking.woldsvaults.network.message.ClientboundSyncGamerulesMessage;
import xyz.iwolfking.woldsvaults.network.message.ElixirParticleMessage;
import xyz.iwolfking.woldsvaults.network.message.ChampionHudMessage;
import xyz.iwolfking.woldsvaults.network.message.MagicMissileWarningMessage;
import xyz.iwolfking.woldsvaults.network.message.RampageCdmMessage;
import xyz.iwolfking.woldsvaults.network.packets.ClientboundTrinketPouchStatePacket;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundOpenTrinketPouchPacket;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundRenameTrinketPouchPresetPacket;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundSwitchWorkstationPacket;
import xyz.iwolfking.woldsvaults.network.packets.ClientboundBackpackContentsRefreshPacket;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundSetTrinketSpeedCapPacket;
import xyz.iwolfking.woldsvaults.network.packets.TimeTrialLeaderboardS2CPacket;
import xyz.iwolfking.woldsvaults.network.packets.UpdateFloatingTextPacket;
import xyz.iwolfking.woldsvaults.armory.network.ServerboundArmoryNavMessage;
import xyz.iwolfking.woldsvaults.armory.network.ServerboundArmorySaveMessage;
import xyz.iwolfking.woldsvaults.armory.network.ClientboundArmoryStateMessage;
import xyz.iwolfking.woldsvaults.network.packets.GlyphViewSyncMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellPlaceMessage;
import xyz.iwolfking.woldsvaults.network.packets.StatMirrorMessage;
import xyz.iwolfking.woldsvaults.network.packets.DepositBackpackPacket;

public class ModNetwork {
    private static int id = 0;
    private static final String PROTOCOL_VERSION = "1.0.0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(WoldsVaults.id("woldsvaults"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void init() {
        CHANNEL.registerMessage(id++, StopFlightMessage.class, StopFlightMessage::encode, StopFlightMessage::decode, StopFlightMessage::handle);
        CHANNEL.registerMessage(id++, OpenFloatingTextScreenPacket.class, OpenFloatingTextScreenPacket::encode, OpenFloatingTextScreenPacket::decode, OpenFloatingTextScreenPacket::handle);
        CHANNEL.registerMessage(id++, UpdateFloatingTextPacket.class, UpdateFloatingTextPacket::encode, UpdateFloatingTextPacket::decode, UpdateFloatingTextPacket::handle);
        CHANNEL.registerMessage(id++, TimeTrialLeaderboardS2CPacket.class, TimeTrialLeaderboardS2CPacket::encode, TimeTrialLeaderboardS2CPacket::decode, TimeTrialLeaderboardS2CPacket::handle);
        CHANNEL.registerMessage(id++, BrewingAltarParticleMessage.class, BrewingAltarParticleMessage::encode, BrewingAltarParticleMessage::decode, BrewingAltarParticleMessage::handle);
        CHANNEL.registerMessage(id++, ClientboundSyncGamerulesMessage.class, ClientboundSyncGamerulesMessage::encode, ClientboundSyncGamerulesMessage::decode, ClientboundSyncGamerulesMessage::handle);
        CHANNEL.registerMessage(id++, ElixirParticleMessage.class, ElixirParticleMessage::encode, ElixirParticleMessage::decode, ElixirParticleMessage::handle);
        CHANNEL.registerMessage(id++, MagicMissileWarningMessage.class, MagicMissileWarningMessage::encode, MagicMissileWarningMessage::decode, MagicMissileWarningMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ChampionHudMessage.class, ChampionHudMessage::encode, ChampionHudMessage::decode, ChampionHudMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LuckyHitCooldownParticleMessage.class, LuckyHitCooldownParticleMessage::encode, LuckyHitCooldownParticleMessage::decode, LuckyHitCooldownParticleMessage::handle);
        CHANNEL.registerMessage(id++, RampageCdmMessage.class, RampageCdmMessage::encode, RampageCdmMessage::decode, RampageCdmMessage::handle);
        CHANNEL.registerMessage(id++, ServerboundSetTrinketSpeedCapPacket.class, ServerboundSetTrinketSpeedCapPacket::encode, ServerboundSetTrinketSpeedCapPacket::decode, ServerboundSetTrinketSpeedCapPacket::handle);
        CHANNEL.registerMessage(id++, ServerboundOpenTrinketPouchPacket.class, ServerboundOpenTrinketPouchPacket::encode, ServerboundOpenTrinketPouchPacket::decode, ServerboundOpenTrinketPouchPacket::handle);
        CHANNEL.registerMessage(id++, ClientboundTrinketPouchStatePacket.class, ClientboundTrinketPouchStatePacket::encode, ClientboundTrinketPouchStatePacket::decode, ClientboundTrinketPouchStatePacket::handle);
        CHANNEL.registerMessage(id++, ServerboundRenameTrinketPouchPresetPacket.class, ServerboundRenameTrinketPouchPresetPacket::encode, ServerboundRenameTrinketPouchPresetPacket::decode, ServerboundRenameTrinketPouchPresetPacket::handle);
        CHANNEL.registerMessage(id++, SuppressorUpdateMessage.class, SuppressorUpdateMessage::encode, SuppressorUpdateMessage::decode, SuppressorUpdateMessage::handle);
        CHANNEL.registerMessage(id++, RiptideFxMessage.class, RiptideFxMessage::encode, RiptideFxMessage::decode, RiptideFxMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ServerboundArmoryNavMessage.class, ServerboundArmoryNavMessage::encode, ServerboundArmoryNavMessage::decode, ServerboundArmoryNavMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, ServerboundArmorySaveMessage.class, ServerboundArmorySaveMessage::encode, ServerboundArmorySaveMessage::decode, ServerboundArmorySaveMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, ClientboundArmoryStateMessage.class, ClientboundArmoryStateMessage::encode, ClientboundArmoryStateMessage::decode, ClientboundArmoryStateMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, BossDpsSnapshotMessage.class, BossDpsSnapshotMessage::encode, BossDpsSnapshotMessage::decode, BossDpsSnapshotMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ServerboundResetBossDpsMessage.class, ServerboundResetBossDpsMessage::encode, ServerboundResetBossDpsMessage::decode, ServerboundResetBossDpsMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, LightningFormMessage.class, LightningFormMessage::encode, LightningFormMessage::decode, LightningFormMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LightningDashMessage.class, LightningDashMessage::encode, LightningDashMessage::decode, LightningDashMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, LightningDashFxMessage.class, LightningDashFxMessage::encode, LightningDashFxMessage::decode, LightningDashFxMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LightningAbilityMessage.class, LightningAbilityMessage::encode, LightningAbilityMessage::decode, LightningAbilityMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, LightningArcFxMessage.class, LightningArcFxMessage::encode, LightningArcFxMessage::decode, LightningArcFxMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LightningMarkMessage.class, LightningMarkMessage::encode, LightningMarkMessage::decode, LightningMarkMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LightningStrikeFxMessage.class, LightningStrikeFxMessage::encode, LightningStrikeFxMessage::decode, LightningStrikeFxMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LightningLashFxMessage.class, LightningLashFxMessage::encode, LightningLashFxMessage::decode, LightningLashFxMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LightningChargeMessage.class, LightningChargeMessage::encode, LightningChargeMessage::decode, LightningChargeMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ServerboundSwitchWorkstationPacket.class, ServerboundSwitchWorkstationPacket::encode, ServerboundSwitchWorkstationPacket::decode, ServerboundSwitchWorkstationPacket::handle);
        CHANNEL.registerMessage(id++, DepositBackpackPacket.class, DepositBackpackPacket::encode, DepositBackpackPacket::decode, DepositBackpackPacket::handle);
        CHANNEL.registerMessage(id++, ClientboundBackpackContentsRefreshPacket.class, ClientboundBackpackContentsRefreshPacket::encode, ClientboundBackpackContentsRefreshPacket::decode, ClientboundBackpackContentsRefreshPacket::handle);
        CHANNEL.registerMessage(id++, ProjectileScaleMessage.class, ProjectileScaleMessage::encode, ProjectileScaleMessage::decode, ProjectileScaleMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, OrbSteerMessage.class, OrbSteerMessage::encode, OrbSteerMessage::decode, OrbSteerMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SpellBarSyncMessage.class, SpellBarSyncMessage::encode, SpellBarSyncMessage::decode, SpellBarSyncMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SpellSlotKeyMessage.class, SpellSlotKeyMessage::encode, SpellSlotKeyMessage::decode, SpellSlotKeyMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, SpellBarEditMessage.class, SpellBarEditMessage::encode, SpellBarEditMessage::decode, SpellBarEditMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, SpellEditMessage.class, SpellEditMessage::encode, SpellEditMessage::decode, SpellEditMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, StatMirrorMessage.class, StatMirrorMessage::encode, StatMirrorMessage::decode, StatMirrorMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, GlyphViewSyncMessage.class, GlyphViewSyncMessage::encode, GlyphViewSyncMessage::decode, GlyphViewSyncMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SpellPlaceMessage.class, SpellPlaceMessage::encode, SpellPlaceMessage::decode, SpellPlaceMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, ServerboundStatSyncRequestMessage.class, ServerboundStatSyncRequestMessage::encode, ServerboundStatSyncRequestMessage::decode, ServerboundStatSyncRequestMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, ClientboundStatSyncMessage.class, ClientboundStatSyncMessage::encode, ClientboundStatSyncMessage::decode, ClientboundStatSyncMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, VaultRodAttackMessage.class, VaultRodAttackMessage::encode, VaultRodAttackMessage::decode, VaultRodAttackMessage::handle);
        CHANNEL.registerMessage(id++, SimonSetMessage.class, SimonSetMessage::encode, SimonSetMessage::decode, SimonSetMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SimonActionMessage.class, SimonActionMessage::encode, SimonActionMessage::decode, SimonActionMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, SimonSequenceMessage.class, SimonSequenceMessage::encode, SimonSequenceMessage::decode, SimonSequenceMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SimonFxMessage.class, SimonFxMessage::encode, SimonFxMessage::decode, SimonFxMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ArcaneBeamMessage.class, ArcaneBeamMessage::encode, ArcaneBeamMessage::decode, ArcaneBeamMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ArcanePrismMessage.class, ArcanePrismMessage::encode, ArcanePrismMessage::decode, ArcanePrismMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ArcanePrismSeedMessage.class, ArcanePrismSeedMessage::encode, ArcanePrismSeedMessage::decode, ArcanePrismSeedMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ArcaneSparkleMessage.class, ArcaneSparkleMessage::encode, ArcaneSparkleMessage::decode, ArcaneSparkleMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, BulletBillMessage.class, BulletBillMessage::encode, BulletBillMessage::decode, BulletBillMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, BulletBillActionMessage.class, BulletBillActionMessage::encode, BulletBillActionMessage::decode, BulletBillActionMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, DecorativeCrystalMessage.class, DecorativeCrystalMessage::encode, DecorativeCrystalMessage::decode, DecorativeCrystalMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, LoadingZoneFadeMessage.class, LoadingZoneFadeMessage::encode, LoadingZoneFadeMessage::decode, LoadingZoneFadeMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LoadingZoneConfigMessage.class, LoadingZoneConfigMessage::encode, LoadingZoneConfigMessage::decode, LoadingZoneConfigMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, MagatsuhiSetMessage.class, MagatsuhiSetMessage::encode, MagatsuhiSetMessage::decode, MagatsuhiSetMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, MagatsuhiActionMessage.class, MagatsuhiActionMessage::encode, MagatsuhiActionMessage::decode, MagatsuhiActionMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, MagatsuhiFxMessage.class, MagatsuhiFxMessage::encode, MagatsuhiFxMessage::decode, MagatsuhiFxMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToServer(T message) {
        CHANNEL.sendToServer(message);
    }

    public static <T> void sendToClient(T message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <T> void sendToAllClients(T message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <T> void sendToLevel(T message, Level level) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public static void sendNear(ServerLevel level, Vec3 at, double range, Object message) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(at.x, at.y, at.z, range, level.dimension())), message);
    }

    // client is a () -> () -> ... lambda: naming client classes only inside the inner one keeps them off dedicated servers
    public static void onClient(Supplier<NetworkEvent.Context> context, Supplier<Runnable> client) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, client));
        context.get().setPacketHandled(true);
    }

    // runs the work on the server thread for the player who sent the packet
    public static void onServer(Supplier<NetworkEvent.Context> context, Consumer<ServerPlayer> work) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                work.accept(player);
            }
        });
        context.get().setPacketHandled(true);
    }
}
