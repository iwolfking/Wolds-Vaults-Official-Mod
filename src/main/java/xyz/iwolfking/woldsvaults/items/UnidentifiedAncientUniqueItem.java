package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModSounds;
import iskallia.vault.item.BasicItem;
import iskallia.vault.util.EntityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Dev and testing source of ancient uniques: rolls for 120 ticks, then becomes a level 100 ancient. */
public class UnidentifiedAncientUniqueItem extends BasicItem implements IdentifiableItem {
    private static final int ANCIENT_ITEM_LEVEL = 100;
    private static final String UNIQUE_ROLL_TYPE = "Unique";
    private static final String ROLLING_KEY = "AncientRolling";
    private static final String CANDIDATE_KEY = "AncientRollCandidate";
    private static final int ANCIENT_COLOR = 16746803;
    private static final int NO_SLOT_HINT = -1;
    private static final Random RANDOM = new Random();

    public UnidentifiedAncientUniqueItem(ResourceLocation id, Properties properties) {
        super(id, properties);
    }

    /** Starts the roll, splitting one piece off a larger stack. Returns the hand slot's stack, never empty. */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(heldStack, true);
        }
        if (getState(heldStack) != VaultGearState.UNIDENTIFIED) {
            return InteractionResultHolder.pass(heldStack);
        }

        if (player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.IDENTIFICATION_TOME)) {
            if (!identifyStack(heldStack, player, 1, NO_SLOT_HINT)) {
                player.displayClientMessage(new TextComponent("No ancient unique could be generated - see the log.").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(heldStack);
            }
            level.playSound(null, player.blockPosition(), ModSounds.IDENTIFICATION_SFX, SoundSource.PLAYERS, 0.3F, 1.0F);
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), false);
        }

        ItemStack rolling = heldStack.getCount() > 1 ? heldStack.split(1) : heldStack;
        setState(rolling, VaultGearState.ROLLING);
        if (rolling != heldStack) {
            EntityHelper.giveItem(player, rolling);
        }
        return InteractionResultHolder.sidedSuccess(heldStack, false);
    }

    /** Reads this item's own rolling flag rather than gear data; never returns {@code IDENTIFIED}. */
    @Override
    public VaultGearState getState(@NotNull ItemStack stack) {
        return isRolling(stack) ? VaultGearState.ROLLING : VaultGearState.UNIDENTIFIED;
    }

    @Override
    public void setState(@NotNull ItemStack stack, @NotNull VaultGearState state) {
        if (state == VaultGearState.ROLLING) {
            stack.getOrCreateTag().putBoolean(ROLLING_KEY, true);
            return;
        }
        clearRollTags(stack);
    }

    @Override
    public boolean canIdentify(@NotNull Player player, @NotNull ItemStack stack) {
        return !stack.isEmpty();
    }

    @Override
    public void tickRoll(@NotNull ItemStack stack, @Nullable Player player) {
        shuffleCandidate(stack, player);
    }

    /** Finish step of base identification. The {@code identify} flag is ignored. */
    @Override
    public void tickFinishRoll(@NotNull ItemStack stack, @Nullable Player player, boolean identify) {
        identifyStack(stack, player, stack.getCount(), NO_SLOT_HINT);
    }

    @Override
    public void inventoryIdentificationTick(@NotNull Player player, @NotNull ItemStack stack) {
        tickIdentification(stack, player, NO_SLOT_HINT);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        tickIdentification(stack, player, itemSlot);
    }

    private static void tickIdentification(ItemStack stack, Player player, int slotHint) {
        if (!isRolling(stack)) {
            return;
        }
        GearRollHelper.tickToll(stack, player, UnidentifiedAncientUniqueItem::shuffleCandidate, finished -> identifyStack(finished, player, 1, slotHint));
    }

    private static void shuffleCandidate(ItemStack stack, Player player) {
        List<ResourceLocation> candidates = getAncientCapableUniques();
        if (candidates.isEmpty()) {
            WoldsVaults.LOGGER.error("No loaded unique has an ancient tier config; the rolling unidentified ancient unique has nothing to shuffle through.");
            return;
        }
        stack.getOrCreateTag().putString(CANDIDATE_KEY, candidates.get(RANDOM.nextInt(candidates.size())).toString());
    }

    /** Produces {@code amount} ancients from the rolling stack; false, consuming nothing, if none could be. */
    private static boolean identifyStack(ItemStack rolling, @Nullable Player player, int amount, int slotHint) {
        if (player == null) {
            WoldsVaults.LOGGER.error("An unidentified ancient unique was identified with no player to give the result to; leaving it unidentified.");
            return false;
        }
        int count = Math.min(amount, rolling.getCount());
        if (count <= 0) {
            return false;
        }

        boolean wasRolling = isRolling(rolling);
        ResourceLocation uniqueKey = readCandidate(rolling);
        if (wasRolling && uniqueKey == null) {
            WoldsVaults.LOGGER.error("An unidentified ancient unique finished rolling with no shuffled candidate; drawing one at random instead.");
        }

        List<ItemStack> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ItemStack ancient = i == 0 && uniqueKey != null ? createAncientUnique(uniqueKey, player) : createRandomAncientUnique(player);
            if (!ancient.isEmpty()) {
                results.add(ancient);
            }
        }

        if (results.isEmpty()) {
            WoldsVaults.LOGGER.error("An unidentified ancient unique was identified but produced nothing; it has been reset to unidentified.");
            clearRollTags(rolling);
            return false;
        }

        clearRollTags(rolling);
        int produced = results.size();
        if (produced >= rolling.getCount()) {
            replaceInPlace(player, rolling, results.get(0), slotHint);
            for (int i = 1; i < produced; i++) {
                EntityHelper.giveItem(player, results.get(i));
            }
            return true;
        }

        rolling.shrink(produced);
        for (ItemStack ancient : results) {
            EntityHelper.giveItem(player, ancient);
        }
        return true;
    }

    private static void clearRollTags(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(ROLLING_KEY);
            tag.remove(CANDIDATE_KEY);
        }
    }

    /** Swaps the ancient into the rolling stack's slot; outside the main inventory the result is given away. */
    private static void replaceInPlace(Player player, ItemStack rolling, ItemStack ancient, int itemSlot) {
        Inventory inventory = player.getInventory();
        if (itemSlot >= 0 && itemSlot < inventory.getContainerSize() && inventory.getItem(itemSlot) == rolling) {
            inventory.setItem(itemSlot, ancient);
            return;
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot) == rolling) {
                inventory.setItem(slot, ancient);
                return;
            }
        }
        WoldsVaults.LOGGER.error("An unidentified ancient unique finished rolling outside the player inventory (reported slot {}); consuming it and giving the result instead of replacing it in place.", itemSlot);
        rolling.setCount(0);
        EntityHelper.giveItem(player, ancient);
    }

    private static boolean isRolling(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(ROLLING_KEY);
    }

    @Nullable
    private static ResourceLocation readCandidate(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CANDIDATE_KEY, Tag.TAG_STRING)) {
            return null;
        }
        return ResourceLocation.tryParse(tag.getString(CANDIDATE_KEY));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Component defaultName = super.getName(stack);
        ResourceLocation candidate = readCandidate(stack);
        if (candidate == null) {
            return defaultName;
        }
        String ancientName = AncientUniqueHelper.getAncientName(candidate).orElse(null);
        if (ancientName == null) {
            return defaultName;
        }
        return new TextComponent(ancientName).withStyle(Style.EMPTY.withColor(ANCIENT_COLOR));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(TextComponent.EMPTY);
        if (isRolling(stack)) {
            tooltip.add(new TextComponent("Identifying...").withStyle(Style.EMPTY.withColor(ANCIENT_COLOR)));
            return;
        }
        tooltip.add(new TextComponent("Right-click to identify into a random Ancient unique.").withStyle(ChatFormatting.GOLD));
        tooltip.add(new TextComponent("Always Ancient, always item level 100.").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Every ancient-configured unique is equally likely.").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Testing item.").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Builds one finished ancient unique, or {@link ItemStack#EMPTY} if none could be built. Failures log. */
    public static ItemStack createRandomAncientUnique(Player player) {
        List<ResourceLocation> candidates = getAncientCapableUniques();
        if (candidates.isEmpty()) {
            WoldsVaults.LOGGER.error("No loaded unique has an ancient tier config; cannot generate an ancient unique. Is the unique gear config loaded?");
            return ItemStack.EMPTY;
        }

        ResourceLocation uniqueKey = candidates.get(RANDOM.nextInt(candidates.size()));
        return createAncientUnique(uniqueKey, player);
    }

    /** Every unique in the loaded registry that carries an ancient name. Derived at call time, not cached. */
    public static List<ResourceLocation> getAncientCapableUniques() {
        List<ResourceLocation> candidates = new ArrayList<>();
        for (ResourceLocation uniqueKey : ModConfigs.UNIQUE_GEAR.getRegistry().keySet()) {
            if (AncientUniqueHelper.getAncientName(uniqueKey).isPresent()) {
                candidates.add(uniqueKey);
            }
        }
        return candidates;
    }

    /** Rolls one named unique as an ancient: the marker is set before the roll, the name and stamp after. */
    public static ItemStack createAncientUnique(ResourceLocation uniqueKey, Player player) {
        return createAncientUnique(uniqueKey, player, ANCIENT_ITEM_LEVEL);
    }

    public static ItemStack createAncientUnique(ResourceLocation uniqueKey, Player player, int itemLevel) {
        UniqueGearConfig.Entry entry = ModConfigs.UNIQUE_GEAR.getEntry(uniqueKey).orElse(null);
        if (entry == null) {
            WoldsVaults.LOGGER.error("Unique {} has an ancient config but no entry in the unique registry; skipping.", uniqueKey);
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(entry.getItem());
        if (stack.isEmpty()) {
            WoldsVaults.LOGGER.error("Unique {} resolved to an empty base item; skipping.", uniqueKey);
            return ItemStack.EMPTY;
        }

        VaultGearData data = VaultGearData.read(stack);
        data.setItemLevel(itemLevel);
        data.setRarity(VaultGearRarity.UNIQUE);
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, UNIQUE_ROLL_TYPE);
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_UNIQUE_POOL, ModConfigs.UNIQUE_GEAR.findPoolForUnique(uniqueKey).orElse(UniqueGearConfig.DEFAULT_POOL));
        data.createOrReplaceAttributeValue(ModGearAttributes.UNIQUE_ITEM_KEY, uniqueKey);
        data.createOrReplaceAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE, true);
        data.write(stack);

        GearRollHelper.initializeGear(stack, player);

        VaultGearData rolled = VaultGearData.read(stack);
        if (!AncientUniqueHelper.isAncient(rolled)) {
            WoldsVaults.LOGGER.error("Ancient marker was lost while rolling {}; the item will roll as a normal unique.", uniqueKey);
        }
        AncientUniqueHelper.getAncientName(uniqueKey)
                .ifPresent(name -> rolled.createOrReplaceAttributeValue(ModGearAttributes.GEAR_NAME, name));
        rolled.createOrReplaceAttributeValue(ModGearAttributes.IS_LOOT, true);
        rolled.write(stack);
        return stack;
    }
}
