package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import iskallia.vault.client.gui.helper.LightmapHelper;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.SigilUtils;
import xyz.iwolfking.woldsvaults.api.util.WoldVaultUtils;
import xyz.iwolfking.woldsvaults.config.SurvivalObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.enchanted_elixir.StringList;
import xyz.iwolfking.woldsvaults.objectives.survival.SurvivalBonusManager;
import xyz.iwolfking.woldsvaults.objectives.survival.SurvivalChallengeManager;
import xyz.iwolfking.woldsvaults.objectives.survival.SurvivalSpawnManager;
import xyz.iwolfking.woldsvaults.objectives.survival.SurvivalVaultHelper;
import java.util.List;
import java.util.Optional;

public class SurvivalObjective extends Objective {
    public static final ResourceLocation HUD = WoldsVaults.id("textures/gui/survival/hud.png");

    public static final SupplierKey<Objective> KEY = SupplierKey.of("survival", Objective.class).with(Version.latest(), SurvivalObjective::new);
    public static final FieldRegistry FIELDS = Objective.FIELDS.merge(new FieldRegistry());
    public static final FieldKey<Float> OBJECTIVE_PROBABILITY = FieldKey.of("objective_probability", Float.class)
            .with(Version.v1_2,
                    Adapters.FLOAT,
                    DISK.all())
            .register(FIELDS);
    public static final FieldKey<Integer> TIME_REQUIRED =
            FieldKey.of("time_required", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all()))
                    .register(FIELDS);

    public static final FieldKey<Integer> TIME_SURVIVED =
            FieldKey.of("time_survived", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all()))
                    .register(FIELDS);

    public static final FieldKey<Integer> WAVE_INDEX =
            FieldKey.of("wave_index", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public static final FieldKey<StringList> WAVE_GROUPS =
            FieldKey.of("wave_groups", StringList.class)
                    .with(Version.v1_0, CompoundAdapter.of(StringList::new), DISK.all())
                    .register(FIELDS);

    public static final FieldKey<Boolean> COMPLETED =
            FieldKey.of("completed", Boolean.class)
                    .with(Version.v1_0, Adapters.BOOLEAN, DISK.all().or(CLIENT.all()))
                    .register(FIELDS);

    private SurvivalSpawnManager spawnManager;
    private SurvivalBonusManager bonusManager;
    private SurvivalChallengeManager challengeManager;

    protected SurvivalObjective() {
        this.set(OBJECTIVE_PROBABILITY, 1.0F);
        this.set(TIME_REQUIRED, 600 * 20);
        this.set(TIME_SURVIVED, 0);
        this.set(WAVE_INDEX, 0);
        this.set(COMPLETED, false);
        this.set(WAVE_GROUPS, new StringList());
    }

    protected SurvivalObjective(float objectiveProbability, int target, List<String> waveGroups) {
        this.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        this.set(TIME_REQUIRED, target * 20);
        this.set(TIME_SURVIVED, 0);
        this.set(WAVE_INDEX, 0);
        this.set(COMPLETED, false);
        StringList waves = new StringList();
        waves.addAll(waveGroups);
        this.set(WAVE_GROUPS, waves);
    }

    public static SurvivalObjective of(float objectiveProbabiltiy, int target, List<String> waveGroups) {
        return new SurvivalObjective(objectiveProbabiltiy, target * 60, waveGroups);
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        SurvivalVaultHelper.handleKillTimeExtensions(this, world, vault);
        SurvivalVaultHelper.preventFruits(this, vault);
        SurvivalVaultHelper.preventTemporalRelics(vault);
        SurvivalVaultHelper.setBaseVaultTimer(vault);
        SigilUtils.addStacksFromSigil(vault);

        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this,
                (data) -> this.ifPresent(SurvivalObjective.OBJECTIVE_PROBABILITY, (probability) -> data.setProbability((double)probability))
        );

        this.registerObjectiveTemplate(world, vault);


        spawnManager = new SurvivalSpawnManager(vault, world, this);
        bonusManager = new SurvivalBonusManager(vault, world, this);
        challengeManager = new SurvivalChallengeManager(vault, world, this);

        super.initServer(world, vault);
    }

    private static Component formatTime(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return new TextComponent(String.format("%d:%02d", minutes, seconds));
    }


    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        //Do nothing while vault is paused.
        if(vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
            return;
        }

        if(spawnManager != null) {
            spawnManager.tick();
        }

        this.set(TIME_SURVIVED, this.get(TIME_SURVIVED) + 1);


        if (this.get(COMPLETED)) {
            handlePostCompletion();
            return;
        }

        if (this.get(TIME_SURVIVED) >= this.get(TIME_REQUIRED)) {
            completeObjective(world, vault);
        }
    }

    private void completeObjective(VirtualWorld world, Vault vault) {
        this.set(COMPLETED, true);
        WoldVaultUtils.sendMessageToAllRunners(vault, new TranslatableComponent("vault_objective.woldsvaults.survival_completion"), true);
        this.set(TIME_SURVIVED, 0);
        this.get(CHILDREN).forEach(child -> child.tickServer(world, vault));
    }

    private void handlePostCompletion() {
        if(bonusManager != null) {
            bonusManager.tick();
        }

        if(challengeManager != null) {
            challengeManager.tick();
        }
    }


    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if (listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }
    }



    @OnlyIn(Dist.CLIENT)
    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
        int midX = window.getGuiScaledWidth() / 2;

        float progress;
        Component label;

        if (!this.get(COMPLETED)) {
            progress = (float) this.get(TIME_SURVIVED) / (float) this.get(TIME_REQUIRED);
            label = new TextComponent("Survive!");
        } else {
            progress = (float) this.get(TIME_SURVIVED) / SurvivalBonusManager.TICKS_PER_ACTION;
            label = new TextComponent("Survive for Rewards!");
        }

        progress = Math.min(1.0F, Math.max(0.0F, progress));

        float smoothSurvivedTicks = this.get(TIME_SURVIVED) - partialTicks;
        smoothSurvivedTicks = Math.max(0.0F, smoothSurvivedTicks);
        int survivedSeconds = (int) (smoothSurvivedTicks / 20.0F);
        int requiredSeconds = this.get(TIME_REQUIRED) / 20;

        Component timeText = new TextComponent(
                String.format("%d / %d s", survivedSeconds, this.get(COMPLETED) ? 150 : requiredSeconds)
        );

        matrixStack.pushPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int previousTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShaderTexture(0, HUD);

        matrixStack.translate(midX - 80, 8.0, 100.0);

        GuiComponent.blit(matrixStack, 0, 0, 0, 0, 200, 26, 200, 100);
        GuiComponent.blit(matrixStack, 0, 8, 0, 30, 13 + (int) (130.0F * progress), 10, 200, 100);

        RenderSystem.setShaderTexture(0, previousTexture);
        matrixStack.popPose();

        Font font = Minecraft.getInstance().font;
        MultiBufferSource.BufferSource buffer =
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        matrixStack.pushPose();
        matrixStack.scale(0.6F, 0.6F, 0.6F);

        font.drawInBatch(
                label.getVisualOrderText(),
                midX / 0.6F - font.width(label) / 2.0F,
                58,
                0xFFFFFF,
                true,
                matrixStack.last().pose(),
                buffer,
                false,
                0,
                LightmapHelper.getPackedFullbrightCoords()
        );

        font.drawInBatch(
                timeText.getVisualOrderText(),
                midX / 0.6F - font.width(timeText) / 2.0F,
                29,
                0xFFFFFF,
                true,
                matrixStack.last().pose(),
                buffer,
                false,
                0,
                LightmapHelper.getPackedFullbrightCoords()
        );

        buffer.endBatch();
        matrixStack.popPose();

        return true;
    }

    public Optional<SurvivalObjectiveConfig.SurvivalSpawnsEntry> getCurrentWaveEntry(int level) {
        String currentWavePool = this.get(WAVE_GROUPS).get(this.get(WAVE_INDEX));
        return ModConfigs.SURVIVAL_OBJECTIVE.SURVIVAL_SPAWNS.get(currentWavePool).getForLevel(level);
    }

    public void incrementWave(Vault vault) {
        adjustWave(vault, 1);
    }

    public void adjustWave(Vault vault, int amount) {
        int currentWave = this.get(WAVE_INDEX);
        this.set(WAVE_INDEX, Math.min(currentWave + amount, this.get(WAVE_GROUPS).size() - 1));
        if(currentWave != this.get(WAVE_INDEX)) {
            WoldVaultUtils.sendMessageToAllRunners(vault, new TranslatableComponent("vault_objective.woldsvaults.survival_wave_increment", StringUtils.convertToTitleCase(this.get(WAVE_GROUPS).get(this.get(WAVE_INDEX)))));
        }
    }

    @Override
    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        if (!this.get(COMPLETED)) {
            return objective == this;
        } else {
            for(Objective child : this.get(CHILDREN)) {
                if (child.isActive(world, vault, objective)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return KEY;
    }


}
