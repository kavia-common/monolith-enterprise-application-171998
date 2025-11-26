package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.EmployeeResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.EmployeeResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EmployeeResourceMapper.class)
public class EmployeeRestEndpointCompatibilityUTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeRestEndpoint systemUnderTest = new EmployeeRestEndpoint();

    @Test
    public void rejectLiteralEmployeeId_shouldReturn400() {
        ResponseEntity<String> response = systemUnderTest.rejectLiteralEmployeeId();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getEmployeeFallback_nonNumeric_shouldReturn404() {
        ResponseEntity<String> response = systemUnderTest.getEmployeeFallback("{employeeId}");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getEmployeeFallback_numeric_shouldReturn200() {
        PowerMockito.mockStatic(EmployeeResourceMapper.class);
        Employee employee = new Employee();
        EmployeeResource resource = new EmployeeResource();
        resource.setEmployeeId(1);
        resource.setFirstName("John");
        resource.setSecondName("Doe");
        resource.setRole("DEV");

        Mockito.when(employeeService.getEmployee(1)).thenReturn(employee);
        PowerMockito.when(EmployeeResourceMapper.mapEmployeeToEmployeeResource(employee)).thenReturn(resource);

        ResponseEntity<String> response = systemUnderTest.getEmployeeFallback("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
