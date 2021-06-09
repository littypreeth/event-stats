package com.litty.eventstats.service;

import com.litty.eventstats.model.Event;
import com.litty.eventstats.model.StatsView;

import java.util.List;

public interface EventStatsSvc {
    /**
     * Process the events one by one and create accumulated statistics of the events.
     *
     * The events older than the allowed interval and events with future timestamps are dropped.
     * The accumulated value of the event is stored in an array at the index of
     * event_timestamp - oldest_time
     * @param events
     * @return List of error string for each event that is invalid
     */
    public List<String> processEvents(List<Event> events);

    /**
     * Get the stats view.
     *
     * Stats view is the sum and avg of values of events that happened from T - allowed_interval
     * @return
     */
    public StatsView getStatsView();
}
