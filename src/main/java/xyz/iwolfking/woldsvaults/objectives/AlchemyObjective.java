package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import iskallia.vault.VaultMod;
import iskallia.vault.client.gui.helper.LightmapHelper;
import iskallia.vault.client.render.IVaultOptions;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.AlchemyObjectiveConfig;
import xyz.iwolfking.woldsvaults.events.vaultevents.BrewingAltarBrewEvent;
import xyz.iwolfking.woldsvaults.events.vaultevents.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.events.vaultevents.client.WoldClientEvents;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.alchemy.CatalystItem;
import xyz.iwolfking.woldsvaults.objectives.data.alchemy.AlchemyTasks;
import xyz.iwolfking.woldsvaults.util.MessageFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlchemyObjective extends Objective {
    public static final ResourceLocation HUD = VaultMod.id("textures/gui/alchemy/hud.png");

    public static final SupplierKey<Objective> KEY = SupplierKey.of("alchemy", Objective.class).with(Version.v1_31, AlchemyObjective::new);
    private static final FieldRegistry FIELDS = Objective.FIELDS.merge(new FieldRegistry());

    private static final FieldKey<Float> OBJECTIVE_PROBABILITY = FieldKey.of("objective_probability", Float.class).with(Version.v1_2, Adapters.FLOAT, DISK.all()).register(FIELDS);
    private static final FieldKey<Float> PROGRESS = FieldKey.of("progress", Float.class).with(Version.v1_31, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    private static final FieldKey<Float> REQUIRED_PROGRESS = FieldKey.of("required_progress", Float.class).with(Version.v1_31, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);


    private static final FieldKey<Integer> VAULT_LEVEL = FieldKey.of("vault_level", Integer.class)
            .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
            .register(FIELDS); // yeah right why would we need the VAULT LEVEL on a client, thats useless, im not upset


    private AlchemyObjectiveConfig.Entry config;

    protected AlchemyObjective() {
    }

    protected AlchemyObjective(float objectiveProbability, int vaultLevel, float requiredProgress) {
        this.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        this.set(PROGRESS, 0F);
        this.set(VAULT_LEVEL, vaultLevel);
        this.set(REQUIRED_PROGRESS, requiredProgress);
    }

    public static AlchemyObjective of(float objectiveProbability, int vaultLevel, float requiredProgress) {
        return new AlchemyObjective(objectiveProbability, vaultLevel, requiredProgress);
    }

    @Override
    public void initClient(Vault vault) {
        config = ModConfigs.ALCHEMY_OBJECTIVE.getConfig(this.get(VAULT_LEVEL));

        WoldClientEvents.TOOLTIP_EVENT.register(vault, itemTooltipEvent -> {
            if (!(itemTooltipEvent.getItemStack().getItem() instanceof AlchemyIngredientItem ingredient) || itemTooltipEvent.getItemStack().getTag() == null) {
                return;
            }
            if (!itemTooltipEvent.getItemStack().getTag().getString("VaultId").equals(vault.get(Vault.ID).toString())) {
                return; // unlikely to be hit but whatevs
            }

            List<MutableComponent> tooltip = new ArrayList<>();
            switch (ingredient.getType())  {
                case VOLATILE -> tooltip.add(new TextComponent("- ")
                        .append(new TextComponent("Applies a "))
                        .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                        .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                        .append(new TextComponent("Modifier and a "))
                        .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                        .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                        .append(new TextComponent("Modifier")));
                case DEADLY -> tooltip.add(new TextComponent("- ")
                        .append(new TextComponent("Applies a "))
                        .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                        .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                        .append(new TextComponent("Modifier")));
                case NEUTRAL -> tooltip.add(new TextComponent("- Has no effects"));
                case REFINED -> tooltip.add(new TextComponent("- ")
                        .append(new TextComponent("Applies a "))
                        .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                        .append(new TextComponent("Modifier")));
                case RUTHLESS -> tooltip.add(new TextComponent("- ")
                        .append(new TextComponent("Applies a "))
                        .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                        .append(new TextComponent("Modifier")));
                case EMPOWERED -> tooltip.add(new TextComponent("- ")
                        .append(new TextComponent("Applies a "))
                        .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                        .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                        .append(new TextComponent("Modifier")));

            }
            
            tooltip.add(new TextComponent("- Adds ").withStyle(Style.EMPTY.withColor(0x858383))
                    .append(new TextComponent(config.getFormattedPercentIngredient(ingredient.getType())).withStyle(Style.EMPTY.withColor(0xF0E68C)))
                    .append(new TextComponent(" to the Objective Progression")));

            tooltip.add(new TextComponent("- Use 3 of the same ").withStyle(Style.EMPTY.withColor(0x858383))
                    .append(new TextComponent("Ingredient Type ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                    .append(new TextComponent("to guarantee its modifiers")));

            itemTooltipEvent.getToolTip().addAll(2, tooltip);
        });

        super.initClient(vault);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        config = ModConfigs.ALCHEMY_OBJECTIVE.getConfig(this.get(VAULT_LEVEL));

        AlchemyTasks.initServer(world, vault, this, config);

        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this,
                (data) -> this.ifPresent(CorruptedObjective.OBJECTIVE_PROBABILITY, (probability) -> data.setProbability((double)probability))
        );

        WoldCommonEvents.BREWING_ALTAR_BREW_EVENT.register(this,
                (data -> handleBrewEvent(data, vault, world))
        );

        this.registerObjectiveTemplate(world, vault);
        super.initServer(world, vault);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        if (this.get(PROGRESS) > this.get(REQUIRED_PROGRESS)) {
            super.tickServer(world, vault);
        }

    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if (listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }

        if (listener instanceof Runner && this.get(PROGRESS) > this.get(REQUIRED_PROGRESS)) {
            super.tickListener(world, vault, listener);
        }
    }

    @Override
    public boolean render(Vault vault, PoseStack poseStack, Window window, float v, Player player) {
        int midX = window.getGuiScaledWidth() / 2;
        Font font = Minecraft.getInstance().font;
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        Component txt;

        if (this.get(PROGRESS) >= this.get(REQUIRED_PROGRESS)) {
            txt = (new TextComponent("Exit to complete the Vault!")).withStyle(ChatFormatting.WHITE);
            FormattedCharSequence var21 = txt.getVisualOrderText();
            float var22 = (float)midX - (float)font.width(txt) / 2.0F;
            font.drawInBatch(var21, var22, 9.0F, -1, true, poseStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
            buffer.endBatch();
        } else {
            float current = this.get(PROGRESS);
            float goal = this.get(REQUIRED_PROGRESS);
            txt = new TextComponent(String.format("%.1f%%", current * 100))
                    .append(new TextComponent(" / "))
                    .append(new TextComponent(String.format("%.1f%%", goal * 100)));
            float userScale = ((IVaultOptions)Minecraft.getInstance().options).getObjectiveScale();
            poseStack.pushPose();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int previousTexture = RenderSystem.getShaderTexture(0);
            RenderSystem.setShaderTexture(0, HUD);
            float progress = current / goal;
            poseStack.translate((float)midX - 80.0F * userScale, 8.0F * userScale, 0.0F);
            poseStack.scale(userScale, userScale, userScale);
            GuiComponent.blit(poseStack, 0, 0, 0.0F, 0.0F, 200, 26, 200, 100);
            GuiComponent.blit(poseStack, 0, 8, 0.0F, 30.0F, 13 + (int)(130.0F * progress), 10, 200, 100);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, previousTexture);
            poseStack.popPose();
            poseStack.pushPose();
            float baseScale = 0.6F;
            float totalScale = baseScale * userScale;
            poseStack.scale(totalScale, totalScale, totalScale);
            FormattedCharSequence var10001 = txt.getVisualOrderText();
            float var10002 = (float)midX / totalScale - (float)font.width(txt) / 2.0F;
            font.drawInBatch(var10001, var10002, (float)(9 + 22), -1, true, poseStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
            buffer.endBatch();
            poseStack.popPose();
        }

        return true;
    }

    @Override
    public boolean isActive(VirtualWorld virtualWorld, Vault vault, Objective objective) {

        if (this.get(PROGRESS) < this.get(REQUIRED_PROGRESS)) {
            return objective == this;
        } else {
            for(Objective child : this.get(CHILDREN)) {
                if (child.isActive(virtualWorld, vault, objective)) {
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

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    private void handleBrewEvent(BrewingAltarBrewEvent.Data data, Vault vault, VirtualWorld world) {
        float percentage = data.getEntity().getProgressIncrease().isRange() ?
                world.getRandom().nextFloat(data.getEntity().getProgressIncrease().min(), data.getEntity().getProgressIncrease().max()) :
                data.getEntity().getProgressIncrease().min();

        String formatted = String.format("%.1f%%", percentage * 100);

        MutableComponent brewName = AlchemyIngredientItem.AlchemyIngredientType.generatePotionNameComponent(AlchemyIngredientItem.AlchemyIngredientType.getTypes(data.getIngredients()));
        MutableComponent cmp = new TextComponent("Created a ").withStyle(Style.EMPTY.withColor(0xF0E68C))
                .append(brewName)
                .append(new TextComponent(" and progressed the vault by ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                .append(new TextComponent(formatted).withStyle(Style.EMPTY.withColor(percentage >= 0 ? 0x00ff04 : 0xDC143C)));
        MessageFunctions.broadcastMessage(world, cmp);


        List<AlchemyIngredientItem> effectIngredients = data.getIngredients().stream()
                .map(ItemStack::getItem)
                .filter(item -> item instanceof AlchemyIngredientItem)
                .map(item -> (AlchemyIngredientItem) item)
                .filter(item -> item.getType() != AlchemyIngredientItem.AlchemyIngredientType.NEUTRAL)
                .toList();

        AlchemyIngredientItem item = effectIngredients.get(world.random.nextInt(effectIngredients.size() - 1));

        List<ResourceLocation> modifiers = new ArrayList<>();
        switch (item.getType()) {
            case DEADLY -> modifiers.add(config.getStrongNegativeModifierPool());
            case RUTHLESS -> modifiers.add(config.getNegativeModifierPool());
            case VOLATILE -> {
                modifiers.add(config.getStrongNegativeModifierPool());
                modifiers.add(config.getStrongPositiveModifierPool());
            }
            case REFINED -> modifiers.add(config.getPositiveModifierPool());
            case EMPOWERED -> modifiers.add(config.getStrongPositiveModifierPool());
        }

        Map<VaultModifier<?>, Integer> toAddToVault = new HashMap<>();
        RandomSource random = JavaRandom.ofNanoTime();
        for (ResourceLocation mod : modifiers) {
            for(VaultModifier<?> modifier : iskallia.vault.init.ModConfigs.VAULT_MODIFIER_POOLS.getRandom(mod, this.get(VAULT_LEVEL), random)) {
                toAddToVault.put(modifier, 1);
            }
        }

        CatalystItem.CatalystType.STABILIZING.applyEffect(this, config, data, toAddToVault, modifiers);
        // apply catalyst effects

        for (Map.Entry<VaultModifier<?>, Integer> entry : toAddToVault.entrySet()) {
            vault.get(Vault.MODIFIERS).addModifier(entry.getKey(), entry.getValue(), true, random);
            world.players().forEach(player ->
                    player.sendMessage(
                            new TextComponent("- ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                    .append(entry.getKey().getChatDisplayNameComponent(1))
                                    .append(new TextComponent(" has been applied!").withStyle(Style.EMPTY.withColor(0xF0E68C))),
                            Util.NIL_UUID
                    )
            );
        }


        this.set(PROGRESS, this.get(PROGRESS) + percentage);

        if (this.get(PROGRESS) >= this.get(REQUIRED_PROGRESS)) {
            int modifiersToAdd = (int) (this.get(PROGRESS) * 100 - this.get(REQUIRED_PROGRESS) * 100);
            VaultModifier<?> crateQuantity = VaultModifierRegistry.get(VaultMod.id("crate_quantity"));
            vault.get(Vault.MODIFIERS).addModifier(crateQuantity, modifiersToAdd, true, random);
            world.players().forEach(player ->
                    player.sendMessage(
                            new TextComponent("- ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                    .append(crateQuantity.getChatDisplayNameComponent(modifiersToAdd))
                                    .append(new TextComponent(" has been applied!").withStyle(Style.EMPTY.withColor(0xF0E68C))),
                            Util.NIL_UUID
                    )
            );
        }
    }


    //todo -> iscompleted mehtod bnecause im supid
}
