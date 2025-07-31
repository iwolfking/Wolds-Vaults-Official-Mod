package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import iskallia.vault.VaultMod;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.ClientEvents;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.PvPObjective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModKeybinds;
import iskallia.vault.task.BingoTask;
import iskallia.vault.task.ProgressConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.TaskContext;
import iskallia.vault.task.counter.TargetTaskCounter;
import iskallia.vault.task.counter.TaskCounter;
import iskallia.vault.task.renderer.context.TaskRendererContext;
import iskallia.vault.task.source.EntityTaskSource;
import iskallia.vault.task.source.TaskSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.api.helper.NormalizedHelper;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BingoObjectiveAccessor;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class BallisticBingoObjective extends BingoObjective {
    public static final SupplierKey<Objective> KEY;
    public static final FieldRegistry FIELDS;
    public static final FieldKey<Task> TASK;
    public static final FieldKey<TaskSource> TASK_SOURCE;
    public static final FieldKey<Integer> JOINED;
    public static final FieldKey<TaskMap> TASKS;
    private boolean pvp;
    private int lastScaledJoined = -1;

    protected BallisticBingoObjective() {
    }

    public static BallisticBingoObjective of(BingoTask task) {
        return (BallisticBingoObjective)(new BallisticBingoObjective()).set(TASK, task);
    }

    public TaskContext getContext(VirtualWorld world, Vault vault) {
        this.setIfAbsent(TASK_SOURCE, () -> EntityTaskSource.ofUuids(JavaRandom.ofInternal((Long)vault.get(Vault.SEED)), new UUID[0]));
        return TaskContext.of((TaskSource)this.get(TASK_SOURCE), world.getServer()).setVault(vault);
    }

    private TaskContext getContext(VirtualWorld world, Vault vault, UUID uuid) {
        return TaskContext.of(EntityTaskSource.ofUuids(JavaRandom.ofInternal((Long)vault.get(Vault.SEED)), new UUID[]{uuid}), world.getServer()).setVault(vault);
    }

    public boolean isCompleted() {
        if (this.pvp) {
            return this.get(TASKS).values().stream().anyMatch((task) -> {
                boolean var10000;
                if (task instanceof BingoTask bingo) {
                    if (bingo.areAllCompleted()) {
                        var10000 = true;
                        return var10000;
                    }
                }

                var10000 = false;
                return var10000;
            });
        } else {
            Object var2 = this.get(TASK);
            boolean var10000;
            if (var2 instanceof BingoTask) {
                BingoTask bingo = (BingoTask)var2;
                if (bingo.areAllCompleted()) {
                    var10000 = true;
                    return var10000;
                }
            }

            var10000 = false;
            return var10000;
        }
    }

    public int getBingos() {
        if (this.pvp) {
            return this.get(TASKS).values().stream().filter((task) -> task instanceof BingoTask).map((task) -> (BingoTask)task).mapToInt(BingoTask::getCompletedBingos).max().orElse(0);
        } else {
            Object var2 = this.get(TASK);
            int var10000;
            if (var2 instanceof BingoTask) {
                BingoTask bingo = (BingoTask)var2;
                var10000 = bingo.getCompletedBingos();
            } else {
                var10000 = 0;
            }

            return var10000;
        }
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        NormalizedHelper.handleAddingNormalizedToVault(vault, world);

        this.pvp = ((Objectives)vault.get(Vault.OBJECTIVES)).forEach(PvPObjective.class, (obj) -> true);
        CommonEvents.LISTENER_JOIN.register(this, (data) -> {
            if (data.getVault() == vault) {
                Listener patt4799$temp = data.getListener();
                if (patt4799$temp instanceof Runner) {
                    Runner runner = (Runner)patt4799$temp;
                    if (this.pvp) {
                        if (!((TaskMap)this.get(TASKS)).containsKey(runner.getId())) {
                            BingoTask board = (BingoTask)((BingoTask)this.get(TASK)).copy();
                            board.onAttach(((BingoObjectiveAccessor)this).getContext(world, vault, runner.getId()));
                            ((TaskMap)this.get(TASKS)).put(runner.getId(), board);
                        }
                    } else {
                        Object patt5225$temp = this.get(TASK_SOURCE);
                        if (patt5225$temp instanceof EntityTaskSource) {
                            EntityTaskSource entitySource = (EntityTaskSource)patt5225$temp;
                            entitySource.add(new UUID[]{runner.getId()});
                        }

                        this.set(JOINED, (Integer)this.getOr(JOINED, 0) + 1);
                    }

                }
            }
        });
        CommonEvents.LISTENER_LEAVE.register(this, (data) -> {
            if (data.getVault() == vault) {
                Listener patt5575$temp = data.getListener();
                if (patt5575$temp instanceof Runner) {
                    Runner runner = (Runner)patt5575$temp;
                    if (this.pvp) {
                        BingoTask board = (BingoTask)((TaskMap)this.get(TASKS)).remove(runner.getId());
                        if (board != null) {
                            board.onDetach();
                        }
                    } else {
                        Object patt5819$temp = this.get(TASK_SOURCE);
                        if (patt5819$temp instanceof EntityTaskSource) {
                            EntityTaskSource entitySource = (EntityTaskSource)patt5819$temp;
                            entitySource.remove(new UUID[]{runner.getId()});
                        }
                    }

                }
            }
        });
        ((Task)this.get(TASK)).onAttach(this.getContext(world, vault));
        if (this.pvp) {
            for(Runner runner : ((Listeners)vault.get(Vault.LISTENERS)).getAll(Runner.class)) {
                BingoTask board = (BingoTask)((BingoTask)this.get(TASK)).copy();
                board.onAttach(((BingoObjectiveAccessor)this).getContext(world, vault, runner.getId()));
                ((TaskMap)this.get(TASKS)).put(runner.getId(), board);
            }

            ((Task)this.get(TASK)).onDetach();
        }

        CommonEvents.GRID_GATEWAY_UPDATE.register(this, (data) -> {
            if (data.getLevel() == world) {
                data.getEntity().setCompletedBingos(this.getBingos());
            }
        });
        this.get(CHILDREN).forEach(child -> child.initServer(world, vault));
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        if (this.getBingos() > 0) {
            this.get(CHILDREN).forEach(child -> child.tickServer(world, vault));
            if (this.isCompleted()) {
                return;
            }
        }

        if (this.pvp) {
            ((TaskMap)this.get(TASKS)).forEach((uuid, task) -> {
                if (task instanceof BingoTask bingo) {
                    bingo.onTick(((BingoObjectiveAccessor)this).getContext(world, vault, uuid));
                }

            });
        } else {
            Object var4 = this.get(TASK);
            if (var4 instanceof BingoTask) {
                BingoTask bingo = (BingoTask)var4;
                bingo.onTick(this.getContext(world, vault));
            }
        }

        if (world.getTickCount() % 20 == 0) {
            int joined = (Integer)this.getOr(JOINED, 0);
            if (this.pvp) {
                ((TaskMap)this.get(TASKS)).values().forEach((task) -> {
                    if (task instanceof BingoTask root) {
                        for(int index = 0; index < root.getWidth() * root.getHeight(); ++index) {
                            if (!root.isCompleted(index)) {
                                root.getChild(index).streamSelfAndDescendants(ProgressConfiguredTask.class).forEach((t) -> {
                                    TaskCounter patt7787$temp = t.getCounter();
                                    if (patt7787$temp instanceof TargetTaskCounter<?, ?> counter) {
                                        if (counter.isPopulated()) {
                                            counter.get("targetPlayerContribution", Adapters.DOUBLE).ifPresent((contribution) -> {
                                                Object patt8038$temp = counter.getBaseTarget();
                                                if (patt8038$temp instanceof Integer base) {
                                                    counter.setTarget((int)((double)base + contribution * (double)base));
                                                } else {
                                                    patt8038$temp = counter.getBaseTarget();
                                                    if (patt8038$temp instanceof Float base) {
                                                        counter.setTarget((float)((double)base + contribution * (double)base));
                                                    }
                                                }

                                            });
                                            return;
                                        }
                                    }

                                });
                            }
                        }

                    }
                });
            } else if (joined != this.lastScaledJoined) {
                Object var5 = this.get(TASK);
                if (var5 instanceof BingoTask) {
                    BingoTask root = (BingoTask)var5;
                    this.lastScaledJoined = joined;

                    for(int index = 0; index < root.getWidth() * root.getHeight(); ++index) {
                        if (!root.isCompleted(index)) {
                            root.getChild(index).streamSelfAndDescendants(ProgressConfiguredTask.class).forEach((task) -> {
                                TaskCounter patt9064$temp = task.getCounter();
                                if (patt9064$temp instanceof TargetTaskCounter<?, ?> counter) {
                                    if (counter.isPopulated()) {
                                        counter.get("targetPlayerContribution", Adapters.DOUBLE).ifPresent((contribution) -> {
                                            int additional = Math.max(joined - 1, 0);
                                            Object patt9378$temp = counter.getBaseTarget();
                                            if (patt9378$temp instanceof Integer base) {
                                                counter.setTarget((int)((double)base + (double)additional * contribution * (double)base));
                                            } else {
                                                patt9378$temp = counter.getBaseTarget();
                                                if (!(patt9378$temp instanceof Float)) {
                                                    throw new UnsupportedOperationException();
                                                }

                                                Float base = (Float)patt9378$temp;
                                                counter.setTarget((float)((double)base + (double)additional * contribution * (double)base));
                                            }

                                        });
                                        return;
                                    }
                                }

                            });
                        }
                    }
                }
            }
        }

    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if (listener instanceof Runner && listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }

        if (listener instanceof Runner runner) {
            if (this.pvp) {
                BingoTask task = (BingoTask)((TaskMap)this.get(TASKS)).get(runner.getId());
                if (task != null && task.getCompletedBingos() > 0) {
                    (this.get(CHILDREN)).forEach(child -> child.tickListener(world, vault, listener));
                }
            } else if (this.getBingos() > 0) {
                (this.get(CHILDREN)).forEach(child -> child.tickListener(world, vault, listener));
            }
        }
    }

    @Override
    public void releaseServer() {
        if (this.pvp) {
            this.get(TASKS).values().forEach((task) -> {
                if (task != null) {
                    task.onDetach();
                }

            });
        } else {
            this.get(TASK).onDetach();
        }

        this.get(CHILDREN).forEach(Objective::releaseServer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initClient(Vault vault) {
        this.pvp = ((Objectives)vault.get(Vault.OBJECTIVES)).forEach(PvPObjective.class, (obj) -> true);
        ClientEvents.MOUSE_SCROLL.register(vault, (event) -> {
            if (Minecraft.getInstance().screen == null && ModKeybinds.openBingo.isDown()) {
                TaskRendererContext context = new TaskRendererContext((PoseStack)null, 0.0F, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()), Minecraft.getInstance().font);
                UUID uuid = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
                context.setUuid(uuid);
                Task task = this.pvp && uuid != null ? (Task)((TaskMap)this.get(TASKS)).get(uuid) : (Task)this.get(TASK);
                if (task != null && task.onMouseScrolled(event.getScrollDelta(), context)) {
                    event.setCanceled(true);
                }

            }
        });
        this.get(CHILDREN).forEach(child -> child.initClient(vault));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean render(Vault vault, PoseStack poseStack, Window window, float partialTicks, Player player) {
        List<PvPObjective> objs = ((Objectives)vault.get(Vault.OBJECTIVES)).getAll(PvPObjective.class);
        if (!objs.isEmpty()) {
            PvPObjective objective = (PvPObjective)objs.get(0);
            if (!objective.has(PvPObjective.COUNTDOWN_FINISHED)) {
                return false;
            }
        }

        if (this.isCompleted() && (Minecraft.getInstance().screen != null || !ModKeybinds.openBingo.isDown())) {
            boolean rendered = false;

            for(Objective objective : (Objective.ObjList)this.get(CHILDREN)) {
                rendered |= objective.render(vault, poseStack, window, partialTicks, player);
            }

            if (rendered) {
                return true;
            }
        }

        Task task = this.pvp ? (Task)((TaskMap)this.get(TASKS)).get(player.getUUID()) : (Task)this.get(TASK);
        if (task == null) {
            return true;
        } else {
            TaskRendererContext context = new TaskRendererContext(poseStack, partialTicks, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()), Minecraft.getInstance().font);
            task.onRender(context);
            return true;
        }
    }

    public void addBingoTaskModifier(Vault vault, String modifierPoolName) {
        ChunkRandom random = ChunkRandom.any();
        TextComponent text = new TextComponent("");
        List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(VaultMod.id(modifierPoolName), 0, JavaRandom.ofNanoTime());
        if(!modifiers.isEmpty()) {
            Iterator<VaultModifier<?>> modIter = modifiers.iterator();

            while(modIter.hasNext()) {
                VaultModifier<?> mod = modIter.next();
                TextComponent suffix = (TextComponent) mod.getChatDisplayNameComponent(1);
                text.append("The task completion").append((new TextComponent(" added ")).withStyle(ChatFormatting.GRAY)).append(suffix).append((new TextComponent(".")).withStyle(ChatFormatting.GRAY));
                if(modIter.hasNext()) {
                    text.append("\n");
                }
                vault.get(Vault.MODIFIERS).addModifier(mod, 1, true, random);
            }

            for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
                listener.getPlayer().ifPresent(other -> other.displayClientMessage(text, false));
            }
        }
    }

    public void onScroll(Player player, double delta) {
        if (this.pvp) {
            if (((TaskMap)this.get(TASKS)).containsKey(player.getUUID())) {
                Task t = (Task)((TaskMap)this.get(TASKS)).get(player.getUUID());
                if (t instanceof BingoTask board) {
                    board.progressBingoLine(player.getUUID(), delta < (double)0.0F ? 1 : -1);
                }
            }
        } else {
            Object var7 = this.get(TASK);
            if (var7 instanceof BingoTask bingo) {
                bingo.progressBingoLine(player.getUUID(), delta < (double)0.0F ? 1 : -1);
            }
        }

    }

    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        if (this.isCompleted()) {
            for(Objective child : this.get(CHILDREN)) {
                if (child.isActive(world, vault, objective)) {
                    return true;
                }
            }

            return false;
        } else {
            if (this.getBingos() > 0) {
                for(Objective child : this.get(CHILDREN)) {
                    if (child.isActive(world, vault, objective)) {
                        return true;
                    }
                }
            }

            return objective == this;
        }
    }


    static {
        KEY = SupplierKey.of("ballistic_bingo", Objective.class).with(Version.v1_27, BallisticBingoObjective::new);
        FIELDS = Objective.FIELDS.merge(new FieldRegistry());
        TASK = FieldKey.of("task", Task.class).with(Version.v1_27, Adapters.TASK_NBT, DISK.all().or(CLIENT.all())).register(FIELDS);
        TASK_SOURCE = FieldKey.of("task_source", TaskSource.class).with(Version.v1_27, Adapters.TASK_SOURCE_NBT, DISK.all().or(CLIENT.all())).register(FIELDS);
        JOINED = FieldKey.of("joined", Integer.class).with(Version.v1_27, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
        TASKS = FieldKey.of("tasks", TaskMap.class).with(Version.v1_38, CompoundAdapter.of(TaskMap::new), DISK.all().or(CLIENT.all())).register(FIELDS);
    }
}
