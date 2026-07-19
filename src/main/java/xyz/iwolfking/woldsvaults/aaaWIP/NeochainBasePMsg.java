package xyz.iwolfking.woldsvaults.aaaWIP;

import com.mojang.math.Vector3f;
import iskallia.vault.init.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;

public class NeochainBasePMsg extends NeochainParticleData<Vector3f> {

    private static final float ADJ_MULT = 20.0F;
    static byte toAdjQuarter(float f) {
        return QuarterF.toQuarter(f / ADJ_MULT);
    }
    static float toAdjFloat(byte q) {
        return QuarterF.toFloat(q) * ADJ_MULT;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void spawnParticles() {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Random random = level.getRandom();
            for (LinkedList<Vector3f> branches : stuff) {
                Iterator<Vector3f> leaves = branches.iterator();
                Vec3 tail = new Vec3(leaves.next());
                while (leaves.hasNext()) {
                    Vec3 head = new Vec3(leaves.next());
                    float diff = (float) (0.15 / head.distanceTo(tail));
                    for (float f = 0; f < 1; f += diff) {
                        Vec3 pos = head.lerp(tail, f);
                        level.addParticle(ModParticles.CHAINING.get(), true, pos.x(), pos.y() + random.nextDouble() * 0.15, pos.z(), 0, random.nextDouble() * 0.1, 0);
                    }
                    tail = head;
                }
            }
        }
    }

    public static NeochainBasePMsg decode(FriendlyByteBuf buf) {
        NeochainBasePMsg msg = new NeochainBasePMsg();
        msg.stuff = new LinkedList<>();
        for(byte i = buf.readByte(); i > 1; i--) {
            float x = buf.readFloat();
            float y = buf.readFloat();
            float z = buf.readFloat();
            msg.addRoot(new Vector3f(x,y,z));
            LinkedList<Vector3f> branch = msg.stuff.getFirst();
            for (byte j = buf.readByte(); j > 1; j--) {
                x += toAdjFloat(buf.readByte());
                y += toAdjFloat(buf.readByte());
                z += toAdjFloat(buf.readByte());
                branch.push(new Vector3f(x,y,z));
            }
        }
        return msg;
    }

    public static void encode(NeochainBasePMsg msg, FriendlyByteBuf buf) {
        LinkedList<LinkedList<Vector3f>> stuff = msg.stuff;
        buf.writeByte(stuff.size());
        for (LinkedList<Vector3f> branches : stuff) {
            Iterator<Vector3f> leaves = branches.iterator();
            Vector3f thing = leaves.next();
            float lastX = thing.x();
            float lastY = thing.y();
            float lastZ = thing.z();
            buf.writeFloat(lastX);
            buf.writeFloat(lastY);
            buf.writeFloat(lastZ);
            buf.writeByte(branches.size());
            while (leaves.hasNext()) {
                thing = leaves.next();
                float dX = QuarterF.crunch(thing.x()-lastX);
                float dY = QuarterF.crunch(thing.y()-lastY);
                float dZ = QuarterF.crunch(thing.z()-lastZ);
                buf.writeByte(toAdjQuarter(dX));
                buf.writeByte(toAdjQuarter(dY));
                buf.writeByte(toAdjQuarter(dZ));
                lastX += dX;
                lastY += dY;
                lastZ += dZ;
            }
        }
    }

    public static void handle(NeochainBasePMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                msg.spawnParticles();
            }
        });
        context.setPacketHandled(true);
    }
}
