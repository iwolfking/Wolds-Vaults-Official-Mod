package xyz.iwolfking.woldsvaults.talent;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.item.bottle.*;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.events.vault.UsedVaultBottleEvent;
import xyz.iwolfking.woldsvaults.events.vault.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.*;

import java.util.Optional;

public class HealthyElixirTalent extends LearnableSkill {
    private float additionalMaxHealthHealed;

    public HealthyElixirTalent(int unlockLevel, int learnPointCost, int regretPointCost, float additionalMaxHealthHealed) {
        super(unlockLevel, learnPointCost, regretPointCost);
        this.additionalMaxHealthHealed = additionalMaxHealthHealed;
    }

    public HealthyElixirTalent() {
    }

    public float getAdditionalMaxHealthHealed() {
        return this.additionalMaxHealthHealed;
    }

    private static void applyBonus(UsedVaultBottleEvent.Data data) {
        if(data.isCancelled()) {
            return;
        }

        ServerPlayer player = data.getDrinker();
        if (player.getServer() != null) {
            TalentTree tree = PlayerTalentsData.get(player.getServer()).getTalents(player);

            for (HealthyElixirTalent talent : tree.getAll(HealthyElixirTalent.class, Skill::isUnlocked)) {
                if(data.getType().isPresent()) {
                    BottleItem.Type type = data.getType().get();
                    switch (type) {
                        case VIAL -> player.heal(player.getMaxHealth() * (talent.getAdditionalMaxHealthHealed() * 0.5F));
                        case POTION -> player.heal(player.getMaxHealth() * (talent.getAdditionalMaxHealthHealed() * 0.75F));
                        case MIXTURE -> player.heal(player.getMaxHealth() * (talent.getAdditionalMaxHealthHealed()));
                        case BREW -> player.heal(player.getMaxHealth() * (talent.getAdditionalMaxHealthHealed() * 1.5F));
                    }
                }
            }
        }
    }

    static {
        WoldCommonEvents.VAULT_BOTTLE_DRINK.register(HealthyElixirTalent.class, HealthyElixirTalent::applyBonus);
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.additionalMaxHealthHealed, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.additionalMaxHealthHealed = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.additionalMaxHealthHealed).ifPresent(tag -> nbt.put("additionalMaxHealthHealed", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.additionalMaxHealthHealed = Adapters.FLOAT.readNbt(nbt.get("additionalMaxHealthHealed")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(this.additionalMaxHealthHealed).ifPresent(element -> json.add("additionalMaxHealthHealed", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.additionalMaxHealthHealed = Adapters.FLOAT.readJson(json.get("additionalMaxHealthHealed")).orElse(0.0F);
    }
}