package com.example.order.order.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.order.order.services.DemoDataStore;
import com.example.order.order.services.DemoDataStore.DemoData;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoDataStore dataStore;

    public DemoController(DemoDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @GetMapping
    public DemoData getData() {
        return dataStore.getData();
    }

}
