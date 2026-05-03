package sa.edu.kau.fcit.cpit252.project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sa.edu.kau.fcit.cpit252.project.model.*;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;
import sa.edu.kau.fcit.cpit252.project.logger.*;

public class SFTSTest {

    @Test
    public void testFileBuilderPattern() {
        // Builder Pattern Test: Ensure the medical file is created correctly
        SecureFile file = new FileBuilder()
                .setId("DOC-101")
                .setName("Patient_Blood_Test")
                .setDept("Laboratory")
                .setUrgent(true)
                .build();

        assertNotNull(file, "File should not be null");
        assertTrue(file.toString().contains("DOC-101"), "File should contain the correct ID");
    }

    @Test
    public void testDepartmentFactoryPattern() {
        // Factory Pattern Test: Ensure the factory returns the correct departments
        Department lab = DepartmentFactory.getDepartment("LAB");
        Department er = DepartmentFactory.getDepartment("ER");

        assertNotNull(lab);
        assertEquals("Laboratory", lab.getName(), "Department name should be 'Laboratory'");

        assertNotNull(er);
        assertEquals("Emergency Room", er.getName(), "Department name should be 'Emergency Room'");
    }

    @Test
    public void testProxyPatternAccess() {
        // Proxy Pattern Test: Verify access control based on user authorization
        Department realLab = DepartmentFactory.getDepartment("LAB");

        // Authorized user (based on the proxy logic)
        Department proxyAuthorized = new DepartmentProxy(realLab, "Abdulaziz_Bukhari");
        assertEquals("Laboratory (Secured)", proxyAuthorized.getName());
        assertDoesNotThrow(() -> proxyAuthorized.processFile("Blood_Test.pdf"),
                "Should not throw an exception for an authorized user");

        // Unauthorized user
        Department proxyUnauthorized = new DepartmentProxy(realLab, "Guest_User");
        assertDoesNotThrow(() -> proxyUnauthorized.processFile("Secret_Records.pdf"),
                "Access should be denied internally without crashing the system");
    }

    @Test
    public void testSingletonLoggerPattern() {
        // Singleton Pattern Test: Ensure only one instance of the Logger exists
        AuditLogger logger1 = AuditLogger.getInstance();
        AuditLogger logger2 = AuditLogger.getInstance();

        assertSame(logger1, logger2, "Both variables should point to the exact same instance");
    }
}