package xyz.iwolfking.woldsvaults.blocks.containers.lib.infuser;


import com.blakebr0.cucumber.client.screen.button.IconButton;
import net.minecraft.core.BlockPos;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultInfuserScreen;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.network.message.EjectModeSwitchMessage;

public class EjectModeSwitchButton extends IconButton {
    public EjectModeSwitchButton(int x, int y, BlockPos pos) {
        super(x, y, 11, 9, 195, 32, VaultInfuserScreen.BACKGROUND, button -> {
            NetworkHandler.INSTANCE.sendToServer(new EjectModeSwitchMessage(pos));
        });
    }

    @Override
    protected int getYImage(boolean isHovered) {
        return isHovered ? 0 : 10;
    }
}
