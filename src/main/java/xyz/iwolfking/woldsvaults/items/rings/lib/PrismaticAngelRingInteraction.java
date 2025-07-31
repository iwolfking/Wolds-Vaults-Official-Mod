package xyz.iwolfking.woldsvaults.items.rings.lib;

import dev.denismasterherobrine.angelring.compat.curios.AbstractRingCurio;
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

public class PrismaticAngelRingInteraction {
    private static int ticksDrained;
    public static boolean once = true;

    public static boolean disabled = false;

    public PrismaticAngelRingInteraction() {
    }

    public static void sendImc() {
        InterModComms.sendTo("curios", "register_type", () -> {
            return (new SlotTypeMessage.Builder("angelring")).build();
        });
    }

    public static ICapabilityProvider initCapabilities() {

        final ICurio curio = new AbstractRingCurio(ModItems.PRISMATIC_ANGEL_RING) {
            final ItemStack stack = new ItemStack(ModItems.PRISMATIC_ANGEL_RING.asItem());

            public ItemStack getStack() {
                return this.stack;
            }



            protected boolean checkIfAllowedToFly(Player player, ItemStack stack) {
//                boolean hasAngelExpertise = false;
//                for (TieredSkill learnedTalentNode : ClientExpertiseData.getLearnedTalentNodes()) {
//                    LearnableSkill temp = learnedTalentNode.getChild();
//                    if (temp instanceof AngelExpertise) {
//                        hasAngelExpertise = true;
//                    }
//                }
//                if (!hasAngelExpertise) {
//                    return false;
//                }
                return ServerVaults.get(player.getLevel()).isEmpty() || GameruleHelper.isEnabled(ModGameRules.ALLOW_FLIGHT_IN_VAULTS, player.getLevel());
            }

            protected TranslatableComponent getNotAbleToFlyMessage() {
                return new TranslatableComponent("item.angelring.itemring.not_enough_xp");
            }

            protected void payForFlight(Player player, ItemStack stack) {}


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
