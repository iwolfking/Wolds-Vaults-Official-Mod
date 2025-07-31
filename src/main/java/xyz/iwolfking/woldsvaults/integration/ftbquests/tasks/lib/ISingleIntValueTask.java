package xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.lib;

public interface ISingleIntValueTask {
    default int getDefaultConfigValue() {
        return 0;
    }

    default int getMinConfigValue() {
        return 0;
    }

    default int getMaxConfigValue() {
        return Integer.MAX_VALUE;
    }

    void setValue(int value);
}
