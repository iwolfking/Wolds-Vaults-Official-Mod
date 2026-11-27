package xyz.iwolfking.woldsvaults.objectives.extraction;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * The extractor item's life inside a vault: using it on a pad, the deployment marks it carries while its rig stands,
 * its rig id, the re-arm when its vault is gone, and the write-back of a run's wear when the rig is collected or
 * destroyed. The item's stats and build belong to {@link ExtractorItem}.
 */
public final class ExtractorDeployment {
    private static final String DEPLOYED = "Deployed";
    private static final String DEPLOY_VAULT = "DeployVault";
    private static final String DEPLOY_ENTITY = "DeployEntity";
    private static final String RESET_COUNTER = "ResetCounter";
    private static final String RIG_ID = "RigId";
    private static final int RESET_GRACE_TICKS = 200;

    private ExtractorDeployment() {
    }

    public static boolean isDeployed(ItemStack stack) {
        return ExtractorItem.getData(stack).getBoolean(DEPLOYED);
    }

    /** The rig entity this item placed, or null while it is not deployed. */
    @Nullable
    public static UUID getDeployedEntity(ItemStack stack) {
        CompoundTag data = ExtractorItem.getData(stack);
        return data.hasUUID(DEPLOY_ENTITY) ? data.getUUID(DEPLOY_ENTITY) : null;
    }

    /** This extractor's rig id, which binds a site's thumpers to it; null before it ever placed a thumper. */
    @Nullable
    public static UUID getRigId(ItemStack stack) {
        CompoundTag data = ExtractorItem.getData(stack);
        return data.hasUUID(RIG_ID) ? data.getUUID(RIG_ID) : null;
    }

    /** Returns this extractor's rig id, giving it a fresh one first if it has none. */
    public static UUID ensureRigId(ItemStack stack) {
        CompoundTag data = ExtractorItem.getData(stack);
        if (!data.hasUUID(RIG_ID)) {
            data.putUUID(RIG_ID, UUID.randomUUID());
            ExtractorItem.setData(stack, data);
        }
        return ExtractorItem.getData(stack).getUUID(RIG_ID);
    }

    public static void markDeployed(ItemStack stack, UUID vaultId, UUID entityId) {
        CompoundTag data = ExtractorItem.getData(stack);
        data.putBoolean(DEPLOYED, true);
        data.putUUID(DEPLOY_VAULT, vaultId);
        data.putUUID(DEPLOY_ENTITY, entityId);
        data.putInt(RESET_COUNTER, 0);
        ExtractorItem.setData(stack, data);
    }

    /** Writes a finished run's hull and attachment wear onto the item and clears its deployment marks. */
    public static void returnToItem(ItemStack stack, int durability, @Nullable float[] attachmentDurability) {
        ExtractorItem.applyRunWear(stack, durability, attachmentDurability);
        CompoundTag data = ExtractorItem.getData(stack);
        clear(data);
        ExtractorItem.setData(stack, data);
    }

    /**
     * Finds the item that placed the given rig in any online player's inventory and returns the run's wear to it.
     * When nobody online holds it, the item re-arms itself on its own timer.
     */
    public static void returnToHolder(@Nullable MinecraftServer server, UUID entityId, int durability, float[] attachmentDurability) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof ExtractorItem && entityId.equals(getDeployedEntity(stack))) {
                    returnToItem(stack, durability, attachmentDurability);
                    return;
                }
            }
        }
        WoldLogger.debug("No online player holds the extractor item of rig {}; it re-arms on its own timer.", entityId);
    }

    /** Using the item on a block: deploys it at the extraction site the block belongs to. */
    public static InteractionResult use(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (isDeployed(stack)) {
            player.displayClientMessage(new TextComponent("This extractor is already deployed.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (ExtractorItem.getDurability(stack) <= 0) {
            player.displayClientMessage(new TextComponent("This extractor is out of durability and cannot drill.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        Optional<Vault> vault = VaultUtils.getVault(level);
        if (vault.isEmpty()) {
            player.displayClientMessage(new TextComponent("Extractors only work inside extraction vaults.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        Optional<ExtractionObjective> objective = ExtractionObjective.get(vault.get());
        if (objective.isEmpty()) {
            player.displayClientMessage(new TextComponent("This vault has no extraction sites.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        return objective.get().tryDeploy(vault.get(), player, stack, context.getClickedPos());
    }

    /** Once a second while held: a deployed item kept out of its vault for {@value #RESET_GRACE_TICKS} ticks re-arms. */
    public static void tick(ItemStack stack, Level level, Entity holder) {
        if (level.isClientSide() || !isDeployed(stack) || level.getGameTime() % 20L != 0L) {
            return;
        }
        CompoundTag data = ExtractorItem.getData(stack);
        UUID deployVault = data.hasUUID(DEPLOY_VAULT) ? data.getUUID(DEPLOY_VAULT) : null;
        Optional<Vault> vault = VaultUtils.getVault(holder.level);
        if (deployVault != null && vault.isPresent() && deployVault.equals(vault.get().get(Vault.ID))) {
            data.putInt(RESET_COUNTER, 0);
            ExtractorItem.setData(stack, data);
            return;
        }
        int counter = data.getInt(RESET_COUNTER) + 20;
        if (counter < RESET_GRACE_TICKS) {
            data.putInt(RESET_COUNTER, counter);
            ExtractorItem.setData(stack, data);
            return;
        }
        clear(data);
        ExtractorItem.setData(stack, data);
        WoldLogger.debug("Extractor item held by {} re-armed itself: its deployment vault {} is gone or was left behind.",
                holder.getName().getString(), deployVault);
        if (holder instanceof Player player) {
            player.displayClientMessage(new TextComponent("Your extractor re-armed itself.").withStyle(ChatFormatting.YELLOW), false);
        }
    }

    private static void clear(CompoundTag data) {
        data.putBoolean(DEPLOYED, false);
        data.remove(DEPLOY_VAULT);
        data.remove(DEPLOY_ENTITY);
        data.remove(RESET_COUNTER);
    }
}
