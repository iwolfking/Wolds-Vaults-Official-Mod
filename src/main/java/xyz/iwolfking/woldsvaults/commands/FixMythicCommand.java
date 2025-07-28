package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.gui.server.ClaimBlockCategoryGui;

public class FixMythicCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fixmymythic").executes(FixMythicCommand::fixMythicRarity));
    }

    private static int fixMythicRarity(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
        ServerPlayer player = commandSourceStackCommandContext.getSource().getPlayerOrException();
        ItemStack handStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof VaultGearItem) {
            VaultGearData data = VaultGearData.read(handStack);
            if(data.getRarity().equals(VaultGearRarity.SPECIAL)) {
                data.setRarity(VaultGearRarity.valueOf("MYTHIC"));
                data.write(handStack);
                player.displayClientMessage(new TextComponent("Fixed Special VaultGearRarity to Mythic."), false);
            }
            else {
                player.displayClientMessage(new TextComponent("VaultGearItem is not valid for this command."), false);
            }
        }
        return 0;
    }
}
