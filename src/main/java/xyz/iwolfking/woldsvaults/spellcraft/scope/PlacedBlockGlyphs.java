package xyz.iwolfking.woldsvaults.spellcraft.scope;

import iskallia.vault.block.entity.LandmineBlockEntity;
import iskallia.vault.block.entity.TotemTileEntity;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;

import javax.annotation.Nullable;

public final class PlacedBlockGlyphs {
    private static final String GLYPHS_KEY = "woldsvaults:spell_glyphs";

    private PlacedBlockGlyphs() {
    }

    /**
     * Gives a block entity created during a cast that cast's tag, and keeps a spell step's glyphs in its saved data.
     */
    public static void tagIfCasting(BlockEntity blockEntity) {
        CastTag tag = CastContext.tagIfCasting(blockEntity);
        if (tag != null && tag.glyphs() != null && keepsGlyphs(blockEntity)) {
            blockEntity.getTileData().put(GLYPHS_KEY, tag.glyphs().save());
        }
    }

    /**
     * The cast tag of a ticking block entity. A totem or landmine a spell step placed whose tag was lost with its chunk
     * gets a tag back from the glyphs in its saved data once its owner is online; that tag never resolves Power Dump.
     */
    @Nullable
    public static CastTag tagOrRestore(BlockEntity blockEntity) {
        CastTag tag = CastContext.tagOf(blockEntity);
        if (tag != null || !keepsGlyphs(blockEntity) || !(blockEntity.getLevel() instanceof ServerLevel level)) {
            return tag;
        }
        CompoundTag data = blockEntity.getTileData();
        if (!data.contains(GLYPHS_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        DeferredGlyphs glyphs = DeferredGlyphs.load(data.getCompound(GLYPHS_KEY));
        if (glyphs == null) {
            data.remove(GLYPHS_KEY);
            return null;
        }
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(glyphs.owner());
        if (owner == null) {
            return null;
        }
        Skill skill = AbilityLookup.child(PlayerAbilitiesData.get(level).getAbilities(owner), glyphs.spec()).orElse(null);
        if (skill == null) {
            WoldLogger.error("Deferred glyphs: {} at {} was placed by {} but {} has no such ability now; the glyphs are dropped.",
                    blockEntity.getClass().getSimpleName(), blockEntity.getBlockPos(), glyphs.spec(), owner.getScoreboardName());
            data.remove(GLYPHS_KEY);
            return null;
        }
        CastTag restored = new CastTag(owner, skill);
        restored.resolvePowerDump(false, 1.0F);
        restored.setGlyphs(glyphs);
        CastContext.tag(blockEntity, restored);
        return restored;
    }

    private static boolean keepsGlyphs(BlockEntity blockEntity) {
        return blockEntity instanceof TotemTileEntity || blockEntity instanceof LandmineBlockEntity;
    }
}
