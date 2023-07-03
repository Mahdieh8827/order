package com.takeaway.repository;

import com.takeaway.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByUuid(UUID uuid);
    Optional<Employee> findByEmail(String email);
}
