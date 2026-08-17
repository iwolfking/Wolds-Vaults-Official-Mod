package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
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
 */
public class UnidentifiedAncientUniqueItem extends BasicItem {
    private static final int ANCIENT_ITEM_LEVEL = 100;
    private static final String UNIQUE_ROLL_TYPE = "Unique";
    private static final String ROLLING_KEY = "AncientRolling";
    private static final String CANDIDATE_KEY = "AncientRollCandidate";
    private static final int ANCIENT_COLOR = 16746803;
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
        if (isRolling(heldStack)) {
            return InteractionResultHolder.pass(heldStack);
        }

        if (player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.IDENTIFICATION_TOME)) {
            ItemStack ancient = createRandomAncientUnique(player);
            if (ancient.isEmpty()) {
                player.displayClientMessage(new TextComponent("No ancient unique could be generated - see the log.").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(heldStack);
            }
            heldStack.shrink(1);
            EntityHelper.giveItem(player, ancient);
            level.playSound(null, player.blockPosition(), ModSounds.IDENTIFICATION_SFX, SoundSource.PLAYERS, 0.3F, 1.0F);
            return InteractionResultHolder.sidedSuccess(heldStack, false);
        }

        ItemStack rolling = heldStack.getCount() > 1 ? heldStack.split(1) : heldStack;
        rolling.getOrCreateTag().putBoolean(ROLLING_KEY, true);
        if (rolling != heldStack) {
            EntityHelper.giveItem(player, rolling);
        }
        return InteractionResultHolder.sidedSuccess(heldStack, false);
    }

    /**
     * The same driver base gear uses: every VaultGearItem calls vaultGearTick from inventoryTick, which
     * ends in GearRollHelper.tickToll. Reusing tickToll rather than reimplementing it keeps the 120 tick
     * duration, the decelerating shuffle cadence and both roll sounds identical to base identification.
     */
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || !isRolling(stack)) {
            return;
        }
        GearRollHelper.tickToll(stack, player, UnidentifiedAncientUniqueItem::shuffleCandidate, finished -> finishRoll(finished, player, itemSlot));
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
     * Lands the roll on whichever unique the last shuffle hit displayed, the way base gear keeps whatever
     * rarity and model its last tickGearRoll wrote.
     */
    private static void finishRoll(ItemStack stack, ServerPlayer player, int itemSlot) {
        ResourceLocation uniqueKey = readCandidate(stack);
        ItemStack ancient;
        if (uniqueKey == null) {
            WoldsVaults.LOGGER.error("An unidentified ancient unique finished rolling with no shuffled candidate; drawing one at random instead.");
            ancient = createRandomAncientUnique(player);
        } else {
            ancient = createAncientUnique(uniqueKey, player);
        }

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(ROLLING_KEY);
            tag.remove(CANDIDATE_KEY);
        }

        if (ancient.isEmpty()) {
            WoldsVaults.LOGGER.error("An unidentified ancient unique finished rolling but produced nothing; it has been reset to unidentified.");
            return;
        }
        replaceInPlace(player, stack, ancient, itemSlot);
    }

    /**
     * Swaps the finished ancient into the slot the unidentified stack was ticking in.
     * <p>
     * The slot index inventoryTick hands out is only trustworthy for the main inventory: Inventory.tick
     * walks items, armor and offhand and passes the index within each compartment, while Inventory.getItem
     * addresses one flat 0-40 space, so armor and offhand indices alias onto the first main slots. The
     * identity check catches that aliasing and the scan resolves the real slot. Anything that ticks this
     * item outside the player inventory at all - a backpack, a curio - falls through to consuming the
     * rolling stack where it sits and handing the result over, which is not in place but does not duplicate.
     */
    private static void replaceInPlace(ServerPlayer player, ItemStack rolling, ItemStack ancient, int itemSlot) {
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
        rolling.shrink(1);
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
        data.setItemLevel(ANCIENT_ITEM_LEVEL);
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
