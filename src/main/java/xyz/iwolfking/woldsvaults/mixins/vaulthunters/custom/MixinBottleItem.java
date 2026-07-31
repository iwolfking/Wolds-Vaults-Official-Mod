package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.bottle.BottleEffect;
import iskallia.vault.item.bottle.BottleItem;
import iskallia.vault.item.core.DataInitializationItem;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.events.vault.UsedVaultBottleEvent;
import xyz.iwolfking.woldsvaults.events.vault.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BottleEffectAccessor;

import java.util.Optional;
import java.util.Random;

@Mixin(value = BottleItem.class, remap = false)
public abstract class MixinBottleItem implements DataInitializationItem  {
    @Shadow
    public static Optional<BottleEffect> getEffect(ItemStack bottle) {
        return Optional.empty();
    }

    @Shadow
    public static Optional<BottleItem.Type> getType(ItemStack stack) {
        return Optional.empty();
    }

    @Shadow
    public static boolean isActive(Vault vault, ItemStack stack) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    public static void consumeCharge(ItemStack stack, ServerPlayer player) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Override
    public void initialize(ItemStack itemStack, RandomSource randomSource) {
        if(itemStack.hasTag() && itemStack.getTag() != null && itemStack.getTag().contains("VaultRoyaleVial")) {
            Random rand = new Random();
            String rechargeType = switch (rand.nextInt(0, 3)) {
                case 0 -> "time";
                case 1 -> "chests";
                case 2 -> "mobs";
                default -> "time";
            };
            itemStack.getOrCreateTag().putString(BottleItem.TYPE, "vial");
            itemStack.getOrCreateTag().putString(BottleItem.RECHARGE, rechargeType);
            itemStack.getOrCreateTag().putInt(BottleItem.CHARGES, rand.nextInt(0, 7));
            if(rand.nextFloat() < 0.8) {
                int effectNum = rand.nextInt(0, ModConfigs.VAULT_ALCHEMY_TABLE.getCraftableEffects().size());
                ModConfigs.VAULT_ALCHEMY_TABLE.getCraftableEffects().get(effectNum).createEffect(BottleItem.Type.VIAL).ifPresent(effect -> BottleItem.setEffect(itemStack, effect));
            }
        }
    }

    @Inject(method = "isActive(Liskallia/vault/core/vault/Vault;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void isActiveInVault(Vault vault, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(stack.hasTag() && stack.getTag() != null && stack.getTag().contains("VaultRoyaleVial")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isActive(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void isActiveOverride(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(stack.hasTag() && stack.getTag() != null && stack.getTag().contains("VaultRoyaleVial")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/ServerVaults;get(Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", shift = At.Shift.AFTER), cancellable = true)
    private void invokeBottleDrinkEvent(ItemStack stack, Level world, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if(entity instanceof ServerPlayer player) {
            UsedVaultBottleEvent.Data eventData = WoldCommonEvents.VAULT_BOTTLE_DRINK.invoke(world, player.getOnPos(), player, stack, getEffect(stack).orElse(null), getType(stack).orElse(null));
            if(!eventData.isCancelled()) {
                Optional<Vault> vaultOpt = ServerVaults.get(player.getLevel());
                vaultOpt.ifPresent(vault -> {
                    if (isActive(vault, stack) && stack.getOrCreateTag().getInt("charges") > 0) {
                        CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
                        eventData.getEffect().ifPresent(bottleEffect -> ((BottleEffectAccessor)bottleEffect).callTrigger(player));
                        eventData.getType().ifPresent(type -> entity.heal(ModConfigs.POTION.getPotion(type).getHealing()));
                        if(eventData.shouldConsumeCharge()) {
                            consumeCharge(stack, player);
                        }
                        world.gameEvent(entity, GameEvent.DRINKING_FINISH, entity.eyeBlockPosition());
                    }
                });

            }
        }

        cir.setReturnValue(stack);
    }
}
