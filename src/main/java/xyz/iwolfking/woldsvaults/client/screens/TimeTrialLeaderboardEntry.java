package xyz.iwolfking.woldsvaults.client.screens;

import java.util.UUID;

public record TimeTrialLeaderboardEntry(
        UUID uuid,
        String name,
        long time
) {}
