package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.prestige.RelicHunterPrestigePower;

import javax.annotation.Nullable;
import net.minecraft.world.item.Item;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AncientUniqueHelper {
    public static final int ANCIENT_COLOR = 16746803;
    private static final String UNIQUE_ROLL_TYPE = "Unique";
    private static final int HUNTER_TIER = 7;
    private static final int MASTER_TIER = 10;
    private static final int CHAMPION_TIER = 14;
    private static final float HUNTER_CHANCE = 0.005F;
    private static final float MASTER_CHANCE = 0.0065F;
    private static final float CHAMPION_CHANCE = 0.008F;
    private static final Map<ResourceLocation, String> ANCIENT_NAMES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Boolean> CONFIG_USABLE = new ConcurrentHashMap<>();

    static {
        vaultUnique("crystal_double_blade", "Ancient Crystal Double Blade");
        vaultUnique("honey_stick", "Ancient Honey Stick");
        vaultUnique("inflated_justice", "Ancient Inflated Justice");
        vaultUnique("iskallibur", "Ancient Iskallibur");
        woldsUnique("lava_chicken_sword", "Ancient Lava Chicken Sword");
        woldsUnique("everfrost", "Ancient Everfrost");
        woldsUnique("everflame", "Ancient Everflame");
        woldsUnique("hexblade", "Ancient Hexblade");
        woldsUnique("mineral_greatsword", "Ancient Mineral Greatsword");
        woldsUnique("grass_sword", "Ancient Grass Sword");
        woldsUnique("aurora_scissors", "Ancient Aurora Scissors");
        woldsUnique("young_kitsune", "Ancient Kitsune Blade");
        vaultUnique("butcher_axe", "Ancient Butcher's Axe");
        vaultUnique("starforge", "Ancient Starforge");
        woldsUnique("leviathan", "Ancient Leviathan");
        woldsUnique("zombie_horse_axe", "Ancient Zombie Horse Axe");
        woldsUnique("ocean_current", "Ancient Zeus's Fury");
        woldsUnique("fork_of_the_glutton", "Ancient Fork of the Glutton");
        woldsUnique("trirang", "Ancient Quad-Rang");
        woldsUnique("bamboo_fightstick", "Ancient Bamboo Fightstick");
        vaultUnique("jester", "Ancient Jester Hat");
        vaultUnique("stormcrown", "Ancient Stormcrown");
        vaultUnique("warbound_helmet", "Ancient Helm of the Warbound");
        vaultUnique("annmari", "Ancient Ann-Mari");
        vaultUnique("castle", "Ancient The Castle");
        vaultUnique("crystalplate", "Ancient Crystal Plate");
        vaultUnique("frozen_throne", "Ancient Frozen Throne");
        vaultUnique("sweetheart", "Ancient The Sweetheart");
        vaultUnique("vitalis", "Ancient Vitalis Plate");
        vaultUnique("crashguards", "Ancient Crashguards");
        vaultUnique("swarmwalkers", "Ancient Swarmwalkers");
        vaultUnique("frostguards", "Ancient Frostguards");
        vaultUnique("pacifist_sandals", "Ancient Pacifist Sandals");
        woldsUnique("plague_steppers", "Ancient Plague Steppers");
        vaultUnique("gladiator_buckler", "Ancient Gladiator's Buckler");
        vaultUnique("grim", "Ancient Grim Shield");
        vaultUnique("ivy", "Ancient Rosethorn Ivy");
        vaultUnique("pestilence_wall", "Ancient Pestilence Wall");
        vaultUnique("frostwarden", "Ancient Frostwarden");
        vaultUnique("broodmother", "Ancient The Broodmother");
        vaultUnique("ender_rings", "Ancient Ender Rings");
        vaultUnique("frozen_orb", "Ancient Frozen Orb");
        vaultUnique("echoflare", "Ancient Echoflare");
        woldsUnique("chroma_brew", "Ancient Chroma Brew");
        vaultUnique("baguette", "Ancient The Baguette");
        vaultUnique("chainlash", "Ancient Chainlash");
        vaultUnique("inferno_reach", "Ancient Steel Inferno");
        woldsUnique("pocket_penguin", "Ancient Pocket Penguin");
        woldsUnique("wicked_witch", "Ancient The Wicked Witch");
        woldsUnique("safer_spaces", "Ancient Safer Spaces");
        vaultUnique("kaleidoscope", "Ancient Kaleidoscope");
        vaultUnique("manabloom", "Ancient Manabloom");
        vaultUnique("pax", "Ancient Pax");
        vaultUnique("frozen_heart", "Ancient Frozen Heart");
        woldsUnique("shattering_jewel", "Ancient Shatterering Jewel");
        woldsUnique("eternal_stella", "Ancient Eternal Stella");
        woldsUnique("treasure_jewel", "Ancient Treasured Jewel");
        vaultUnique("quickstone", "Ancient Quickstone");
        woldsUnique("aural_magnet", "Ancient Aural Magnet");
        woldsUnique("chonknet", "Ancient Chonknet");
        woldsUnique("treasure_magnet", "Ancient Treasure Hunter's Magnet");
        woldsUnique("bloodseeking_magnet", "Ancient Bloodfetcher");
        woldsUnique("tidebreaker_reign", "Ancient Tidebreaker's Reign");
        woldsUnique("tidebreaker_heart", "Ancient Tidebreaker's Heart");
        woldsUnique("tidebreaker_leggings", "Ancient Tidebreaker's Leggings");
        woldsUnique("tidebreaker_greaves", "Ancient Tidebreaker's Greaves");
        woldsUnique("tidebreaker_tideripper", "Ancient Tideripper");
        woldsUnique("tidebreaker_hurricane", "Ancient Hurricane");
        woldsUnique("lightning_fiend_crown", "Ancient Lightning Fiend's Crown");
        woldsUnique("lightning_fiend_mantle", "Ancient Lightning Fiend's Mantle");
        woldsUnique("lightning_fiend_legguards", "Ancient Lightning Fiend's Legguards");
        woldsUnique("lightning_fiend_talons", "Ancient Lightning Fiend's Talons");
        woldsUnique("lightning_fiend_orb", "Ancient Lightning Orb");
        woldsUnique("lightning_fiend_pure_lightning", "Ancient Pure Lightning");
    }

    private AncientUniqueHelper() {
    }

    private static void vaultUnique(String path, String ancientName) {
        ancient(VaultMod.id(path), ancientName);
    }

    private static void woldsUnique(String path, String ancientName) {
        ancient(WoldsVaults.id(path), ancientName);
    }

    private static void ancient(ResourceLocation uniqueKey, String ancientName) {
        ANCIENT_NAMES.put(uniqueKey, ancientName);
    }

    public static boolean isAncient(VaultGearData data) {
        return data.getFirstValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE).orElse(false);
    }

    public static boolean isAncient(ItemStack stack) {
        return isAncient(VaultGearData.read(stack));
    }

    /**
     * Resolves the shared ancient tier table for an ancient item; empty when the item is not ancient, the unique has
     * no ancient name, or the table is missing or cannot roll any of the unique's identifiers.
     */
    public static Optional<VaultGearTierConfig> getAncientConfig(VaultGearData data) {
        if (!isAncient(data)) {
            return Optional.empty();
        }
        ResourceLocation uniqueKey = data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY).orElse(null);
        if (uniqueKey == null) {
            return Optional.empty();
        }
        if (!ANCIENT_NAMES.containsKey(uniqueKey)) {
            WoldsVaults.LOGGER.warn("Ancient unique {} has no ancient name; falling back to the normal unique config.", uniqueKey);
            return Optional.empty();
        }
        VaultGearTierConfig config = ModConfigs.VAULT_GEAR_CONFIG.get(UniqueRollTables.ANCIENT_TABLE);
        if (config == null) {
            WoldsVaults.LOGGER.warn("Ancient tier table {} is not registered; falling back to the normal unique config.", UniqueRollTables.ANCIENT_TABLE);
            return Optional.empty();
        }
        if (!isConfigUsable(config, uniqueKey)) {
            return Optional.empty();
        }
        return Optional.of(config);
    }

    /** Whether the ancient table resolves at least one of the unique's modifier identifiers; memoised per unique. */
    private static boolean isConfigUsable(VaultGearTierConfig config, ResourceLocation uniqueKey) {
        Boolean cached = CONFIG_USABLE.get(uniqueKey);
        if (cached != null) {
            return cached;
        }
        boolean usable = UniqueRollTables.resolvesAny(config, uniqueKey);
        if (!usable) {
            WoldsVaults.LOGGER.error("Ancient tier table {} resolves none of {}'s modifier identifiers - the vault_configs overlay did not load. Falling back to the normal unique config.", UniqueRollTables.ANCIENT_TABLE, uniqueKey);
        }
        CONFIG_USABLE.put(uniqueKey, usable);
        return usable;
    }

    public static void invalidateConfigCache() {
        CONFIG_USABLE.clear();
    }

    public static Optional<String> getAncientName(ResourceLocation uniqueKey) {
        return Optional.ofNullable(ANCIENT_NAMES.get(uniqueKey));
    }

    /**
     * Every registered unique that carries an ancient name. Drawing uniformly from this list weights each gear type
     * by how many ancients it holds, so every ancient is equally likely regardless of type.
     */
    public static List<ResourceLocation> getAncientCapableUniques() {
        List<ResourceLocation> candidates = new ArrayList<>();
        for (ResourceLocation uniqueKey : ModConfigs.UNIQUE_GEAR.getRegistry().keySet()) {
            if (ANCIENT_NAMES.containsKey(uniqueKey)) {
                candidates.add(uniqueKey);
            }
        }
        return candidates;
    }

    /**
     * An unidentified piece of the unique's own gear item, locked to that unique and flagged ancient, exactly as a
     * plain unidentified unique is built but with the ancient marker preset so identification never rolls the chance.
     * The caller runs it through {@code LootInitialization} so it takes the vault's item level like any other loot.
     * {@link ItemStack#EMPTY} if the unique cannot be built; failures log.
     */
    public static ItemStack createUnidentifiedAncient(ResourceLocation uniqueKey) {
        ItemStack stack = createUniqueBase(uniqueKey);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        VaultGearData data = VaultGearData.read(stack);
        data.createOrReplaceAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE, true);
        data.write(stack);
        return stack;
    }

    /** An unidentified ancient of a unique drawn uniformly from {@link #getAncientCapableUniques()}, set pieces included. */
    public static ItemStack rollUnidentifiedAncient(Random random) {
        return rollUnidentifiedAncient(random, true);
    }

    /** As {@link #rollUnidentifiedAncient(Random)}, optionally leaving set pieces out of the draw. */
    public static ItemStack rollUnidentifiedAncient(Random random, boolean includeSetPieces) {
        List<ResourceLocation> candidates = getAncientCapableUniques();
        if (!includeSetPieces) {
            candidates.removeIf(xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper::isSetPiece);
        }
        if (candidates.isEmpty()) {
            WoldsVaults.LOGGER.error("No loaded unique has an ancient name; cannot roll an unidentified ancient unique. Is the unique gear config loaded?");
            return ItemStack.EMPTY;
        }
        return createUnidentifiedAncient(candidates.get(random.nextInt(candidates.size())));
    }

    /** Builds and identifies one named ancient at the given item level; used by the give command. Failures log. */
    public static ItemStack createAncientUnique(ResourceLocation uniqueKey, Player player, int itemLevel) {
        ItemStack stack = createUniqueBase(uniqueKey);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        VaultGearData data = VaultGearData.read(stack);
        data.setItemLevel(itemLevel);
        data.setRarity(VaultGearRarity.UNIQUE);
        data.createOrReplaceAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE, true);
        data.write(stack);

        GearRollHelper.initializeGear(stack, player);

        VaultGearData rolled = VaultGearData.read(stack);
        if (!isAncient(rolled)) {
            WoldsVaults.LOGGER.error("Ancient marker was lost while rolling {}; the item will roll as a normal unique.", uniqueKey);
        }
        if (!rolled.getFirstValue(ModGearAttributes.GEAR_NAME).map(name -> name.startsWith("Ancient")).orElse(false)) {
            WoldsVaults.LOGGER.error("Ancient {} identified without its ancient name; stamping it after the roll.", uniqueKey);
            getAncientName(uniqueKey).ifPresent(name -> rolled.createOrReplaceAttributeValue(ModGearAttributes.GEAR_NAME, name));
        }
        rolled.createOrReplaceAttributeValue(ModGearAttributes.IS_LOOT, true);
        rolled.write(stack);
        return stack;
    }

    private static ItemStack createUniqueBase(ResourceLocation uniqueKey) {
        UniqueGearConfig.Entry entry = ModConfigs.UNIQUE_GEAR.getEntry(uniqueKey).orElse(null);
        if (entry == null) {
            WoldsVaults.LOGGER.error("Unique {} has an ancient name but no entry in the unique registry; skipping.", uniqueKey);
            return ItemStack.EMPTY;
        }
        Item item = entry.getItem();
        ItemStack stack = new ItemStack(item);
        if (stack.isEmpty()) {
            WoldsVaults.LOGGER.error("Unique {} resolved to an empty base item; skipping.", uniqueKey);
            return ItemStack.EMPTY;
        }
        VaultGearData data = VaultGearData.read(stack);
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, UNIQUE_ROLL_TYPE);
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_UNIQUE_POOL, ModConfigs.UNIQUE_GEAR.findPoolForUnique(uniqueKey).orElse(UniqueGearConfig.DEFAULT_POOL));
        data.createOrReplaceAttributeValue(ModGearAttributes.UNIQUE_ITEM_KEY, uniqueKey);
        data.write(stack);
        return stack;
    }

    /**
     * Ancient identify chance for a raw greed tier; the 7 / 10 / 14 gates are ranks Hunter 1, Master 1 and
     * Champion 2.
     */
    public static float getBaseIdentifyChance(int greedTier) {
        if (greedTier >= CHAMPION_TIER) {
            return CHAMPION_CHANCE;
        }
        if (greedTier >= MASTER_TIER) {
            return MASTER_CHANCE;
        }
        if (greedTier >= HUNTER_TIER) {
            return HUNTER_CHANCE;
        }
        return 0.0F;
    }

    public static float getRelicHunterMultiplier(ServerPlayer player) {
        if (player.getServer() == null) {
            WoldsVaults.LOGGER.debug("Relic Hunter multiplier requested with no server; treating as no bonus.");
            return 1.0F;
        }
        float bonus = 0.0F;
        for (RelicHunterPrestigePower power : PrestigePowerHelper.getPrestigePowersOfType(player, RelicHunterPrestigePower.class)) {
            bonus += power.getChanceMultiplier();
        }
        return 1.0F + bonus;
    }

    /**
     * Whether the gear may become ancient: it dropped inside a vault (base {@code IS_LOOT}, which crafted and
     * vendor gear never carries) or came from the black market.
     */
    public static boolean hasAncientProvenance(VaultGearData data) {
        if (data.getFirstValue(ModGearAttributes.IS_LOOT).orElse(false)) {
            return true;
        }
        return isBlackMarketOrigin(data);
    }

    public static boolean isBlackMarketOrigin(VaultGearData data) {
        return data.getFirstValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.BLACK_MARKET_ORIGIN).orElse(false);
    }

    /** Rolls whether an identifying unique becomes ancient; false unless {@code player} is a {@link ServerPlayer}. */
    public static boolean rollAncient(VaultGearData data, @Nullable Player player, Random random) {
        ResourceLocation uniqueKey = data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY).orElse(null);
        if (uniqueKey == null || !ANCIENT_NAMES.containsKey(uniqueKey)) {
            return false;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            WoldsVaults.LOGGER.debug("Skipping ancient roll for {}: identified with no server player, failing closed.", uniqueKey);
            return false;
        }
        if (!hasAncientProvenance(data)) {
            return false;
        }
        int greedTier = PlayerGreedTreeData.get(serverPlayer.getLevel()).getGreedTier(serverPlayer);
        float chance = getBaseIdentifyChance(greedTier) * getRelicHunterMultiplier(serverPlayer);
        return chance > 0.0F && random.nextFloat() < chance;
    }
}
