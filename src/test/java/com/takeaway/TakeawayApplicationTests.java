package com.takeaway;

import com.takeaway.dto.EmployeeDto;
import com.takeaway.event.EmployeeEvent;
import com.takeaway.event.Event;
import com.takeaway.exception.DuplicateEmailException;
import com.takeaway.exception.NotFoundException;
import com.takeaway.entity.Employee;
import com.takeaway.producer.KafkaProducer;
import com.takeaway.repository.EmployeeRepository;
import com.takeaway.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@DataJpaTest
class TakeawayApplicationTests {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private KafkaProducer<EmployeeEvent> kafkaProducer;

    @InjectMocks
    private EmployeeService employeeService;

    @Captor
    private ArgumentCaptor<EmployeeEvent> employeeEventCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_Employee_With_UniqueEmail_Should_Save_Employee_And_PublishEvent() {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmail("mahdieh.bashiri@gmail.com");
        employeeDto.setFullName("Bashiri");

        Employee createdEmployee = new Employee();
        createdEmployee.setUuid(UUID.randomUUID());
        createdEmployee.setEmail("mahdieh.bashiri@gmail.com");

        when(employeeRepository.findByEmail(employeeDto.getEmail())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(createdEmployee);

        EmployeeDto result = employeeService.createEmployee(employeeDto);

        assertNotNull(result);
        assertEquals(createdEmployee.getUuid(), result.getUuid());
        assertEquals(createdEmployee.getEmail(), result.getEmail());

        verify(employeeRepository, times(1)).findByEmail(employeeDto.getEmail());
        verify(employeeRepository, times(2)).save(any(Employee.class));
        verify(kafkaProducer, times(1)).send(employeeEventCaptor.capture());

        EmployeeEvent capturedEvent = employeeEventCaptor.getValue();
        assertEquals(Event.CREATE.getValue(), capturedEvent.getEventType());
        assertEquals(createdEmployee.getUuid(), capturedEvent.getEmployee().getUuid());
        assertEquals(createdEmployee.getEmail(), capturedEvent.getEmployee().getEmail());
    }

    @Test
    void getAll_Employees_Should_Return_PageOfEmployeeDto() {
        int page = 0;
        int size = 10;
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(new Employee());
        employeeList.add(new Employee());
        Page<Employee> employeePage = new PageImpl<>(employeeList);

        when(employeeRepository.findAll(PageRequest.of(page, size))).thenReturn(employeePage);
        Page<EmployeeDto> result = employeeService.getAllEmployees(page, size);

        assertNotNull(result);
        assertEquals(employeeList.size(), result.getContent().size());

        verify(employeeRepository, times(1)).findAll(PageRequest.of(page, size));
    }
    @Test
    void create_Employee_With_DuplicateEmail_Should_ThrowDuplicateEmailException() {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmail("mahdieh.bashiri@gmail.com");

        Mockito.when(employeeRepository.findByEmail(employeeDto.getEmail())).thenReturn(Optional.of(new Employee()));

        assertThrows(DuplicateEmailException.class, () -> employeeService.createEmployee(employeeDto));

        verify(employeeRepository, times(1)).findByEmail(employeeDto.getEmail());
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(kafkaProducer, never()).send(any(EmployeeEvent.class));
    }
    @Test
    void update_Employee_Should_Update_Employee_And_PublishEvent() {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setUuid(UUID.randomUUID());
        employeeDto.setEmail("mahdieh.bashiri@gmail.com");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setUuid(employeeDto.getUuid());
        updatedEmployee.setEmail(employeeDto.getEmail());

        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        EmployeeDto result = employeeService.updateEmployee(employeeDto);

        assertNotNull(result);
        assertEquals(updatedEmployee.getUuid(), result.getUuid());
        assertEquals(updatedEmployee.getEmail(), result.getEmail());

        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(kafkaProducer, times(1)).send(employeeEventCaptor.capture());

        EmployeeEvent capturedEvent = employeeEventCaptor.getValue();
        assertEquals(Event.UPDATE.getValue(), capturedEvent.getEventType());
        assertEquals(updatedEmployee.getUuid(), capturedEvent.getEmployee().getUuid());
        assertEquals(updatedEmployee.getEmail(), capturedEvent.getEmployee().getEmail());
    }

    @Test
    void delete_Employee_With_ExistingUuid_Should_Delete_Employee_And_PublishEvent() {
        UUID uuid = UUID.randomUUID();
        Employee existingEmployee = new Employee();
        existingEmployee.setUuid(uuid);

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(existingEmployee));

        employeeService.deleteEmployee(uuid);

        verify(employeeRepository, times(1)).findByUuid(uuid);
        verify(employeeRepository, times(1)).deleteById(uuid);
        verify(kafkaProducer, times(1)).send(employeeEventCaptor.capture());

        EmployeeEvent capturedEvent = employeeEventCaptor.getValue();
        assertEquals(Event.DELETE.getValue(), capturedEvent.getEventType());
        assertEquals(existingEmployee.getUuid(), capturedEvent.getEmployee().getUuid());
        assertEquals(existingEmployee.getEmail(), capturedEvent.getEmployee().getEmail());
    }

    @Test
    void delete_Employee_With_NonExistingUuid_Should_ThrowNotFoundException() {
        UUID uuid = UUID.randomUUID();
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.deleteEmployee(uuid));

        verify(employeeRepository, times(1)).findByUuid(uuid);
        verify(employeeRepository, never()).deleteById(uuid);
        verify(kafkaProducer, never()).send(any(EmployeeEvent.class));
    }
}