package xyz.iwolfking.woldsvaults.gods.trees.velara;

/** Re-entrancy guards for the Velara nodes that push damage back into the hit pipeline. */
public enum VelaraActiveFlags {
    IS_SACRIFICE_SYPHONING,
    IS_COUNTERSTRIKING;

    private final ThreadLocal<Integer> activeReferences = ThreadLocal.withInitial(() -> 0);

    public boolean isSet() {
        return this.activeReferences.get() > 0;
    }

    public void runWithFlag(Runnable run) {
        this.push();
        try {
            run.run();
        } finally {
            this.pop();
        }
    }

    public void push() {
        this.activeReferences.set(this.activeReferences.get() + 1);
    }

    public void pop() {
        this.activeReferences.set(this.activeReferences.get() - 1);
    }
}
