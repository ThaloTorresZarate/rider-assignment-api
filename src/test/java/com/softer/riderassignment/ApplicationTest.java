package com.softer.riderassignment;

import com.softer.riderassignment.model.AssignmentStatus;
import com.softer.riderassignment.model.RiderAssignment;
import com.softer.riderassignment.repository.TestH2Repository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ApplicationTest {

    @LocalServerPort
    private  int port;

    private static RestTemplate restTemplate;
    private String baseUrl;

    @Autowired
    private TestH2Repository testH2Repository;

    @BeforeAll
    static void init(){
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/assignments";
    }

    @Test
    @DisplayName("should get all assignments when table is not empty")
    @Sql(statements = "INSERT INTO rider_assignments(id, delivery_id, rider_id, status, assigned_at) "
            + "VALUES (1, 7001, 301, 'ASSIGNED', CURRENT_TIMESTAMP)",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetAssignmentsWhenTableIsNotEmpty() {
        List<RiderAssignment> response = restTemplate.getForObject(baseUrl, List.class);
        assertEquals(1, response.size());
        assertEquals(1, testH2Repository.findAll().size());
    }

    @Test
    @DisplayName("should get an assignment by id")
    @Sql(statements = "INSERT INTO rider_assignments(id, delivery_id, rider_id, status, assigned_at) "
            + "VALUES (1, 7001, 301, 'ASSIGNED', CURRENT_TIMESTAMP)",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetAssignmentById() {
        RiderAssignment response = restTemplate.getForObject(baseUrl + "/1", RiderAssignment.class);
        assertEquals(7001L, response.getDeliveryId());
        assertEquals(301L, response.getRiderId());
    }

    @Test
    @DisplayName("should add a new assignment")
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testAddAssignment() {
        RiderAssignment request =
                new RiderAssignment(null, 7001L, 301L, AssignmentStatus.ASSIGNED, null);
        ResponseEntity<RiderAssignment> response =
                restTemplate.postForEntity(baseUrl, request, RiderAssignment.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
        assertEquals(1, testH2Repository.findAll().size());

        }

    @Test
    @DisplayName("should update an existing assignment")
    @Sql(statements = "INSERT INTO rider_assignments(id, delivery_id, rider_id, status, assigned_at) "
            + "VALUES (1, 7001, 301, 'ASSIGNED', CURRENT_TIMESTAMP)",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testUpdateAssignment() {
        RiderAssignment request =
                new RiderAssignment(null, 7001L, 302L, AssignmentStatus.ACCEPTED, null);
        restTemplate.put(baseUrl + "/1", request);
        RiderAssignment updated = testH2Repository.findById(1L).get();
        assertEquals(302L, updated.getRiderId());
        assertEquals(AssignmentStatus.ACCEPTED, updated.getStatus());
    }

    @Test
    @DisplayName("should delete an existing assignment")
    @Sql(statements = "INSERT INTO rider_assignments(id, delivery_id, rider_id, status, assigned_at) "
            + "VALUES (1, 7001, 301, 'ASSIGNED', CURRENT_TIMESTAMP)",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDeleteAssignment() {
        restTemplate.delete(baseUrl + "/1");
        assertEquals(0, testH2Repository.findAll().size());
    }
    @Test
    @DisplayName("should not add an assignment for a delivery that is already assigned")
    @Sql(statements = "INSERT INTO rider_assignments(id, delivery_id, rider_id, status, assigned_at) "
            + "VALUES (1, 7001, 301, 'ASSIGNED', CURRENT_TIMESTAMP)",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testAddDuplicateDelivery() {
        RiderAssignment duplicate =
                new RiderAssignment(null, 7001L, 302L, AssignmentStatus.ASSIGNED, null);
        assertThrows(HttpClientErrorException.Conflict.class,
                () -> restTemplate.postForEntity(baseUrl, duplicate, RiderAssignment.class));
        assertEquals(1, testH2Repository.findAll().size());
    }


}
