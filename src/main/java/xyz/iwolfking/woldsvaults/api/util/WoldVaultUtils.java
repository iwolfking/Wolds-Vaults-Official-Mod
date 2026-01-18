package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

public class WoldVaultUtils {
    public static <O extends Objective> O getObjective(Vault vault, Class<O> objectiveClass) {
        if (vault == null) {
            return null;
        } else {
            Objectives objectives = vault.get(Vault.OBJECTIVES);
            if(objectives == null) {
                return null;
            }

            for(Objective objective : objectives.get(Objectives.LIST)) {
                if(objective.getClass().equals(objectiveClass)) {
                    return (O) objective;
                }
            }
        }

        return null;
    }

    public static void sendMessageToAllRunners(@NotNull Vault vault, TranslatableComponent message, boolean useActionBar) {
        vault.get(Vault.LISTENERS).getAll().forEach(listener -> {
            listener.getPlayer().ifPresent(player -> {
                player.displayClientMessage(message, useActionBar);
            });
        });
    }

    public static void sendMessageToAllRunners(Vault vault, TranslatableComponent message) {
        sendMessageToAllRunners(vault, message, false);
    }
}
