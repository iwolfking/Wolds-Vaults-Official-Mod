package xyz.iwolfking.woldsvaults.client.screens.armory;

import iskallia.vault.client.data.ClientStatisticsData;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.client.gui.screen.player.StatisticsElementContainerScreenData;
import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import iskallia.vault.client.gui.screen.player.element.VaultGodFavorIconElement;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatRowStyle;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatScreenLayout;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.StatLabelElementBuilderAccessor;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The H screen's stat lists as text, captured on the client at Save and replayed on the armory's Gear tab. The
 * rows are what the player's own H screen showed at that moment; nothing is recomputed later. Replayed rows get the
 * H screen's layout: categories, value colours and compact numbers.
 */
public final class ArmoryStats extends StatisticsElementContainerScreenData {
    public static final String PROMINENT = "prominent";

    private ArmoryStats(Player player) {
        super(player);
    }

    /** The client player's current H-screen stats, for {@code ServerboundArmorySaveMessage}. */
    public static CompoundTag capture(Player player) {
        ArmoryStats data = new ArmoryStats(player);
        CompoundTag tag = new CompoundTag();
        tag.put(PROMINENT, rows(data.getStatListProminent()));
        tag.put("player", rows(data.getStatListPlayer()));
        CompoundTag reputation = new CompoundTag();
        for (VaultGod god : VaultGod.values()) {
            reputation.putInt(god.name(), ClientStatisticsData.getReputation(god));
        }
        tag.put("reputation", reputation);
        ClientStatisticsData.getFavour().ifPresent(god -> tag.putString("favour", god.name()));
        tag.putInt("level", VaultBarOverlay.vaultLevel);
        tag.putFloat("levelPercent", VaultBarOverlay.tnl <= 0 ? 0.0F : (float) VaultBarOverlay.vaultExp / VaultBarOverlay.tnl);
        return tag;
    }

    private static ListTag rows(List<StatLabelElementBuilder<?>> builders) {
        ListTag list = new ListTag();
        for (StatLabelElementBuilder<?> builder : builders) {
            try {
                StatLabelElementBuilderAccessor accessor = (StatLabelElementBuilderAccessor) builder;
                CompoundTag row = new CompoundTag();
                row.putString("label", accessor.woldsvaults$getLabelSupplier().get());
                String key = ((StatRowStyle) builder).woldsvaults$getStatKey();
                if (key != null) {
                    row.putString("key", key);
                }
                Object value = accessor.woldsvaults$getFinalValueSupplier().get();
                MutableComponent shown = accessor.woldsvaults$getValueFormatFunction().apply(value);
                row.putString("value", Component.Serializer.toJson(shown));
                TextColor color = accessor.woldsvaults$getTextColor();
                row.putInt("color", color == null ? 0 : color.getValue());
                ListTag description = new ListTag();
                accessor.woldsvaults$getDescriptionSupplier().get().forEach(line -> description.add(StringTag.valueOf(line)));
                row.put("description", description);
                list.add(row);
            } catch (RuntimeException e) {
                WoldLogger.error("Armory: one H-screen stat row could not be recorded and is left out ({})", e.toString());
            }
        }
        return list;
    }

    /** The saved rows as builders the H screen's list elements accept, styled as the H screen styles that list. */
    public static List<StatLabelElementBuilder<?>> builders(CompoundTag stats, String key) {
        List<StatLabelElementBuilder<?>> out = new ArrayList<>();
        ListTag list = stats.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag row = list.getCompound(i);
            String label = row.getString("label");
            MutableComponent value;
            try {
                value = Component.Serializer.fromJson(row.getString("value"));
            } catch (RuntimeException e) {
                WoldLogger.warn("Armory: saved stat value for {} could not be read; shown as '?'", label);
                value = null;
            }
            MutableComponent shown = value == null ? new TextComponent("?") : value;
            List<String> description = new ArrayList<>();
            ListTag lines = row.getList("description", Tag.TAG_STRING);
            for (int j = 0; j < lines.size(); j++) {
                description.add(lines.getString(j));
            }
            StatLabelElementBuilder<String> builder = new StatLabelElementBuilder<>(() -> label, () -> description, () -> label,
                    ignored -> shown.copy(), TextColor.fromRgb(row.getInt("color")));
            if (row.contains("key", Tag.TAG_STRING)) {
                ((StatRowStyle) builder).woldsvaults$setStatKey(row.getString("key"));
            }
            out.add(builder);
        }
        if (PROMINENT.equals(key)) {
            StatScreenLayout.colorProminent(out);
        } else {
            StatScreenLayout.colorPlayer(out);
        }
        return out;
    }

    public static int reputation(CompoundTag stats, VaultGod god) {
        return stats.getCompound("reputation").getInt(god.name());
    }

    /** A favour icon value fed from the save: reputation number, gold when it was the favoured god. */
    public static VaultGodFavorIconElement.ValueSupplier favour(CompoundTag stats, VaultGod god) {
        int color = god.name().equals(stats.getString("favour")) ? 0xFFAA00 : 0xFFFFFF;
        MutableComponent title = new TranslatableComponent("screen.the_vault.god_favor_title", god.getName(),
                Introspector.decapitalize(god.getTitle()));
        return VaultGodFavorIconElement.ValueSupplier.of(() -> reputation(stats, god), () -> title,
                () -> List.of(title, new TranslatableComponent("screen.the_vault.god_favor_description", god.getName())), color);
    }
}
