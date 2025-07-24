package xyz.iwolfking.woldsvaults.items.alchemy;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.BasicItem;
import iskallia.vault.util.MiscUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CallbackI;
import xyz.iwolfking.woldsvaults.config.AlchemyObjectiveConfig;
import xyz.iwolfking.woldsvaults.events.vaultevents.BrewingAltarBrewEvent;
import xyz.iwolfking.woldsvaults.objectives.AlchemyObjective;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CatalystItem extends BasicItem {
    private final CatalystType type;
    
    public CatalystItem(ResourceLocation id, CatalystType type) {
        super(id);
        this.type = type;
    }

    @Override
    public @NotNull Component getName(ItemStack pStack) {
        if (!(pStack.getItem() instanceof CatalystItem it)) return super.getName(pStack);
        
        String baseName = "Catalyst of " + switch(it.getType()) {
            case STABILIZING -> "Stability";
            case AMPLIFYING -> "Amplification";
            case FOCUSING -> "Focus";
            case TEMPORAL -> "Haste";
            case UNSTABLE -> "Instability";
        };
        return ComponentUtils.wavingComponent(new TextComponent(baseName), 0xF7C707, 0.2F, 0.4F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        switch(((CatalystItem) stack.getItem()).getType()) {
                case STABILIZING -> tooltip.add(
                        new TextComponent("- ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                .append(new TextComponent("50% Chance ").withStyle(Style.EMPTY.withColor(0xF0E68C))) //yellowish
                                .append(new TextComponent("to stabilize ").withStyle(Style.EMPTY).withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("the Brew ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("and ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("prevent ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent("a ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("Negative Modifier").withStyle(Style.EMPTY.withColor(0xDC143C))) //red
                                .append(new TextComponent(".").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                );

                case AMPLIFYING -> tooltip.add(
                        new TextComponent("- Amplifies ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                .append(new TextComponent("Vault Progression ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent("between ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("25% - 75%").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent(".").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                );

                case FOCUSING -> {
                    tooltip.add(
                            new TextComponent("").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                    .append(new TextComponent("If ").withStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)))
                                    .append(new TextComponent("all the ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                    .append(new TextComponent("Ingredients ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                    .append(new TextComponent("are ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                    .append(new TextComponent("equal").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                    .append(new TextComponent(":").withStyle(Style.EMPTY))
                    );
                    tooltip.add(
                        new TextComponent("- ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                .append(new TextComponent("Doubles ").withStyle(Style.EMPTY.withColor(0x6EFA75)))
                                .append(new TextComponent("the effect ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent("of ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("the Brew").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent(". ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                    );

                }

                case TEMPORAL -> {
                    tooltip.add(
                            new TextComponent("- ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                    .append(new TextComponent("Protects ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                    .append(new TextComponent("the ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                    .append(new TextComponent("Brewing Altar ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                    .append(new TextComponent("from ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                    .append(new TextComponent("decay").withStyle(Style.EMPTY.withColor(0xFA5F69)))
                                    .append(new TextComponent(". ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                    );
                }
                case UNSTABLE -> tooltip.add(
                        new TextComponent("- ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                .append(new TextComponent("Causes ").withStyle(Style.EMPTY))
                                .append(new TextComponent("the Brew ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("to ").withStyle(Style.EMPTY))
                                .append(new TextComponent("randomize").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent(".").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                );
        };



    }

    public CatalystType getType() {
        return type;
    }

    public enum CatalystType {
        STABILIZING {
            @Override
            public void applyEffect(AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools) {
                //if (JavaRandom.ofNanoTime().nextBoolean()) {
                    boolean hasNegative = chosenPools.stream().anyMatch(pool ->
                            pool.equals(cfg.getNegativeModifierPool()) || pool.equals(cfg.getStrongNegativeModifierPool()));

                    if (hasNegative && !modifierMap.isEmpty()) {
                        Iterator<Map.Entry<VaultModifier<?>, Integer>> iterator = modifierMap.entrySet().iterator();
                        if (iterator.hasNext()) {
                            iterator.next();
                            System.out.println("removing: " + iterator);
                            iterator.remove();

                        }
                    }
                //}
            }
        },
        AMPLIFYING {
            @Override
            public void applyEffect(AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools) {


            }
        },
        FOCUSING {
            @Override
            public void applyEffect(AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools) {


            }
        },
        TEMPORAL {
            @Override
            public void applyEffect(AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools) {


            }
        },
        UNSTABLE {
            @Override
            public void applyEffect(AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools) {


            }
        };

        public abstract void applyEffect(
                AlchemyObjective obj,
                AlchemyObjectiveConfig.Entry cfg,
                BrewingAltarBrewEvent.Data data,
                Map<VaultModifier<?>, Integer> modifierMap,
                List<ResourceLocation> chosenPools);
    }
}
