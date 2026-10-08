package com.challenge.api.service;

import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeImpl;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public EmployeeService() {
        createEmployee(mockEmployee("John", "Stewart", 85000, 30, "Software Engineer"));
        createEmployee(mockEmployee("Hal", "Jordan", 95000, 42, "Engineering Manager"));
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = employees.get(uuid);

        if (employee == null) {
            log.warn("Employee not found: {}", uuid);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        return employee;
    }

    public Employee createEmployee(EmployeeImpl employee) {
        // rejects the command if anything required is missing
        if (isBlank(employee.getFirstName())
                || isBlank(employee.getLastName())
                || isBlank(employee.getJobTitle())
                || isBlank(employee.getEmail())
                || employee.getSalary() == null
                || employee.getAge() == null
                || employee.getContractHireDate() == null) {
            log.warn("Rejected employee with missing required fields");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "firstName, lastName, salary, age, jobTitle, email and contractHireDate are required");
        }

        if (employee.getSalary() < 0) {
            log.warn("Rejected employee with negative salary");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salary must not be negative");
        }

        employee.setUuid(UUID.randomUUID());
        employee.setFullName(employee.getFirstName() + " " + employee.getLastName());
        employees.put(employee.getUuid(), employee);
        log.info("Created employee {}", employee.getUuid());
        return employee;
    }

    public Employee terminateEmployee(UUID uuid) {

        Employee employee = getEmployeeByUuid(uuid);
        // make sure original termination date doesnt get overwritten
        if (employee.getContractTerminationDate() != null) {
            log.warn("Employee {} is already terminated", uuid);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee is already terminated");
        }
        employee.setContractTerminationDate(Instant.now());
        log.info("Terminated employee {}", uuid);
        return employee;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // helper to make fake employees for the mock
    private EmployeeImpl mockEmployee(String firstName, String lastName, int salary, int age, String jobTitle) {
        EmployeeImpl employee = new EmployeeImpl();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setSalary(salary);
        employee.setAge(age);
        employee.setJobTitle(jobTitle);
        employee.setEmail(firstName.toLowerCase() + "@company.com");
        employee.setContractHireDate(Instant.parse("2022-01-01T00:00:00Z"));
        return employee;
    }
}
