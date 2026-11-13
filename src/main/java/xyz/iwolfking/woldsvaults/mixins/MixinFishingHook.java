package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.init.ModItems;
import static net.minecraft.world.item.Items.FISHING_ROD;

@Mixin(FishingHook.class)
public abstract class MixinFishingHook extends Projectile {

    protected MixinFishingHook(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Redirect(method = "shouldStopFishing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean letVRodsCast(ItemStack stack, Item x) {
        return stack.is(FISHING_ROD) || stack.is(ModItems.VAULT_ROD);
    }
}