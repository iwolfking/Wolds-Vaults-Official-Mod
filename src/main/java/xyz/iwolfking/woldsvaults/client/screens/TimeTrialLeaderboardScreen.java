package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

public class TimeTrialLeaderboardScreen extends Screen {

    private static final ResourceLocation BG =
            new ResourceLocation("woldsvaults", "textures/gui/timetrial_leaderboard.png");

    private final String objective;
    private final long timeRemaining;
    private final List<TimeTrialLeaderboardEntry> entries;

    private int leftPos;
    private int topPos;
    private final int imageWidth = 248;
    private final int imageHeight = 200;

    public TimeTrialLeaderboardScreen(
            String objective,
            long timeRemaining,
            List<TimeTrialLeaderboardEntry> entries
    ) {
        super(new TextComponent("Weekly Time Trial"));
        this.objective = objective;
        this.timeRemaining = timeRemaining;
        this.entries = entries;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (this.height - imageHeight) / 2;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);

        RenderSystem.setShaderTexture(0, BG);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        drawCenteredString(pose, font,
                "Weekly Time Trial",
                width / 2,
                topPos + 10,
                0xFFD700
        );

        font.draw(pose, "Objective:", leftPos + 16, topPos + 30, 0xAAAAAA);
        font.draw(pose, objective, leftPos + 16, topPos + 42, 0xFFFFFF);

        font.draw(pose,
                "Time Remaining: " + formatRemaining(timeRemaining),
                leftPos + 16,
                topPos + 58,
                0xAAAAAA
        );

        renderEntries(pose);

        super.render(pose, mouseX, mouseY, partialTick);
    }

    private void renderEntries(PoseStack pose) {
        int x = leftPos + 16;
        int y = topPos + 80;

        for (int i = 0; i < entries.size(); i++) {
            TimeTrialLeaderboardEntry e = entries.get(i);

            drawPlayerHead(pose, e.uuid(), x, y);
            font.draw(pose,
                    "#" + (i + 1) + " " + e.name(),
                    x + 20,
                    y + 4,
                    0xFFFFFF
            );
            font.draw(pose,
                    formatTime(e.time()),
                    leftPos + imageWidth - 80,
                    y + 4,
                    0xAAAAAA
            );

            y += 18;
        }
    }

    private void drawPlayerHead(PoseStack pose, UUID uuid, int x, int y) {
        PlayerInfo info = Minecraft.getInstance()
                .getConnection()
                .getPlayerInfo(uuid);

        ResourceLocation skin = info != null
                ? info.getSkinLocation()
                : DefaultPlayerSkin.getDefaultSkin();

        RenderSystem.setShaderTexture(0, skin);
        blit(pose, x, y, 8, 8, 8, 8, 64, 64);
        blit(pose, x, y, 40, 8, 8, 8, 64, 64);
    }

    private static String formatTime(long ticks) {
        return String.format("%.2f s", ticks / 20.0);
    }

    private static String formatRemaining(long millis) {
        long s = millis / 1000;
        return (s / 3600) + "h " + ((s % 3600) / 60) + "m";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
