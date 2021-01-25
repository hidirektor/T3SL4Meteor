package me.t3sl4.meteor.util;

public final class BitMath {

    public static int mask(int bits) {
        return ~(~0 << bits);
    }

    public static int unpackX(long packed) {
        return extractSigned(packed, 0, 26);
    }

    public static int unpackZ(long packed) {
        return extractSigned(packed, 26, 26);
    }

    public static int unpackY(long packed) {
        return extractSigned(packed, 26 + 26, 12);
    }

    public static int extractSigned(long i, int shift, int bits) {
        return fixSign((int) (i >> shift) & mask(bits), bits);
    }

    public static int fixSign(int i, int bits) {
        // Using https://stackoverflow.com/a/29266331/436524
        return i << (32 - bits) >> (32 - bits);
    }

    private BitMath() {
    }

}

