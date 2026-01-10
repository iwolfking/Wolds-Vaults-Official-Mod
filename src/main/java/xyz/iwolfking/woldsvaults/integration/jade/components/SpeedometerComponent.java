package xyz.iwolfking.woldsvaults.integration.jade.components;

import iskallia.vault.item.tool.ToolItem;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.integration.jade.WoldsJadePlugin;

public class SpeedometerComponent implements IComponentProvider {
    public static final SpeedometerComponent INSTANCE = new SpeedometerComponent();

    private static final int MAX_TICKS_FOR_COLOR = 80;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (!iPluginConfig.get(WoldsJadePlugin.INSTANCE)) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ToolItem)) {
            return;
        }

        Block block = blockAccessor.getBlock();
        if (block.defaultDestroyTime() == -1) {
            return;
        }

        BlockState state = blockAccessor.getBlockState();

        float requiredSpeed = getMiningSpeed(player, stack, block, state, false);
        float currentSpeed = stack.getDestroySpeed(state);

        WoldsVaultsConfig.OutputFormat format =
                WoldsVaultsConfig.CLIENT.tooltipOutputFormat.get();

        float formattedSpeed = format(format, requiredSpeed, currentSpeed);

        boolean canInstamine = requiredSpeed == 0 || currentSpeed >= requiredSpeed;
        boolean additional = format == WoldsVaultsConfig.OutputFormat.ADDITIONAL_MINING_SPEED;

        int ticks = getMiningTicks(requiredSpeed, currentSpeed);

        if (ticks > 1 && !canInstamine && format.equals(WoldsVaultsConfig.OutputFormat.NEXT_MINING_SPEED_BREAKPOINT)) {
            float delta = getMiningSpeedDeltaForNextTick(requiredSpeed, currentSpeed);

            if (delta > 0) {
                String deltaFormatted = String.format("%.2f", delta);

                iTooltip.add(new TranslatableComponent("speedometer.jade.tooltip.speed_for_breakpoint", deltaFormatted, ticks - 1).withStyle(Style.EMPTY.withColor(getColorForTicks(ticks))));
            }
        }
        else {
            if (canInstamine && additional) {
                iTooltip.add(
                        new TranslatableComponent("speedometer.jade.tooltip.instamine")
                                .withStyle(ChatFormatting.GREEN)
                );
                return;
            }

            iTooltip.add(
                    new TranslatableComponent(
                            "speedometer.jade.tooltip.speed_" + (additional ? "additional" : "needed"),
                            formattedSpeed,
                            (canInstamine ? "✔" : "✘")
                    ).withStyle(canInstamine ? ChatFormatting.GREEN : ChatFormatting.RED)
            );
        }
    }


    public static float getMiningSpeed(Player player, ItemStack stack, Block block, BlockState state, boolean sendTips) {
        float baseSpeed = stack.getDestroySpeed(state);
        float speedToInstamine = block.defaultDestroyTime() * 30;

        if (MobEffectUtil.hasDigSpeed(player)) {
            int haste = MobEffectUtil.getDigSpeedAmplification(player);
            speedToInstamine *= 1 / (1.0F + (float) (haste + 1) * 0.2F);
        }

        if (baseSpeed > 1.0F) {
            int efficiency = EnchantmentHelper.getBlockEfficiency(player);
            if (efficiency > 0) {
                speedToInstamine -= (float) (efficiency * efficiency + 1);
            }
        }

        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float speedReduction = switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
            speedToInstamine *= (float) 1 / speedReduction;
        }

        if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            speedToInstamine *= 5.0F;
        }

        if (!player.isOnGround()) {
            speedToInstamine *= 5.0F;
        }

        return Math.max(speedToInstamine, 0);
    }

    public static float format(WoldsVaultsConfig.OutputFormat outputFormat, float speedToInstamine, float baseSpeed) {
        return Math.max(switch (outputFormat) {
            case TOTAL_MINING_SPEED_WITHBASE -> speedToInstamine;
            case TOTAL_MINING_SPEED_WITHOUTBASE -> speedToInstamine - 9.0F;
            case ADDITIONAL_MINING_SPEED -> speedToInstamine - baseSpeed;
            case NEXT_MINING_SPEED_BREAKPOINT -> 0F;
        }, 0);
    }

    public static int getMiningTicks(float requiredSpeed, float miningSpeed) {
        if (miningSpeed <= 0) return Integer.MAX_VALUE;
        return (int) Math.ceil(requiredSpeed / miningSpeed);
    }

    public static float getNextMiningSpeedBreakpoint(
            float requiredSpeed,
            float currentMiningSpeed
    ) {
        int ticks = getMiningTicks(requiredSpeed, currentMiningSpeed);

        if (ticks <= 1) {
            return currentMiningSpeed;
        }

        return requiredSpeed / (ticks - 1);
    }

    public static float getMiningSpeedDeltaForNextTick(
            float requiredSpeed,
            float currentMiningSpeed
    ) {
        float next = getNextMiningSpeedBreakpoint(requiredSpeed, currentMiningSpeed);
        return Math.max(0, next - currentMiningSpeed);
    }

    public static TextColor getColorForTicks(int ticks) {

        int clamped = Math.min(Math.max(ticks, 1), MAX_TICKS_FOR_COLOR);


        float t = 1.0F - (float) (clamped - 1) / (MAX_TICKS_FOR_COLOR - 1);


        int red   = (int) (0xFF * (1.0F - t) + 0x55 * t);
        int green = (int) (0x55 * (1.0F - t) + 0xFF * t);
        int blue  = 0x55;

        int rgb = (red << 16) | (green << 8) | blue;
        return TextColor.fromRgb(rgb);
    }






}