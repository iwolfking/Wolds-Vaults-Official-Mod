package xyz.iwolfking.woldsvaults.gods.sacrifice;

import iskallia.vault.block.entity.GreedCauldronTileEntity;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server logic for the Greed Cauldron's sacrificial-altar role: builds the menu snapshot,
 * routes deposits (pipes, the item vacuum, anything hitting the side item handler) into
 * {@link GodSacrificeData} against the cauldron owner's selected god, and fires the sacrifice on
 * a rising redstone edge once the gate's items are complete and the owner's god XP has filled the
 * level. Deposited items are consumed on acceptance and can never be retrieved, by design.
 */
public final class SacrificeAltarLogic {
    private static final double PULL_RANGE = 4.0;
    private static final Map<GlobalPos, Boolean> LAST_POWERED = new ConcurrentHashMap<>();

    private SacrificeAltarLogic() {
    }

    public static void openMenu(ServerPlayer player) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), buildSnapshot(player));
    }

    public static void selectGod(ServerPlayer player, VaultGod god) {
        GodSacrificeData.get(player.getLevel()).setSelectedGod(player.getUUID(), god);
        openMenu(player);
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
     * Routes an insertion from the cauldron's side item handler into the owner's selected god's
     * current gate. Returns the remainder stack; the accepted portion is consumed outright.
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
     * Replacement for the base cauldron tick: vacuums nearby item entities the owner's current
     * gate still needs, and fires the sacrifice on a rising redstone edge.
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
        GlobalPos key = GlobalPos.of(level.dimension(), pos.immutable());
        boolean powered = level.hasNeighborSignal(pos);
        boolean wasPowered = LAST_POWERED.getOrDefault(key, false);
        LAST_POWERED.put(key, powered);
        if (powered && !wasPowered) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(tile.getOwnerUuid());
            if (owner != null) {
                attemptSacrifice(owner, level, pos);
            }
        }
    }

    /**
     * Pulls in the item entities the owner's current gate still needs.
     *
     * <p>Only the owner's own drops qualify. Deposits are one-way and credit the tile owner, so a
     * filter on item id alone let a chunkloaded altar swallow anything any player dropped nearby -
     * death drops included - and there is no way to get it back. Items still inside their pickup
     * delay are skipped as well, which keeps an accidental drop recoverable for the two seconds
     * before the altar takes it.
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
        openMenu(owner);
    }

    /**
     * Whether {@code player} owns the cauldron at {@code pos}. The base block only answered its
     * interaction for the owner, and the altar keeps that rule: the menu is built from the
     * clicking player's own ledger, so opening it on someone else's altar would show one player's
     * progress on another player's block while every deposit still credits the owner.
     */
    public static boolean isOwner(BlockGetter level, BlockPos pos, Player player) {
        if (player == null) {
            return false;
        }
        return level.getBlockEntity(pos) instanceof GreedCauldronTileEntity tile
                && player.getUUID().equals(tile.getOwnerUuid());
    }

    /** Drops the redstone edge memory of an altar that no longer exists. */
    public static void forget(ServerLevel level, BlockPos pos) {
        LAST_POWERED.remove(GlobalPos.of(level.dimension(), pos.immutable()));
    }

    /** Drops every altar's redstone edge memory when the server stops. */
    public static void clearAll() {
        LAST_POWERED.clear();
    }
}
