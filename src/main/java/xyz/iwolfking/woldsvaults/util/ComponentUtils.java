package xyz.iwolfking.woldsvaults.util;
import iskallia.vault.util.TextComponentUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public class ComponentUtil {

    /**
     * Method to corrupt a component, making it randomly obfuscate letters.
     *
     * @param cmp the component to be "corrupted"
     * @return a {@code MutableComponent} which has been "corrupted"
     */
    public static MutableComponent corruptComponent(MutableComponent cmp) {
        Random rand = new Random(cmp.getString().hashCode());
        Style corruptColor = Style.EMPTY.withColor(TextColor.fromRgb(11337728));
        int cmpLength = TextComponentUtils.getLength(cmp);
        if (cmpLength == 0) return new TextComponent("").append(cmp);
        int time = (int) ((double) cmpLength * 2.2);
        int baseStep = (int) (System.currentTimeMillis() / (200L) % (long) time);
        int randomOffset = rand.nextInt(Math.max(1, cmpLength / 2));
        int step = (baseStep + randomOffset) % cmpLength;
        if (step >= cmpLength) {
            return new TextComponent("").append(cmp);
        }
        List<Integer> indices = IntStream.range(0, cmpLength).boxed().collect(Collectors.toList());
        Collections.shuffle(indices, rand);
        step = indices.get(step);
        CommandSourceStack stack = TextComponentUtils.createClientSourceStack();
        MutableComponent startCmp = TextComponentUtils.substring(stack, cmp, 0, step);
        MutableComponent highlight = TextComponentUtils.substring(stack, cmp, step, Math.min(step + 1, cmpLength));
        MutableComponent endCmp = TextComponentUtils.substring(stack, cmp, step + 1);
        TextComponentUtils.applyStyle(highlight, corruptColor.withObfuscated(true));
        return new TextComponent("").append(startCmp).append(highlight).append(endCmp);
    }
}