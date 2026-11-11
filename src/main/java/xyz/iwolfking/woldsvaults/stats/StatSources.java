package xyz.iwolfking.woldsvaults.stats;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.StatUtils;
import iskallia.vault.util.calc.AbilityPowerHelper;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.AttributeLimitHelper;
import iskallia.vault.util.calc.BlockChanceHelper;
import iskallia.vault.util.calc.ChainHelper;
import iskallia.vault.util.calc.CooldownHelper;
import iskallia.vault.util.calc.CopiousHelper;
import iskallia.vault.util.calc.DoubleHitHelper;
import iskallia.vault.util.calc.DurabilityWearReductionHelper;
import iskallia.vault.util.calc.ItemQuantityHelper;
import iskallia.vault.util.calc.ItemRarityHelper;
import iskallia.vault.util.calc.LuckyHitHelper;
import iskallia.vault.util.calc.ResistanceHelper;
import iskallia.vault.util.calc.SoulChanceHelper;
import iskallia.vault.util.calc.StunChanceHelper;
import iskallia.vault.util.calc.ThornsHelper;
import iskallia.vault.util.calc.TrapDisarmChanceHelper;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.WoldAttributeHelper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

/**
 * The Statistics tab values that can depend on server-only state: stat listeners that only run for a server player
 * (god nodes, prestige, talents, vault modifiers), ability data kept on the server, and effects whose client copy
 * loses its subclass. The server evaluates every source for the player and syncs the numbers to the open screen;
 * the client uses the same functions as its local fallback. Keys match the stat screen table: a gear attribute id,
 * {@code cap:} plus that id for a row's cap, and {@code prominent:} plus the label for the rows beside the model.
 */
public final class StatSources {
    public static final String CAP_PREFIX = "cap:";
    public static final String PROMINENT_PREFIX = "prominent:";

    private static final Map<String, ToDoubleFunction<Player>> SOURCES = new LinkedHashMap<>();
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();

    static {
        stat(ModGearAttributes.ITEM_QUANTITY, ItemQuantityHelper::getItemQuantity);
        stat(ModGearAttributes.ITEM_RARITY, ItemRarityHelper::getItemRarity);
        stat(ModGearAttributes.COPIOUSLY, CopiousHelper::getCopiousChance);
        stat(ModGearAttributes.TRAP_DISARMING, TrapDisarmChanceHelper::getTrapDisarmChance);
        stat(ModGearAttributes.SOUL_QUANTITY, player -> SoulChanceHelper.getSoulChance(player));
        stat(ModGearAttributes.LUCKY_HIT_CHANCE, LuckyHitHelper::getLuckyHitChanceUnlimited);
        cap(ModGearAttributes.LUCKY_HIT_CHANCE, LuckyHitHelper::getLuckyHitChance);
        stat(ModGearAttributes.ABILITY_POWER, AbilityPowerHelper::getAbilityPower);
        stat(ModGearAttributes.AREA_OF_EFFECT, AreaOfEffectHelper::getAreaOfEffectUnlimited);
        cap(ModGearAttributes.AREA_OF_EFFECT, AttributeLimitHelper::getAreaOfEffectLimit);
        stat(ModGearAttributes.COOLDOWN_REDUCTION, CooldownHelper::getCooldownMultiplierUnlimited);
        cap(ModGearAttributes.COOLDOWN_REDUCTION, AttributeLimitHelper::getCooldownReductionLimit);
        stat(ModGearAttributes.RESISTANCE, player -> ResistanceHelper.getResistanceUnlimited(player, null));
        cap(ModGearAttributes.RESISTANCE, player -> WoldAttributeHelper.getResistanceLimit(player, AttributeLimitHelper.getResistanceLimit(player)));
        stat(ModGearAttributes.BLOCK, BlockChanceHelper::getBlockChanceUnlimited);
        cap(ModGearAttributes.BLOCK, AttributeLimitHelper::getBlockChanceLimit);
        stat(ModGearAttributes.DURABILITY_WEAR_REDUCTION, DurabilityWearReductionHelper::getDurabilityWearReductionUnlimited);
        cap(ModGearAttributes.DURABILITY_WEAR_REDUCTION, AttributeLimitHelper::getDurabilityWearReductionLimit);
        stat(ModGearAttributes.DOUBLE_HIT_CHANCE, DoubleHitHelper::getDoubleHitChance);
        stat(ModGearAttributes.THORNS_DAMAGE_FLAT, ThornsHelper::getAdditionalThornsFlatDamage);
        stat(ModGearAttributes.ON_HIT_CHAIN, ChainHelper::getChainCount);
        stat(ModGearAttributes.ON_HIT_STUN, player -> StunChanceHelper.getStunChance(player, null));
        stat(xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT, WoldAttributeHelper::getDodgeChance);
        stat(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_CHANCE, WoldAttributeHelper::getEchoingChance);
        stat(xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEXING_CHANCE, WoldAttributeHelper::getHexingChance);
        prominent("Damage", StatDisplayValues::averageDps);
        prominent("Defense", StatUtils::getDefence);
        prominent("Eff. HP", StatSources::effectiveHealth);
    }

    private StatSources() {
    }

    public static boolean has(String key) {
        return SOURCES.containsKey(key);
    }

    public static Map<String, ToDoubleFunction<Player>> all() {
        return Collections.unmodifiableMap(SOURCES);
    }

    /** Every source evaluated for the player; a source that throws is logged once and left out. */
    public static Map<String, Double> evaluate(Player player) {
        Map<String, Double> values = new LinkedHashMap<>();
        for (Map.Entry<String, ToDoubleFunction<Player>> source : SOURCES.entrySet()) {
            try {
                double value = source.getValue().applyAsDouble(player);
                if (Double.isFinite(value)) {
                    values.put(source.getKey(), value);
                }
            } catch (RuntimeException e) {
                if (FAILED.add(source.getKey())) {
                    WoldsVaults.LOGGER.error("Stat sync: source {} failed for {}; the screen keeps its local value", source.getKey(),
                            player.getGameProfile().getName(), e);
                }
            }
        }
        return values;
    }

    /** Effective health as the base screen computes it: max health over the share of damage that passes defense. */
    public static double effectiveHealth(Player player) {
        double passthrough = 1.0D - StatUtils.getDefence(player);
        return passthrough <= 0.0D ? player.getMaxHealth() : Math.floor(player.getMaxHealth() / passthrough);
    }

    public static String key(VaultGearAttribute<?> attribute) {
        return attribute.getRegistryName().toString();
    }

    private static void stat(VaultGearAttribute<?> attribute, ToDoubleFunction<Player> source) {
        SOURCES.put(key(attribute), source);
    }

    private static void cap(VaultGearAttribute<?> attribute, ToDoubleFunction<Player> source) {
        SOURCES.put(CAP_PREFIX + key(attribute), source);
    }

    private static void prominent(String label, ToDoubleFunction<Player> source) {
        SOURCES.put(PROMINENT_PREFIX + label, source);
    }
}
