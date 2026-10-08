package com.challenge.api.controller;

import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeImpl;
import com.challenge.api.service.EmployeeService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// passes requests to the service
@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    // spring passes in the service
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{uuid}")
    public Employee getEmployeeByUuid(@PathVariable UUID uuid) {
        return employeeService.getEmployeeByUuid(uuid);
    }

    // the 201 means its created instead of the springs default 200
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee createEmployee(@RequestBody EmployeeImpl requestBody) {
        return employeeService.createEmployee(requestBody);
    }

    // sets the termination date to now
    @PatchMapping("/{uuid}/terminate")
    public Employee terminateEmployee(@PathVariable UUID uuid) {
        return employeeService.terminateEmployee(uuid);
    }
}
