package xyz.iwolfking.woldsvaults.items.rings.lib;

import dev.denismasterherobrine.angelring.compat.curios.AbstractRingCurio;
import dev.denismasterherobrine.angelring.config.Configuration;
import dev.denismasterherobrine.angelring.utils.ExperienceUtils;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICurio;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ChromaticGoldAngelRingInteraction {
    private static int ticksDrained;
    public static boolean once = true;

    public ChromaticGoldAngelRingInteraction() {
    }

    public static void sendImc() {
        InterModComms.sendTo("curios", "register_type", () -> (new SlotTypeMessage.Builder("angelring")).build());
    }

    public static ICapabilityProvider initCapabilities() {
        final ICurio curio = new AbstractRingCurio(ModItems.CHROMATIC_GOLD_ANGEL_RING) {
            final ItemStack stack = new ItemStack(ModItems.CHROMATIC_GOLD_ANGEL_RING.asItem());

            public ItemStack getStack() {
                return this.stack;
            }

            protected boolean checkIfAllowedToFly(Player player, ItemStack stack) {
                if(ServerVaults.get(player.getLevel()).isPresent() && !GameruleHelper.isEnabled(ModGameRules.ALLOW_FLIGHT_IN_VAULTS, player.getLevel())) {
                    return false;
                }
                if (Configuration.XPCost.get() == 0) {
                    return true;
                } else {
                    return ExperienceUtils.getPlayerXP(player) >= Configuration.XPCost.get();
                }
            }

            protected TranslatableComponent getNotAbleToFlyMessage() {
                return new TranslatableComponent("item.angelring.itemring.not_enough_xp");
            }

            protected void payForFlight(Player player, ItemStack stack) {
                ++ChromaticGoldAngelRingInteraction.ticksDrained;
                if (ChromaticGoldAngelRingInteraction.ticksDrained > Configuration.TicksPerDrain.get() * 3) {
                    if (!ChromaticGoldAngelRingInteraction.once) {
                        return;
                    }

                    ServerPlayer serverPlayer = ChromaticGoldAngelRingInteraction.getServerPlayerInstance(player.getUUID());
                    if (serverPlayer != null) {
                        serverPlayer.giveExperiencePoints(-Configuration.XPCost.get());
                    }

                    ChromaticGoldAngelRingInteraction.ticksDrained = 0;
                }

            }


        };
        return new ICapabilityProvider() {
            private final LazyOptional<ICurio> curioOpt = LazyOptional.of(() -> curio);

            @Nonnull
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return CuriosCapability.ITEM.orEmpty(cap, this.curioOpt);
            }
        };
    }

    public static ServerPlayer getServerPlayerInstance(UUID playerUUID) {
        ServerPlayer player = null;
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        }

        return player;
    }
}
