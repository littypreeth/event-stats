package com.litty.eventstats;

import com.litty.eventstats.model.StatsView;
import com.litty.eventstats.service.EventStatsSvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;

import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventStatsSvc mockEventStatsSvc;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getStats() throws Exception {
        StatsView s = new StatsView(10,
                new BigDecimal(1.1, MathContext.DECIMAL64),
                new BigDecimal(11647743060L, MathContext.DECIMAL64));
        when(mockEventStatsSvc.getStatsView()).thenReturn(s);
        // Execute the GET request
        mockMvc.perform(get("/stats"))
                // Validate the response code and content type
                .andExpect(status().isOk())
                // Validate the returned fields
                .andExpect(content().string(
                        String.format("%s,%s,%s,%s,%s\n",
                                String.valueOf(s.getTotal()),
                                s.getSumOfXStr(),
                                s.getAvgOfXStr(),
                                s.getSumOfYStr(),
                                s.getAvgOfYStr())));
    }

    @Test
    void postEvents() throws Exception {
        when(mockEventStatsSvc.processEvents(anyList())).thenReturn(new LinkedList<>());
        mockMvc.perform(post("/event")
                .content("1622896362103,0.0876221433,1194727708")
                .contentType("application/csv"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.inputCount", is(1)))
                .andExpect(jsonPath("$.failureCount", is(0)));
    }
}