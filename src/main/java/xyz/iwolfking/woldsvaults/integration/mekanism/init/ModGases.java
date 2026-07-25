package xyz.iwolfking.woldsvaults.integration.mekanism.init;

import mekanism.api.chemical.gas.Gas;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModGases {
    public static final GasDeferredRegister GASES = new GasDeferredRegister(WoldsVaults.MOD_ID);

    public static final GasRegistryObject<Gas> LEAD_GAS = GASES.register(
            "lead_gas",
            0x232f42
    );

    public static void register(IEventBus eventBus) {
        GASES.register(eventBus);
    }

}
