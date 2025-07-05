package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.config.AlchemyObjectiveConfig;
import xyz.iwolfking.woldsvaults.events.vaultevents.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.events.vaultevents.client.WoldClientEvents;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.objectives.data.alchemy.AlchemyTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlchemyObjective extends Objective {
    public static final SupplierKey<Objective> KEY = SupplierKey.of("alchemy", Objective.class).with(Version.v1_31, AlchemyObjective::new);
    private static final FieldRegistry FIELDS = Objective.FIELDS.merge(new FieldRegistry());

    private static final FieldKey<Float> OBJECTIVE_PROBABILITY = FieldKey.of("objective_probability", Float.class).with(Version.v1_2, Adapters.FLOAT, DISK.all()).register(FIELDS);
    private static final FieldKey<Float> PROGRESS = FieldKey.of("progress", Float.class).with(Version.v1_31, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);

    private static final FieldKey<Integer> VAULT_LEVEL = FieldKey.of("vault_level", Integer.class)
            .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
            .register(FIELDS); // yeah right why would we need the VAULT LEVEL on a client, thats useless, im not upset


    private AlchemyObjectiveConfig.Entry config;

    protected AlchemyObjective() {
    }

    protected AlchemyObjective(float objectiveProbability, int vaultLevel) {
        this.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        this.set(PROGRESS, 0F);
        this.set(VAULT_LEVEL, vaultLevel);
    }

    public static AlchemyObjective of(float objectiveProbability, int vaultLevel) {
        return new AlchemyObjective(objectiveProbability, vaultLevel);
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
                case VOLATILE -> {
                    tooltip.add(new TextComponent("- ")
                            .append(new TextComponent("Applies a "))
                            .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                            .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                            .append(new TextComponent("Modifier and a "))
                            .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                            .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                            .append(new TextComponent("Modifier")));
                }
                case DEADLY -> {
                    tooltip.add(new TextComponent("- ")
                            .append(new TextComponent("Applies a "))
                            .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                            .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                            .append(new TextComponent("Modifier")));
                }
                case NEUTRAL -> {
                    tooltip.add(new TextComponent("- Has no effects"));
                }
                case REFINED -> {
                    tooltip.add(new TextComponent("- ")
                            .append(new TextComponent("Applies a "))
                            .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                            .append(new TextComponent("Modifier")));
                }
                case RUTHLESS -> {
                    tooltip.add(new TextComponent("- ")
                            .append(new TextComponent("Applies a "))
                            .append(new TextComponent("Negative ").withStyle(Style.EMPTY.withColor(0x910000)))
                            .append(new TextComponent("Modifier")));
                }
                case EMPOWERED -> {
                    tooltip.add(new TextComponent("- ")
                            .append(new TextComponent("Applies a "))
                            .append(new TextComponent("Strong ").withStyle(Style.EMPTY.withColor(0xF75625)))
                            .append(new TextComponent("Positive ").withStyle(Style.EMPTY.withColor(0x30E004)))
                            .append(new TextComponent("Modifier")));
                }

            }
            tooltip.add(new TextComponent("- Adds ").withStyle(Style.EMPTY.withColor(0x858383))
                    .append(new TextComponent(config.getFormattedPercentIngredient(ingredient.getType())).withStyle(Style.EMPTY.withColor(0xF0E68C)))
                    .append(new TextComponent(" to the Objective Progression")));

            tooltip.add(new TextComponent("- Use 3 of the same ").withStyle(Style.EMPTY.withColor(0x858383))
                    .append(new TextComponent("Ingredient Type ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                    .append(new TextComponent("to guarantee its effects")));

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


        this.registerObjectiveTemplate(world, vault);

        super.initServer(world, vault);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        if (this.get(PROGRESS) > 1) {
            super.tickServer(world, vault);
        }

    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if (listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }

        if (listener instanceof Runner && this.get(PROGRESS) > 1) {
            super.tickListener(world, vault, listener);
        }
    }

    @Override
    public boolean render(Vault vault, PoseStack poseStack, Window window, float v, Player player) {
        return false;
    }

    @Override
    public boolean isActive(VirtualWorld virtualWorld, Vault vault, Objective objective) {

        if (this.get(PROGRESS) < 1) {
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

    //todo -> iscompleted mehtod bnecause im supid
}
