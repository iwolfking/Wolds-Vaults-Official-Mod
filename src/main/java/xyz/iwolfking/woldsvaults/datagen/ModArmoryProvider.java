package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.LinkedItem;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.RefusedTag;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.Store;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.StrippedTags;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.List;

/** Generates the Creative Armory copy rules: the contents limit, the linked items, the stripped tags and the refused tags. */
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
                new LinkedItem("extrastorage", "Id", Store.NONE),
                new LinkedItem("industrialforegoing", "Id", Store.NONE),
                new LinkedItem("ae2things", "diskuuid", Store.NONE),
                new LinkedItem("rftoolsstorage", "uuid", Store.NONE),
                new LinkedItem("ae2", "plot_id", Store.NONE));
        table.strippedTags = List.of(
                new StrippedTags("the_vault:companion", List.of("Owner", "OwnerName")),
                new StrippedTags("sophisticatedbackpacks:identification_upgrade", List.of("Owner")),
                new StrippedTags("sophisticatedbackpacks:identification_upgrade_advanced", List.of("Owner")),
                new StrippedTags("sophisticatedstorage:identification_upgrade", List.of("Owner")),
                new StrippedTags("the_vault:vault_doll", List.of("vaultUUID", "xpPoints", "completedBy", "vaultLevel")),
                new StrippedTags("woldsvaults:extractor", List.of("Extractor.RigId", "Extractor.Deployed",
                        "Extractor.DeployVault", "Extractor.DeployEntity", "Extractor.ResetCounter")),
                new StrippedTags("woldsvaults:mythic_vault_charm", List.of("woldsMythicBlessingId")),
                new StrippedTags("the_vault:rang", List.of("RangFlightId")));
        table.refusedTags = List.of(
                new RefusedTag("woldsvaults:trinket_pouch", "temporary"));
        return table;
    }
}
