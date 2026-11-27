package xyz.iwolfking.woldsvaults.events;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModList;
import xyz.iwolfking.woldsvaults.commands.*;
import xyz.iwolfking.woldsvaults.integration.ftbquests.QuestProgressCommand;
import xyz.iwolfking.woldsvaults.menu.PlayerResearchesGui;

public class RegisterCommandEventHandler {
    public static void woldsvaults_registerCommandsEvent(RegisterCommandsEvent event) {
        ClaimPurchaseCommand.register(event.getDispatcher());
        VaultTeamCommand.register(event.getDispatcher());
        FixMythicCommand.register(event.getDispatcher());
        TimeTrialCommand.register(event.getDispatcher());
        TriggerVaultEventCommand.register(event.getDispatcher());
        ResearchMenuCommand.register(event.getDispatcher());
        VaultChampionCommand.register(event.getDispatcher());
        ExtractionCommand.register(event.getDispatcher());
        HyperTestCommand.register(event.getDispatcher());
        SpellcraftCommand.register(event.getDispatcher());
        if (ModList.get().isLoaded("ftbquests")) {
            QuestProgressCommand.register(event.getDispatcher());
        }
        PocketTestCommand.register(event.getDispatcher());
    }
}