package xyz.iwolfking.woldsvaults.events;

public enum WoldActiveFlags {
    IS_REAVING_ATTACKING,
    IS_ECHOING_ATTACKING,
    IS_USING_SAFER_SPACE;

    private final ThreadLocal<Integer> activeReferences = ThreadLocal.withInitial(() -> 0);

    public boolean isSet() {
        return this.activeReferences.get() > 0;
    }

    public synchronized void runIfNotSet(Runnable run) {
        if (!this.isSet()) {
            this.push();

            try {
                run.run();
            } finally {
                this.pop();
            }
        }
    }

    public synchronized void push() {
        this.activeReferences.set(this.activeReferences.get() + 1);
    }
    public synchronized void pop() {
        this.activeReferences.set(this.activeReferences.get() - 1);
    }

    public synchronized void trySet(boolean goalState) {
        if(goalState)
            this.tryPush();
        else
            this.tryPop();
    }

    public synchronized boolean tryPush() {
        if (this.isSet())
            return false;
        this.push();
        return true;
    }

    public synchronized boolean tryPop() {
        if (!this.isSet())
            return false;
        this.pop();
        return true;
    }
}