package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.core.VaultLevelItem;
import iskallia.vault.item.crystal.layout.ClassicCircleCrystalLayout;
import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import iskallia.vault.item.crystal.layout.ClassicPolygonCrystalLayout;
import iskallia.vault.item.crystal.layout.ClassicSpiralCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicCircleCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicPolygonCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicSpiralCrystalLayoutAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LayoutModificationItem extends Item implements VaultLevelItem, DataTransferItem {

    public LayoutModificationItem(CreativeModeTab group, ResourceLocation id) {
        super((new Item.Properties()).tab(group));
        this.setRegistryName(id);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag advanced) {
        super.appendHoverText(stack, world, tooltip, advanced);
        getLayout(stack).ifPresent(key -> {

            if(key instanceof ClassicCircleCrystalLayout crystalLayout) {
                tooltip.add((new TextComponent("Layout: ")).append((new TextComponent("Circle")).withStyle(Style.EMPTY.withColor(3191506))));
                tooltip.add(new TextComponent("Tunnel Span: ").append(new TextComponent(String.valueOf(((ClassicInfiniteCrystalLayoutAccessor)crystalLayout).getTunnelSpan())).withStyle(ChatFormatting.GOLD)));
                tooltip.add(new TextComponent("Radius: ").append(new TextComponent(String.valueOf(((ClassicCircleCrystalLayoutAccessor)crystalLayout).getRadius())).withStyle(ChatFormatting.GOLD)));
            }

            else if(key instanceof ClassicPolygonCrystalLayout crystalLayout) {
                tooltip.add((new TextComponent("Layout: ")).append((new TextComponent("Polygon")).withStyle(Style.EMPTY.withColor(16757504))));
                tooltip.add(new TextComponent("Tunnel Span: ").append(new TextComponent(String.valueOf(((ClassicInfiniteCrystalLayoutAccessor)crystalLayout).getTunnelSpan())).withStyle(ChatFormatting.GOLD)));
                tooltip.add(new TextComponent("Vertices: ").append(new TextComponent(Arrays.toString(((ClassicPolygonCrystalLayoutAccessor) crystalLayout).getVertices())).withStyle(ChatFormatting.GOLD)));
            }

            else if(key instanceof ClassicSpiralCrystalLayout crystalLayout) {
                tooltip.add((new TextComponent("Layout: ")).append((new TextComponent("Spiral")).withStyle(Style.EMPTY.withColor(3191296))));
                tooltip.add(new TextComponent("Tunnel Span: ").append(new TextComponent(String.valueOf(((ClassicInfiniteCrystalLayoutAccessor)crystalLayout).getTunnelSpan())).withStyle(ChatFormatting.GOLD)));
                tooltip.add(new TextComponent("Half Length: ").append(new TextComponent(String.valueOf(((ClassicSpiralCrystalLayoutAccessor)crystalLayout).getHalfLength())).withStyle(ChatFormatting.GOLD)));
            }
            else if(key instanceof ClassicInfiniteCrystalLayout crystalLayout) {
                tooltip.add((new TextComponent("Layout: ")).append((new TextComponent("Infinite")).withStyle(Style.EMPTY.withColor(16730880))));
                tooltip.add(new TextComponent("Tunnel Span: ").append(new TextComponent(String.valueOf(((ClassicInfiniteCrystalLayoutAccessor)crystalLayout).getTunnelSpan())).withStyle(ChatFormatting.GOLD)));
            }
        });
    }



    public static Optional<CrystalLayout> getLayout(ItemStack stack) {
        if (stack.getTag() == null) {
            return Optional.empty();
        } else {
            CompoundTag nbt = stack.getOrCreateTag();
            if (!nbt.contains("layout") || !nbt.contains("tunnel") || !nbt.contains("value")) {
                return Optional.empty();
            } else {
                String layoutName = nbt.getString("layout");
                int tunnel = nbt.getInt("tunnel");
                int value = nbt.getInt("value");

                return switch (layoutName) {
                    case "infinite" -> Optional.of(new ClassicInfiniteCrystalLayout(tunnel));
                    case "circle" -> Optional.of(new ClassicCircleCrystalLayout(tunnel, value));
                    case "polygon" -> Optional.of(new ClassicPolygonCrystalLayout(tunnel, new int[]{-1 * value, value, value, value, value, -1 * value, -1 * value, -1 * value}));
                    case "spiral" -> Optional.of(new ClassicSpiralCrystalLayout(tunnel, value, Rotation.CLOCKWISE_90));
                    default -> Optional.empty();
                };
            }
        }
    }

    public static ItemStack create(String type, int tunnelSpan, int value) {
        ItemStack stack = new ItemStack(ModItems.LAYOUT_MANIPULATOR);
        stack.getOrCreateTag().putString("layout", type);
        stack.getOrCreateTag().putInt("tunnel", tunnelSpan);
        stack.getOrCreateTag().putInt("value", value);
        return stack;
    }

    @Override
    public void initializeVaultLoot(int i, ItemStack stack, @org.jetbrains.annotations.Nullable BlockPos blockPos, @org.jetbrains.annotations.Nullable Vault vault) {
        createStackFromLevel(i, stack);
    }

    private String getLayoutType(CrystalLayout key) {
        if(key instanceof ClassicCircleCrystalLayout) {
           return "circle";
        }

        else if(key instanceof ClassicPolygonCrystalLayout) {
           return "polygon";
        }

        else if(key instanceof ClassicSpiralCrystalLayout) {
          return "spiral";
        }
        else if(key instanceof ClassicInfiniteCrystalLayout) {
            return "infinite";
        }

        return null;
    }

    @Override
    public ItemStack convertStack(ItemStack stack, RandomSource random) {
        if (stack.getTag() == null) {
            return stack;
        } else {
            CompoundTag nbt = stack.getOrCreateTag();
            if (!nbt.contains("level")) {
                return stack;
            } else {
                int level = nbt.getInt("pool");
                return createStackFromLevel(level, stack);
            }
        }
    }

    private ItemStack createStackFromLevel(int level, ItemStack stack) {
        if(stack.getTag() == null) {
            return stack;
        }
        else {
            CompoundTag nbt = stack.getOrCreateTag();
            if(nbt.contains("layout")) {
                return stack;
            }
            else {
                Optional<CrystalLayout> layout = ModConfigs.VAULT_CRYSTAL.getRandomLayout(level, JavaRandom.ofNanoTime());

                if(layout.isEmpty()) {
                    return stack;
                }

                String type = getLayoutType(layout.get());

                if(type != null) {
                    nbt.putString("layout", type);
                    nbt.putInt("tunnel", 1);
                    switch (type) {
                        case "polygon" -> {
                            int i = ((ClassicPolygonCrystalLayoutAccessor) layout.get()).getVertices()[1];
                            if(i == 0) {
                                i = ((ClassicPolygonCrystalLayoutAccessor) layout.get()).getVertices()[3];
                            }
                            nbt.putInt("value", i);
                        }
                        case "circle" ->
                                nbt.putInt("value", ((ClassicCircleCrystalLayoutAccessor) layout.get()).getRadius());
                        case "spiral" ->
                                nbt.putInt("value", ((ClassicSpiralCrystalLayoutAccessor) layout.get()).getHalfLength());
                        default -> nbt.putInt("value", 0);
                    }

                    stack.setTag(nbt);
                    return stack;
                }
            }
        }
        return stack;
    }
}
