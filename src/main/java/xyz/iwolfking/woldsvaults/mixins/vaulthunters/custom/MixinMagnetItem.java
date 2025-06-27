package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;


import iskallia.vault.block.entity.DemagnetizerTileEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.CuriosGearItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.trinket.TrinketHelper;
import iskallia.vault.gear.trinket.effects.EnderAnchorTrinket;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModParticles;
import iskallia.vault.item.MagnetItem;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xyz.iwolfking.woldsvaults.enchantment.DecorShieldEnchantment;
import xyz.iwolfking.woldsvaults.init.ModEnchantments;

import java.util.List;
import java.util.Optional;

@Mixin(value = MagnetItem.class, remap = false)
public abstract class MixinMagnetItem extends Item implements VaultGearItem, CuriosGearItem, ICurioItem {

    public MixinMagnetItem(Properties pProperties) {
        super(pProperties);
    }

    @Shadow
    public static Optional<ItemStack> getMagnet(LivingEntity entity) {
        return Optional.empty();
    }

    @Shadow
    public static void teleportToPlayer(Player player, List<? extends Entity> entities) {
    }

    @Shadow
    public static void moveToPlayer(Player player, List<? extends Entity> entities, float speed) {
    }


    /**
     * @author iwolfking
     * @reason Add Endergized Magnet Modifier
     */
    @Overwrite
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (event.player instanceof ServerPlayer player && player.level instanceof ServerLevel world) {
                getMagnet(event.player).ifPresent(stack -> {
                    if (stack.getItem() instanceof VaultGearItem gearItem && gearItem.isBroken(stack)) {
                        return;
                    }

                    if (!DemagnetizerTileEntity.hasDemagnetizerAround(event.player)) {
                        VaultGearData data = VaultGearData.read(stack);
                        float range = data.get(ModGearAttributes.RANGE, VaultGearAttributeTypeMerger.floatSum());
                        float speed = data.get(ModGearAttributes.VELOCITY, VaultGearAttributeTypeMerger.floatSum());
                        List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(range), (entity) -> {
                            return entity.distanceToSqr(player) <= range * range && !entity.getTags().contains("PreventMagnetMovement");
                        });
                        List<ExperienceOrb> orbs = world.getEntitiesOfClass(ExperienceOrb.class, player.getBoundingBox().inflate(range), (entity) -> {
                            return entity.distanceToSqr(player) <= range * range  && !entity.getTags().contains("PreventMagnetMovement");
                        });
                        List<TrinketHelper.TrinketStack<EnderAnchorTrinket>> enderAnchors = TrinketHelper.getTrinkets(player, EnderAnchorTrinket.class);
                        for(TrinketHelper.TrinketStack<EnderAnchorTrinket> enderAnchor : enderAnchors) {
                            if(enderAnchor.isUsable(player)) {
                                woldsVaults$teleportToPlayerEnhanced(player, items);
                                woldsVaults$teleportToPlayerEnhanced(player, orbs);
                                return;
                            }
                        }
                        if(AttributeGearData.read(stack).get(xyz.iwolfking.woldsvaults.init.ModGearAttributes.MAGNET_ENDERGIZED, VaultGearAttributeTypeMerger.anyTrue())) {
                            teleportToPlayer(player, items);
                            teleportToPlayer(player, orbs);
                        }
                        moveToPlayer(player, items, speed);
                        moveToPlayer(player, orbs, speed);
                    }
                });
            }
        }
    }

    @Unique
    private static void woldsVaults$teleportToPlayerEnhanced(Player player, List<? extends Entity> entities) {
        for(Entity entity : entities) {
            if (entity instanceof ItemEntity item) {
                if (allowsNoPickupDelay(item, player)) {
                    item.setNoPickUpDelay();
                }
            }

            Level var5 = player.level;
            if (var5 instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles((SimpleParticleType) ModParticles.ENDER_ANCHOR.get(), entity.position().x, entity.position().y + (double)0.25F, entity.position().z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
            }

            entity.teleportTo(player.position().x, player.position().y, player.position().z);
            entity.hurtMarked = true;

            if(random.nextBoolean()) {
                entity.getTags().add("MagnetPulled");
                return;
            }
        }

    }

    @Shadow
    private static boolean allowsNoPickupDelay(ItemEntity itemEntity, Player player) {
        return false;
    }


    // they're in different methods, so I need to share them somehow - let's hope that it's not run in parallel
    @Unique private static final ThreadLocal<ItemEntity> woldsVaults$pickedUpItem = new ThreadLocal<>();

    @Inject(method = "onPlayerPickup", at = @At(value = "INVOKE", target = "Liskallia/vault/item/MagnetItem;getMagnet(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;"))
    private static void capturePickedUpItem(Player player, ItemEntity item, CallbackInfo ci) {
        woldsVaults$pickedUpItem.set(item);
    }

    @Inject(method = "lambda$onPlayerPickup$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"), cancellable = true)
    private static void decorShieldEnchantment(Player player, ItemStack stack, CallbackInfo ci) {
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DECOR_SHIELD, stack) > 0){
            var itemEntity = woldsVaults$pickedUpItem.get();
            if (itemEntity == null) {
                return;
            }
            if (DecorShieldEnchantment.isDecorItem(itemEntity.getItem().getItem())) {
                ci.cancel();
            }
        }
    }
}
