package com.litty.eventstats.model;

import com.litty.eventstats.Utils;

import java.math.BigDecimal;

/**
 * Stats accumulated till a timestamp.
 */
public class CumulativeStats {
    private final long timestamp;
    private final long total;
    private final BigDecimal sumOfX;
    private final BigDecimal sumOfY;

    public CumulativeStats(long timestamp, long total, double sumOfX, long sumOfY) {
        this(timestamp, total, Utils.toBigDecimal(sumOfX), Utils.toBigDecimal(sumOfY));
    }

    public CumulativeStats(long timestamp, long total, BigDecimal sumOfX, BigDecimal sumOfY) {
        this.timestamp = Utils.getTruncatedTimestamp(timestamp);
        this.total = total;
        this.sumOfX = sumOfX;
        this.sumOfY = sumOfY;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTotal() {
        return total;
    }

    public BigDecimal getSumOfX() {
        return sumOfX;
    }

    public BigDecimal getSumOfY() {
        return sumOfY;
    }

    private CumulativeStats add(long total, BigDecimal x, BigDecimal y) {
        return new CumulativeStats(timestamp, this.total + total,
                Utils.add(sumOfX, x), Utils.add(this.sumOfY, y));
    }

    private CumulativeStats subtract(long total, BigDecimal x, BigDecimal y) {
        return new CumulativeStats(timestamp, this.total - total,
                Utils.subtract(sumOfX, x), Utils.subtract(sumOfY, y));
    }

    public CumulativeStats add(CumulativeStats o) {
        return add(o.getTotal(), o.getSumOfX(), o.getSumOfY());
    }

    public CumulativeStats add(Event o) {
        return add(1, Utils.toBigDecimal(o.getX()), Utils.toBigDecimal(o.getY()));
    }

    public CumulativeStats subtract(CumulativeStats o) {
        return subtract(o.getTotal(), o.getSumOfX(), o.getSumOfY());
    }

    public CumulativeStats subtract(Event o) {
        return subtract(1, Utils.toBigDecimal(o.getX()), Utils.toBigDecimal(o.getY()));
    }

    @Override
    public String toString() {
        return "CumulativeStats{" +
                "timestamp=" + timestamp +
                ", total=" + total +
                ", sumOfX=" + Utils.formatDouble(sumOfX.doubleValue()) +
                ", sumOfY=" + String.format("%d", sumOfY.longValue()) +
                '}';
    }
}
