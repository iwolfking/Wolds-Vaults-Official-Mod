package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin (value = ItemEntity.class, remap = false)
public abstract class MixinItemEntity extends Entity {

    public MixinItemEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void thunderHit(ServerLevel pLevel, LightningBolt pLightning) { }

}
