package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModGearAttributeGenerators;
import iskallia.vault.init.ModGearAttributeReaders;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.util.UniqueEffectGearAttribute;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModGearAttributes {
    //Utility Modifiers
    public static final VaultGearAttribute<Boolean> IS_ETCHED = attr("is_etched", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Boolean> DIVINE = attr("divine", VaultGearAttributeType.booleanType(), (ConfigurableAttributeGenerator<Boolean, ?>)ModGearAttributeGenerators.booleanFlag(), (VaultGearModifierReader<Boolean>)ModGearAttributeReaders.booleanReader("Divine", 46276), VaultGearAttributeComparator.booleanComparator());

    //New Tool Modifiers
    public static final VaultGearAttribute<Float> DISMANTLE_CHANCE = attr("dismantle_chance", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Dismantling Chance", 13388311), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Boolean> ROTATING_TOOL = attr("rotating", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.booleanReader("Stylish", 15378160), VaultGearAttributeComparator.booleanComparator());

    //New Gear Modifiers
    public static final VaultGearAttribute<Float> ECHOING_CHANCE = attr("echoing_chance", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Echoing Chance", 6886199), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> ECHOING_DAMAGE = attr("echoing_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Increased Echoing Damage", 6886199), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> DODGE_PERCENT = attr("dodge_percent", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Dodge Chance", 10389562), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Integer> SOUL_LEECH_FLAT = attr("soul_leech_flat", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Soul Leech", 10952853), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> TRIDENT_LOYALTY = attr("trident_loyalty", VaultGearAttributeType.intType(), ModGearAttributeGenerators.intRange(), ModGearAttributeReaders.addedIntReader("Loyalty", 3114911), VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> TRIDENT_RIPTIDE = attr("trident_riptide", VaultGearAttributeType.intType(), ModGearAttributeGenerators.intRange(), ModGearAttributeReaders.addedIntReader("Riptide", 9514005), VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Float> TRIDENT_WINDUP = attr("trident_wind_up_percent", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Windup Time Reduction", 12925717), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Boolean> TRIDENT_CHANNELING = attr("trident_channeling", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.booleanReader("Channeling", 12925823), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Float> CHANNELING_CHANCE = attr("trident_channeling_chance", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Channeling Chance", 12925893), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Boolean> MAGNET_ENDERGIZED = attr("endergized", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.booleanReader("Endergized", 46276), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Float> REAVING_DAMAGE = attr("reaving_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Bonus Reaving Damage", 12417954), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> HEXING_CHANCE = attr("hexing_chance", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Hexing Chance", 11468966), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Integer> PIERCING = attr("piercing", VaultGearAttributeType.intType(), ModGearAttributeGenerators.intRange(), ModGearAttributeReaders.addedIntReader("Piercing", 8847359), VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Float> RETURNING_DAMAGE = attr("returning_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Returning Damage", 8833629), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> THORNS_SCALING_DAMAGE = attr("thorns_scaling_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Thorns Scaling", 3134464), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> AP_SCALING_DAMAGE = attr("ap_scaling_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Ability Power Scaling", 11422101), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> EXECUTION_DAMAGE = attr("execution_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Execution Damage", 10302464), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> CHAINING_DAMAGE = attr("chaining_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Reduced Chaining Falloff", 6119096), VaultGearAttributeComparator.floatComparator());

    //Extended Vanilla Modifiers
    public static final VaultGearAttribute<EffectGearAttribute> UNIQUE_EFFECT = attr("unique_effect", EffectGearAttribute.type(), EffectGearAttribute.generator(), UniqueEffectGearAttribute.reader());


    //Unique Jewel Modifiers
    public static final VaultGearAttribute<Boolean> BREACHING = woldsAttr("breaching", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.booleanReader("Breaching", 10031431), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Boolean> TREASURE_AFFINITY = attr("treasure_affinity", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.booleanReader("Treasure Affinity", 16749824), VaultGearAttributeComparator.booleanComparator());


    //Map Modifiers
    //Settable Decorator Add Modifiers
    public static final VaultGearAttribute<Integer> BONUS_COIN_PILES = attr("bonus_coin_piles", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Coin Piles", 12219392), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> BONUS_GILDED = attr("bonus_gilded", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Gilded Chests", 12233728), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> BONUS_ORNATE = attr("bonus_ornate", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Ornate Chests", 12204032), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> BONUS_LIVING = attr("bonus_living", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Living Chests", 2323968), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> BONUS_WOODEN = attr("bonus_wooden", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Wooden Chests", 9401929), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> BONUS_WILD = attr("bonus_wild", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Vault Dwellers", 9379913), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> BONUS_LAVA = attr("bonus_lava", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Lava Pools", 9371648), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> BONUS_VOID_POOLS = attr("bonus_void_pools", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Void Pools", 3811217), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> BONUS_BEDROCK = attr("bonus_bedrock", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Chances for Vault Bedrock", 3805759), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());

    //Map Mob Modifiers
    public static final VaultGearAttribute<Float> MOB_HEALTH = attr("mob_health", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Health", 2925397), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_DAMAGE = attr("mob_damage", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Damage", 15901525), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_SPEED = attr("mob_speed", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Speed", 15901469), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_CRIT = attr("mob_crit", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Critical Strike Chance", 752051), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_KBR = attr("mob_kbr", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Knockback Resistance", 751998), VaultGearAttributeComparator.floatComparator());

    //Map Grouped Modifiers
    public static final VaultGearAttribute<Float> MOB_HEALTH_AND_DAMAGE = attr("mob_health_and_damage", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Health and Damage", 751952), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> GROUPED_QUANTITY = attr("grouped_quantity", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Item Quantity and Rarity", 10587216), VaultGearAttributeComparator.floatComparator());

    //Map Cascade Modifiers
    public static final VaultGearAttribute<Float> MORE_MOBS = attr("more_mobs", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("More Mobs", 5606480), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MORE_ORES = attr("more_ores", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("More Ores", 5606422), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MORE_GILDED = attr("more_gilded", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("More Gilded Chests", 12953344), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MORE_WOODEN = attr("more_wooden", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("More Wooden Chests", 5586432), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MORE_ORNATE = attr("more_ornate", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("More Ornate Chests", 5577750), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MORE_LIVING = attr("more_living", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("More Living Chests", 4368128), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MORE_COIN_PILES = attr("more_coins", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("More Coin Piles", 15315200), VaultGearAttributeComparator.floatComparator());

    //Map Implicits
    public static final VaultGearAttribute<Integer> MAP_TIER = attr("map_tier", VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Tier", 8847359), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<String> THEME = attr("vault_theme", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.themeReader("Vault Theme", 8229444, "Theme: %s"));
    public static final VaultGearAttribute<String> THEME_POOL = attr("vault_theme_pool", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.themePoolReader("Vault Theme Pool", 8222532, "Theme Pool: %s"));
    public static final VaultGearAttribute<String> OBJECTIVE = attr("vault_objective", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.objectiveReader("Vault Objective", 888947, "Objective: %s"));
    public static final VaultGearAttribute<String> VAULT_DIFFICULTY = attr("vault_difficulty", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.difficultyReader("Unknown Difficulty", 14068735, "%s"));

    //Map Increase Modifiers
    public static final VaultGearAttribute<Float> CRATE_QUANTITY = attr("crate_quantity", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Crate Quantity", 14680173), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> CATALYST_CHANCE = attr("catalyst_chance", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Catalyst Chance", 14680319), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> TRAP_CHANCE = attr("trap_chance", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Trap Chance", 8229524), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> CHAMPION_CHANCE = attr("champion_chance", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Champion Chance", 8247956), VaultGearAttributeComparator.floatComparator());

    //Map Special Modifiers
    public static final VaultGearAttribute<String> STATIC_PLACEHOLDER_MODIFIER = attr("static_modifier", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.staticPlaceholderReader("Unknown Crystal Modifier", 14706614, "%s"));
    public static final VaultGearAttribute<Float> INSCRIPTION = attr("inscription", VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.inscriptionReader("Guaranteed Special Rooms", 8833629, "%s"), VaultGearAttributeComparator.floatComparator());



    //Deprecated
    @Deprecated
    public static final VaultGearAttribute<String> WEAPON_TYPE = attr("weapon_type", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.weaponTypeReader("Weapon Type", 888888, "Type: %s"));



    @SubscribeEvent
    public static void init(RegistryEvent.Register<VaultGearAttribute<?>> event) {
         IForgeRegistry<VaultGearAttribute<?>> registry = event.getRegistry();
                      registry.register(TRIDENT_LOYALTY);
                      registry.register(TRIDENT_RIPTIDE);
                      registry.register(TRIDENT_WINDUP);
                      registry.register(TRIDENT_CHANNELING);
                      registry.register(CHANNELING_CHANCE);
                      registry.register(MAGNET_ENDERGIZED);
                      registry.register(REAVING_DAMAGE);
                      registry.register(ROTATING_TOOL);
                      registry.register(PIERCING);
                      registry.register(RETURNING_DAMAGE);
                      registry.register(HEXING_CHANCE);
                      registry.register(THEME);
                      registry.register(THEME_POOL);
                      registry.register(OBJECTIVE);
                      registry.register(MOB_HEALTH);
                      registry.register(MOB_DAMAGE);
                      registry.register(MOB_CRIT);
                      registry.register(MOB_SPEED);
                      registry.register(MOB_KBR);
                      registry.register(MOB_HEALTH_AND_DAMAGE);
                      registry.register(GROUPED_QUANTITY);
                      registry.register(STATIC_PLACEHOLDER_MODIFIER);
                      registry.register(VAULT_DIFFICULTY);
                      registry.register(INSCRIPTION);
                      registry.register(MAP_TIER);
                      registry.register(DIVINE);
                      registry.register(ECHOING_CHANCE);
                      registry.register(ECHOING_DAMAGE);
                      registry.register(DISMANTLE_CHANCE);
                      registry.register(EXECUTION_DAMAGE);
                      registry.register(CHAINING_DAMAGE);
                      registry.register(THORNS_SCALING_DAMAGE);
                      registry.register(TREASURE_AFFINITY);
                      registry.register(BREACHING);
                      registry.register(WEAPON_TYPE);
                      registry.register(UNIQUE_EFFECT);
                      registry.register(AP_SCALING_DAMAGE);
                      registry.register(IS_ETCHED);
                      registry.register(SOUL_LEECH_FLAT);
                      registry.register(DODGE_PERCENT);
                      registry.register(CRATE_QUANTITY);
                      registry.register(CATALYST_CHANCE);
                      registry.register(TRAP_CHANCE);
                      registry.register(MORE_MOBS);
                      registry.register(CHAMPION_CHANCE);
                      registry.register(MORE_ORES);
                      registry.register(MORE_GILDED);
                      registry.register(MORE_ORNATE);
                      registry.register(MORE_LIVING);
                      registry.register(MORE_WOODEN);
                      registry.register(MORE_COIN_PILES);
                      registry.register(BONUS_GILDED);
                      registry.register(BONUS_LAVA);
                      registry.register(BONUS_LIVING);
                      registry.register(BONUS_ORNATE);
                      registry.register(BONUS_WILD);
                      registry.register(BONUS_WOODEN);
                      registry.register(BONUS_VOID_POOLS);
                      registry.register(BONUS_BEDROCK);
                      registry.register(BONUS_COIN_PILES);
       }
  
    public static void registerVanillaAssociations() {
    }

    private static <T> VaultGearAttribute<T> attr(String name, VaultGearAttributeType<T> type,ConfigurableAttributeGenerator<T, ?> generator,VaultGearModifierReader<T> reader,@Nullable VaultGearAttributeComparator<T> comparator) {
        return new VaultGearAttribute<>(VaultMod.id(name), type, generator, reader, comparator);
    }

    private static <T> VaultGearAttribute<T> woldsAttr(String name, VaultGearAttributeType<T> type,ConfigurableAttributeGenerator<T, ?> generator,VaultGearModifierReader<T> reader,@Nullable VaultGearAttributeComparator<T> comparator) {
        return new VaultGearAttribute<>(WoldsVaults.id(name), type, generator, reader, comparator);
    }

    private static <T> VaultGearAttribute<T> attr(String name, VaultGearAttributeType<T> type,ConfigurableAttributeGenerator<T, ?> generator,VaultGearModifierReader<T> reader) {
        return attr(name, type, generator, reader, null);
    }

    private static <T> VaultGearAttribute<T> woldsAttr(String name, VaultGearAttributeType<T> type,ConfigurableAttributeGenerator<T, ?> generator,VaultGearModifierReader<T> reader) {
        return woldsAttr(name, type, generator, reader, null);
    }
}
