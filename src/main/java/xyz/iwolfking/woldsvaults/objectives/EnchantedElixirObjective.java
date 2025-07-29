package xyz.iwolfking.woldsvaults.objectives;

import iskallia.vault.VaultMod;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.ElixirObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.elixir.ElixirGoal;
import iskallia.vault.core.vault.objective.elixir.ElixirTask;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.helper.NormalizedHelper;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EnchantedElixirObjective extends ElixirObjective {

    protected EnchantedElixirObjective() {
        this.set(GOALS, new GoalMap());
    }

    public static final SupplierKey<Objective> E_KEY = SupplierKey.of("enchanted_elixir", Objective.class).with(Version.v1_12, EnchantedElixirObjective::new);

    public static EnchantedElixirObjective create() {
        return new EnchantedElixirObjective();
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return E_KEY;
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        NormalizedHelper.handleAddingNormalizedToVault(vault, world);
        super.initServer(world, vault);
    }


    private Map<ServerPlayer, Integer> elixirCollectionMap = new HashMap<>();

    private Map<ServerPlayer, List<Float>> elixirBreakpointsMap = new HashMap<>();

    private List<Listener> completedListeners = new ArrayList<>();

    private boolean shouldCascadeRandomly = false;
    private boolean shouldCascade = true;
    private float cascadeIncrease = 0.0F;

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if(world == null || vault == null || listener == null) {
            return;
        }
        if (listener instanceof Runner runner) {
            if (listener.getPriority(this) < 0) {
                listener.addObjective(vault, this);
                this.generateGoal(world, vault, runner);
                if(listener.getPlayer().isPresent()) {
                    elixirCollectionMap.put(listener.getPlayer().get(), 0);
                    generateElixirBreakpointsMap(listener, true);
                }
            }
        }
        ServerPlayer listenerPlayer = listener.getPlayer().orElse(null);
        if(listenerPlayer == null) {
            return;
        }
        ElixirGoal goal = this.get(GOALS).get(listener.get(Listener.ID));
        if(elixirBreakpointsMap != null && elixirBreakpointsMap.get(listener.getPlayer().get()) == null) {
            generateElixirBreakpointsMap(listener, false);
            Integer currentElixir = goal.get(ElixirGoal.CURRENT);
            for(int i = 0; i < elixirBreakpointsMap.get(listenerPlayer).size() - 1; i++) {
                if(elixirBreakpointsMap.get(listenerPlayer).get(i) >= currentElixir) {
                    if(i + 1 >= elixirBreakpointsMap.get(listenerPlayer).size()) {
                        completedListeners.add(listener);
                    }
                    elixirCollectionMap.put(listenerPlayer, i);
                    break;
                }
            }
        }


        if(!goal.isCompleted()) {
            ServerPlayer objPlayer = listener.getPlayer().get();
            if(goal.get(ElixirGoal.CURRENT) >= elixirBreakpointsMap.get(objPlayer).get(elixirCollectionMap.get(objPlayer))) {
                elixirCollectionMap.put(objPlayer, elixirCollectionMap.get(objPlayer) + 1);
                    triggerRandomEvent(objPlayer, vault);
            }
        }
        if(goal.isCompleted() && !completedListeners.contains(listener)) {
            completedListeners.add(listener);
            triggerOmegaRandomEvent(listener.getPlayer().get(), vault);
        }
        if (goal.isCompleted()) {
            this.get(CHILDREN).forEach(child -> child.tickListener(world, vault, listener));
        }

    }

    private void generateGoal(VirtualWorld world, Vault vault, Runner listener) {
        ElixirGoal goal = new ElixirGoal();
        this.get(GOALS).put(listener.get(Listener.ID), goal);
        JavaRandom random = JavaRandom.ofInternal(vault.get(Vault.SEED) ^ listener.get(Listener.ID).getMostSignificantBits());
        goal.set(ElixirGoal.TARGET, ModConfigs.ENCHANTED_ELIXIR.generateTarget(vault.get(Vault.LEVEL).get(), random));
        goal.set(ElixirGoal.BASE_TARGET, goal.get(ElixirGoal.TARGET));

        for (ElixirTask task: ModConfigs.ENCHANTED_ELIXIR.generateGoals(vault.get(Vault.LEVEL).get(), random)) {
            goal.get(ElixirGoal.TASKS).add(task);
        }

        goal.initServer(world, vault, this, listener.getId());
    }

    private void generateElixirBreakpointsMap(Listener listener, boolean sendMessage) {
        Random random = new Random();
        int numberOfBreakpoints = random.nextInt(10, 25);
        if(listener.getPlayer().isPresent()) {
            ElixirGoal goal = this.get(GOALS).get(listener.get(Listener.ID));
            List<Float> elixirBreakPointsList = new ArrayList<>();
            Float elixirTarget = goal.get(ElixirGoal.TARGET).floatValue();
            for(int i = 0; i < numberOfBreakpoints; i++) {
                float decimalModifier = (i + 1.0F) / (numberOfBreakpoints);
                elixirBreakPointsList.add(elixirTarget * decimalModifier);
            }
            elixirBreakpointsMap.put(listener.getPlayer().get(), elixirBreakPointsList);
            if(!sendMessage) {
                return;
            }

            if(numberOfBreakpoints >= 10 && numberOfBreakpoints < 15) {
                listener.getPlayer().ifPresent(serverPlayer -> {
                    serverPlayer.displayClientMessage(new TextComponent("You will experience a low number of random events this vault!").withStyle(ChatFormatting.YELLOW), false);
                });
            }
            else if(numberOfBreakpoints > 14 && numberOfBreakpoints < 19) {
                listener.getPlayer().ifPresent(serverPlayer -> {
                    serverPlayer.displayClientMessage(new TextComponent("You will experience a moderate number of random events this vault!").withStyle(ChatFormatting.AQUA), false);
                });
            }
            else if(numberOfBreakpoints > 18 && numberOfBreakpoints < 25) {
                listener.getPlayer().ifPresent(serverPlayer -> {
                    serverPlayer.displayClientMessage(new TextComponent("You will experience a high number of random events this vault!").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                });
            }
        }
    }

    private void triggerRandomEvent(ServerPlayer objPlayer, Vault vault) {
        if(EnchantedEventsRegistry.getEvents().getRandom().isPresent()) {
            EnchantedEventsRegistry.getEvents().getRandom().get().triggerEvent(objPlayer.getOnPos(), objPlayer, vault);
        }
    }

    private void triggerOmegaRandomEvent(ServerPlayer objPlayer, Vault vault) {
        if(EnchantedEventsRegistry.getEvents().getRandom().isPresent()) {
            EnchantedEventsRegistry.getOmegaEvents().getRandom().get().triggerEvent(objPlayer.getOnPos(), objPlayer, vault);
        }
    }

    public void setShouldCascadeRandomly(boolean bool) {
        shouldCascadeRandomly = bool;
    }

    public void setShouldCascade(boolean bool) {
        shouldCascade = bool;
    }

    public void setCascadeIncrease(float value) {
        cascadeIncrease = value;
    }

    public float getCascadeIncrease() {
        return cascadeIncrease;
    }

}
