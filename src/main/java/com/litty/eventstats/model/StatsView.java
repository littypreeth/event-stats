package com.litty.eventstats.model;

import com.litty.eventstats.Utils;

import java.math.BigDecimal;

/**
 * The Stats view that is to be returned
 */
public class StatsView {

    /**
     * Number of events that happened
     */
    private final long total;
    /**
     * Sum of X fields among the events within the allowed interval
     */
    private final BigDecimal sumOfX;
    /**
     * Avg of X fields among the events within the allowed interval
     */
    private final BigDecimal avgOfX;
    /**
     * Sum of Y fields among events within the allowed interval
     */
    private final BigDecimal sumOfY;
    /**
     * Avg of Y fields among events within the allowed interval
     */
    private final BigDecimal avgOfY;

    public StatsView(long total, BigDecimal sumOfX, BigDecimal sumOfY) {
        this.total = total;
        this.sumOfX = sumOfX;
        this.avgOfX = total > 0L ?
                sumOfX.divide(Utils.toBigDecimal(total), Utils.MATH_CONTEXT) : Utils.toBigDecimal(0.0);
        this.sumOfY = sumOfY;
        this.avgOfY = total > 0L ?
                sumOfY.divide(Utils.toBigDecimal(total), Utils.MATH_CONTEXT) : Utils.toBigDecimal(0.0);
    }

    public long getTotal() {
        return total;
    }

    public BigDecimal getSumOfX() {
        return sumOfX;
    }

    public String getSumOfXStr() {
        return Utils.formatDouble(sumOfX.doubleValue());
    }

    public BigDecimal getAvgOfX() {
        return avgOfX;
    }

    public String getAvgOfXStr() {
        return Utils.formatDouble(avgOfX.doubleValue());
    }

    public BigDecimal getSumOfY() {
        return sumOfY;
    }

    public String getSumOfYStr() {
        return String.format("%d", sumOfY.longValue());
    }

    public BigDecimal getAvgOfY() {
        return avgOfY;
    }

    public String getAvgOfYStr() {
        return String.format("%.3f", avgOfY.doubleValue());
    }
}
