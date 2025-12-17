package xyz.iwolfking.woldsvaults.api.data;

import iskallia.vault.core.util.WeightedList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class FlightCancellationStrings {
    public static WeightedList<Component> flightCancellationStrings = new WeightedList<>();

    static
    {
        initFlightCancellationStrings();
    }

    public static void initFlightCancellationStrings() {
        flightCancellationStrings.add((new TranslatableComponent("misc.woldsvaults.flight_block_0")), 10);
    }


    public static Component getMessage() {
        if(!flightCancellationStrings.isEmpty()) {
                return flightCancellationStrings.getRandom().get().copy().withStyle(ChatFormatting.RED);

        }

        WoldsVaults.LOGGER.warn("Flight Cancellation messages were empty!");
        return (Component) Component.EMPTY;
    }
}
