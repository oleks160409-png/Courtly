package com.courtly;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private BookingService service;

    @BeforeEach
    void clearBookings() {
        for (Booking booking : service.getBookings()) {
            service.cancelBooking(booking.getId());
        }
    }

    private String request(int court, String start, String end) throws Exception {
        return mapper.writeValueAsString(new CreateBookingRequest(court, LocalDate.of(2026, 11, 12),
                LocalTime.parse(start), LocalTime.parse(end)));
    }

    @Test
    void createsListsAndCancelsBooking() throws Exception {
        String body = mvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(request(1, "10:00", "11:00")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id = mapper.readTree(body).get("id").asInt();
        mvc.perform(get("/bookings/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.court.id").value(1));
        mvc.perform(get("/bookings")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/courts/1/availability").param("date", "2026-11-12"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(28))
                .andExpect(jsonPath("$[4].available").value(false));
        mvc.perform(delete("/bookings/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/bookings/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/bookings")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsConflictButAllowsOtherCourt() throws Exception {
        mvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(request(1, "10:00", "11:00"))).andExpect(status().isCreated());
        mvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(request(1, "10:30", "11:30"))).andExpect(status().isConflict());
        mvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(request(2, "10:30", "11:30"))).andExpect(status().isCreated());
    }

    @Test
    void returnsBadRequestForInvalidInput() throws Exception {
        mvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(request(1, "10:15", "11:00"))).andExpect(status().isBadRequest());
        mvc.perform(get("/courts/1/availability").param("date", "2026-02-30"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/courts/1/availability")).andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownResources() throws Exception {
        mvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(request(999, "10:00", "11:00"))).andExpect(status().isNotFound());
        mvc.perform(delete("/bookings/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/courts/999/availability").param("date", "2026-11-12"))
                .andExpect(status().isNotFound());
    }
}
