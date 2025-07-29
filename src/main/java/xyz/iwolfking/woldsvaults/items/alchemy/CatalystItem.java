package xyz.iwolfking.woldsvaults.items.alchemy;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.item.BasicItem;
import iskallia.vault.network.message.ClientboundTESyncMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.BrewingAltar;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;
import xyz.iwolfking.woldsvaults.config.AlchemyObjectiveConfig;
import xyz.iwolfking.woldsvaults.events.vaultevents.BrewingAltarBrewEvent;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.objectives.AlchemyObjective;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

                case TEMPORAL -> tooltip.add(
                        new TextComponent("- ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                .append(new TextComponent("Protects ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent("the ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("Brewing Altar ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent("from ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("decay").withStyle(Style.EMPTY.withColor(0xFA5F69)))
                                .append(new TextComponent(". ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                );
                case UNSTABLE -> tooltip.add(
                        new TextComponent("- ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                .append(new TextComponent("Causes ").withStyle(Style.EMPTY))
                                .append(new TextComponent("the Brew ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                                .append(new TextComponent("to ").withStyle(Style.EMPTY))
                                .append(new TextComponent("randomize").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent(".").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                );
        }



    }

    public CatalystType getType() {
        return type;
    }

    public static ItemStack createRandomCatalyst(Vault vault, Random random) {
        List<Item> catalysts = List.of(ModItems.CATALYST_UNSTABLE, ModItems.CATALYST_AMPLIFYING, ModItems.CATALYST_FOCUSING, ModItems.CATALYST_STABILITY, ModItems.CATALYST_TEMPORAL);
        ItemStack stack = new ItemStack(catalysts.get(random.nextInt(catalysts.size())));
        stack.getOrCreateTag().putString("VaultId", vault.get(Vault.ID).toString());
        return stack;
    }

    public enum CatalystType {
        STABILIZING {
            @Override
            public void applyEffect(VirtualWorld world, AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools, BrewingAltarTileEntity.PercentageResult result) {
                if (JavaRandom.ofNanoTime().nextBoolean()) {
                    ResourceLocation removedNegativePool = null;

                    Iterator<ResourceLocation> poolIterator = chosenPools.iterator();
                    while (poolIterator.hasNext()) {
                        ResourceLocation pool = poolIterator.next();
                        if (pool.equals(cfg.getNegativeModifierPool()) || pool.equals(cfg.getStrongNegativeModifierPool())) {
                            removedNegativePool = pool;
                            poolIterator.remove(); // safely remove from list during iteration
                            break; // only remove the first matching one
                        }
                    }

                    if (removedNegativePool != null && !modifierMap.isEmpty()) {
                        modifierMap.clear();
                        RandomSource random = JavaRandom.ofNanoTime();
                        for (ResourceLocation mod : chosenPools) {
                            for(VaultModifier<?> modifier : iskallia.vault.init.ModConfigs.VAULT_MODIFIER_POOLS.getRandom(mod, obj.get(AlchemyObjective.VAULT_LEVEL), random)) {
                                modifierMap.put(modifier, 1);
                            }
                        }
                        world.players().forEach(player -> player.sendMessage(
                                new TextComponent("The Potion has been cleansed by the Catalyst.").withStyle(Style.EMPTY.withColor(0xF0E68C)),
                                Util.NIL_UUID
                        ));
                    }


                } else {
                    world.players().forEach(player -> player.sendMessage(
                            new TextComponent("The Catalyst fails.").withStyle(ChatFormatting.RED),
                            Util.NIL_UUID
                    ));
                }
            }
        },
        AMPLIFYING {
            @Override
            public void applyEffect(VirtualWorld world, AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools, BrewingAltarTileEntity.PercentageResult result) {
                float boostFactor = 1 + (0.25f + JavaRandom.ofNanoTime().nextFloat() * 0.5f); // 25% to 75%

                result.setMin(data.getEntity().getProgressIncrease().min() * boostFactor);
                result.setMax(data.getEntity().getProgressIncrease().max() * boostFactor);

                String formatted = String.format("%.1f%%", (boostFactor -1) * 100);
                world.players().forEach(player -> player.sendMessage(
                        new TextComponent("The potion has been amplified ").withStyle(Style.EMPTY.withColor(0xFFFFFF))
                                .append(new TextComponent("by " + formatted + " ").withStyle(Style.EMPTY.withColor(0xF0E68C)))
                                .append(new TextComponent("with the Catalyst.").withStyle(Style.EMPTY.withColor(0xFFFFFF))),
                        Util.NIL_UUID
                ));
            }
        },
        FOCUSING {
            @Override
            public void applyEffect(VirtualWorld world, AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools, BrewingAltarTileEntity.PercentageResult result) {
                boolean allSame = data.getIngredients().stream()
                        .map(ItemStack::getItem)
                        .distinct()
                        .count() == 1;

                if (allSame) {
                    result.setMin(data.getEntity().getProgressIncrease().min() * 2);
                    result.setMax(data.getEntity().getProgressIncrease().max() * 2);

                    modifierMap.replaceAll((mod, val) -> val * 2);
                    world.players().forEach(player -> player.sendMessage(
                            new TextComponent("The modifiers have been doubled by the Catalyst").withStyle(Style.EMPTY.withColor(0xFFFFFF)),
                            Util.NIL_UUID
                    ));
                }

            }
        },
        TEMPORAL {
            @Override
            public void applyEffect(VirtualWorld world, AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools, BrewingAltarTileEntity.PercentageResult result) {
                int newUses = Math.min(5, data.getEntity().getBlockState().getValue(BrewingAltar.USES) + 1);
                BlockState newState = data.getEntity().getBlockState().setValue(BrewingAltar.USES, newUses);
                world.setBlock(data.getPos(), newState, Block.UPDATE_ALL);
                world.players().forEach(player -> player.sendMessage(
                        new TextComponent("The altar remains stable.").withStyle(Style.EMPTY.withColor(0xFFFFFF)),
                        Util.NIL_UUID
                ));
            }
        },
        UNSTABLE {
            @Override
            public void applyEffect(VirtualWorld world, AlchemyObjective obj, AlchemyObjectiveConfig.Entry cfg, BrewingAltarBrewEvent.Data data, Map<VaultModifier<?>, Integer> modifierMap, List<ResourceLocation> chosenPools, BrewingAltarTileEntity.PercentageResult result) {
                modifierMap.clear();

                List<ResourceLocation> allPools = List.of(
                        cfg.getNegativeModifierPool(),
                        cfg.getStrongNegativeModifierPool(),
                        cfg.getPositiveModifierPool(),
                        cfg.getStrongPositiveModifierPool()
                );

                RandomSource random = JavaRandom.ofNanoTime();
                for (int i = 0; i < 2 + random.nextInt(3); i++) { // Add 2–4 random modifiers
                    ResourceLocation pool = allPools.get(random.nextInt(allPools.size()));
                    List<VaultModifier<?>> mods = iskallia.vault.init.ModConfigs.VAULT_MODIFIER_POOLS.getRandom(pool, obj.get(AlchemyObjective.VAULT_LEVEL), random);
                    if (!mods.isEmpty()) {
                        VaultModifier<?> picked = mods.get(random.nextInt(mods.size()));
                        modifierMap.put(picked, 1);
                    }
                }
                float randMin = -0.9f + random.nextFloat() * 1.8f; // Range: -0.9 to +0.9
                float randMax = -0.9f + random.nextFloat() * 1.8f;

                float finalMin = Math.min(randMin, randMax);
                float finalMax = Math.max(randMin, randMax);

                result.setMin(finalMin);
                result.setMax(finalMax);

                world.players().forEach(player -> player.sendMessage(
                        new TextComponent("The brew has been randomized by the Catalyst").withStyle(Style.EMPTY.withColor(0xFFFFFF)),
                        Util.NIL_UUID
                ));
            }
        };

        public abstract void applyEffect(
                VirtualWorld world,
                AlchemyObjective obj,
                AlchemyObjectiveConfig.Entry cfg,
                BrewingAltarBrewEvent.Data data,
                Map<VaultModifier<?>, Integer> modifierMap,
                List<ResourceLocation> chosenPools,
                BrewingAltarTileEntity.PercentageResult result
        );
    }
}
