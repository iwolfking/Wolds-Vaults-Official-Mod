package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.client.data.ClientPrestigePowersData;
import iskallia.vault.config.TrinketConfig;
import iskallia.vault.core.vault.challenge.curse.ChallengeCurseHelper;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.item.gear.VaultUsesHelper;
import iskallia.vault.item.render.core.IManualModelLoading;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.NoVaultUsesPrestigePower;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.world.data.DiscoveredTrinketsData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.renderers.CombinedTrinketRenderer;
import xyz.iwolfking.woldsvaults.init.ModItems;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CombinedTrinketItem extends TrinketItem implements IManualModelLoading {

    public CombinedTrinketItem(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getName(ItemStack stack) {

        if (!isIdentified(stack)) {
            return super.getName(stack)
                    .copy()
                    .withStyle(Style.EMPTY.withColor(16004495));
        }

        List<TrinketEffect<?>> effects = getTrinkets(stack);

        MutableComponent result = new TextComponent("");

        for (int i = 0; i < effects.size(); i++) {

            TrinketConfig.Trinket cfg = effects.get(i).getTrinketConfig();

            MutableComponent part = new TextComponent(cfg.getName())
                    .withStyle(Style.EMPTY.withColor(cfg.getComponentColor()));

            result.append(part);

            if (i != effects.size() - 1) {
                result.append(new TextComponent("-")
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        return result;
    }

    public static ItemStack createCombined(List<TrinketEffect<?>> effects, int uses) {
        ItemStack stack = new ItemStack(ModItems.COMBINED_TRINKET);

        AttributeGearData data = AttributeGearData.read(stack);
        data.createOrReplaceAttributeValue(ModGearAttributes.STATE, VaultGearState.IDENTIFIED);
        effects.forEach(trinketEffect -> {
            data.addAttribute(new VaultGearAttributeInstance<>(ModGearAttributes.TRINKET_EFFECT, trinketEffect));
        });
        data.write(stack);

        VaultUsesHelper.setUses(stack, uses);
        return stack;
    }

    public static List<TrinketEffect<?>> getTrinkets(ItemStack stack) {
        if (!(stack.getItem() instanceof CombinedTrinketItem)) return List.of();

        return AttributeGearData.read(stack)
                .getAttributes(ModGearAttributes.TRINKET_EFFECT)
                .map(VaultGearAttributeInstance::getValue).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (!isIdentified(stack)) return;

        for (TrinketEffect<?> effect : getTrinkets(stack)) {
            if (effect.getRegistryName() == null) continue;

            if (!ChallengeCurseHelper.isTrinketDisabled(
                    ctx.entity().getUUID(),
                    effect.getRegistryName())) {

                effect.onWornTick(ctx.entity(), stack);
            }
        }

        super.curioTick(ctx, stack);
    }

    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        for (TrinketEffect<?> effect : getTrinkets(stack)) {
            effect.onEquip(ctx.entity(), stack);
        }
        super.onEquip(ctx, prev, stack);
    }

    @Override
    public void onUnequip(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        super.onUnequip(identifier, index, livingEntity, stack);
        getTrinkets(stack).forEach(trinketEffect -> {
            trinketEffect.onUnEquip(livingEntity, stack);
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (!isIdentified(stack)) {
            tooltip.add((new TextComponent("Right click to identify")).withStyle(ChatFormatting.GRAY));
        } else {
            if (ClientPrestigePowersData.getTree().getAll(NoVaultUsesPrestigePower.class, Skill::isUnlocked).stream().noneMatch((power) -> power.getItemId().equals(stack.getItem().getRegistryName()))) {
                int totalUses = VaultUsesHelper.getUses(stack);
                int used = VaultUsesHelper.getUsedVaults(stack).size();
                int remaining = Math.max(totalUses - used, 0);
                MutableComponent usesTxt = (new TextComponent("Uses: ")).append(new TextComponent(String.valueOf(remaining)));
                tooltip.add(usesTxt);
            }

            AttributeGearData data = AttributeGearData.read(stack);
            data.getFirstValue(ModGearAttributes.CRAFTED_BY).ifPresent((crafter) -> tooltip.add((new TextComponent("Crafted by: ")).append((new TextComponent(crafter)).setStyle(Style.EMPTY.withColor(16770048)))));
            for (TrinketEffect<?> effect : getTrinkets(stack)) {
                TrinketConfig.Trinket cfg = effect.getTrinketConfig();

                tooltip.add(new TextComponent(cfg.getName())
                        .withStyle(ChatFormatting.GOLD));

                for (TextComponent line : MiscUtils.splitDescriptionText(cfg.getEffectText())) {
                    tooltip.add(line.withStyle(ChatFormatting.GRAY));
                }
            }
            getSlotIdentifier(stack).ifPresent((slotIdentifier) -> {
                MutableComponent slotsTooltip = (new TranslatableComponent("curios.slot")).append(": ").withStyle(ChatFormatting.GOLD);
                MutableComponent type = new TranslatableComponent("curios.identifier." + slotIdentifier);
                type = type.withStyle(ChatFormatting.YELLOW);
                slotsTooltip.append(type);
                tooltip.add(TextComponent.EMPTY);
                tooltip.add(slotsTooltip);
            });
        }
    }

    public void tickRoll(ItemStack stack, @Nullable Player player) {
        AttributeGearData data = AttributeGearData.read(stack);
        TrinketEffect<?> randomTrinket;
        TrinketEffect<?> randomTrinket2;

        if (player instanceof ServerPlayer sPlayer) {
                DiscoveredTrinketsData trinketData = DiscoveredTrinketsData.get(sPlayer.getLevel().getServer());
                if (trinketData.discoveredAllTrinkets(sPlayer)) {
                    randomTrinket = ModConfigs.TRINKET.getRandomTrinketSet();
                    randomTrinket2 = ModConfigs.TRINKET.getRandomTrinketSet();
                } else {
                    randomTrinket = ModConfigs.TRINKET.getRandomTrinketSet((trinket, weight) -> trinketData.hasDiscovered(sPlayer, trinket) ? Mth.ceil((float)weight * 0.16666667F) : weight);
                    randomTrinket2 = ModConfigs.TRINKET.getRandomTrinketSet((trinket, weight) -> trinketData.hasDiscovered(sPlayer, trinket) ? Mth.ceil((float)weight * 0.16666667F) : weight);
                }
            } else {
                randomTrinket = ModConfigs.TRINKET.getRandomTrinketSet();
                randomTrinket2 = ModConfigs.TRINKET.getRandomTrinketSet();
            }

        if (randomTrinket != null) {
            if(randomTrinket.equals(randomTrinket2)) {
                return;
            }
            data.addAttribute(new VaultGearAttributeInstance<>(ModGearAttributes.TRINKET_EFFECT, randomTrinket));
            data.addAttribute(new VaultGearAttributeInstance<>(ModGearAttributes.TRINKET_EFFECT, randomTrinket2));
            data.createOrReplaceAttributeValue(ModGearAttributes.STATE, VaultGearState.IDENTIFIED);
            this.setUses(stack, 25);

        }


        data.write(stack);
    }

    public void tickFinishRoll(ItemStack stack, @Nullable Player player, boolean identify) {
        if (player instanceof ServerPlayer sPlayer) {
            if (identify) {
                DiscoveredTrinketsData trinketData = DiscoveredTrinketsData.get(sPlayer.getLevel().getServer());
                trinketData.discoverTrinketAndBroadcast(stack, sPlayer);
            }
        }

    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.IItemRenderProperties> consumer) {
        consumer.accept(new net.minecraftforge.client.IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return CombinedTrinketRenderer.INSTANCE;
            }
        });
    }

    @Override
    public void loadModels(Consumer<ModelResourceLocation> consumer) {
        consumer.accept(new ModelResourceLocation(WoldsVaults.id("item/combined_trinket_base"), "inventory"));
        consumer.accept(new ModelResourceLocation(WoldsVaults.id("combined_trinket_base"), "inventory"));
    }
}