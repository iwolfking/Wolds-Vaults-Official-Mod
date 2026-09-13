package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
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
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEtchingGearAttributes {
    public static final VaultGearAttribute<Float> CONCENTRATE_DRAIN = woldsAttr("concentrate_drain", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> COLOSSUS_TITAN_RESISTANCE = woldsAttr("colossus_titan_resistance", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> SNEAKY_GETAWAY_NINJA = woldsAttr("sneaky_getaway_ninja", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Float> FIREBALL_GREEDBALL = woldsAttr("fireball_greedball", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Boolean> LEVITATE_SLOW_FALLING = woldsAttr("levitate_slow_falling", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Boolean> PURIST_COMMON = woldsAttr("purist_common", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Boolean> PRUDENT_CHAOS = woldsAttr("prudent_chaos", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Boolean> REVERBERATION = woldsAttr("reverberation", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Boolean> DIVINITY = woldsAttr("divinity", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Integer> DIFFUSE_CHEMICAL_BOMB = woldsAttr("diffuse_chemical_bomb", VaultGearAttributeType.intType(), ModGearAttributeGenerators.intRange(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> FIREBALL_VOLLEY_MITOSIS = woldsAttr("fireball_volley_mitosis", VaultGearAttributeType.intType(), ModGearAttributeGenerators.intRange(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Integer> REAVING_HEMMORAGE = woldsAttr("reaving_hemmorage", VaultGearAttributeType.intType(), ModGearAttributeGenerators.intRange(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.intComparator());
    public static final VaultGearAttribute<Float> IMPLODING_BARRIER = woldsAttr("imploding_barrier", VaultGearAttributeType.floatType(), ModGearAttributeGenerators.floatRange(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.floatComparator());
    public static final VaultGearAttribute<Boolean> RAVENOUS_FANGS = woldsAttr("ravenous_fangs", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.booleanComparator());
    public static final VaultGearAttribute<Boolean> CONSERVATION_OF_MOMENTUM = woldsAttr("conservation_of_momentum", VaultGearAttributeType.booleanType(), ModGearAttributeGenerators.booleanFlag(), ModGearAttributeReaders.none(), VaultGearAttributeComparator.booleanComparator());

    @SubscribeEvent
    public static void init(RegistryEvent.Register<VaultGearAttribute<?>> event) {
        IForgeRegistry<VaultGearAttribute<?>> registry = event.getRegistry();
        registry.register(CONCENTRATE_DRAIN);
        registry.register(COLOSSUS_TITAN_RESISTANCE);
        registry.register(SNEAKY_GETAWAY_NINJA);
        registry.register(LEVITATE_SLOW_FALLING);
        registry.register(PURIST_COMMON);
        registry.register(PRUDENT_CHAOS);
        registry.register(REVERBERATION);
        registry.register(DIFFUSE_CHEMICAL_BOMB);
        registry.register(FIREBALL_VOLLEY_MITOSIS);
        registry.register(REAVING_HEMMORAGE);
        registry.register(DIVINITY);
        registry.register(FIREBALL_GREEDBALL);
        registry.register(IMPLODING_BARRIER);
        registry.register(RAVENOUS_FANGS);
        registry.register(CONSERVATION_OF_MOMENTUM);
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
