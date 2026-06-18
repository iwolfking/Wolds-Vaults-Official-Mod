package xyz.iwolfking.woldsvaults.aaaWIP;

/**
 * ** CURRENTLY NOT NaN/INF SAFE **
 */
public abstract class QuarterF {
    private static final float MAGIC = Float.intBitsToFloat(0x7d800000);
    private static final double MAGIC_D = Double.longBitsToDouble(0);

    public static float toFloat(byte q) {
        int sign = ((int) q >> 7) << 31;
        int signless = ((int) q << 25) >> 6;
        return MAGIC * Float.intBitsToFloat(sign | signless);
    }
    public static byte toQuarter(float f) {
        float inRange = f / MAGIC;
        int qSign = (Float.floatToIntBits(f) >> 31) << 7;
        int q = (Float.floatToIntBits(inRange) >> 19);
        return (byte) (q ^ qSign);
    }
    public static byte toQuarter(double d) {
        double inRange = d / MAGIC_D;
        long qSign = (Double.doubleToLongBits(d) >> 63) << 7;
        long q = (Double.doubleToLongBits(inRange) >> 46);
        return (byte) (q ^ qSign);
    }
    public static float crunch(float f) {
        return Float.intBitsToFloat(Float.floatToIntBits(f / MAGIC) & 0x83f80000) * MAGIC;
        // 0b1000_0011_1111_1[000] 0x83f80000
        // 0b1_0000_0111_1111_[0000]
    }
    public static double crunch(double d) {
        return Double.longBitsToDouble(Double.doubleToLongBits(d / MAGIC_D) & 0x807f0000_00000000L) * MAGIC;
        // 0b1000_0000_0111_1111_[0000] 0x807f0000_00000000L
        // 0b1_0000_0000_111_1111_[0000]
    }
}
