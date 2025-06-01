package xyz.iwolfking.woldsvaults.divinities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityData;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FallReductionDivinity extends LearnableSkill {
    private float damageReduction;


    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.damageReduction, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.damageReduction = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.damageReduction).ifPresent((tag) -> {
                nbt.put("damageReduction", tag);
            });
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.damageReduction = Adapters.FLOAT.readNbt(nbt.get("damageReduction")).orElseThrow();
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.damageReduction).ifPresent((element) -> {
                json.add("damageReduction", element);
            });
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.damageReduction = Adapters.FLOAT.readJson(json.get("damageReduction")).orElseThrow();
    }

    public float getDamageReduction() {
        return damageReduction;
    }

    @SubscribeEvent
    public static void onLivingEntityHurt(LivingHurtEvent event) {
        LivingEntity var2 = event.getEntityLiving();
        if (var2 instanceof Player player) {
            if(player.getServer() == null) return;
            if(!event.getSource().isFall()) return;

            float damageReduction = 0.0F;
            DivinityTree divinityTree = PlayerDivinityData.get(player.getServer()).getDivinityTree(player);

            for (FallReductionDivinity power : divinityTree.getAll(FallReductionDivinity.class, Skill::isUnlocked)) {
                damageReduction = power.getDamageReduction();
            }

            event.setAmount(event.getAmount() * (1.0F - damageReduction));
        }
    }
}
