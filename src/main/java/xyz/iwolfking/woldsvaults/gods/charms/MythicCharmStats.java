package xyz.iwolfking.woldsvaults.gods.charms;

import iskallia.vault.VaultMod;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.ItemVaultFruit;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import javax.annotation.Nullable;

/**
 * Consumers for the mythic charm stats that have no base pipeline of their own: the ability power
 * multiplier joins the base {@code ABILITY_POWER_MULTIPLIER} player-stat event (which the ability
 * power helper and the statistics screen both run through, on both sides), boss damage multiplies
 * hits against anything in the pack's {@code the_vault:bosses} entity group, and fruit saver
 * gives eaten vault fruits a chance to not be consumed. The item quantity, item rarity and trap
 * disarm multipliers live in the attribute-snapshot mixin instead, and the vanilla-attribute
 * multipliers ride the charm's curio attribute modifiers.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MythicCharmStats {
    private MythicCharmStats() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(MythicCharmStats::register);
    }

    private static void register() {
        MinecraftForge.EVENT_BUS.addListener(MythicCharmStats::onLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(MythicCharmStats::onFruitEaten);
        CommonEvents.PLAYER_STAT.of(PlayerStat.ABILITY_POWER_MULTIPLIER).register(MythicCharmStats.class, data -> {
            if (data.getEntity() instanceof Player player) {
                float multiplier = snapshotSum(player, ModGearAttributes.ABILITY_POWER_MULTIPLIER);
                if (multiplier > 0.0F) {
                    data.setValue(data.getValue() * (1.0F + multiplier));
                }
            }
        });
    }

    /**
     * Maps a base stat attribute to the mythic multiplier that scales it at the snapshot level.
     * Consulted by the attribute-snapshot mixin on every merged read, so the multiplied value
     * reaches gameplay and the statistics screen through the same door.
     */
    @Nullable
    public static VaultGearAttribute<Float> snapshotMultiplierFor(VaultGearAttribute<?> attribute) {
        if (attribute == iskallia.vault.init.ModGearAttributes.ITEM_QUANTITY) {
            return ModGearAttributes.ITEM_QUANTITY_MULTIPLIER;
        }
        if (attribute == iskallia.vault.init.ModGearAttributes.ITEM_RARITY) {
            return ModGearAttributes.ITEM_RARITY_MULTIPLIER;
        }
        if (attribute == iskallia.vault.init.ModGearAttributes.TRAP_DISARMING) {
            return ModGearAttributes.TRAP_DISARM_MULTIPLIER;
        }
        return null;
    }

    public static float snapshotSum(LivingEntity entity, VaultGearAttribute<Float> attribute) {
        if (!AttributeSnapshotHelper.canHaveSnapshot(entity)) {
            return 0.0F;
        }
        return AttributeSnapshotHelper.getInstance().getSnapshot(entity)
                .getAttributeValue(attribute, VaultGearAttributeTypeMerger.floatSum());
    }

    private static void onLivingHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof LivingEntity attacker) || attacker.getLevel().isClientSide()
                || !AttributeSnapshotHelper.canHaveSnapshot(attacker)) {
            return;
        }
        if (!ModConfigs.ENTITY_GROUPS.isInGroup(VaultMod.id("bosses"), event.getEntityLiving())) {
            return;
        }
        float bonus = snapshotSum(attacker, ModGearAttributes.DAMAGE_BOSS);
        if (bonus > 0.0F) {
            event.setAmount(event.getAmount() * (1.0F + bonus));
        }
    }

    private static void onFruitEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getItem().getItem() instanceof ItemVaultFruit)
                || !(event.getEntityLiving() instanceof ServerPlayer player)) {
            return;
        }
        float chance = snapshotSum(player, ModGearAttributes.FRUIT_SAVER);
        if (chance <= 0.0F || player.getRandom().nextFloat() >= chance) {
            return;
        }
        ItemStack used = event.getItem();
        ItemStack result = event.getResultStack();
        ItemStack restored;
        if (result.isEmpty()) {
            restored = used.copy();
            restored.setCount(1);
        } else {
            if (result.getItem() != used.getItem() || result.getCount() >= used.getCount()) {
                return;
            }
            restored = result.copy();
            restored.grow(1);
        }
        event.setResultStack(restored);
        player.displayClientMessage(new TextComponent("Fruit Saver preserved the fruit.")
                .withStyle(ChatFormatting.GRAY), true);
    }
}
