package xyz.iwolfking.woldsvaults.mixins.vaulthunters;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import iskallia.vault.client.render.hud.module.settings.ModuleSettingsPage;
import iskallia.vault.client.render.hud.module.vault.VaultObjectivesModule;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.task.BingoTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.objectives.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = VaultObjectivesModule.class, remap = false)
public abstract class MixinVaultObjectivesModule {
    @Shadow
    protected abstract ModuleSettingsPage createObjectiveSettingsPage(String objectiveType, String displayName);

    @Shadow
    protected abstract Optional<Objective> getFirstRenderedObjective(Vault vault, Listener listener);

    @Inject(method = "getObjectiveTypeKey", at = @At("HEAD"), cancellable = true)
    private static void checkForWoldObjectives(Objective objective, CallbackInfoReturnable<String> cir) {
        if(objective == null) {
            cir.setReturnValue(null);
        }
        else if(objective instanceof ZealotObjective) {
            cir.setReturnValue("zealot");
        }
        else if(objective instanceof SurvivalObjective) {
            cir.setReturnValue("survival");
        }
        else if(objective instanceof AlchemyObjective) {
            cir.setReturnValue("alchemy");
        }
        else if(objective instanceof CorruptedObjective) {
            cir.setReturnValue("corrupted");
        }
        else if(objective instanceof UnhingedScavengerObjective) {
            cir.setReturnValue("scavenger");
        }
        else if(objective instanceof BallisticBingoObjective || objective instanceof ScavengerBingoObjective) {
            cir.setReturnValue("bingo");
        }
        else if(objective instanceof HauntedBraziersObjective) {
            cir.setReturnValue("monolith");
        }
        else if(objective instanceof EnchantedElixirObjective) {
            cir.setReturnValue("elixir");
        }
        else if(objective instanceof BrutalBossesObjective) {
            cir.setReturnValue("brutal_bosses");
        }
        else if(objective instanceof HyperVaultObjective) {
            cir.setReturnValue("hyper");
        }
    }

    @Inject(method = "getSize", at = @At("HEAD"), cancellable = true)
    private void addWoldObjectiveSizes(ModuleRenderContext context, CallbackInfoReturnable<Pair<Integer, Integer>> cir) {
        Vault vault = context.getCustomData("vault", null);
        if (vault != null) {
            Listener listener = context.getCustomData("listener", null);
            if (listener != null) {
                Objective obj = this.getFirstRenderedObjective(vault, listener).orElse(null);
                if (obj != null) {
                    if (obj instanceof ZealotObjective || obj instanceof AlchemyObjective || obj instanceof BrutalBossesObjective || obj instanceof HauntedBraziersObjective || obj instanceof EnchantedElixirObjective || obj instanceof SurvivalObjective || obj instanceof CorruptedObjective) {
                        cir.setReturnValue(Pair.of(155, 25));
                    }
                    if (obj instanceof HyperVaultObjective) {
                        cir.setReturnValue(Pair.of(200, 90));
                    }
                }

                if (obj instanceof BallisticBingoObjective bingo) {
                    if ((VaultUtils.isPvPVault(vault) ? bingo.get(BingoObjective.TASKS).get(listener.getId()) : bingo.get(BingoObjective.TASK)) instanceof BingoTask bingoTask
                    ) {
                        boolean isExpanded = context.getCustomData("expanded", false);
                        if (isExpanded) {
                            float cellSize = 42.0F;
                            float gridWidth = cellSize * bingoTask.getWidth();
                            float gridHeight = cellSize * bingoTask.getHeight();
                            float maxGridWidth = 294.0F;
                            float maxGridHeight = 210.0F;
                            float scale = Math.min(maxGridWidth / gridWidth, maxGridHeight / gridHeight);
                            int width = (int) (gridWidth * scale) + 4;
                            int height = (int) (gridHeight * scale) + 10;
                            cir.setReturnValue(Pair.of(width, height));
                        }

                        int lineSize = Math.max(bingoTask.getWidth(), bingoTask.getHeight());
                        int itemsWidth = 30 * lineSize;
                        float miniGridHeight = 45.0F;
                        float miniGridWidth = miniGridHeight / bingoTask.getHeight() * bingoTask.getWidth();
                        int width = (int) (itemsWidth + 14 + miniGridWidth);
                        int height = 55;
                        cir.setReturnValue(Pair.of(width, height));
                    }
                }

            if (obj instanceof ScavengerBingoObjective bingo) {
                boolean isExpanded = context.getCustomData("expanded", false);
                if (isExpanded) {
                    float cellSize = 42.0F;
                    float gridWidth = cellSize * bingo.getWidth();
                    float gridHeight = cellSize * bingo.getHeight();
                    float maxGridWidth = 294.0F;
                    float maxGridHeight = 210.0F;
                    float scale = Math.min(maxGridWidth / gridWidth, maxGridHeight / gridHeight);
                    int width = (int) (gridWidth * scale) + 4;
                    int height = (int) (gridHeight * scale) + 10;
                    cir.setReturnValue(Pair.of(width, height));
                }

                int lineSize = Math.max(bingo.getWidth(), bingo.getHeight());
                int itemsWidth = 30 * lineSize;
                float miniGridHeight = 45.0F;
                float miniGridWidth = miniGridHeight / bingo.getHeight() * bingo.getWidth();
                int width = (int) (itemsWidth + 14 + miniGridWidth);
                int height = 55;
                cir.setReturnValue(Pair.of(width, height));
            }
        }

        }
    }

    @Inject(method = "getSettingsPages", at = @At("TAIL"), cancellable = true)
    private void addWoldObjectivePages(CallbackInfoReturnable<List<ModuleSettingsPage>> cir) {
        List<ModuleSettingsPage> existingPages = cir.getReturnValue();
        List<ModuleSettingsPage> newAndExistingPages = new ArrayList<>(existingPages);
        newAndExistingPages.add(createObjectiveSettingsPage("alchemy", "Alchemy"));
        newAndExistingPages.add(createObjectiveSettingsPage("zealot", "Zealot"));
        newAndExistingPages.add(createObjectiveSettingsPage("survival", "Survival"));
        newAndExistingPages.add(createObjectiveSettingsPage("corrupted", "Corrupted"));
        newAndExistingPages.add(createObjectiveSettingsPage("brutal_bosses", "Brutal Bosses"));
        newAndExistingPages.add(createObjectiveSettingsPage("hyper", "Hyper"));
        cir.setReturnValue(newAndExistingPages);
    }
}
