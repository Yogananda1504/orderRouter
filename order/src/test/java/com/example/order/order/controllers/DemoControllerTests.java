package com.example.order.order.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "demo.simulation.enabled=false")
class DemoControllerTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void exposesMonitorDataAndServesTheAdminPage() throws Exception {
        mvc.perform(get("/api/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users['1'].choice").value("DISTANCE"))
                .andExpect(jsonPath("$.users['2'].choice").value("COST"))
                .andExpect(jsonPath("$.centers['1'].items['101']").value(10))
                .andExpect(jsonPath("$.orders['1004'].items['999']").value(1))
                .andExpect(jsonPath("$.simulatedOrders").value(0));
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin monitor")));
    }

    @Test
    void doesNotExposeAdminMutationEndpoints() throws Exception {
        mvc.perform(post("/api/demo/allocate")).andExpect(status().is4xxClientError());
        mvc.perform(post("/api/demo/reset")).andExpect(status().is4xxClientError());
        mvc.perform(patch("/api/demo/users/1/choice").contentType(MediaType.APPLICATION_JSON)
                .content("{\"choice\":\"COST\"}"))
                .andExpect(status().is4xxClientError());
    }
}
