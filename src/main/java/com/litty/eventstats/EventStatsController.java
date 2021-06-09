package com.litty.eventstats;

import com.litty.eventstats.model.Event;
import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvException;

import com.litty.eventstats.model.StatsView;
import com.litty.eventstats.service.EventStatsSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

@RestController
public class EventStatsController {

    private static final Logger log = LoggerFactory.getLogger(EventStatsController.class);

    @Autowired
    EventStatsSvc eventStatsSvc;

    @GetMapping(path = "/stats")
    public void getStats(HttpServletResponse response) throws IOException, CsvException {
        StatsView statsView = eventStatsSvc.getStatsView();

        ColumnPositionMappingStrategy<StatsView> mapStrategy
                = new ColumnPositionMappingStrategy<>();
        mapStrategy.setType(StatsView.class);
        String[] columns = new String[]{"total", "sumOfXStr", "avgOfXStr", "sumOfYStr", "avgOfYStr"};
        mapStrategy.setColumnMapping(columns);
        StatefulBeanToCsv<StatsView> toCsv = new StatefulBeanToCsvBuilder<StatsView>(response.getWriter())
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .withMappingStrategy(mapStrategy)
                .withSeparator(',')
                .build();

        toCsv.write(statsView);
    }

    @PostMapping(path = "/event",
            consumes = {"application/csv", "application/text", "text/csv"},
            produces = {"application/json"})
    public ProcessStatus postEvents(HttpServletResponse response,
                                    @RequestBody String body) throws CsvException {
        log.debug("Received body \n{}", body);
        ColumnPositionMappingStrategy<Event> mapStrategy
                = new ColumnPositionMappingStrategy<>();
        mapStrategy.setType(Event.class);
        String[] columns = new String[]{"timestamp", "x", "y"};
        mapStrategy.setColumnMapping(columns);
        List<Event> events;
        try {
            events = new CsvToBeanBuilder<Event>(new StringReader(body))
                    .withType(Event.class)
                    .withMappingStrategy(mapStrategy)
                    .withSeparator(',')
                    .build().parse();
        } catch (Exception e) {
            log.error("CSV parsing error", e);
            throw new CsvException(e.getLocalizedMessage());
        }
        log.info("Received {} events", events.size());
        List<String> errors = eventStatsSvc.processEvents(events);
        response.setStatus(202);
        return new ProcessStatus(System.currentTimeMillis(), events.size(), errors.size(), errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CsvException.class)
    @ResponseBody
    ErrorInfo
    handleBadCsv(HttpServletRequest req, Exception ex) {
        return new ErrorInfo(System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                req.getPathInfo(),
                "INVALID_CSV",
                ex);
    }

    static class ProcessStatus {
        public final String timestamp;
        public final int inputCount;
        public final int failureCount;
        public final List<String> errors;

        ProcessStatus(long timestamp, int inputCount, int failureCount, List<String> errors) {
            this.timestamp = Utils.formatTimestamp(timestamp);
            this.inputCount = inputCount;
            this.failureCount = failureCount;
            this.errors = errors;
        }
    }

    static class ErrorInfo {
        public final String timestamp;
        public int status;
        public final String path;
        public final String errorCode;
        public final String message;

        public ErrorInfo(long timestamp, int status, String path, String errorCode, Exception ex) {
            this.timestamp = Utils.formatTimestamp(timestamp);
            this.status = status;
            this.path = path;
            this.errorCode = errorCode;
            this.message = ex.getLocalizedMessage();
        }
    }
}
