package xyz.iwolfking.woldsvaults.modifiers.gear.simon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.config.SimonConfig;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;
import xyz.iwolfking.woldsvaults.abilities.arcane.BeamOptics;

import java.util.List;

/**
 * Simon's Set's crystals bending Arcane beams. Plugs {@link CrystalOptics} into {@link BeamOptics}: a beam that enters a
 * crystal bounces between crystals and on to an enemy, and a Set wearer's Prism reaches further.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CrystalBending implements BeamOptics.Bender {
    private CrystalBending() {
    }

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        BeamOptics.setBender(new CrystalBending());
    }

    @Override
    public float prismReach(ServerPlayer caster, float radius) {
        return SimonsSet.prismReach(caster, radius);
    }

    @Override
    public BeamOptics.Lens lens() {
        SimonConfig config = SimonConfig.installed();
        return config == null ? new BeamOptics.Lens(0.0, 0.0) : new BeamOptics.Lens(config.reflectRange, config.reflectRadius);
    }

    @Override
    public float bentDamage() {
        SimonConfig config = SimonConfig.installed();
        return config == null ? 1.0F : config.arcaneReflectDamage;
    }

    @Override
    public BeamOptics.Paths near(Level level, Vec3 at, double reach, BeamOptics.Lens lens) {
        // only the server keeps track of who has crystals out
        if (lens.bentLength() <= 0.0 || !level.isClientSide && !SimonsSet.hasCrystals()) {
            return BeamOptics.STRAIGHT;
        }
        List<SimonCrystalEntity> crystals = CrystalOptics.crystalsNear(level, at, reach + lens.bentLength());
        return crystals.isEmpty() ? BeamOptics.STRAIGHT : (start, dir, length, context) -> CrystalOptics.trace(level, crystals, start, dir, length, lens, context);
    }

    @Override
    public void rayFired(ServerPlayer player, Vec3 start, Vec3 end, float percentAbilityPower, int beamTicks, float beamWidth) {
        SimonsSet.refract(player, start, end, percentAbilityPower, beamTicks, beamWidth);
    }
}
