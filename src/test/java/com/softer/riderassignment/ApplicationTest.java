package com.softer.riderassignment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ApplicationTest {

    @LocalServerPort
    private  int port;

    private static RestTemplate restTemplate;
    private String baseUrl;

    @BeforeAll
    static void init(){
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/assignments";
    }
}
