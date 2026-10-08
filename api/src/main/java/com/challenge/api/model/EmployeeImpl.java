package com.challenge.api.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

// our employee class that follows the employee interface
// @Data (lombok) makes all the getters and setters
@Data
public class EmployeeImpl implements Employee {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private String fullName;
    // using Integer instead of int so a missing value is a null instead of a 0
    private Integer salary;
    private Integer age;
    private String jobTitle;
    private String email;
    private Instant contractHireDate;
    // the null means the employee has not been terminated
    private Instant contractTerminationDate;
}
