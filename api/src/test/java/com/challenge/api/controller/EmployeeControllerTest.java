package com.challenge.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// sends fake http requests to app with mockmvc
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    private static final String BASE_URL = "/api/v1/employee";
    // a valid request body, the bad request tests change one thing in it
    private static final String VALID_EMPLOYEE =
            """
            {"firstName": "Ada", "lastName": "Wong", "salary": 100000, "age": 36,
             "jobTitle": "Engineer", "email": "ada@company.com", "contractHireDate": "2024-01-15T00:00:00Z"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllEmployeesReturnsList() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void createEmployeeReturnsCreatedEmployee() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_EMPLOYEE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value("Ada Wong"));
    }

    @Test
    void createEmployeeMissingFieldReturnsBadRequest() throws Exception {
        String body = VALID_EMPLOYEE.replace("\"firstName\": \"Ada\",", "");
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployeeWithNegativeSalaryReturnsBadRequest() throws Exception {
        String body = VALID_EMPLOYEE.replace("100000", "-1");
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployeeWithMalformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmployeeByUuidReturnsEmployee() throws Exception {
        String uuid = createEmployee();
        mockMvc.perform(get(BASE_URL + "/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid));
    }

    @Test
    void getUnknownEmployeeReturnsNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeWithInvalidUuidReturnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void terminateEmployeeSetsTerminationDate() throws Exception {
        String uuid = createEmployee();
        mockMvc.perform(patch(BASE_URL + "/" + uuid + "/terminate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractTerminationDate").isNotEmpty());
    }

    @Test
    void terminateEmployeeTwiceReturnsConflict() throws Exception {
        String uuid = createEmployee();
        mockMvc.perform(patch(BASE_URL + "/" + uuid + "/terminate")).andExpect(status().isOk());
        mockMvc.perform(patch(BASE_URL + "/" + uuid + "/terminate")).andExpect(status().isConflict());
    }

    // creates employee and returns its uuid so each test has its own employee
    private String createEmployee() throws Exception {
        String response = mockMvc.perform(
                        post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_EMPLOYEE))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }
}
