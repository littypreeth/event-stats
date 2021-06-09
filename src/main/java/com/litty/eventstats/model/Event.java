package com.litty.eventstats.model;

public class Event {
    private long timestamp;
    private double x;
    private int y;

    public Event() { }

    public Event(long timestamp, double x, int y) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Event{" +
                "timestamp=" + timestamp +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
