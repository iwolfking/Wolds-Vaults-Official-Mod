package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModSounds;
import iskallia.vault.item.BasicItem;
import iskallia.vault.util.EntityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dev and testing source of ancient uniques: right-clicking consumes one and yields a finished,
 * already identified level 100 ancient unique picked uniformly at random from every unique that has
 * an ancient tier config.
 * <p>
 * The produced stack is built through the same path a natural identify takes - the unique's own base
 * item out of the unique registry, then GearRollHelper.initializeGear - with the ancient marker
 * stamped before the roll so VaultGearTierConfig.getConfig routes the modifiers through the ancient
 * ranges exactly as it does for a drop that rolled ancient.
 */
public class UnidentifiedAncientUniqueItem extends BasicItem {
    private static final int ANCIENT_ITEM_LEVEL = 100;
    private static final String UNIQUE_ROLL_TYPE = "Unique";
    private static final Random RANDOM = new Random();

    public UnidentifiedAncientUniqueItem(ResourceLocation id, Properties properties) {
        super(id, properties);
        this.withTooltip(new TextComponent("Right-click to open into a random Ancient unique.").withStyle(ChatFormatting.GOLD));
        this.withTooltip(new TextComponent("Always Ancient, always item level 100.").withStyle(ChatFormatting.GRAY));
        this.withTooltip(new TextComponent("Every ancient-configured unique is equally likely.").withStyle(ChatFormatting.GRAY));
        this.withTooltip(new TextComponent("Testing item.").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(heldStack, true);
        }

        ItemStack ancient = createRandomAncientUnique(player);
        if (ancient.isEmpty()) {
            player.displayClientMessage(new TextComponent("No ancient unique could be generated - see the log.").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(heldStack);
        }

        EntityHelper.giveItem(player, ancient);
        level.playSound(null, player.blockPosition(), ModSounds.IDENTIFICATION_SFX, SoundSource.PLAYERS, 0.5F, 1.0F);
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(heldStack, false);
    }

    /**
     * Builds one finished ancient unique, or an empty stack if none could be built. Every failure
     * path logs, because a silent empty stack here reads in game as a right-click that did nothing.
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
