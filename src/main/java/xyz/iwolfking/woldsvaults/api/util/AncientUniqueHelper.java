package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AncientUniqueHelper {
    public static final String CONFIG_PREFIX = "unique_ancient_";
    private static final int HUNTER_TIER = 7;
    private static final int MASTER_TIER = 10;
    private static final int CHAMPION_TIER = 14;
    private static final float HUNTER_CHANCE = 0.005F;
    private static final float MASTER_CHANCE = 0.0065F;
    private static final float CHAMPION_CHANCE = 0.008F;
    private static final Map<ResourceLocation, String> ANCIENT_NAMES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> ANCIENT_CONFIG_KEYS = new LinkedHashMap<>();
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
        woldsUnique("trirang", "Ancient Tri-Rang");
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
        vaultUnique("inferno_reach", "Ancient Inferno's Reach");
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
        ANCIENT_CONFIG_KEYS.put(uniqueKey, VaultMod.id(CONFIG_PREFIX + uniqueKey.getPath()));
    }

    public static Iterable<ResourceLocation> ancientConfigKeys() {
        return Collections.unmodifiableCollection(ANCIENT_CONFIG_KEYS.values());
    }

    public static boolean isAncient(VaultGearData data) {
        return data.getFirstValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE).orElse(false);
    }

    public static boolean isAncient(ItemStack stack) {
        return isAncient(VaultGearData.read(stack));
    }

    /**
     * Resolves the per-unique ancient tier config; empty when the item is not ancient or the config is missing or
     * unusable.
     */
    public static Optional<VaultGearTierConfig> getAncientConfig(VaultGearData data) {
        if (!isAncient(data)) {
            return Optional.empty();
        }
        ResourceLocation uniqueKey = data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY).orElse(null);
        if (uniqueKey == null) {
            return Optional.empty();
        }
        ResourceLocation configKey = ANCIENT_CONFIG_KEYS.get(uniqueKey);
        if (configKey == null) {
            WoldsVaults.LOGGER.warn("Ancient unique {} has no ancient tier config; falling back to the normal unique config.", uniqueKey);
            return Optional.empty();
        }
        VaultGearTierConfig config = ModConfigs.VAULT_GEAR_CONFIG.get(configKey);
        if (config == null) {
            WoldsVaults.LOGGER.warn("Ancient tier config {} is not registered; falling back to the normal unique config.", configKey);
            return Optional.empty();
        }
        if (!isConfigUsable(configKey, config, uniqueKey)) {
            return Optional.empty();
        }
        return Optional.of(config);
    }

    /**
     * Whether the ancient tier config resolves at least one of the unique's modifier identifiers; memoised per
     * config key.
     */
    private static boolean isConfigUsable(ResourceLocation configKey, VaultGearTierConfig config, ResourceLocation uniqueKey) {
        Boolean cached = CONFIG_USABLE.get(configKey);
        if (cached != null) {
            return cached;
        }
        UniqueGearConfig.Entry entry = ModConfigs.UNIQUE_GEAR.getEntry(uniqueKey).orElse(null);
        boolean usable = entry == null || entry.getModifierIdentifiers().values().stream()
                .flatMap(List::stream)
                .anyMatch(identifier -> config.getTierGroup(identifier) != null);
        if (!usable) {
            WoldsVaults.LOGGER.error("Ancient tier config {} resolves none of {}'s modifier identifiers - the vault_configs overlay did not load. Falling back to the normal unique config.", configKey, uniqueKey);
        }
        CONFIG_USABLE.put(configKey, usable);
        return usable;
    }

    public static void invalidateConfigCache() {
        CONFIG_USABLE.clear();
    }

    public static Optional<String> getAncientName(ResourceLocation uniqueKey) {
        return Optional.ofNullable(ANCIENT_NAMES.get(uniqueKey));
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
        if (uniqueKey == null || !ANCIENT_CONFIG_KEYS.containsKey(uniqueKey)) {
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
