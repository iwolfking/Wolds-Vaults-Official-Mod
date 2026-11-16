package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.LinkedItem;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.OwnerTags;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.Store;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.List;

/** Generates the Creative Armory copy rules: the contents limit, the linked items and the owner tags. */
public class ModArmoryProvider extends AbstractWoldsTableProvider {

    public ModArmoryProvider(DataGenerator generator) {
        super(generator, "armory");
    }

    @Override
    public void registerConfigs() {
        table("armory", armory());
    }

    /** The armory table. */
    public static ArmoryConfig armory() {
        ArmoryConfig table = new ArmoryConfig();
        table.contentsLimitKb = 128;
        table.linkedItems = List.of(
                new LinkedItem("sophisticatedbackpacks", "contentsUuid", Store.BACKPACK),
                new LinkedItem("sophisticatedstorage", "uuid", Store.STORAGE_ITEM),
                new LinkedItem("sophisticatedvault", "uuid", Store.STORAGE_ITEM),
                new LinkedItem("refinedstorage", "Id", Store.NONE),
                new LinkedItem("extrastorage", "Id", Store.NONE));
        table.ownerTags = List.of(
                new OwnerTags("the_vault:companion", List.of("Owner", "OwnerName")),
                new OwnerTags("sophisticatedbackpacks:identification_upgrade", List.of("Owner")),
                new OwnerTags("sophisticatedbackpacks:identification_upgrade_advanced", List.of("Owner")),
                new OwnerTags("sophisticatedstorage:identification_upgrade", List.of("Owner")));
        return table;
    }
}
