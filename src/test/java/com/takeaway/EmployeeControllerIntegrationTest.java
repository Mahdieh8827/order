package com.takeaway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.takeaway.dto.EmployeeDto;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TakeawayApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class EmployeeControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get_AllEmployees_Should_ReturnListOfEmployees() throws Exception {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setUuid(UUID.randomUUID());
        employeeDto.setEmail("mahdieh.bashiri@yahoo.com");

        mockMvc.perform(MockMvcRequestBuilders
                .post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(employeeDto)))
                .andExpect(status().isCreated());

        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/employees")
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").exists());
    }

    @Test
    void delete_Employee_Should_Return_NoContent() throws Exception {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmail("david.raf@gmail.com");
        employeeDto.setFullName("David");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(employeeDto))).andReturn();

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/employees/{uuid}", UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.uuid").toString())))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_Employee_Should_Return_Updated_Employee() throws Exception {
        UUID uuid = UUID.randomUUID();

        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setUuid(uuid);
        employeeDto.setEmail("mahdieh.bashiri@gmail.com");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(employeeDto))).andReturn();

        UUID updatedUuid = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.uuid").toString());
        employeeDto.setEmail("amir.bashiri@yahoo.com");
        employeeDto.setUuid(updatedUuid);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/employees/{uuid}", updatedUuid)
                .content(asJsonString(employeeDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(employeeDto.getEmail()));

    }
}
