package xyz.iwolfking.woldsvaults.api.crystal.tasks;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public record ConcealedChaosBackfireTask() implements VaultCrystalItem.IScheduledTask {
    public static final String ID = "concealed_chaos_backfire_task";
    public static final ConcealedChaosBackfireTask INSTANCE = new ConcealedChaosBackfireTask();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(ServerPlayer player, ItemStack stack, CrystalData data) {
        data.getProperties().setUnmodifiable(true);

        data.getProperties().getLevel().ifPresent(level -> ModConfigs.VAULT_MODIFIER_POOLS
                .getRandom(WoldsVaults.id("concealed_chaos_backfire"), level, JavaRandom.ofNanoTime())
                .forEach(modifier -> data.getModifiers().add(VaultModifierStack.of(modifier))));

        data.write(stack);
    }

    public static ConcealedChaosBackfireTask deserializeNBT(CompoundTag nbt) {
        return INSTANCE;
    }
}