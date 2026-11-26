package xyz.iwolfking.woldsvaults.client.init;

import iskallia.vault.client.gui.helper.ObjectiveHudPreview;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.vhapi.api.extensions.client.ObjectiveSealLookFactory;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModVaultCrateTypes;
import xyz.iwolfking.woldsvaults.objectives.AlchemyCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.EnchantedElixirCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.HauntedBraziersCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.ScalingBallisticBingoCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.ScalingUnhingedScavengerBingoCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.SurvivalCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.UnhingedScavengerCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.ZealotCrystalObjective;

@OnlyIn(Dist.CLIENT)
public class ModObjectiveSealLooks {

    public static final Object ALCHEMY = ObjectiveSealLookFactory.INSTANCE.create(
            "ALCHEMY",
            ModItems.CRYSTAL_SEAL_ALCHEMY,
            AlchemyCrystalObjective::new,
            "Alchemy",
            ModVaultCrateTypes.ALCHEMY,
            ModObjectiveHudPreviewKinds.ALCHEMY,
            0,
            "Collect alchemical ingredients and reagents and brew them together!",
            "Going past the standard completion amount grants Crate Quantity!"
    );

    public static final Object ENCHANTED_ELIXIR = ObjectiveSealLookFactory.INSTANCE.create(
            "ENCHANTED_ELIXIR",
            ModItems.CRYSTAL_SEAL_ENCHANTER,
            EnchantedElixirCrystalObjective::new,
            "Enchanted Elixir",
            ModVaultCrateTypes.ENCHANTED_ELIXIR,
            ObjectiveHudPreview.Kind.ELIXIR,
            0,
            "Like a standard Elixir vault but random Enchanted events occur as you reach certain points in your completion!"
    );

    public static final Object HAUNTED_BRAZIERS = ObjectiveSealLookFactory.INSTANCE.create(
            "HAUNTED_BRAZIERS",
            ModItems.CRYSTAL_SEAL_SPIRITS,
            HauntedBraziersCrystalObjective::new,
            "Haunted Braziers",
            ModVaultCrateTypes.HAUNTED_BRAZIERS,
            ObjectiveHudPreview.Kind.BRAZIERS,
            0,
            "Like a standard Braziers vault but Spooky Spirits come to stop you from lighting your way!"
    );

    public static final Object UNHINGED_SCAVENGER = ObjectiveSealLookFactory.INSTANCE.create(
            "UNHINGED_SCAVENGER",
            ModItems.CRYSTAL_SEAL_UNHINGED,
            UnhingedScavengerCrystalObjective::new,
            "Unhinged Scavenger Hunt",
            ModVaultCrateTypes.UNHINGED_SCAVENGER,
            ObjectiveHudPreview.Kind.SCAVENGER,
            0,
            "Like Scavenger Hunt but the normal items are all jumbled up and mob drops are MUCH more specific!"
    );

    public static final Object SENSELESS_SCAVINGO = ObjectiveSealLookFactory.INSTANCE.create(
            "SENSELESS_SCAVINGO",
            ModItems.CRYSTAL_SEAL_UNHINGED_SCAVINGO,
            ScalingUnhingedScavengerBingoCrystalObjective::new,
            "Senseless Scavenger Bingo",
            ModVaultCrateTypes.UNHINGED_SCAVENGER,
            ObjectiveHudPreview.Kind.COLLECTOR,
            5,
            "Like Collector but mixed with an Unhinged Scavenger Hunt!"
    );

    public static final Object BALLISTIC_BINGO = ObjectiveSealLookFactory.INSTANCE.create(
            "BALLISTIC_BINGO",
            ModItems.CRYSTAL_SEAL_DOOMSAYER,
            ScalingBallisticBingoCrystalObjective::new,
            "Ballistic Bingo",
            ModVaultCrateTypes.BALLISTIC_BINGO,
            ObjectiveHudPreview.Kind.BINGO,
            5,
            "Like Bingo but each square grants a postive and negative modifier, and Bingos are much more wild!"
    );

    public static final Object BRUTAL_BOSSES = ObjectiveSealLookFactory.INSTANCE.create(
            "BRUTAL_BOSSES",
            ModItems.CRYSTAL_SEAL_TITAN,
            BrutalBossesCrystalObjective::new,
            "Brutal Bosses",
            ModVaultCrateTypes.BRUTAL_BOSSES,
            ModObjectiveHudPreviewKinds.BRUTAL_BOSSES,
            0,
            "Hunt down Obelisks to tackle enhanced Elite mobs for completion!"
    );

    public static final Object ZEALOT = ObjectiveSealLookFactory.INSTANCE.create(
            "ZEALOT",
            ModItems.CRYSTAL_SEAL_ZEALOT,
            ZealotCrystalObjective::new,
            "Zealot",
            ModVaultCrateTypes.ZEALOT,
            ModObjectiveHudPreviewKinds.ZEALOT,
            0,
            "Complete God Altars to impress the Vault Gods to complete!"
    );

    public static final Object SURVIVAL = ObjectiveSealLookFactory.INSTANCE.create(
            "SURVIVAL",
            ModItems.CRYSTAL_SEAL_SURVIVOR,
            SurvivalCrystalObjective::new,
            "Survival",
            ModVaultCrateTypes.SURVIVAL,
            ModObjectiveHudPreviewKinds.SURVIVAL,
            0,
            "Survive for as long as you can with increasing difficulty!"
    );

    public static void init() {
    }
}