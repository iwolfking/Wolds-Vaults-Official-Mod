package xyz.iwolfking.woldsvaults.mixins.angelrings;

import dev.denismasterherobrine.angelring.compat.curios.AbstractRingCurio;
import dev.denismasterherobrine.angelring.compat.curios.ClassicAngelRingIntegration;
import dev.denismasterherobrine.angelring.config.Configuration;
import dev.denismasterherobrine.angelring.register.ItemRegistry;
import dev.denismasterherobrine.angelring.utils.ExperienceUtils;
import iskallia.vault.world.data.ServerVaults;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "angelring")
        }
)
@Mixin(value = ClassicAngelRingIntegration.class, remap = false)
public abstract class MixinClassicAngelRingInteraction {
    @Shadow private static int ticksDrained;
    @Shadow public static boolean once;

    @Shadow
    public static ServerPlayer getServerPlayerInstance(UUID playerUUID) {
        return null;
    }

    /**
     * @author iwolfking
     * @reason Disable Angel Ring in vaults
     */
    @Overwrite
    public static ICapabilityProvider initCapabilities() {
        final ICurio curio = new AbstractRingCurio(ItemRegistry.ItemRing) {
            final ItemStack stack = new ItemStack(ItemRegistry.ItemRing.asItem());
            public ItemStack getStack() {
                return this.stack;
            }

            protected boolean checkIfAllowedToFly(Player player, ItemStack stack) {
                if (Configuration.XPCost.get() == 0) {
                    return true;
                } else {
                    if(ServerVaults.get(player.getUUID()).isPresent() && !player.getLevel().getGameRules().getBoolean(ModGameRules.ALLOW_FLIGHT_IN_VAULTS)) {
                        return false;
                    }
                    return ExperienceUtils.getPlayerXP(player) >= Configuration.XPCost.get();
                }
            }

            protected TranslatableComponent getNotAbleToFlyMessage() {
                return new TranslatableComponent("item.angelring.itemring.not_enough_xp");
            }

            protected void payForFlight(Player player, ItemStack stack) {
                ++ticksDrained;
                if (ticksDrained > Configuration.TicksPerDrain.get()) {
                    if (!ClassicAngelRingIntegration.once) {
                        return;
                    }

                    ServerPlayer serverPlayer = ClassicAngelRingIntegration.getServerPlayerInstance(player.getUUID());
                    if (serverPlayer != null) {
                        serverPlayer.giveExperiencePoints(-Configuration.XPCost.get());
                    }

                    ticksDrained = 0;
                    once = false;
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
}
