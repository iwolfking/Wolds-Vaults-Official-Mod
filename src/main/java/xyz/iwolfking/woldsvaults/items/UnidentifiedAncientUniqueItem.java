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

/**
 * Dev and testing source of ancient uniques. Right-clicking puts the stack into the same rolling state
 * base unidentified vault gear uses: it ticks in the player inventory for 120 ticks through
 * GearRollHelper.tickToll, shuffling a candidate ancient every "hit" with the raffle sound, then
 * replaces itself in place with a finished, already identified level 100 ancient unique.
 * <p>
 * The produced stack is built through the same path a natural identify takes - the unique's own base
 * item out of the unique registry, then GearRollHelper.initializeGear - with the ancient marker
 * stamped before the roll so VaultGearTierConfig.getConfig routes the modifiers through the ancient
 * ranges exactly as it does for a drop that rolled ancient.
 * <p>
 * Implementing IdentifiableItem is what makes the base game's Identification Stand accept this item:
 * IdentificationStandBlock.use, IdentificationStandTileEntity.canOpenBookModel and
 * IdentificationStandRenderer all filter the player inventory on
 * "instanceof IdentifiableItem and getState == UNIDENTIFIED", and the stand then drives the item
 * through IdentifiableItem.instantIdentify, which is tickRoll followed by tickFinishRoll. The state
 * is mapped onto this item's own rolling flag rather than onto AttributeGearData, the way
 * JewelPouchItem maps it onto its stored jewel list, because this item carries no gear data.
 */
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

    /**
     * Starts the roll rather than finishing it, mirroring base gear: VaultGearHelper.rightClick hands off
     * to IdentifiableItem.tryStartIdentification, which either honours an Identification Tome in the off
     * hand with an instant identify or flips the stack into the rolling state and lets inventoryTick do
     * the work. A stack of more than one splits a single piece off so the roll, which can only ever
     * produce one item, never destroys the rest of the stack.
     */
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
            return InteractionResultHolder.sidedSuccess(heldStack, false);
        }

        ItemStack rolling = heldStack.getCount() > 1 ? heldStack.split(1) : heldStack;
        setState(rolling, VaultGearState.ROLLING);
        if (rolling != heldStack) {
            EntityHelper.giveItem(player, rolling);
        }
        return InteractionResultHolder.sidedSuccess(heldStack, false);
    }

    /**
     * The state the Identification Stand and every other base identification consumer read. This item has
     * no AttributeGearData to keep a VaultGearState in, so the base default - which would parse gear data
     * off a non-gear stack - is replaced by a read of this item's own rolling flag. It is never IDENTIFIED,
     * because an identified stack is no longer this item at all: it has become the ancient unique.
     */
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

    /**
     * Base gear is stacksTo(1) so its canIdentify demands a count of one; this item stacks to 16 and a
     * stack that the stand silently skipped would read in game as the stand being broken. tickFinishRoll
     * identifies the whole stack instead, the way JewelPouchItem opens up to 32 pouches from one stand use.
     */
    @Override
    public boolean canIdentify(@NotNull Player player, @NotNull ItemStack stack) {
        return !stack.isEmpty();
    }

    /**
     * The shuffle step of base identification. Reached once per tickToll hit while rolling, and once
     * immediately before tickFinishRoll on every instant identify, so an instant identify lands on a
     * uniformly drawn candidate exactly like a completed roll does.
     */
    @Override
    public void tickRoll(@NotNull ItemStack stack, @Nullable Player player) {
        shuffleCandidate(stack, player);
    }

    /**
     * The finish step of base identification, and the seam the Identification Stand ends on. The stand has
     * no container of its own - it sweeps the player's own inventory and hands us the live ItemStack out of
     * Inventory.items - so the round 8 in-place swap already addresses the right slot and is reused as is.
     * <p>
     * The identify flag is ignored on purpose: in base it only chooses initializeAndDiscoverGear over
     * initializeGear, and that difference is model discovery, which initializeAndDiscoverGear skips for
     * UNIQUE rarity anyway. Every ancient this item produces is UNIQUE, so both flags mean the same thing.
     */
    @Override
    public void tickFinishRoll(@NotNull ItemStack stack, @Nullable Player player, boolean identify) {
        identifyStack(stack, player, stack.getCount(), NO_SLOT_HINT);
    }

    /**
     * Never reached from this item's own inventoryTick, which drives the roll directly so it can pass the
     * slot hint; kept working rather than stubbed so any other caller of the base interface still rolls.
     */
    @Override
    public void inventoryIdentificationTick(@NotNull Player player, @NotNull ItemStack stack) {
        tickIdentification(stack, player, NO_SLOT_HINT);
    }

    /**
     * The same driver base gear uses: every VaultGearItem calls vaultGearTick from inventoryTick, which
     * ends in GearRollHelper.tickToll. Reusing tickToll rather than reimplementing it keeps the 120 tick
     * duration, the decelerating shuffle cadence and both roll sounds identical to base identification.
     */
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

    /**
     * The shuffling display. Base gear re-rolls its rarity and its gear model on every tickToll hit, so
     * the slot sprite and the rarity coloured name visibly cycle while it identifies; this item has one
     * sprite, so the cycling lands on the name instead - each hit picks a new ancient unique and the
     * stack renders as that name until the next hit.
     */
    private static void shuffleCandidate(ItemStack stack, Player player) {
        List<ResourceLocation> candidates = getAncientCapableUniques();
        if (candidates.isEmpty()) {
            WoldsVaults.LOGGER.error("No loaded unique has an ancient tier config; the rolling unidentified ancient unique has nothing to shuffle through.");
            return;
        }
        stack.getOrCreateTag().putString(CANDIDATE_KEY, candidates.get(RANDOM.nextInt(candidates.size())).toString());
    }

    /**
     * The one place a finished ancient is produced, shared by every entry point: the roll finishing under
     * inventoryTick, the off-hand Identification Tome, and the Identification Stand through
     * IdentifiableItem.instantIdentify.
     * <p>
     * The first result lands on whichever unique the last shuffle hit displayed, the way base gear keeps
     * whatever rarity and model its last tickGearRoll wrote; any further results from the same stack are
     * fresh uniform draws. Returns false, having consumed nothing, if the stack could not be turned into
     * at least one ancient.
     */
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

    /**
     * Swaps the finished ancient into the slot the unidentified stack was ticking in. Only ever called with
     * one ancient per item in the rolling stack, so it consumes the whole stack: the setItem path discards
     * it by overwriting the slot and the fallback has to empty it by hand to match, or a bulk identify of a
     * stack of sixteen would hand over sixteen ancients while removing one.
     * <p>
     * The slot index inventoryTick hands out is only trustworthy for the main inventory: Inventory.tick
     * walks items, armor and offhand and passes the index within each compartment, while Inventory.getItem
     * addresses one flat 0-40 space, so armor and offhand indices alias onto the first main slots. The
     * identity check catches that aliasing and the scan resolves the real slot. Anything that ticks this
     * item outside the player inventory at all - a backpack, a curio - falls through to consuming the
     * rolling stack where it sits and handing the result over, which is not in place but does not duplicate.
     * <p>
     * The Identification Stand arrives here with no slot hint and goes straight to the scan. That is correct
     * rather than a fallback: the stand owns no container, it iterates Inventory.items and hands
     * instantIdentify the live stack out of that list, so the stack really is in the player inventory and the
     * scan finds it. Mutating the list under the stand's own iteration is safe - NonNullList.set delegates
     * to List.set without touching AbstractList.modCount, so its iterator cannot fail fast. The stand's
     * second pass, over InventoryUtil.findAllItemsInMainHand, can instead hand over a stack living inside a
     * held backpack or shulker; that one misses the scan and takes the consume-and-give path, and the stand
     * writes the emptied stack back into the container itself.
     */
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

    /**
     * Builds one finished ancient unique, or an empty stack if none could be built. Every failure
     * path logs, because a silent empty stack here reads in game as an identification that did nothing.
     */
    public static ItemStack createRandomAncientUnique(Player player) {
        List<ResourceLocation> candidates = getAncientCapableUniques();
        if (candidates.isEmpty()) {
            WoldsVaults.LOGGER.error("No loaded unique has an ancient tier config; cannot generate an ancient unique. Is the unique gear config loaded?");
            return ItemStack.EMPTY;
        }

        ResourceLocation uniqueKey = candidates.get(RANDOM.nextInt(candidates.size()));
        return createAncientUnique(uniqueKey, player);
    }

    /**
     * Every unique that is both present in the loaded unique registry and carries an ancient name,
     * which is the same set AncientUniqueHelper will resolve an ancient tier config for. Derived at
     * call time rather than cached so a config reload is picked up.
     */
    public static List<ResourceLocation> getAncientCapableUniques() {
        List<ResourceLocation> candidates = new ArrayList<>();
        for (ResourceLocation uniqueKey : ModConfigs.UNIQUE_GEAR.getRegistry().keySet()) {
            if (AncientUniqueHelper.getAncientName(uniqueKey).isPresent()) {
                candidates.add(uniqueKey);
            }
        }
        return candidates;
    }

    /**
     * Rolls one named unique as an ancient. The ancient marker and the unique key are written to the
     * stack before initializeGear runs, so the tier config lookup inside initializeUniqueGear already
     * sees an ancient and rolls every modifier at ancient ranges.
     * <p>
     * The ancient display name and the loot stamp are applied afterwards on purpose. The base call
     * overwrites GEAR_NAME with the plain unique name, and leaving IS_LOOT off until after the roll
     * keeps AncientUniqueHelper.hasAncientProvenance false during the roll, so the natural identify
     * chance cannot also fire and award an ancient milestone for a testing item.
     */
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
