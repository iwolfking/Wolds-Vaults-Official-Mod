package xyz.iwolfking.woldsvaults.objectives.survival.lib;

public class TickTimer {
    private final int ticksPerAction;
    private int timer = 0;
    private boolean disabled = false;

    public TickTimer(int ticksPerAction) {
        this.ticksPerAction = ticksPerAction;
    }

    private void increment() {
        timer++;
    }

    public int time() {
        return timer;
    }

    public int completionTime() {
        return ticksPerAction;
    }

    public void disable() {
        disabled = true;
    }

    public boolean ready() {
        if(disabled) {
            return false;
        }

        if(timer != 0 && timer % ticksPerAction == 0) {
            timer = 0;
            return true;
        }

        increment();
        return false;
    }
}
