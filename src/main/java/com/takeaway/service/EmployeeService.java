package com.takeaway.service;

import com.takeaway.dto.EmployeeDto;
import com.takeaway.event.EmployeeEvent;
import com.takeaway.event.Event;
import com.takeaway.exception.DuplicateEmailException;
import com.takeaway.exception.NotFoundException;
import com.takeaway.entity.Employee;
import com.takeaway.producer.KafkaProducer;
import com.takeaway.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.takeaway.util.Convertor.convertToDto;
import static com.takeaway.util.Convertor.convertToEntity;

@Service
@AllArgsConstructor
public class EmployeeService {
    EmployeeRepository employeeRepository;
    KafkaProducer<EmployeeEvent> kafkaProducer;

    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        if (employeeRepository.findByEmail(employeeDto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Employee with the same email already exists.");
        }
        var createdEmployee = employeeRepository.save(convertToEntity(employeeDto));
        employeeDto.setUuid(createdEmployee.getUuid());
        publishEvent(Event.CREATE, employeeDto);
        return convertToDto(employeeRepository.save(createdEmployee));
    }

    public Page<EmployeeDto> getAllEmployees(int page, int size) {
        return employeeRepository.findAll(PageRequest.of(page, size)).map( item -> convertToDto(item));
    }

    public EmployeeDto getEmployeeByUuid(UUID uuid) {
        return convertToDto(employeeRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(String.format("There is no employee with uuid =%s",uuid))));
    }

    public EmployeeDto updateEmployee(EmployeeDto employeeDto) {
        Optional<Employee> employee = employeeRepository.findByEmail(employeeDto.getEmail());
        if (employee.isPresent() && employee.get().getUuid() != employeeDto.getUuid()) {
            throw new DuplicateEmailException("Employee with the same email already exists.");
        }
        var updatedEmployee = employeeRepository.save(convertToEntity(employeeDto));
        publishEvent(Event.UPDATE,employeeDto);

        return convertToDto(updatedEmployee);
    }

    @Transactional
    public void deleteEmployee(UUID uuid) {
            var employee = employeeRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(String.format("There is no employee with uuid =%s", uuid)));
            if (employee != null) {
                employeeRepository.deleteById(uuid);
                publishEvent(Event.DELETE, convertToDto(employee));
            }
    }
    private void publishEvent(Event event,EmployeeDto employee)
    {
        kafkaProducer.send(new EmployeeEvent(event.getValue(), employee));
    }
}
