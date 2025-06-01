package xyz.iwolfking.woldsvaults.events;

import net.minecraftforge.event.RegisterCommandsEvent;
import xyz.iwolfking.woldsvaults.commands.ClaimPurchaseCommand;
import xyz.iwolfking.woldsvaults.commands.DivinitySkillCommands;
import xyz.iwolfking.woldsvaults.commands.VaultTeamCommand;

public class RegisterCommandEventHandler {
    public static void woldsvaults_registerCommandsEvent(RegisterCommandsEvent event) {
        //PlayerResearchCommand.register(event.getDispatcher());
        ClaimPurchaseCommand.register(event.getDispatcher());
        VaultTeamCommand.register(event.getDispatcher());
        DivinitySkillCommands.register(event.getDispatcher());
    }
}