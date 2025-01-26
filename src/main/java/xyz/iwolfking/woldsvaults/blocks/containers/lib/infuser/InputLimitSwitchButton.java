package xyz.iwolfking.woldsvaults.blocks.containers.lib.infuser;


import com.blakebr0.cucumber.client.screen.button.IconButton;
import net.minecraft.core.BlockPos;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultInfuserScreen;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.network.message.InputLimitSwitchMessage;

import java.util.function.Supplier;

public class InputLimitSwitchButton extends IconButton {
    private final Supplier<Boolean> isLimitingInput;

    public InputLimitSwitchButton(int x, int y, BlockPos pos, Supplier<Boolean> isLimitingInput) {
        super(x, y, 8, 13, 195, 43, VaultInfuserScreen.BACKGROUND, button -> {
            NetworkHandler.INSTANCE.sendToServer(new InputLimitSwitchMessage(pos));
        });

        this.isLimitingInput = isLimitingInput;
    }

    @Override
    protected int getYImage(boolean isHovered) {
        return isHovered ? this.isLimitingInput.get() ? 1 : 0 : 10;
    }
}
