package sa.edu.kau.fcit.cpit252.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sa.edu.kau.fcit.cpit252.project.department.Department;
import sa.edu.kau.fcit.cpit252.project.department.DepartmentFactory;
import sa.edu.kau.fcit.cpit252.project.department.ERDepartment;
import sa.edu.kau.fcit.cpit252.project.department.LabDepartment;
import sa.edu.kau.fcit.cpit252.project.logger.AuditLogger;
import sa.edu.kau.fcit.cpit252.project.model.FileBuilder;
import sa.edu.kau.fcit.cpit252.project.model.FileVersion;
import sa.edu.kau.fcit.cpit252.project.model.RecycleBinItem;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import sa.edu.kau.fcit.cpit252.project.observer.FileEvent;
import sa.edu.kau.fcit.cpit252.project.observer.FileEventType;
import sa.edu.kau.fcit.cpit252.project.observer.FileObserver;
import sa.edu.kau.fcit.cpit252.project.observer.RecycleBinObserver;
import sa.edu.kau.fcit.cpit252.project.observer.VersionObserver;
import sa.edu.kau.fcit.cpit252.project.observer.ViewTrackingObserver;
import sa.edu.kau.fcit.cpit252.project.proxy.DepartmentProxy;
import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class SFTSTest {

    private Department lab;
    private Department er;
    private SecureFile testFile;

    @BeforeEach
    void setUp() {
        lab = DepartmentFactory.getDepartment("LAB");
        er = DepartmentFactory.getDepartment("ER");

        testFile = new FileBuilder()
                .setFileId("TEST-001")
                .setFileName("Blood_Report.pdf")
                .setDepartment("Laboratory")
                .setOwnerId("Abdulaziz_Bukhari")
                .setContent("Sensitive patient report")
                .setEncrypted(true)
                .setEncryptionType("AES-256")
                .setViewOnly(true)
                .setMaxViews(3)
                .setExpiryTime(LocalDateTime.now().plusHours(2))
                .setWatermarkText("Viewer Copy")
                .build();
    }

    @Test
    void testBuilderCreatesSecureFileCorrectly() {
        assertNotNull(testFile);
        assertEquals("TEST-001", testFile.getFileId());
        assertEquals("Blood_Report.pdf", testFile.getFileName());
        assertEquals("Laboratory", testFile.getDepartment());
        assertEquals("Abdulaziz_Bukhari", testFile.getOwnerId());
        assertEquals("Sensitive patient report", testFile.getContent());
        assertTrue(testFile.isEncrypted());
        assertEquals("AES-256", testFile.getEncryptionType());
        assertTrue(testFile.isViewOnly());
        assertEquals(3, testFile.getMaxViews());
        assertEquals("Viewer Copy", testFile.getWatermarkText());
    }

    @Test
    void testDepartmentFactory() {
        assertNotNull(lab);
        assertNotNull(er);
        assertTrue(lab instanceof LabDepartment);
        assertTrue(er instanceof ERDepartment);
    }

    @Test
    void testProxyAuthorizedAccessDoesNotCrash() {
        Department authorizedProxy = new DepartmentProxy(lab, "Abdulaziz_Bukhari");
        assertDoesNotThrow(() -> authorizedProxy.processFile(testFile));
    }

    @Test
    void testProxyUnauthorizedAccessDoesNotCrash() {
        Department unauthorizedProxy = new DepartmentProxy(lab, "Unknown_User");
        assertDoesNotThrow(() -> unauthorizedProxy.processFile(testFile));
    }

    @Test
    void testFileOpenAndViewLimit() {
        SecureFile limitedFile = new FileBuilder()
                .setFileId("VIEW-001")
                .setFileName("Limited_File.txt")
                .setDepartment("Laboratory")
                .setOwnerId("Motaz_Alsayed")
                .setContent("Limited content")
                .setMaxViews(2)
                .setExpiryTime(LocalDateTime.now().plusHours(1))
                .build();

        assertTrue(limitedFile.canBeOpened());
        assertTrue(limitedFile.openFile());

        assertTrue(limitedFile.canBeOpened());
        assertTrue(limitedFile.openFile());

        assertFalse(limitedFile.canBeOpened());
    }

    @Test
    void testTimeLimitedAccess() {
        SecureFile validFile = new FileBuilder()
                .setFileId("TIME-001")
                .setFileName("Timed_File.pdf")
                .setDepartment("ER")
                .setOwnerId("Abdulmalik_Aldahari")
                .setContent("Temporary file")
                .setExpiryTime(LocalDateTime.now().plusMinutes(30))
                .build();

        assertTrue(validFile.canBeOpened());

        SecureFile expiredFile = new FileBuilder()
                .setFileId("TIME-002")
                .setFileName("Expired_File.pdf")
                .setDepartment("ER")
                .setOwnerId("Abdulmalik_Aldahari")
                .setContent("Expired file")
                .setExpiryTime(LocalDateTime.now().minusMinutes(1))
                .build();

        assertFalse(expiredFile.canBeOpened());
    }

    @Test
    void testEncryptionDecryption() throws Exception {
        String originalData = "Sensitive Patient Record Content";
        byte[] encrypted = SecurityManager.encryptData(originalData.getBytes());

        assertNotNull(encrypted);
        assertNotEquals(originalData, new String(encrypted));

        byte[] decrypted = SecurityManager.decryptData(encrypted);
        assertEquals(originalData, new String(decrypted));
    }

    @Test
    void testSingletonAuditLogger() {
        AuditLogger instance1 = AuditLogger.getInstance();
        AuditLogger instance2 = AuditLogger.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    void testObserverNotificationManual() {
        AtomicBoolean notified = new AtomicBoolean(false);

        FileObserver testObserver = event -> {
            if (event.getEventType() == FileEventType.FILE_OPENED &&
                    event.getSecureFile().getFileId().equals("TEST-001")) {
                notified.set(true);
            }
        };

        FileEvent event = new FileEvent(
                FileEventType.FILE_OPENED,
                testFile,
                "Viewer123",
                "File opened successfully"
        );

        testObserver.update(event);

        assertTrue(notified.get());
    }

    @Test
    void testVersionObserver() {
        VersionObserver versionObserver = new VersionObserver();

        FileEvent updateEvent = new FileEvent(
                FileEventType.FILE_UPDATED,
                testFile,
                "Abdulaziz_Bukhari",
                "Updated file content"
        );

        versionObserver.update(updateEvent);

        assertEquals(1, testFile.getVersions().size());

        FileVersion version = (FileVersion) testFile.getVersions().get(0);
        assertEquals(1, version.getVersionNumber());
        assertEquals("Sensitive patient report", version.getContentSnapshot());
        assertEquals("Abdulaziz_Bukhari", version.getUpdatedBy());
    }

    @Test
    void testRecycleBinObserver() {
        RecycleBinObserver recycleBinObserver = new RecycleBinObserver();

        FileEvent deleteEvent = new FileEvent(
                FileEventType.FILE_DELETED,
                testFile,
                "Motaz_Alsayed",
                "File moved to recycle bin"
        );

        recycleBinObserver.update(deleteEvent);

        assertTrue(testFile.isDeleted());
        assertEquals(1, recycleBinObserver.getRecycleBin().size());

        RecycleBinItem item = (RecycleBinItem) recycleBinObserver.getRecycleBin().get(0);
        assertEquals(testFile, item.getFile());
        assertEquals("Motaz_Alsayed", item.getDeletedBy());
    }

    @Test
    void testViewTrackingObserver() {
        ViewTrackingObserver observer = new ViewTrackingObserver();

        SecureFile file = new FileBuilder()
                .setFileId("VIEW-TRACK-1")
                .setFileName("Tracked.txt")
                .setDepartment("Laboratory")
                .setOwnerId("ViewerX")
                .setContent("Track views")
                .setMaxViews(1)
                .build();

        FileEvent openEvent = new FileEvent(
                FileEventType.FILE_OPENED,
                file,
                "ViewerX",
                "File viewed"
        );

        observer.update(openEvent);

        assertEquals(1, file.getViewCount());
        assertTrue(file.isAccessRevoked());
    }
}