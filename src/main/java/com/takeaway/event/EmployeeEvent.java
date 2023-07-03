package com.takeaway.event;

import com.takeaway.dto.EmployeeDto;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeEvent implements Serializable {
    private String eventType;
    private EmployeeDto employee;
}
