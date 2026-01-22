package xyz.iwolfking.woldsvaults.objectives.survival;

import iskallia.vault.VaultMod;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveHelper;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;

public class SurvivalVaultHelper {

    public static void setBaseVaultTimer(Vault vault) {
        vault.ifPresent(Vault.CLOCK, clock -> {
            if(clock instanceof TickClock && clock.get(TickClock.GLOBAL_TIME) < 5) {
                clock.set(TickClock.DISPLAY_TIME, 3000);
            }
        });
    }

    public static void handleKillTimeExtensions(SurvivalObjective obj, VirtualWorld world, Vault vault) {
        CommonEvents.ENTITY_DEATH.register(obj, event -> {
            if(event.getEntity().level != world) return;
            if(event.getSource().getEntity() instanceof Player) {
                int increase = ModConfigs.SURVIVAL_OBJECTIVE.getTimeRewardFor(vault.get(Vault.LEVEL).get(), event.getEntity());
                TickClock clock = vault.get(Vault.CLOCK);
                clock.set(TickClock.DISPLAY_TIME, clock.get(TickClock.DISPLAY_TIME) + increase);
            }
        });
    }


    public static void preventFruits(SurvivalObjective obj, Vault vault) {
        CommonEvents.FRUIT_EATEN.register(obj,  (data) -> {
            Vault playerVault = VaultUtils.getVault(data.getPlayer().getLevel()).orElse(null);
            if(playerVault != null && playerVault.equals(vault)) {
                data.setTime(0);
                data.getPlayer().displayClientMessage(new TranslatableComponent("vault_objective.woldsvaults.survival_fruit_disable").withStyle(ChatFormatting.RED), true);
            }
        });
    }

    public static void preventTemporalRelics(Vault vault) {
        ObjectiveHelper.addInitModifiersToVault(vault, resourceLocations -> {
            resourceLocations.add(VaultMod.id("no_temporal_shard"));
        });
    }
}
