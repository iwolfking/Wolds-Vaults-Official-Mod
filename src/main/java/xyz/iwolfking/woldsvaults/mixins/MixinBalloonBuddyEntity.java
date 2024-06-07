package xyz.iwolfking.woldsvaults.mixins;

import com.github.alexthe668.cloudstorage.entity.BalloonBuddyEntity;
import com.github.alexthe668.cloudstorage.entity.BalloonFace;
import com.github.alexthe668.cloudstorage.entity.BalloonFlyer;
import com.github.alexthe668.cloudstorage.entity.LivingBalloon;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BalloonBuddyEntity.class, remap = false)
public abstract class MixinBalloonBuddyEntity extends TamableAnimal implements LivingBalloon, BalloonFlyer {

    @Shadow public abstract BalloonFace getPersonality();

    protected MixinBalloonBuddyEntity(EntityType<? extends TamableAnimal> p_21803_, Level p_21804_) {
        super(p_21803_, p_21804_);
    }


    @Override
    public void tick() {
        if(ServerVaults.get(this.getUUID()).isPresent() && this.getPersonality() == BalloonFace.SCARY) {
            return;
        }
        else {
            super.tick();
        }

    }
}