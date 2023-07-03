package com.takeaway.util;

import com.takeaway.dto.EmployeeDto;
import com.takeaway.entity.Employee;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Convertor {

    private Convertor(){}

    public static EmployeeDto convertToDto(Employee employee) {
        return new EmployeeDto(employee.getUuid(), employee.getEmail(),
                employee.getFullName(), employee.getBirthday(), convertHobbies(employee.getHobbies()));
    }

    public static Employee convertToEntity(EmployeeDto employeeDto) {
        return new Employee(employeeDto.getUuid(), employeeDto.getEmail(),
                employeeDto.getFullName(), employeeDto.getBirthday(), String.valueOf(employeeDto.getHobbies()));
    }

    private static List<String> convertHobbies(String str) {
        if (str == null)
            return Collections.emptyList();
        String cleanStr = str.replaceAll("[\\[\\]\\s]", "");
        if(cleanStr.isEmpty())
            return new ArrayList<>();
        String[] elements = cleanStr.split(",");
        return Stream.of(elements)
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
