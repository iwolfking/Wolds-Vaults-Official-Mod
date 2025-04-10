package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.block.item.SoulPlaqueBlockItem;
import iskallia.vault.core.data.compound.ItemStackList;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.Event;
import iskallia.vault.core.event.common.ListenerLeaveEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.objective.AscensionObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.SoulFlameItem;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
import iskallia.vault.item.crystal.modifiers.DefaultCrystalModifiers;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(value = AscensionObjective.class, remap = false)
public abstract class MixinAscensionObjective extends Objective {

    @Shadow @Final public static FieldKey<UUID> PLAYER_UUID;
    @Shadow @Final public static FieldKey<Integer> STACKS;
    @Shadow @Final public static FieldKey<Tag> MODIFIERS;
    @Shadow @Final public static FieldKey<String> PLAYER_NAME;

    /**
     * @author iwolfking
     * @reason Override soul flame behaviour
     */
    @Overwrite
    public void initServer(VirtualWorld world, Vault vault) {
        CommonEvents.LISTENER_LEAVE.register(this, (data) -> {
            if (data.getVault() == vault) {
                if (data.getListener().getId().equals(this.get(PLAYER_UUID))) {
                    vault.ifPresent(Vault.STATS, (stats) -> {
                        StatCollector stat = stats.get(data.getListener());
                        if (stat != null) {
                            if (stat.getCompletion() == Completion.COMPLETED) {
                                PlayerVaultStats playerStats = PlayerVaultStatsData.get(world).getVaultStats(data.getListener().getId());
                                int vaultLevel = ((VaultLevel) vault.get(Vault.LEVEL)).get();
                                int playerLevel = playerStats.getVaultLevel();
                                int stacks = (Integer) this.getOr(STACKS, 0) + ((playerLevel - WoldsVaultsConfig.COMMON.soulFlameLevelAllowance.get()) <= vaultLevel ? 1 : 0);
                                CrystalModifiers modifiers = (CrystalModifiers) this.getOptional(MODIFIERS).flatMap((tag) -> CrystalData.MODIFIERS.readNbt(tag)).orElseGet(DefaultCrystalModifiers::new);
                                ((ItemStackList) stat.get(StatCollector.REWARD)).add(SoulFlameItem.create(stacks, (String) this.get(PLAYER_NAME), (UUID) this.get(PLAYER_UUID), modifiers));
                            } else {
                                ItemStack stack = new ItemStack(ModItems.EMBER);
                                stack.setCount(ModConfigs.ASCENSION.getEmberCount((Integer) this.get(STACKS)));
                                ItemStack plaque = SoulPlaqueBlockItem.create((UUID) this.get(PLAYER_UUID), (String) this.get(PLAYER_NAME), (Integer) this.get(STACKS));
                                if (!stack.isEmpty()) {
                                    ((ItemStackList) stat.get(StatCollector.REWARD)).add(stack);
                                }

                                if (!plaque.isEmpty()) {
                                    ((ItemStackList) stat.get(StatCollector.REWARD)).add(plaque);
                                }
                            }

                        }
                    });
                }
            }
        });
    }
}
