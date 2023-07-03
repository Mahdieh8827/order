package com.takeaway.controller;

import com.takeaway.dto.EmployeeDto;
import com.takeaway.service.EmployeeService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/employees")
@AllArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @ApiOperation(value = "rest api for create employee. it needs employee.",
            produces = "Application/JSON", httpMethod = "POST")
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeDto employee) {
        var createdEmployee = employeeService.createEmployee(employee);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @GetMapping
    @ApiOperation(value = "rest api for get all employee.",
            produces = "Application/JSON", httpMethod = "GET")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "1") int size) {
        return new ResponseEntity<>(employeeService.getAllEmployees(page, size), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    @ApiOperation(value = "rest api for get employee. it needs uuid.",
            produces = "Application/JSON", httpMethod = "GET")
    public ResponseEntity<EmployeeDto> getEmployeeByUuid(@PathVariable UUID uuid) {
        var employee = employeeService.getEmployeeByUuid(uuid);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    @ApiOperation(value = "rest api for update employee. it needs uuid and employeeDto.",
            produces = "Application/JSON", httpMethod = "PUT")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable UUID uuid, @RequestBody EmployeeDto employee) {
        var existingEmployee = employeeService.getEmployeeByUuid(uuid);
        employee.setUuid(existingEmployee.getUuid());
        return new ResponseEntity<>(employeeService.updateEmployee(employee), HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    @ApiOperation(value = "rest api for delete employee. it needs uuid.",
            produces = "Application/JSON", httpMethod = "DELETE")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID uuid) {
        employeeService.deleteEmployee(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
