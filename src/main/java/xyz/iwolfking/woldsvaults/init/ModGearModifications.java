package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.gear.modification.GearModification;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import xyz.iwolfking.woldsvaults.api.gear.actions.modifications.unusual.BlazingGearModification;
import xyz.iwolfking.woldsvaults.api.gear.actions.modifications.unusual.FreezeAllGearModification;
import xyz.iwolfking.woldsvaults.api.gear.actions.modifications.unusual.ReforgeWeaponTypeAttributes;
import xyz.iwolfking.woldsvaults.api.gear.actions.modifications.unusual.UnusualGearModification;

public class ModGearModifications {
    public static final UnusualGearModification UNUSUAL_GEAR_MODIFICATION = new UnusualGearModification();
    public static final ReforgeWeaponTypeAttributes WEAPON_TYPE_MODIFICATION = new ReforgeWeaponTypeAttributes();
    public static final BlazingGearModification BLAZING_GEAR_MODIFICATION = new BlazingGearModification();
    public static final FreezeAllGearModification FREEZE_ALL_GEAR_MODIFICATION = new FreezeAllGearModification();

    public static void init(RegistryEvent.Register<GearModification> event) {
        IForgeRegistry<GearModification> registry = event.getRegistry();
        registry.register(UNUSUAL_GEAR_MODIFICATION);
        registry.register(WEAPON_TYPE_MODIFICATION);
        registry.register(BLAZING_GEAR_MODIFICATION);
        registry.register(FREEZE_ALL_GEAR_MODIFICATION);
    }
}
