package xyz.iwolfking.woldsvaults.gods.trees.velara;

/**
 * Re-entrancy guards for the two Velara nodes that push damage back into the hit pipeline,
 * matching the shape of the addon's own {@code WoldActiveFlags}. Every new damage producer needs
 * one, or Sacrifice users protecting each other and Counterstrike procs off their own retaliation
 * recurse until the stack runs out.
 */
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
