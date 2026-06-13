package xyz.iwolfking.woldsvaults.aaaWIP;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.LinkedList;

public abstract class NeochainParticleData<o_o> {
    public LinkedList<LinkedList<o_o>> stuff = new LinkedList<>();

    @OnlyIn(Dist.CLIENT)
    public abstract void spawnParticles();

    public void add(o_o newThing, o_o offOf) {
        for (LinkedList<o_o> things : stuff)
            if(things.peek() == offOf) {
                things.push(newThing);
                return;
            }
        this.addRoot(newThing, offOf);
    }
    public void addRoot(o_o newLeaf, o_o offOf) {
        LinkedList<o_o> newBranch = new LinkedList<>();
        newBranch.push(offOf);
        newBranch.push(newLeaf);
        stuff.push(newBranch);
    }
    public void addRoot(o_o newLeaf) {
        LinkedList<o_o> newBranch = new LinkedList<>();
        newBranch.push(newLeaf);
        stuff.push(newBranch);
    }
    public boolean growBranch(o_o newLeaf) {
        LinkedList<o_o> branch = stuff.peek();
        if(branch != null) {
            branch.push(newLeaf);
            return true;
        }
        return false;
    }
}
