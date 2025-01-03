package xyz.iwolfking.woldsvaults.util;
import iskallia.vault.util.TextComponentUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.Mutable;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComponentUtils {

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

    public static MutableComponent partiallyObfuscate(MutableComponent cmp, double percentage) {
        if (percentage < 0.0 || percentage > 1.0) {
            throw new IllegalArgumentException("Percentage must be between 0.0 and 1.0");
        }

        int cmpLength = TextComponentUtils.getLength(cmp);
        if (cmpLength == 0) return new TextComponent("").append(cmp); // Empty component, return unchanged.

        int charsToObfuscate = (int) Math.round(cmpLength * percentage);
        if (charsToObfuscate == 0) return new TextComponent("").append(cmp); // No obfuscation needed.

        Random random = new Random(cmp.getString().hashCode()); // Seed for deterministic results
        List<Integer> indices = IntStream.range(0, cmpLength).boxed().collect(Collectors.toList());
        Collections.shuffle(indices, random); // Shuffle indices to randomly pick obfuscated characters.

        Set<Integer> obfuscateIndices = indices.stream().limit(charsToObfuscate).collect(Collectors.toSet());

        CommandSourceStack stack = TextComponentUtils.createClientSourceStack();
        MutableComponent result = new TextComponent("");

        for (int i = 0; i < cmpLength; i++) {
            MutableComponent charComponent = TextComponentUtils.substring(stack, cmp, i, i + 1);
            if (obfuscateIndices.contains(i)) {
                // Apply obfuscation while preserving existing styles
                TextComponentUtils.applyStyle(charComponent, charComponent.getStyle().withObfuscated(true));
            }
            result.append(charComponent); // Append the character to the result.
        }

        return result;
    }
}