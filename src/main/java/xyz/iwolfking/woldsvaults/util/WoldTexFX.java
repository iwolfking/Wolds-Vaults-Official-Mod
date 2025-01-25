package xyz.iwolfking.woldsvaults.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.util.TextComponentUtils;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WoldTexFX {
    public static MutableComponent legendaryShimmer(MutableComponent cmp) {
        return legendaryShimmer(null, cmp);
    }
    public static MutableComponent legendaryShimmer(Style style, MutableComponent cmp) {
        Style shine = style == null ? Style.EMPTY.withColor(ChatFormatting.WHITE) : style;
        int cmpLength = TextComponentUtils.getLength(cmp);
        int time = (int)(cmpLength * 1.4);
        int step = (int)(System.currentTimeMillis() / 90L % time);
        if (step >= cmpLength) {
            return cmp;
        } else {
            int stepCap = Math.min(step + 1, cmpLength);
            CommandSourceStack stack = TextComponentUtils.createClientSourceStack();
            MutableComponent startCmp = TextComponentUtils.substring(stack, cmp, 0, step);
            MutableComponent highlight = TextComponentUtils.substring(stack, cmp, step, stepCap);
            MutableComponent endCmp = TextComponentUtils.substring(stack, cmp, stepCap);
            TextComponentUtils.applyStyle(highlight, shine);
            return startCmp.append(highlight).append(endCmp);
        }
    }

    public static MutableComponent corruptedEffect(MutableComponent cmp) {
        return corruptedEffect(null, cmp);
    }
    public static MutableComponent corruptedEffect(Style style, MutableComponent cmp) {
        Style corrupt = style == null ? Style.EMPTY : style;
        int cmpLength = TextComponentUtils.getLength(cmp);
        int time = (int) (cmpLength * 2.2);
        int step = (int) (System.currentTimeMillis() / 150L % time);
        if (step >= cmpLength) {
            return cmp;
        } else {
            List<Integer> indices = IntStream.range(0, cmpLength).boxed().collect(Collectors.toList());
            Random rand = new Random(cmp.getString().hashCode());
            Collections.shuffle(indices, rand);
            step = indices.get(step);
            int stepCap = Math.min(step + 1, cmpLength);
            CommandSourceStack stack = TextComponentUtils.createClientSourceStack();
            MutableComponent startCmp = TextComponentUtils.substring(stack, cmp, 0, step);
            MutableComponent highlight = TextComponentUtils.substring(stack, cmp, step, stepCap);
            MutableComponent endCmp = TextComponentUtils.substring(stack, cmp, stepCap);
            TextComponentUtils.applyStyle(highlight, corrupt.withObfuscated(true));
            return startCmp.append(highlight).append(endCmp);
        }
    }


    public static MutableComponent enclose(String prefix, MutableComponent cmp) {
        return enclose(prefix, "", null, cmp);
    }
    public static MutableComponent enclose(String prefix, Style style, MutableComponent cmp) {
        return enclose(prefix, "", style, cmp);
    }
    public static MutableComponent enclose(String prefix, String suffix, MutableComponent cmp) {
        return enclose(prefix, suffix, null, cmp);
    }
    public static MutableComponent enclose(String prefix, String suffix, Style style, MutableComponent cmp) {
        TextComponent pCmp = new TextComponent(prefix);
        TextComponent sCmp = new TextComponent(suffix);

        final Style newStyle = style != null ? style : cmp.getStyle();

        pCmp.withStyle(newStyle);
        sCmp.withStyle(newStyle);

        return pCmp.append(cmp).append(sCmp);
    }

    //base fancy reader
    public abstract static class FancyReader<T> extends VaultGearModifierReader<T> {

        public FancyReader(VaultGearModifierReader<T> reader) {
            super(reader.getModifierName(), reader.getRgbColor());
            this.reader = reader;
        }
        final VaultGearModifierReader<T> reader;

        @Nullable
        public MutableComponent getDisplay(VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType type) {
            return reader.getDisplay(instance, type);
        }

        @Override
        public @Nullable MutableComponent getValueDisplay(T t) {
            return reader.getValueDisplay(t);
        }

        @NotNull
        public JsonObject serializeDisplay(VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType type) {
            return reader.serializeDisplay(instance, type);
        }

        @Override
        public MutableComponent formatConfigDisplay(LogicalSide side, Component configRange) {
            return reader.formatConfigDisplay(side, configRange);
        }

        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType type) {}
    }

    //reader to add a corrupted obfuscation effect
    public static class Corrupted<T> extends FancyReader<T> {

        public Corrupted(VaultGearModifierReader<T> reader) {
            this(null, reader);
        }
        public Corrupted(Style style, VaultGearModifierReader<T> reader) {
            super(reader);
            this.style = style;
        }
        private final Style style;

        @Override
        public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType affixType) {
            return reader == null ? null : corruptedEffect(style, reader.getDisplay(instance, affixType));
        }
    }

    //reader to a legendary-like shimmer effect
    public static class Shimmer<T> extends FancyReader<T> {

        public Shimmer(VaultGearModifierReader<T> reader) {
            this(null, reader);
        }
        public Shimmer(Style style, VaultGearModifierReader<T> reader) {
            super(reader);
            this.style = style;
        }
        private final Style style;

        @Override
        public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType affixType) {
            return reader == null ? null : legendaryShimmer(style, reader.getDisplay(instance, affixType));
        }
    }

    //reader to add prefixes and suffixes
    public static class Enclose<T> extends FancyReader<T> {

        public Enclose(Style style, VaultGearModifierReader<T> reader) {
            this("", "", style, reader);
        }
        public Enclose(String prefix, VaultGearModifierReader<T> reader) {
            this(prefix, "", null, reader);
        }
        public Enclose(String prefix, String suffix, VaultGearModifierReader<T> reader) {
            this(prefix, suffix, null, reader);
        }
        public Enclose(String prefix, Style style, VaultGearModifierReader<T> reader) {
            this(prefix, "", style, reader);
        }
        public Enclose(String prefix, String suffix, Style style, VaultGearModifierReader<T> reader) {
            super(reader);
            this.prefix = prefix;
            this.suffix = suffix;
            this.style = null;
        }
        private final String prefix;
        private final String suffix;
        private final Style style;

        @Override
        public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType affixType) {
            return reader == null ? null : enclose(prefix, suffix, style, reader.getDisplay(instance, affixType));
        }
    }

    //reader to replace text completely (this will end up hiding numbers)
    public static class Fresh<T> extends FancyReader<T> {
        public Fresh(String text, VaultGearModifierReader<T> reader) {
            this(text, null, reader);
        }
        public Fresh(Style style, VaultGearModifierReader<T> reader) {
            this("", style, reader);
        }
        public Fresh(String text, Style style, VaultGearModifierReader<T> reader) {
            super(reader);
            this.text = text;
            this.style = style;
        }
        private final String text;
        private final Style style;

        @Override
        public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType affixType) {
            if(reader == null)
                return null;

            if(style == null)
                return new TextComponent(text).setStyle(reader.getDisplay(instance, affixType).getStyle());
            else
                return new TextComponent(text).setStyle(style);
        }
    }
}
