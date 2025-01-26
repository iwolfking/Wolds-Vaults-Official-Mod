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
    public static final VaultGearAttribute<Boolean> ROTATING_TOOL = attr("rotating", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.booleanReader("Stylish", 15378160), VaultGearAttributeComparator.booleanComparator());
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

    public static final VaultGearAttribute<String> THEME = attr("vault_theme", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.themeReader("Vault Theme", 888888, "Theme: %s"));
    public static final VaultGearAttribute<String> THEME_POOL = attr("vault_theme_pool", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.themePoolReader("Vault Theme Pool", 888888, "Theme Pool: %s"));
    public static final VaultGearAttribute<String> OBJECTIVE = attr("vault_objective", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.objectiveReader("Vault Objective", 888888, "Objective: %s"));
    public static final VaultGearAttribute<String> STATIC_PLACEHOLDER_MODIFIER = attr("static_modifier", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.staticPlaceholderReader("Unknown Crystal Modifier", 888888, "%s"));
    public static final VaultGearAttribute<String> VAULT_DIFFICULTY = attr("vault_difficulty", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.difficultyReader("Unknown Difficulty", 888888, "%s"));
    public static final VaultGearAttribute<Float> MOB_HEALTH = attr("mob_health",
            VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Health", 8833629), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_DAMAGE = attr("mob_damage",
            VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Damage", 8833629), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_SPEED = attr("mob_speed",
            VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Speed", 8833629), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_CRIT = attr("mob_crit",
            VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Critical Strike Chance", 8833629), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_KBR = attr("mob_kbr",
            VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Knockback Resistance", 8833629), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> MOB_HEALTH_AND_DAMAGE = attr("mob_health_and_damage",
            VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Mob Health and Damage", 8833629), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> GROUPED_QUANTITY = attr("grouped_quantity",
            VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Item Quantity and Rarity", 8833629), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> INSCRIPTION = attr("inscription",
            VaultGearAttributeType.floatType(), (ConfigurableAttributeGenerator<Float, ?>)ModGearAttributeGenerators.floatRange(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.inscriptionReader("Guaranteed Special Rooms", 8833629, "%s"), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Integer> MAP_TIER = attr("map_tier",
            VaultGearAttributeType.intType(), (ConfigurableAttributeGenerator<Integer, ?>)ModGearAttributeGenerators.intRange(), (VaultGearModifierReader<Integer>)ModGearAttributeReaders.addedIntReader("Tier", 8847359), (VaultGearAttributeComparator<Integer>)VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Boolean> DIVINE = attr("divine",
            VaultGearAttributeType.booleanType(), (ConfigurableAttributeGenerator<Boolean, ?>)ModGearAttributeGenerators.booleanFlag(), (VaultGearModifierReader<Boolean>)ModGearAttributeReaders.booleanReader("Divine", 46276), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Float> DISMANTLE_CHANCE = attr("dismantle_chance", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Dismantling Chance", 13388311), VaultGearAttributeComparator.floatComparator());

    public static final VaultGearAttribute<Float> EXECUTION_DAMAGE = attr("execution_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Execution Damage", 10302464), VaultGearAttributeComparator.floatComparator());

//    public static final VaultGearAttribute<Float> CHAINING_DAMAGE = attr("chaining_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Reduced Chaining Falloff", 6119096), VaultGearAttributeComparator.floatComparator());

    public static final VaultGearAttribute<Float> THORNS_SCALING_DAMAGE = attr("thorns_scaling_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Thorns Scaling", 3134464), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> AP_SCALING_DAMAGE = attr("ap_scaling_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Ability Power Scaling", 11422101), VaultGearAttributeComparator.floatComparator());

    public static final VaultGearAttribute<Boolean> TREASURE_AFFINITY = attr("treasure_affinity", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.booleanReader("Treasure Affinity", 16749824), VaultGearAttributeComparator.booleanComparator());

    public static final VaultGearAttribute<Boolean> BREACHING = attr("breaching", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.booleanReader("Breaching", 10031431), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<String> WEAPON_TYPE = attr("weapon_type", VaultGearAttributeType.stringType(), xyz.iwolfking.woldsvaults.init.ModGearAttributeGenerators.stringValue(), xyz.iwolfking.woldsvaults.init.ModGearAttributeReaders.weaponTypeReader("Weapon Type", 888888, "Type: %s"));
    public static final VaultGearAttribute<EffectGearAttribute> UNIQUE_EFFECT = attr("unique_effect", EffectGearAttribute.type(), EffectGearAttribute.generator(), UniqueEffectGearAttribute.reader());
    public static final VaultGearAttribute<Float> ECHOING_CHANCE = attr("echoing_chance", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Echoing Chance", 6886199), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> ECHOING_DAMAGE = attr("echoing_damage", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.percentageReader("Increased Echoing Damage", 6886199), VaultGearAttributeComparator.floatComparator());



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
                      //        registry.register(CHAINING_DAMAGE);
                      registry.register(THORNS_SCALING_DAMAGE);
                      registry.register(TREASURE_AFFINITY);
                      registry.register(BREACHING);
                      registry.register(WEAPON_TYPE);
                      registry.register(UNIQUE_EFFECT);
                      registry.register(AP_SCALING_DAMAGE);
       }
  
    public static void registerVanillaAssociations() {
    }

    private static <T> VaultGearAttribute<T> attr(String name, VaultGearAttributeType<T> type,ConfigurableAttributeGenerator<T, ?> generator,VaultGearModifierReader<T> reader,@Nullable VaultGearAttributeComparator<T> comparator) {
        return new VaultGearAttribute<>(VaultMod.id(name), type, generator, reader, comparator);
    }

    private static <T> VaultGearAttribute<T> attr(String name, VaultGearAttributeType<T> type,ConfigurableAttributeGenerator<T, ?> generator,VaultGearModifierReader<T> reader) {
        return attr(name, type, generator, reader, null);
    }
}
