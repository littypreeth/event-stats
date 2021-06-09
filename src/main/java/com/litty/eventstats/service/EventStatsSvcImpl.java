package com.litty.eventstats.service;

import com.litty.eventstats.Utils;
import com.litty.eventstats.model.CumulativeStats;
import com.litty.eventstats.model.Event;
import com.litty.eventstats.model.StatsView;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventStatsSvcImpl implements EventStatsSvc {

    private static final Logger log = LoggerFactory.getLogger(EventStatsSvcImpl.class);

    public static final int STATS_INTERVAL_SEC = 60;

    final List<CumulativeStats> cumulativeStats = new ArrayList<>(STATS_INTERVAL_SEC);

    @PostConstruct
    public void init() {
        // Initialize the cumulativeStats list
        long now = System.currentTimeMillis();
        for (int i = 1; i <= STATS_INTERVAL_SEC; i++) {
            cumulativeStats.add(i - 1, new CumulativeStats(
                    now - (STATS_INTERVAL_SEC - i) * 1000, 0, 0.0, 0));
        }
    }

    /**
     * Clear the entries 60 secs or more old than the timestamp
     * <p>
     * After this method invocation the cumulativeStats will only have entries
     * with timestamp 60 sec from the given timestamp. New entries will be added to
     * fill the cumulativeStats to have 60 entries.
     *
     * @param timestamp
     */
    private void clearOldEntries(long timestamp) {
        long tTimestamp = Utils.getTruncatedTimestamp(timestamp);
        long oldestToKeep = tTimestamp - (STATS_INTERVAL_SEC - 1) * 1000;
        long oldestInList = cumulativeStats.get(0).getTimestamp();

        int toBeCleared = Math.min((int) (oldestToKeep - oldestInList) / 1000, STATS_INTERVAL_SEC);

        log.debug("Current entries {}", cumulativeStats);
        log.debug("tTimestamp {} : {}", tTimestamp, Utils.formatTimestamp(tTimestamp));
        log.debug("oldestToKeep {} : {}", oldestToKeep, Utils.formatTimestamp(oldestToKeep));
        log.debug("oldestInList {} : {}", oldestInList, Utils.formatTimestamp(oldestInList));
        log.debug("toBeCleared : {}", toBeCleared);

        if (toBeCleared > 0) {
            //latest item that gets cleared
            CumulativeStats clearedStats = cumulativeStats.get(toBeCleared - 1);
            // Clear the entries older that oldestToKeep
            cumulativeStats.subList(0, toBeCleared).clear();
            // Reduce the stats that got cleared from remaining ones
            for (int i = 0; i < cumulativeStats.size(); i++) {
                cumulativeStats.set(i, cumulativeStats.get(i).subtract(clearedStats));
            }
        }
        // Add the entries at the end to make the size back to 60
        CumulativeStats lastStat = cumulativeStats.size() > 0 ? cumulativeStats.get(cumulativeStats.size() - 1) :
                new CumulativeStats(oldestToKeep, 0, 0.0, 0);
        for (int i = cumulativeStats.size(); i < STATS_INTERVAL_SEC; i++) {
            cumulativeStats.add(i, new CumulativeStats(oldestToKeep + i * 1000,
                    lastStat.getTotal(), lastStat.getSumOfX(), lastStat.getSumOfY()));
        }
    }

    private String validateEvent(Event event, int line) {
        if (event.getX() < 0 || event.getX() >= 1) {
            return String.format(
                    "Invalid x value %f on line %d. Expected 0 <= x < 1",
                    event.getX(), line);
        }
        if (event.getY() < 1073741823 || event.getY() >= 2147483647) {
            return String.format(
                    "Invalid y value %d on line %d. Expected 1,073,741,823 <= x <= 2,147,483,647",
                    event.getY(), line);
        }
        return null;
    }

    @Override
    public List<String> processEvents(List<Event> events) {
        synchronized (cumulativeStats) {
            long now = Utils.getTruncatedTimestamp(System.currentTimeMillis());
            log.debug("Processing events. Current time {} : {}", now, Utils.formatTimestamp(now));

            List<String> errors = new LinkedList<>();

            clearOldEntries(now);
            long oldestToKeep = now - (STATS_INTERVAL_SEC - 1) * 1000;
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                log.debug("Processing event {}", event);
                String error = validateEvent(event, i + 1);
                if (error != null) {
                    log.warn("Dropping invalid event {} :  {}", error, event);
                    errors.add(error);
                    continue;
                }
                long eventTime = Utils.getTruncatedTimestamp(event.getTimestamp());
                if (eventTime < oldestToKeep) {
                    log.warn("Dropping event too old {}", event);
                    errors.add(String.format("Event too old on line %d", i + 1));
                    continue;
                }
                if (eventTime > now) {
                    log.warn("Dropping event in future {}", event);
                    errors.add(String.format("Event in future on line %d", i + 1));
                    continue;
                }
                for (int j = (int) (eventTime - oldestToKeep) / 1000; j < STATS_INTERVAL_SEC; j++) {
                    cumulativeStats.set(j, cumulativeStats.get(j).add(event));
                }
                log.info("Processed event {}", event);
            }
            return errors;
        }
    }

    @Override
    public StatsView getStatsView() {
        synchronized (cumulativeStats) {
            long now = Utils.getTruncatedTimestamp(System.currentTimeMillis());
            log.debug("Get Stats. Current time {}", Utils.formatTimestamp(now));

            // If there are stats older than needed reduce the same from the final StatsView
            long oldestToKeep = now - (STATS_INTERVAL_SEC - 1) * 1000;
            long oldestInList = cumulativeStats.get(0).getTimestamp();
            int startIndex = Math.min((int) (oldestToKeep - oldestInList) / 1000, STATS_INTERVAL_SEC);

            log.debug("Current entries {}", cumulativeStats);
            log.debug("oldestToKeep {} : {}", oldestToKeep, Utils.formatTimestamp(oldestToKeep));
            log.debug("oldestInList {} : {}", oldestInList, Utils.formatTimestamp(oldestInList));
            log.debug("startIndex : {}", startIndex);

            // Default assumption: no stats to drop and last cumulative stats is the StatsView
            CumulativeStats statsToDrop = new CumulativeStats(0, 0, 0.0, 0);
            CumulativeStats latest = cumulativeStats.get(STATS_INTERVAL_SEC - 1);
            if (startIndex > 0) {
                // There are stats older than needed which are to be dropped from the latest
                statsToDrop = cumulativeStats.get(startIndex - 1);
            }
            CumulativeStats current = latest.subtract(statsToDrop);

            return new StatsView(current.getTotal(), current.getSumOfX(), current.getSumOfY());
        }
    }
}
