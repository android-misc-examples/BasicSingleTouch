package it.pgp.basicsingletouch.utils;

import java.math.BigInteger;
import java.util.Arrays;

public class Misc {
    public static long castBytesToUnsignedNumber(byte[] b, Integer cut_) {
        long value = 0;
        int cut = b.length;
        if (cut_ != null) cut = cut_;
        for (int i = cut-1; i >= 0; i--)
            value = (value << 8) + (b[i] & 0xFF);
        return value;
    }

    public static long castBytesToUnsignedNumberWithBigInteger(byte[] b, Integer cut_) {
        BigInteger value = BigInteger.ZERO;
        int cut = b.length;
        if (cut_ != null) cut = cut_;
        for (int i = cut-1; i >= 0; i--) { // replace with decrement for for little endianness compliance
            value = value.shiftLeft(8);
            value = value.add(BigInteger.valueOf(b[i] & 0xFF));
        }
        return value.longValue();
    }

    public static byte[] castUnsignedNumberToBytes(long l, Integer cut_) {
        byte[] o = (cut_ == null)?new byte[8]:new byte[cut_];
        long mask = 0xFF;
        for (int i=0;i<o.length;i++) {
            o[i] = (byte) (l & mask);
            l >>= 8; // FIXME maybe better to replace with unsigned shift >>>
        }
        return o;
    }

    @SafeVarargs
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
