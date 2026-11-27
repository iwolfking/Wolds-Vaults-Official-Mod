package xyz.iwolfking.woldsvaults.client.invhud;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.client.render.hud.module.AbstractHudModule;
import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import iskallia.vault.client.render.hud.module.settings.ButtonSetting;
import iskallia.vault.client.render.hud.module.settings.ModuleSetting;
import iskallia.vault.client.render.hud.module.settings.SliderSetting;
import iskallia.vault.client.render.hud.module.settings.ToggleButtonSetting;
import iskallia.vault.options.VaultOption;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.util.DamageNumberFormat;
import xyz.iwolfking.woldsvaults.client.hyper.ClientBossDps;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.BossDpsSnapshotMessage.Snapshot;
import xyz.iwolfking.woldsvaults.network.message.BossDpsSnapshotMessage.Source;
import xyz.iwolfking.woldsvaults.network.message.ServerboundResetBossDpsMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Hyperboss DPS debug readout for the inventory HUD: the damage the local player has actually landed
 * on the current hyperboss, as tracked server side by HyperBossDpsTracker. Shows nothing outside a
 * hyperboss fight; while editing the HUD without a fight it previews sample numbers.
 */
public class BossDpsHudModule extends AbstractHudModule<ModuleRenderContext> {
    private static final int LINE_HEIGHT = 10;
    private static final int SOURCE_LABEL_WIDTH = 120;
    private static final Snapshot SAMPLE = new Snapshot(true, false, 1.2075e9, 1.0062e8, 9.81e7, 100, 240, 143,
            12, 30, 3, 4.52e7, 1.21e7, 2.1e9F, 3.3e9F,
            List.of(new Source("player", 9.9e8, 120), new Source("indirectMagic:fireball", 2.175e8, 23)));

    private final VaultOption<BossDpsHudOptions> option;

    public BossDpsHudModule(String key, VaultOption<BossDpsHudOptions> option) {
        super(key, "Boss DPS", "Hyperboss DPS debug tracker");
        this.option = option;
    }

    private Snapshot snapshot(ModuleRenderContext context) {
        Snapshot snapshot = ClientBossDps.get();
        if (!snapshot.active() && context.isEditing()) {
            return SAMPLE;
        }
        return snapshot;
    }

    private static MutableComponent label(String text) {
        return new TextComponent(text).withStyle(ChatFormatting.GRAY);
    }

    private static MutableComponent value(String text, ChatFormatting color) {
        return new TextComponent(text).withStyle(color);
    }

    private List<Component> lines(ModuleRenderContext context) {
        Snapshot s = snapshot(context);
        List<Component> lines = new ArrayList<>();
        if (!s.active()) {
            return lines;
        }
        BossDpsHudOptions opts = this.option.getValue();
        MutableComponent headline = label("Boss DPS ").append(value(DamageNumberFormat.format(s.windowDps()), ChatFormatting.GOLD));
        headline.append(label(String.format(Locale.ROOT, " (%ds)", Math.round(s.windowTicks() / 20.0F))));
        if (s.ended()) {
            headline.append(value(" killed", ChatFormatting.GREEN));
        }
        lines.add(headline);
        if (!opts.isDetailed()) {
            return lines;
        }
        lines.add(label("Fight ").append(value(DamageNumberFormat.format(s.fightDps()) + "/s", ChatFormatting.YELLOW))
                .append(label(" | " + DamageNumberFormat.format(s.total()) + " in "
                        + String.format(Locale.ROOT, "%.1fs", s.durationTicks() / 20.0F))));
        lines.add(label("Hits ").append(value(String.valueOf(s.hits()), ChatFormatting.WHITE))
                .append(label(" | max ")).append(value(DamageNumberFormat.format(s.maxHit()), ChatFormatting.WHITE))
                .append(label(" | last ")).append(value(DamageNumberFormat.format(s.lastHit()), ChatFormatting.WHITE)));
        boolean anyBlocked = s.iframeBlocked() + s.iframeReduced() + s.otherBlocked() > 0;
        lines.add(label("I-frames ").append(value(s.iframeBlocked() + " blocked", s.iframeBlocked() > 0 ? ChatFormatting.RED : ChatFormatting.WHITE))
                .append(label(" | ")).append(value(s.iframeReduced() + " reduced", s.iframeReduced() > 0 ? ChatFormatting.RED : ChatFormatting.WHITE))
                .append(label(" | ")).append(value(s.otherBlocked() + " other", anyBlocked && s.otherBlocked() > 0 ? ChatFormatting.RED : ChatFormatting.WHITE)));
        if (s.bossMaxHealth() > 0.0F) {
            lines.add(label("Boss ").append(value(DamageNumberFormat.percent(s.bossHealth() / s.bossMaxHealth()), ChatFormatting.WHITE))
                    .append(label(" | " + DamageNumberFormat.format(s.bossHealth()) + " / " + DamageNumberFormat.format(s.bossMaxHealth()))));
        }
        if (opts.isShowSources()) {
            for (Source source : s.sources()) {
                double share = s.total() <= 0.0 ? 0.0 : source.dealt() / s.total();
                String name = Minecraft.getInstance().font.plainSubstrByWidth(source.label(), SOURCE_LABEL_WIDTH);
                lines.add(label(" " + name + " ").append(value(DamageNumberFormat.percent(share), ChatFormatting.AQUA))
                        .append(label(" | " + DamageNumberFormat.format(source.dealt()) + " (" + source.hits() + ")")));
            }
        }
        return lines;
    }

    @Override
    protected void renderModule(ModuleRenderContext context) {
        List<Component> lines = lines(context);
        if (lines.isEmpty()) {
            return;
        }
        PoseStack poseStack = context.getPoseStack();
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 999.0);
        int y = 0;
        for (Component line : lines) {
            font.drawShadow(poseStack, line, 0, y, 0xFFFFFFFF);
            y += LINE_HEIGHT;
        }
        poseStack.popPose();
    }

    @Override
    protected int baseWidth(ModuleRenderContext context) {
        Font font = Minecraft.getInstance().font;
        int width = 16;
        for (Component line : lines(context)) {
            width = Math.max(width, font.width(line));
        }
        return width;
    }

    @Override
    protected int baseHeight(ModuleRenderContext context) {
        return Math.max(LINE_HEIGHT, lines(context).size() * LINE_HEIGHT - 1);
    }

    @Override
    public HudPosition getPosition() {
        return this.option.getValue().getHudPosition();
    }

    @Override
    public void setPosition(HudPosition pos) {
        this.option.setValue(this.option.getValue().setHudPosition(pos));
    }

    @Override
    public float getScale() {
        return this.option.getValue().getSize();
    }

    @Override
    public void resetToDefaultPosition() {
        BossDpsHudOptions def = this.option.getDefaultValue();
        BossDpsHudOptions current = this.option.getValue();
        current.setHudPosition(def.getHudPosition());
        current.setSize(def.getSize());
        current.setEnabled(def.isEnabled());
        current.setDetailed(def.isDetailed());
        current.setShowSources(def.isShowSources());
        this.option.setValue(current);
    }

    @Override
    public List<ModuleSetting> getSettings() {
        BossDpsHudOptions opts = this.option.getValue();
        return List.of(
                new ToggleButtonSetting(
                        "enabled",
                        new TextComponent("Enabled"),
                        new TextComponent("Enabled"),
                        () -> opts.isEnabled() ? "ON" : "OFF",
                        () -> this.option.setValue(opts.setEnabled(!opts.isEnabled()))
                ),
                new SliderSetting(
                        "size",
                        new TextComponent("Size"),
                        new TextComponent("Controls the size of the boss DPS readout."),
                        () -> new TextComponent("Size"), () -> opts.getSize() - 0.5F,
                        value -> this.option.setValue(opts.setSize(value + 0.5F))
                ),
                new ToggleButtonSetting(
                        "detailed",
                        new TextComponent("Details"),
                        new TextComponent("Show fight DPS, hit counts, i-frame losses and boss health."),
                        () -> opts.isDetailed() ? "ON" : "OFF",
                        () -> this.option.setValue(opts.setDetailed(!opts.isDetailed()))
                ),
                new ToggleButtonSetting(
                        "sources",
                        new TextComponent("Sources"),
                        new TextComponent("Show the top damage sources and their share of the total."),
                        () -> opts.isShowSources() ? "ON" : "OFF",
                        () -> this.option.setValue(opts.setShowSources(!opts.isShowSources()))
                ),
                new ButtonSetting(
                        "reset",
                        new TextComponent("Reset"),
                        new TextComponent("Clears the current fight statistics."),
                        () -> new TextComponent("Reset fight"),
                        () -> {
                            ClientBossDps.update(Snapshot.INACTIVE);
                            ModNetwork.sendToServer(new ServerboundResetBossDpsMessage());
                        }
                )
        );
    }

    @Override
    public boolean isHidden(ModuleRenderContext context) {
        return !this.option.getValue().isEnabled();
    }
}
