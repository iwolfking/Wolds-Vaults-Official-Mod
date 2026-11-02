package xyz.iwolfking.woldsvaults.armory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * What every armory screen needs about the block and the open page: position, page, tab, upgrades, the 8 page
 * tabs (icon, title, saved) and the Load preview (refusal reasons, or the notes Load would produce).
 */
public record ArmoryViewInfo(BlockPos pos, int page, ArmoryTab tab, boolean creative, boolean skillsUpgrade,
                             boolean godsUpgrade, List<ItemStack> pageIcons, List<String> pageTitles,
                             List<Boolean> pageSaved, List<String> loadRefusals, List<String> loadNotes) {

    public static ArmoryViewInfo of(ArmoryBlockEntity armory, int page, ArmoryTab tab, List<String> refusals, List<String> notes) {
        List<ItemStack> icons = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<Boolean> saved = new ArrayList<>();
        for (int i = 0; i < ArmoryBlockEntity.PAGES; i++) {
            ArmoryPage p = armory.page(i);
            ItemStack icon = p.icon();
            icons.add(icon.isEmpty() ? ItemStack.EMPTY : ArmoryPage.ghost(icon));
            titles.add(icon.isEmpty() ? "Build " + (i + 1) : "Build " + (i + 1) + ": " + icon.getHoverName().getString());
            saved.add(p.hasSave());
        }
        return new ArmoryViewInfo(armory.getBlockPos(), page, tab, armory.isCreative(), armory.hasSkillsUpgrade(),
                armory.hasGodsUpgrade(), icons, titles, saved, refusals, notes);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.page);
        buf.writeEnum(this.tab);
        buf.writeBoolean(this.creative);
        buf.writeBoolean(this.skillsUpgrade);
        buf.writeBoolean(this.godsUpgrade);
        buf.writeVarInt(this.pageIcons.size());
        for (int i = 0; i < this.pageIcons.size(); i++) {
            buf.writeItem(this.pageIcons.get(i));
            buf.writeUtf(this.pageTitles.get(i));
            buf.writeBoolean(this.pageSaved.get(i));
        }
        writeLines(buf, this.loadRefusals);
        writeLines(buf, this.loadNotes);
    }

    public static ArmoryViewInfo read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int page = buf.readVarInt();
        ArmoryTab tab = buf.readEnum(ArmoryTab.class);
        boolean creative = buf.readBoolean();
        boolean skills = buf.readBoolean();
        boolean gods = buf.readBoolean();
        int count = buf.readVarInt();
        List<ItemStack> icons = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<Boolean> saved = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            icons.add(buf.readItem());
            titles.add(buf.readUtf());
            saved.add(buf.readBoolean());
        }
        return new ArmoryViewInfo(pos, page, tab, creative, skills, gods, icons, titles, saved, readLines(buf), readLines(buf));
    }

    public ArmoryViewInfo withTab(ArmoryTab tab) {
        return new ArmoryViewInfo(this.pos, this.page, tab, this.creative, this.skillsUpgrade, this.godsUpgrade,
                this.pageIcons, this.pageTitles, this.pageSaved, this.loadRefusals, this.loadNotes);
    }

    private static void writeLines(FriendlyByteBuf buf, List<String> lines) {
        buf.writeVarInt(lines.size());
        lines.forEach(buf::writeUtf);
    }

    private static List<String> readLines(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> out = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            out.add(buf.readUtf());
        }
        return out;
    }
}
