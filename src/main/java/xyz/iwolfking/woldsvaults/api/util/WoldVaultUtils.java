package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
                if(objective.getClass() == objectiveClass) {
                    return objectiveClass.cast(objective);
                }

                Optional<O> result = getObjective(objective, objectiveClass);

                if(result.isPresent()) {
                    return result.get();
                }
            }
        }

        return null;
    }

    public static <O extends Objective> Optional<O> getObjective(Objective objective, Class<O> objectiveClass) {
        for(Objective childObj : objective.get(Objective.CHILDREN)) {
            if(childObj.getClass() == objectiveClass) {
                return Optional.of(objectiveClass.cast(childObj));
            }

            Optional<O> result = getObjective(childObj, objectiveClass);

            if(result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
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
