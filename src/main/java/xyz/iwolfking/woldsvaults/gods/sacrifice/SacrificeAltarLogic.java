package xyz.iwolfking.woldsvaults.gods.sacrifice;

import iskallia.vault.block.entity.GreedCauldronTileEntity;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.network.ClientboundSacrificeMenuMessage;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Server logic for the Greed Cauldron's sacrificial-altar role: builds the menu snapshot, routes deposits
 * into {@link GodSacrificeData} against the owner's selected god, and fires the sacrifice on a rising
 * redstone edge once the gate is complete and the owner's god XP has filled the level.
 */
public final class SacrificeAltarLogic {
    private static final double PULL_RANGE = 4.0;

    private SacrificeAltarLogic() {
    }

    public static void openMenu(ServerPlayer player) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), buildSnapshot(player));
    }

    /** Refreshes the altar screen if the player has it open, and never opens it. */
    public static void refreshMenu(ServerPlayer player) {
        ClientboundSacrificeMenuMessage snapshot = buildSnapshot(player);
        snapshot.refreshOnly = true;
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), snapshot);
    }

    public static void selectGod(ServerPlayer player, VaultGod god) {
        GodSacrificeData.get(player.getLevel()).setSelectedGod(player.getUUID(), god);
        refreshMenu(player);
    }

    /**
     * Whether the cauldron's piped intake would take any of {@code stack} right now, backing the side handler's
     * {@code isItemValid}.
     */
    public static boolean pipeAccepts(GreedCauldronTileEntity tile, ItemStack stack) {
        if (tile.getLevel() == null || tile.getLevel().isClientSide() || tile.getOwnerUuid() == null
                || stack.isEmpty() || !(tile.getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        return depositForOwner(serverLevel, tile.getOwnerUuid(), stack, true) > 0;
    }

    public static ClientboundSacrificeMenuMessage buildSnapshot(ServerPlayer player) {
        GodSacrificeData sacrifices = GodSacrificeData.get(player.getLevel());
        GodAlignmentData alignment = GodAlignmentData.get(player.getServer());
        ClientboundSacrificeMenuMessage message = new ClientboundSacrificeMenuMessage();
        message.selectedGod = sacrifices.getSelectedGod(player.getUUID());
        for (VaultGod god : VaultGod.values()) {
            int completed = alignment.getSacrifices(player.getUUID(), god);
            ClientboundSacrificeMenuMessage.GodSnapshot snapshot = new ClientboundSacrificeMenuMessage.GodSnapshot();
            snapshot.completed = completed;
            snapshot.xpReady = alignment.getXp(player.getUUID(), god) >= GodLevels.xpForLevel(completed + 1);
            GodSacrifices.Gate gate = GodSacrifices.gate(god, completed);
            if (gate != null) {
                snapshot.gateLabel = gate.label();
                for (GodSacrifices.Entry entry : gate.entries()) {
                    snapshot.entries.add(new ClientboundSacrificeMenuMessage.EntrySnapshot(entry.item(),
                            entry.count(), sacrifices.getDeposited(player.getUUID(), god, entry.item())));
                }
            }
            message.gods.put(god, snapshot);
        }
        return message;
    }

    /**
     * Routes a side-handler insertion into the owner's current gate; returns the remainder, the accepted portion
     * being consumed.
     */
    public static ItemStack pipeInsert(GreedCauldronTileEntity tile, ItemStack stack, boolean simulate) {
        if (tile.getLevel() == null || tile.getLevel().isClientSide() || tile.getOwnerUuid() == null
                || stack.isEmpty() || !(tile.getLevel() instanceof ServerLevel serverLevel)) {
            return stack;
        }
        int accepted = depositForOwner(serverLevel, tile.getOwnerUuid(), stack, simulate);
        if (accepted <= 0) {
            return stack;
        }
        if (!simulate) {
            GreedCauldronTileEntity.spawnConsumeParticles(serverLevel, tile.getBlockPos());
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(accepted);
        return remainder;
    }

    private static int depositForOwner(ServerLevel level, UUID ownerId, ItemStack stack, boolean simulate) {
        GodSacrificeData sacrifices = GodSacrificeData.get(level);
        VaultGod god = sacrifices.getSelectedGod(ownerId);
        if (god == null) {
            return 0;
        }
        int completed = GodAlignmentData.get(level.getServer()).getSacrifices(ownerId, god);
        GodSacrifices.Gate gate = GodSacrifices.gate(god, completed);
        if (gate == null) {
            return 0;
        }
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) {
            return 0;
        }
        if (simulate) {
            int required = 0;
            for (GodSacrifices.Entry entry : gate.entries()) {
                if (entry.item().equals(itemId)) {
                    required = entry.count();
                    break;
                }
            }
            int have = sacrifices.getDeposited(ownerId, god, itemId);
            return Math.max(0, Math.min(stack.getCount(), required - have));
        }
        return sacrifices.deposit(ownerId, god, gate, itemId, stack.getCount());
    }

    /**
     * Replacement for the base cauldron tick: vacuums nearby needed items and fires the sacrifice on a rising
     * redstone edge.
     */
    public static void tickCauldron(ServerLevel level, BlockPos pos, GreedCauldronTileEntity tile) {
        if (tile.getOwnerUuid() == null || level.getGameTime() % 5L != 0L) {
            return;
        }
        if ((tile.getDemandedItem() != null && tile.getDemandedItem() != net.minecraft.world.item.Items.AIR)
                || tile.getRemainingAmount() > 0) {
            tile.syncFromSavedData(net.minecraft.world.item.Items.AIR, 0, 1.0D);
        }
        vacuumNeededItems(level, pos, tile);
        if (((SacrificeAltarEdge) tile).woldsvaults$observePower(level.hasNeighborSignal(pos))) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(tile.getOwnerUuid());
            if (owner != null) {
                attemptSacrifice(owner, level, pos);
            }
        }
    }

    /**
     * Pulls in the item entities the owner's current gate still needs: only the owner's own drops, past their
     * pickup delay.
     */
    private static void vacuumNeededItems(ServerLevel level, BlockPos pos, GreedCauldronTileEntity tile) {
        UUID ownerId = tile.getOwnerUuid();
        GodSacrificeData sacrifices = GodSacrificeData.get(level);
        VaultGod god = sacrifices.getSelectedGod(ownerId);
        if (god == null) {
            return;
        }
        int completed = GodAlignmentData.get(level.getServer()).getSacrifices(ownerId, god);
        GodSacrifices.Gate gate = GodSacrifices.gate(god, completed);
        if (gate == null) {
            return;
        }
        AABB range = new AABB(pos).inflate(PULL_RANGE);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, range, entity -> {
            if (!ownerId.equals(entity.getThrower()) || entity.hasPickUpDelay()) {
                return false;
            }
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(entity.getItem().getItem());
            if (id == null) {
                return false;
            }
            for (GodSacrifices.Entry entry : gate.entries()) {
                if (entry.item().equals(id) && sacrifices.getDeposited(ownerId, god, id) < entry.count()) {
                    return true;
                }
            }
            return false;
        });
        for (ItemEntity itemEntity : items) {
            Vec3 target = Vec3.atCenterOf(pos);
            Vec3 velocity = target.subtract(itemEntity.position()).normalize().scale(0.075D);
            itemEntity.push(velocity.x, velocity.y, velocity.z);
            if (itemEntity.blockPosition().distSqr(pos) > 4.0D) {
                continue;
            }
            ItemStack stack = itemEntity.getItem();
            int accepted = depositForOwner(level, ownerId, stack, false);
            if (accepted <= 0) {
                continue;
            }
            stack.shrink(accepted);
            if (stack.isEmpty()) {
                itemEntity.discard();
            }
            GreedCauldronTileEntity.spawnConsumeParticles(level, pos);
            level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.6F, 1.4F);
        }
    }

    public static void attemptSacrifice(ServerPlayer owner, ServerLevel level, BlockPos pos) {
        GodSacrificeData sacrifices = GodSacrificeData.get(level);
        VaultGod god = sacrifices.getSelectedGod(owner.getUUID());
        if (god == null) {
            owner.displayClientMessage(new TextComponent("No god selected at the sacrificial altar.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        GodAlignmentData alignment = GodAlignmentData.get(level.getServer());
        int completed = alignment.getSacrifices(owner.getUUID(), god);
        GodSacrifices.Gate gate = GodSacrifices.gate(god, completed);
        if (gate == null) {
            owner.displayClientMessage(new TextComponent(god.getName() + " demands no further sacrifices.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (!sacrifices.isGateComplete(owner.getUUID(), god, gate)) {
            owner.displayClientMessage(new TextComponent("The " + gate.label() + " offering to " + god.getName()
                    + " is not yet complete.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (alignment.getXp(owner.getUUID(), god) < GodLevels.xpForLevel(completed + 1)) {
            owner.displayClientMessage(new TextComponent("You need full god experience for this level before "
                    + god.getName() + " will accept the sacrifice.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        alignment.completeSacrifice(owner, god);
        sacrifices.clearProgress(owner.getUUID(), god);
        for (int i = 0; i < 5; i++) {
            GreedCauldronTileEntity.spawnConsumeParticles(level, pos);
        }
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
        owner.displayClientMessage(new TextComponent(god.getName() + " accepts your " + gate.label() + ".")
                .withStyle(ChatFormatting.GOLD), false);
        WoldsVaults.LOGGER.info("{} completed the {} sacrifice for {}.",
                owner.getGameProfile().getName(), gate.label(), god.getName());
        refreshMenu(owner);
    }

    public static boolean isOwner(BlockGetter level, BlockPos pos, Player player) {
        if (player == null) {
            return false;
        }
        return level.getBlockEntity(pos) instanceof GreedCauldronTileEntity tile
                && player.getUUID().equals(tile.getOwnerUuid());
    }
}
