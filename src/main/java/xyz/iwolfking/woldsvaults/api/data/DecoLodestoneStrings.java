package xyz.iwolfking.woldsvaults.api.data;

import iskallia.vault.core.util.WeightedList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class DecoLodestoneStrings {
    public static WeightedList<Component> decoLodestoneMessages = new WeightedList<>();
    public static WeightedList<TextColor> decoLodestoneColors = new WeightedList<>();

    static
    {
        initLodestoneStrings();
        initLodestoneColors();
    }

    public static void initLodestoneStrings() {
        decoLodestoneMessages.add(msg(new TextComponent("yes.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("no.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("maybe.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("it is certain.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("it is decidedly so.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("it is without a doubt.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("yes, definitely.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("you may rely on it.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("as I see it, yes.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("most likely.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("outlook good.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("signs point to yes.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("reply hazy, try again.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("ask again later.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("it better not tell you now.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("it cannot predict now.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("concentrate and ask again.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("don't count on it.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("my reply is no.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("my sources say no.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("outlook not so good.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("very doubtful.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("you should brush your teeth twice a day!.")), 5);
        decoLodestoneMessages.add(msg(new TextComponent("it's Wolf, not Wold!")), 1);
        decoLodestoneMessages.add(msg(new TextComponent("thanks for playing Wold's Vaults!")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("thank the Vault Hunters developers!")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("Sophisticated Storage is good for your health.")), 5);
        decoLodestoneMessages.add(msg(new TextComponent("you are an amazing individual.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("you are a wonderful person!")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("Botania is a tech mod.")), 5);
        decoLodestoneMessages.add(msg(new TextComponent("eep!")), 6);
        decoLodestoneMessages.add(msg(new TextComponent("that it really doesn't prefer Lua.")), 3);
        decoLodestoneMessages.add(msg(new TextComponent("nothing matters, and that's good.")), 1);
        decoLodestoneMessages.add(msg(new TextComponent("please don't let anyone named Joseph near your mobs.")), 4);
        decoLodestoneMessages.add(msg(new TextComponent("have you tried using Vault Filters?")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("it thanks you for using it.")), 10);
        decoLodestoneMessages.add(msg(new TextComponent("you should stop talking to crystals.")), 5);
        decoLodestoneMessages.add(msg(new TextComponent("your next vault will be a good one!")), 10);
    }

    public static void initLodestoneColors() {
        color("99bbff");
        color("99ffff");
        color("aaff80");
        color("99ffbb");
        color("ff8080");
        color("9f9fdf");
        color("5353c6");
        color("cc6699");
        color("ffd6cc");
        color("cc6666");
        color("ffff66");
        color("d966ff");
        color("ff33cc");
        color("8000ff");
        color("008000");
        color("ffaa00");
        color("ff99ff");
    }

    private static Component msg(Component append) {
      return new TextComponent("The ").append(new TextComponent("Magic Lodestone ").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#b366ff")))).append("says ").append(append);
    }

    private static void color(String color) {
        decoLodestoneColors.add(TextColor.parseColor('#' + color), 10);
    }

    public static Component getMessage() {
        if(!decoLodestoneMessages.isEmpty()) {
            if(!decoLodestoneColors.isEmpty()) {
                return decoLodestoneMessages.getRandom().get().copy().withStyle(Style.EMPTY.withColor(decoLodestoneColors.getRandom().get()));
            }
        }

        WoldsVaults.LOGGER.warn("Decorative Lodestone messages were empty!");
        return (Component) Component.EMPTY;
    }
}
