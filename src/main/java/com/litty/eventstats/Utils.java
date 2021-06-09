package com.litty.eventstats;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {

    public static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
    /**
     * Remove milliseconds from timestamp as we need seconds level accuracy only
     *
     * @param timestamp
     * @return
     */
    public static long getTruncatedTimestamp(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static String formatTimestamp(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return new SimpleDateFormat("yyyy-MM-dd-HH:mm.ss.SSS").format(c.getTime());
    }

    public static String formatDouble(double d) {
        return String.format("%.10f", d);
    }

    public static BigDecimal toBigDecimal(double d) {
        return new BigDecimal( d, MATH_CONTEXT);
    }

    public static BigDecimal toBigDecimal(long d) {
        return new BigDecimal( d, MATH_CONTEXT);
    }

    public static BigDecimal add(BigDecimal b, BigDecimal d) {
        return b.add(d, MATH_CONTEXT);
    }

    public static BigDecimal subtract(BigDecimal b, BigDecimal d) {
        return b.subtract(d, MATH_CONTEXT);
    }

    public static BigDecimal multiply(long x, long y) {
        return toBigDecimal(x).multiply(toBigDecimal(y), MATH_CONTEXT);
    }

    public static BigDecimal multiply(long x, double y) {
        return toBigDecimal(x).multiply(toBigDecimal(y), MATH_CONTEXT);
    }
}
