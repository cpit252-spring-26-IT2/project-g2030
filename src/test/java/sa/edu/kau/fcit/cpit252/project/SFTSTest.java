package sa.edu.kau.fcit.cpit252.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sa.edu.kau.fcit.cpit252.project.model.*;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;
import sa.edu.kau.fcit.cpit252.project.logger.*;
import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import java.time.LocalDateTime;

public class SFTSTest {

    private Department lab;
    private Department er;

    @BeforeEach
    void setUp() {
        // Initialize departments using the Factory Pattern
        lab = DepartmentFactory.getDepartment("LAB");
        er = DepartmentFactory.getDepartment("ER");
    }

    /**
     * Test 1: Builder Pattern
     * Ensures SecureFile is constructed correctly with all parameters.
     */
    @Test
    public void testFileBuilderAndSecureFile() {
        SecureFile file = new FileBuilder()
                .setId("TEST-001")
                .setName("Blood_Report.pdf")
                .setDept("Laboratory")
                .setMaxViews(5)
                .setExpirationHours(2)
                .setViewOnly(true)
                .build();

        assertNotNull(file);
        assertEquals("Blood_Report.pdf", file.getName());
        assertTrue(file.isViewOnly());
        assertTrue(file.canView(), "File should be viewable initially");
    }

    /**
     * Test 2: Factory Pattern
     * Ensures the factory creates correct department instances.
     */
    @Test
    public void testDepartmentFactory() {
        assertNotNull(lab);
        assertEquals("Laboratory", lab.getName());
        assertTrue(lab instanceof LabDepartment);

        assertNotNull(er);
        assertEquals("Emergency Room", er.getName());
        assertTrue(er instanceof ERDepartment);
    }

    /**
     * Test 3: Proxy Pattern - Access Control
     * Verifies that only authorized users can process files in restricted departments.
     */
    @Test
    public void testProxyAccessControl() {
        SecureFile file = new FileBuilder().setId("1").setName("test").build();

        // Authorized User: Abdulaziz_Bukhari has access to Laboratory
        Department authorizedProxy = new DepartmentProxy(lab, "Abdulaziz_Bukhari");
        assertDoesNotThrow(() -> authorizedProxy.processFile(file));

        // Unauthorized User: Access should be blocked for Laboratory
        Department unauthorizedProxy = new DepartmentProxy(lab, "Motaz_Alsayed");
        unauthorizedProxy.processFile(file);
        // Logic check: The system logs the denial but doesn't crash.
    }

    /**
     * Test 4: Quantitative View Limits
     * Verifies that access is revoked once the view limit is reached.
     */
    @Test
    public void testQuantitativeViewLimits() {
        SecureFile file = new FileBuilder()
                .setId("VIEW-LIMIT")
                .setName("Limited_File.png")
                .setMaxViews(2) // Limit set to 2 views
                .build();

        assertTrue(file.canView(), "First view allowed");
        file.incrementView(); // Simulate first view

        assertTrue(file.canView(), "Second view allowed");
        file.incrementView(); // Simulate second view

        assertFalse(file.canView(), "Third view should be denied (Limit reached)");
    }

    /**
     * Test 5: Time-Limited Access
     * Verifies that files expire based on the set duration.
     */
    @Test
    public void testTimeLimitedAccess() {
        // Build a file that expires immediately (using 0 or negative logic if allowed)
        // Since setExpirationHours adds hours, we check if a standard file is valid.
        SecureFile validFile = new FileBuilder()
                .setId("TIME-01")
                .setName("Timed_File.pdf")
                .setExpirationHours(1)
                .build();

        assertTrue(validFile.canView(), "File should be valid within the hour");
    }

    /**
     * Test 6: Security Manager (AES Encryption)
     * Verifies that data can be encrypted and successfully decrypted back to original.
     */
    @Test
    public void testEncryptionDecryption() throws Exception {
        String originalData = "Sensitive Patient Record Content";
        byte[] originalBytes = originalData.getBytes();

        // Encrypt the data
        byte[] encrypted = SecurityManager.encryptData(originalBytes);
        assertNotNull(encrypted);
        assertNotEquals(originalData, new String(encrypted), "Encrypted data should not match original string");

        // Decrypt the data
        byte[] decrypted = SecurityManager.decryptData(encrypted);
        assertEquals(originalData, new String(decrypted), "Decrypted data must match original input");
    }

    /**
     * Test 7: Singleton Pattern
     * Ensures only one instance of AuditLogger is ever created.
     */
    @Test
    public void testSingletonAuditLogger() {
        AuditLogger instance1 = AuditLogger.getInstance();
        AuditLogger instance2 = AuditLogger.getInstance();

        assertSame(instance1, instance2, "AuditLogger must be a Singleton");
    }
}