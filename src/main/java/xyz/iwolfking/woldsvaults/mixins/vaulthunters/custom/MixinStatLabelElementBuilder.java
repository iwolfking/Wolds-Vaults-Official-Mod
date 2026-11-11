package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.screen.player.element.StatLabelElement;
import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatNumberFormat;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatRowStyle;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatSyncClient;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatValueColor;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatValueStyle;
import xyz.iwolfking.woldsvaults.stats.StatSources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * H-screen stat rows: values above 999 are shown in compact form with the exact value added to the shift tooltip,
 * and a row given a value colour draws its value in that colour with a shadow while its name stays black and is cut
 * short with "..." before it reaches the value.
 */
@Mixin(value = StatLabelElementBuilder.class, remap = false)
public abstract class MixinStatLabelElementBuilder<V extends Comparable<V>> implements StatRowStyle {
    @Unique private static final String EXACT_VALUE = "Exact value: ";
    @Unique private static final int VALUE_GAP = 4;
    @Unique private static final String ELLIPSIS = "...";

    @Shadow @Final @Mutable private Supplier<List<String>> descriptionSupplier;
    @Shadow @Final @Mutable private Supplier<V> valueSupplier;
    @Shadow private Supplier<V> finalValueSupplier;
    @Shadow private Supplier<V> valueCapSupplier;
    @Shadow @Final private Function<V, MutableComponent> valueFormatFunction;

    @Unique private String woldsvaults$statKey;
    @Unique private TextColor woldsvaults$valueColor;

    @Override
    public void woldsvaults$setStatKey(String key) {
        this.woldsvaults$statKey = key;
    }

    @Override
    public String woldsvaults$getStatKey() {
        return this.woldsvaults$statKey;
    }

    @Override
    public void woldsvaults$setValueColor(TextColor color) {
        this.woldsvaults$valueColor = color;
    }

    @Override
    public TextColor woldsvaults$getValueColor() {
        return this.woldsvaults$valueColor;
    }

    @Override
    public void woldsvaults$syncFrom(String key) {
        Supplier<V> local = this.valueSupplier;
        Supplier<V> synced = () -> StatSyncClient.value(key, local.get());
        if (this.finalValueSupplier == local) {
            this.finalValueSupplier = synced;
        }
        this.valueSupplier = synced;
        if (this.valueCapSupplier != null) {
            Supplier<V> localCap = this.valueCapSupplier;
            this.valueCapSupplier = () -> StatSyncClient.value(StatSources.CAP_PREFIX + key, localCap.get());
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void woldsvaults$addExactValueLine(CallbackInfo ci) {
        Supplier<List<String>> description = this.descriptionSupplier;
        this.descriptionSupplier = () -> {
            List<String> lines = description.get();
            if (!lines.isEmpty() && lines.get(0).startsWith(EXACT_VALUE)) {
                return lines;
            }
            String shown = this.valueFormatFunction.apply(this.valueSupplier.get()).getString();
            if (StatNumberFormat.compact(shown).equals(shown)) {
                return lines;
            }
            List<String> out = new ArrayList<>(lines.size() + 1);
            out.add(EXACT_VALUE + StatNumberFormat.grouped(shown));
            out.addAll(lines);
            return out;
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(method = "build", at = @At(value = "NEW", target = "(Liskallia/vault/client/gui/framework/spatial/spi/IPosition;Liskallia/vault/client/gui/framework/spatial/spi/ISize;Ljava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/util/function/Function;Liskallia/vault/client/gui/framework/text/LabelTextStyle$Builder;Lnet/minecraft/network/chat/TextColor;Ljava/util/function/Supplier;)Liskallia/vault/client/gui/screen/player/element/StatLabelElement;"))
    private StatLabelElement woldsvaults$styleRow(IPosition position, ISize size, Supplier label, Supplier value, Function formatter,
                                                  LabelTextStyle.Builder style, TextColor textColor, Supplier valueColor,
                                                  Operation<StatLabelElement> original) {
        Function<Object, MutableComponent> compact = shown -> {
            MutableComponent component = (MutableComponent) formatter.apply(shown);
            String text = component.getString();
            String compacted = StatNumberFormat.compact(text);
            return compacted.equals(text) ? component : new TextComponent(compacted);
        };
        TextColor color = this.woldsvaults$valueColor;
        if (color == null) {
            return original.call(position, size, label, value, compact, style, textColor, valueColor);
        }
        StatLabelElement[] row = new StatLabelElement[1];
        Supplier<String> fitted = () -> woldsvaults$fit((String) label.get(), row[0],
                compact.apply(value.get()).getString());
        row[0] = original.call(position, size, fitted, value, compact, new StatValueStyle(), textColor,
                new StatValueColor((Supplier<TextColor>) valueColor, textColor, color));
        return row[0];
    }

    @Unique
    private static String woldsvaults$fit(String name, StatLabelElement<?> row, String value) {
        if (row == null || row.width() <= 0) {
            return name;
        }
        Font font = Minecraft.getInstance().font;
        int room = row.width() - font.width(value) - 1 - VALUE_GAP;
        if (font.width(name) <= room) {
            return name;
        }
        return font.plainSubstrByWidth(name, Math.max(0, room - font.width(ELLIPSIS))).stripTrailing() + ELLIPSIS;
    }
}
