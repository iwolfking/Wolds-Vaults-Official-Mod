package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.util.ClientScheduler;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.dynamodel.DynamicModelItem;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.core.DataInitializationItem;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.render.ColorBlender;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.models.Jewels;

import java.util.Optional;

@Mixin(value = JewelItem.class, remap = false)
public abstract class MixinJewelItem extends Item implements VaultGearItem, DataInitializationItem, DataTransferItem {

    private ServerPlayer player;

    public MixinJewelItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if(this.player == null && pEntity instanceof ServerPlayer player) {
            this.player = player;
        }
    }


    @Override
    public Optional<? extends DynamicModel<?>> resolveDynamicModel(ItemStack stack, ResourceLocation key) {
        return Jewels.REGISTRY.get(key);
    }

    /**
     * @author iwolfking
     * @reason Try stuff
     */
    @Overwrite
    public void initialize(ItemStack stack, RandomSource random) {
        if(player != null) {
            this.instantIdentify(player, stack);
        }
        else {
            this.instantIdentify(null, stack);
        }

    }

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private static void noColorIfModel(ItemStack stack, CallbackInfoReturnable<Integer> cir){
        ((DynamicModelItem) stack.getItem()).getDynamicModelId(stack).ifPresent(modelId -> cir.setReturnValue(0xFFFFFF));
    }

    @Redirect(method = "getName", at = @At(value = "INVOKE", target = "Liskallia/vault/item/tool/JewelItem;getColor(Lnet/minecraft/world/item/ItemStack;)I", remap = false), remap = true)
    private int keepColoredName(ItemStack stack){
        GearDataCache clientCache = GearDataCache.of(stack);
        if (clientCache.getState() == VaultGearState.UNIDENTIFIED) {
            return -12632257;
        }
        ColorBlender blender = new ColorBlender(1.0F);
        Optional.ofNullable(clientCache.getJewelColorComponents()).ifPresent((colors) -> colors.forEach((color) -> blender.add(color, 60.0F)));
        float time = ClientScheduler.INSTANCE.getTick();
        return blender.getColor(time);
    }
}
