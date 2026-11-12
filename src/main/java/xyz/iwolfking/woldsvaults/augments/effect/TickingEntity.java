package xyz.iwolfking.woldsvaults.augments.effect;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The entity whose tick is running right now, pushed and popped around {@code Level.guardEntityTick}. VH projectiles
 * resolve their hits, explosions and split children inside their own tick, so this attributes those effects to the
 * projectile exactly, without a time window that also catches the owner's other damage. Server thread only.
 */
public final class TickingEntity {
    private static final Deque<Entity> STACK = new ArrayDeque<>();

    private TickingEntity() {
    }

    public static void push(Entity entity) {
        STACK.push(entity);
    }

    public static void pop() {
        STACK.poll();
    }

    @Nullable
    public static Entity current() {
        return STACK.peek();
    }
}
