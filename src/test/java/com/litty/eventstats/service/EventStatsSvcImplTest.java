package com.litty.eventstats.service;

import com.litty.eventstats.Utils;
import com.litty.eventstats.model.CumulativeStats;
import com.litty.eventstats.model.Event;
import com.litty.eventstats.model.StatsView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventStatsSvcImplTest {

    private static final double X = 0.1;

    private static final int Y = 2098977008; //  In range 1,073,741,823..2,147,483,647.

    @Autowired
    private EventStatsSvcImpl obj;

    @BeforeEach
    void setUp() {
        obj = new EventStatsSvcImpl();
        obj.init();
    }

    @Test
    void init() {
        StatsView statsView = obj.getStatsView();
        // Zero stats on init
        assertEquals(0, statsView.getTotal());
        assertEquals(Utils.toBigDecimal(0.0D).compareTo(statsView.getSumOfX()), 0);
        assertEquals(Utils.toBigDecimal(0.0D).compareTo(statsView.getAvgOfX()), 0);
        assertEquals(Utils.toBigDecimal(0).compareTo(statsView.getSumOfY()), 0);
        assertEquals(Utils.toBigDecimal(0.0D).compareTo(statsView.getAvgOfY()), 0);
    }

    @Test
    void processEvents_AllExistingEventsOlder() {
        setUpAllEventsOlder();

        // Post a unitary event
        Event ein = new Event(System.currentTimeMillis(), X, Y);
        obj.processEvents(Arrays.asList(ein));

        //Only the current event should have been contributed to the cumulativeStats
        assertCumulativeStats(1);
    }

    @Test
    void processEvents_FewExistingEventsOlder() {
        setUpThreeEventsOlder();

        // Post an event
        Event ein = new Event(System.currentTimeMillis(), 0.1D, Y);
        obj.processEvents(Arrays.asList(ein));

        // The first three events should have been dropped and last one added.
        // So the total should amount to STATS_INTERVAL - 3 + 1
        assertCumulativeStats(EventStatsSvcImpl.STATS_INTERVAL_SEC - 3 + 1);
    }

    @Test
    void processEvents_AllExistingEventsValid() {
        setUpAllEventsValid();

        // Post an event
        Event ein = new Event(System.currentTimeMillis(), 0.1D, Y);
        obj.processEvents(Arrays.asList(ein));

        // So the total should amount to STATS_INTERVAL + 1
        assertCumulativeStats(EventStatsSvcImpl.STATS_INTERVAL_SEC + 1);
    }

    @Test
    void getStatsView_AllEventsValid() {
        setUpAllEventsValid();
        StatsView statsView = obj.getStatsView();

        // So the total should amount to STATS_INTERVAL
        assertStatsView(statsView, EventStatsSvcImpl.STATS_INTERVAL_SEC);
    }

    @Test
    void getStatsView_AllEventsOlder() {
        setUpAllEventsOlder();
        StatsView statsView = obj.getStatsView();

        // So the total should amount to 0
        assertEquals(0, statsView.getTotal());
        assertEquals(Utils.toBigDecimal(0.0D).compareTo(statsView.getSumOfX()), 0);
        assertEquals(Utils.toBigDecimal(0.0D).compareTo(statsView.getAvgOfX()), 0);
        assertEquals(Utils.toBigDecimal(0).compareTo(statsView.getSumOfY()), 0);
        assertEquals(Utils.toBigDecimal(0.0D).compareTo(statsView.getAvgOfY()), 0);
    }

    @Test
    void getStatsView_FewExistingEventsOlder() {
        setUpThreeEventsOlder();
        StatsView statsView = obj.getStatsView();

        // The first three events should have been dropped and last one added.
        // So the total should amount to STATS_INTERVAL - 3
        assertStatsView(statsView, EventStatsSvcImpl.STATS_INTERVAL_SEC - 3);
    }

    private void assertCumulativeStats(int expectedTotal) {
        CumulativeStats end = obj.cumulativeStats.get(EventStatsSvcImpl.STATS_INTERVAL_SEC - 1);
        assertEquals(expectedTotal, end.getTotal());
        assertEquals(Utils.multiply(expectedTotal, X).compareTo(end.getSumOfX()), 0);
        assertEquals(Utils.multiply(expectedTotal, Y), end.getSumOfY());
    }

    private void assertStatsView(StatsView statsView, int expectedTotal) {
        assertEquals(expectedTotal, statsView.getTotal());
        assertEquals(Utils.multiply(expectedTotal, X).compareTo(statsView.getSumOfX()),
                0);
        assertEquals(Utils.toBigDecimal(X).compareTo(statsView.getAvgOfX()), 0);
        assertEquals(Utils.multiply(expectedTotal, Y), statsView.getSumOfY());
        assertEquals(Utils.toBigDecimal(Y).compareTo(statsView.getAvgOfY()), 0);
    }

    private void setUpAllEventsValid() {
        // Make sure all events are with same values
        for (int i = 0; i < EventStatsSvcImpl.STATS_INTERVAL_SEC; i++) {
            CumulativeStats e = obj.cumulativeStats.get(i);
            CumulativeStats s = i == 0 ? e : obj.cumulativeStats.get(i - 1);
            obj.cumulativeStats.set(i, s.add(new Event(1, X, Y)));
        }
    }

    private void setUpAllEventsOlder() {
        // Make sure all events are with same values
        for (int i = 0; i < EventStatsSvcImpl.STATS_INTERVAL_SEC; i++) {
            CumulativeStats e = obj.cumulativeStats.get(i);
            CumulativeStats s = i == 0 ? e : obj.cumulativeStats.get(i - 1);
            obj.cumulativeStats.set(i, s.add(new Event(1, X, Y)));
        }
        // Pull the time of all events to 2 sec older than allowed
        for (int i = 0; i < EventStatsSvcImpl.STATS_INTERVAL_SEC; i++) {
            CumulativeStats e = obj.cumulativeStats.get(i);
            long older = e.getTimestamp() - ((EventStatsSvcImpl.STATS_INTERVAL_SEC + 2) * 1000);
            CumulativeStats s = new CumulativeStats(older, e.getTotal(), e.getSumOfX(), e.getSumOfY());
            obj.cumulativeStats.set(i, s);
        }
    }

    private void setUpThreeEventsOlder() {
        // Make sure all events are with same values
        for (int i = 0; i < EventStatsSvcImpl.STATS_INTERVAL_SEC; i++) {
            CumulativeStats e = obj.cumulativeStats.get(i);
            CumulativeStats s = i == 0 ? e : obj.cumulativeStats.get(i - 1);
            obj.cumulativeStats.set(i, s.add(new Event(1, X, Y)));
        }
        // Pull the time of all events by 3 sec so that 3 events will move out of allowed interval
        for (int i = 0; i < EventStatsSvcImpl.STATS_INTERVAL_SEC; i++) {
            CumulativeStats e = obj.cumulativeStats.get(i);
            long older = e.getTimestamp() - (3 * 1000);
            CumulativeStats s = new CumulativeStats(older, e.getTotal(), e.getSumOfX(), e.getSumOfY());
            obj.cumulativeStats.set(i, s);
        }
    }
}