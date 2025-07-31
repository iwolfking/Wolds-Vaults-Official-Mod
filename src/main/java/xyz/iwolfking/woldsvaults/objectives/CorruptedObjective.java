package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.*;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.key.*;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.ClientEvents;
import iskallia.vault.core.vault.*;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.client.sfx.LoopSoundHandler;
import xyz.iwolfking.woldsvaults.data.compound.FloatList;
import xyz.iwolfking.woldsvaults.events.vaultevents.client.WoldClientEvents;
import xyz.iwolfking.woldsvaults.util.CorruptedVaultClientHelper;
import xyz.iwolfking.woldsvaults.util.CorruptedVaultHelper;

public class CorruptedObjective extends Objective {

    public static final SupplierKey<Objective> S_KEY = SupplierKey.of("corrupted", Objective.class).with(Version.v1_31, CorruptedObjective::new);
    private static final ResourceLocation SHADER = new ResourceLocation("shaders/post/sobel.json");
    private static boolean queuedRefresh = true; // used for shader
    private static boolean showedToggleShaderMessage = false;

    public static final FieldRegistry FIELDS = Objective.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<Float> OBJECTIVE_PROBABILITY = FieldKey.of("objective_probability", Float.class)
            .with(Version.v1_2,
                    Adapters.FLOAT,
                    DISK.all())
            .register(FIELDS);

    public static final FieldKey<CData> DATA = FieldKey.of("corrupted_vault_data", CData.class)
            .with(Version.v1_31,
                    CompoundAdapter.of(CData::new),
                    DISK.all().or(CLIENT.all()))
            .register(FIELDS);

    public static final FieldKey<FloatList> CORRUPTION_THRESHOLDS = FieldKey.of("corruption_thresholds", FloatList.class)
            .with(Version.v1_31,
                    CompoundAdapter.of(FloatList::create),
                    DISK.all())
            .register(FIELDS);

    public static final FieldKey<FloatList> ACTIVE_THRESHOLDS = FieldKey.of("active_thresholds", FloatList.class)
            .with(Version.v1_31,
                    CompoundAdapter.of(FloatList::create),
                    DISK.all())
            .register(FIELDS);

    public CorruptedObjective() {

    }

    public CorruptedObjective(int target, int secondaryTarget, float objectiveProbability, ResourceLocation randomModifierPool) {
        this.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        this.set(DATA, new CData(target, secondaryTarget, randomModifierPool));
        this.set(CORRUPTION_THRESHOLDS, FloatList.create());
        this.set(ACTIVE_THRESHOLDS, FloatList.create());
    }

    public static CorruptedObjective of(int target, int secondaryTarget, float objectiveProbability, ResourceLocation randomModifierPool) {
        return new CorruptedObjective(target, secondaryTarget, objectiveProbability, randomModifierPool);
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return S_KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }


    @Override
    public void initClient(Vault vault) {
        WoldClientEvents.RENDER_TICK_EVENT.register(vault, renderTickEvent -> {
            Minecraft mc = Minecraft.getInstance();
            GameRenderer render = mc.gameRenderer;

            if (queuedRefresh || render.currentEffect() == null) {
                render.loadEffect(SHADER);
                queuedRefresh = false;
            }
        });

        ClientEvents.CLIENT_TICK.register(vault, EventPriority.HIGH, data -> LoopSoundHandler.tick());

        CorruptedVaultHelper.isVaultCorrupted = true;

        if (Minecraft.getInstance().player != null && !showedToggleShaderMessage) {
            Minecraft.getInstance().player.displayClientMessage(new TextComponent("Shader may be toggled by pressing [F4]").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), false);
            showedToggleShaderMessage = true;
        }

        super.initClient(vault);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        CorruptedVaultHelper.setBaseVaultTimer(vault);                                      // Set the timer to 5min
        CorruptedVaultHelper.preventFruits(this, vault);                                       // Prevent fruits from being effective
        CorruptedVaultHelper.removeLocatorItem(this, vault);                            // Remove the locator item upon exiting the vault
        CorruptedVaultHelper.removeHealthReductionAttribute(this, vault);               // Remove the AttributeModifier from this vault on listener leave
        CorruptedVaultHelper.generateVaultModifierThresholds(this, world.getRandom());  // Generate the thresholds for applying random modifiers
        CorruptedVaultHelper.generateRandomObelisks(this, world);                       // Generate the obelisks on the roof
        CorruptedVaultHelper.generateObjectiveBlocks(this, world, vault);               // Generate the FracturedObelisks in place of the placeholders
        CorruptedVaultHelper.generateMonolithRoom(this, vault);                         // Generate the Lost Void
        CorruptedVaultHelper.handleObeliskUsage(this, world, vault);                    // Handles the right click behaviour of FracturedObelisks
        CorruptedVaultHelper.handleMonolithUsage(this, world, vault);                   // Handles the right click behaviour of MonolithControllers
        CorruptedVaultHelper.handleKillTimeExtensions(this, world, vault);              // Handles the additional time per killed mob
        CorruptedVaultHelper.updateFracturedObeliskObfuscation(this);               // Handles the updating of Fractured Obelisks

        CorruptedVaultHelper.isVaultCorrupted = true;
        super.initServer(world, vault);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        float corruptionMultiplier = CorruptedVaultHelper.setupVaultObjectiveValues(this, world, vault);

        CorruptedVaultHelper.tickDisplayOverlay(this, world, vault);
        CorruptedVaultHelper.tickCorruption(this, vault, corruptionMultiplier);

        if(world.getGameTime() % 20 == 0) {
            CorruptedVaultHelper.checkCorruptionEvents(this, vault, world, this.get(DATA).get(CData.CORRUPTION));
        }


        if(this.get(DATA).get(CData.INITIAL_COMPLETION) && this.get(DATA).get(CData.TIME_TICKED_FAKE) <= 400) {
            CorruptedVaultHelper.tickFakeVictory(this);
        }

        // If the Objective is fully completed, we tick super to end the objective.
        if(this.get(DATA).hasFullyCompleted()) {
            super.tickServer(world, vault);
        }
    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if(listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }

        if(this.get(DATA).get(CData.INITIAL_COMPLETION) && this.get(DATA).get(CData.TIME_TICKED_FAKE) <= 400) {
            CorruptedVaultHelper.fakeVictory(world, vault, listener, this);
        }


        listener.getPlayer().ifPresent(player -> {
            CorruptedVaultHelper.breakVaultPortal(world, vault, player);
            CorruptedVaultHelper.summonRoofSpikes(world, player, 64, 250, 0.45F);
            CorruptedVaultHelper.summonLightning(world, player, 64, 300, 0.6F);
            CorruptedVaultHelper.summonVoidSlashes(this, world, player, 96, 320, 0.5F);

            if(this.get(DATA).get(CData.EVENT_CHECK) == 0 && this.get(DATA).get(CData.CORRUPTION) > 10F && this.get(DATA).hasCompletedInitial() && this.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TIME_TICKED_FAKE) == 401) {
                CorruptedVaultHelper.summonWithers(world, player, 64, 3);
                this.get(DATA).set(CData.EVENT_CHECK, 1);
            }
        });

        // If the Objective is fully completed, we tick super to end the objective.
        if(listener instanceof Runner && this.get(DATA).hasFullyCompleted()) {
            super.tickListener(world, vault, listener);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean render(Vault vault, PoseStack poseStack, Window window, float partialTicks, Player player) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int centerX = window.getGuiScaledWidth() / 2;

        CorruptedVaultClientHelper.renderCorruptionOverlay(this, poseStack, font, window, centerX);
        CorruptedVaultClientHelper.renderTimeAddendOverlay(this, poseStack, window, player);

        if (CorruptedVaultHelper.shouldDisplayEscapePrompt(this)) {
            CorruptedVaultClientHelper.renderEscapePrompt(this, poseStack, font, centerX);
            return true;
        }

        CorruptedVaultClientHelper.renderObjectiveProgress(this, poseStack, font, centerX);
        return true;
    }

    @Override
    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        if(!this.get(DATA).hasFullyCompleted()) {
            return objective == this;
        }

        for(Objective child : this.get(CHILDREN)) {
            if(child.isActive(world, vault, objective)) return true;
        }

        return false;
    }




    public static class CData extends DataObject<CData> {
        public static final FieldRegistry FIELDS = new FieldRegistry();

        /* ---- Normal ---- */
        public static final FieldKey<Integer> COUNT = FieldKey.of("count", Integer.class)
                .with(Version.v1_0, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Integer> TARGET = FieldKey.of("target", Integer.class)
                .with(Version.v1_0, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Integer> BASE_TARGET = FieldKey.of("base_target", Integer.class)
                .with(Version.v1_25, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
                .register(FIELDS);
        /* ---------------- */

        /* ----- Roof ----- */
        public static final FieldKey<Integer> SECONDARY_COUNT = FieldKey.of("secondary_count", Integer.class)
                .with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Integer> SECONDARY_TARGET = FieldKey.of("secondary_target", Integer.class)
                .with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Integer> SECONDARY_BASE_TARGET = FieldKey.of("secondary_base_target", Integer.class)
                .with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
                .register(FIELDS);
        /* ---------------- */

        public static final FieldKey<ResourceLocation> CORRUPTED_MODIFIER_POOL = FieldKey.of("corrupted_modifier_pool", ResourceLocation.class)
                .with(Version.v1_31, Adapters.IDENTIFIER, DISK.all())
                .register(FIELDS);

        public static final FieldKey<Integer> DISPLAY_OVERLAY_TICK = FieldKey.of("display_overlay_tick", Integer.class)
                .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Integer> TIME_ADDEND_TICKS = FieldKey.of("time_addend_ticks", Integer.class)
                .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Boolean> INITIAL_COMPLETION = FieldKey.of("initial_completion", Boolean.class)
                .with(Version.v1_31, Adapters.BOOLEAN, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Boolean> TRUE_COMPLETION = FieldKey.of("true_completion", Boolean.class)
                .with(Version.v1_31, Adapters.BOOLEAN, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Integer> TIME_TICKED_FAKE = FieldKey.of("time_ticked_fake", Integer.class)
                .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Integer> SPAWNED_OBELISKS = FieldKey.of("spawned_obelisks", Integer.class)
                .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Float> CORRUPTION = FieldKey.of("corruption", Float.class)
                .with(Version.v1_31, Adapters.FLOAT, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        public static final FieldKey<Integer> EVENT_CHECK = FieldKey.of("event_check", Integer.class)
                .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
                .register(FIELDS);

        protected CData() {
        }

        protected CData(int target, int secondaryTarget, ResourceLocation stackModifierPool) {
            this.set(COUNT, 0);
            this.set(TARGET, target);
            this.set(BASE_TARGET, target);

            this.set(SECONDARY_COUNT, 0);
            this.set(SECONDARY_TARGET, secondaryTarget);
            this.set(SECONDARY_BASE_TARGET, secondaryTarget);

            this.set(CORRUPTED_MODIFIER_POOL, stackModifierPool); // Used for the random corrupted modifiers that get chosen as the vault gets further corrupted
            this.set(DISPLAY_OVERLAY_TICK, 0); // Duration for which the additional time is displayed on hud
            this.set(TIME_ADDEND_TICKS, 0); // Time added to display on hud
            this.set(INITIAL_COMPLETION, false); // Whether the primary objective was Completed
            this.set(TRUE_COMPLETION, false); // Whether the true Objective was completed
            this.set(TIME_TICKED_FAKE, 0); // The time ticked by the fake completion -> stops at 15s ticked
            this.set(SPAWNED_OBELISKS, 0); // The amount of obelisks that spawned on the roof, to make sure we dont have our 2nd target too high
            this.set(EVENT_CHECK, 0); // Variable used for special events, e.g spawning a wither

            // This value is used for certain aspects of the vault, this value continuously grows,
            // to prevent players from using this vault as a permanent one, since time is different here
            this.set(CORRUPTION, 0.0F);
        }

        @Override
        public FieldRegistry getFields() {
            return FIELDS;
        }

        protected boolean hasFullyCompleted() {
            return hasCompletedInitial() && this.get(SECONDARY_COUNT) >= this.get(SECONDARY_TARGET) && this.get(TRUE_COMPLETION);
        }

        public boolean hasCompletedInitial() {
            return this.get(COUNT) >= this.get(TARGET) && this.get(INITIAL_COMPLETION);
        }
    }
}