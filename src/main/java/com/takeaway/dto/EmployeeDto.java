package com.takeaway.dto;

import lombok.*;
import java.io.Serializable;
import java.util.*;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto implements Serializable {
    private UUID uuid;
    private String email;
    private String fullName;
    private Date birthday;
    private List<String> hobbies;
}
