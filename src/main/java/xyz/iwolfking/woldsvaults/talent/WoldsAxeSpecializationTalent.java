package xyz.iwolfking.woldsvaults.talent;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;

import java.util.Optional;

public class WoldsAxeSpecializationTalent extends LearnableSkill {
    private float cleavingDamagePercentage;

    public WoldsAxeSpecializationTalent(int unlockLevel, int learnPointCost, int regretPointCost, float cleavingDamagePercentage) {
        super(unlockLevel, learnPointCost, regretPointCost);
        this.cleavingDamagePercentage = cleavingDamagePercentage;
    }

    public WoldsAxeSpecializationTalent() {
    }

    public float getCleavingDamagePercentage() {
        return this.cleavingDamagePercentage;
    }

    public static float getCleavingDamagePercentage(Player player) {
        if (player instanceof ServerPlayer serverPlayer && player.getMainHandItem().getItem() instanceof AxeItem) {
            TalentTree talents = PlayerTalentsData.get(serverPlayer.getLevel()).getTalents(serverPlayer);
            float damageIncrease = 0.0F;

            for (WoldsAxeSpecializationTalent talent : talents.getAll(WoldsAxeSpecializationTalent.class, Skill::isUnlocked)) {
                damageIncrease += talent.getCleavingDamagePercentage();
            }

            return damageIncrease;
        } else {
            return 0.0F;
        }
    }

    public static float applyCleavingDamageBonus(Player player, LivingEntity target) {
        float percentBonus = getCleavingDamagePercentage(player);
        float bonusDamage = 0F;

        if(percentBonus > 0) {
            bonusDamage = target.getHealth() * percentBonus;
        }

        return bonusDamage;
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.cleavingDamagePercentage, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.cleavingDamagePercentage = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.cleavingDamagePercentage).ifPresent(tag -> nbt.put("cleavingDamagePercentage", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.cleavingDamagePercentage = Adapters.FLOAT.readNbt(nbt.get("cleavingDamagePercentage")).orElseThrow();
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(this.cleavingDamagePercentage).ifPresent(element -> json.add("cleavingDamagePercentage", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.cleavingDamagePercentage = Adapters.FLOAT.readJson(json.get("cleavingDamagePercentage")).orElseThrow();
    }
}
