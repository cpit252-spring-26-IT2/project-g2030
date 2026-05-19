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
import sa.edu.kau.fcit.cpit252.project.observer.*;
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
        er  = DepartmentFactory.getDepartment("ER");

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

    // ── Builder ───────────────────────────────────────────────────────────────
    @Test
    void testBuilderCreatesSecureFileCorrectly() {
        assertNotNull(testFile);
        assertEquals("TEST-001",                  testFile.getFileId());
        assertEquals("Blood_Report.pdf",           testFile.getFileName());
        assertEquals("Laboratory",                 testFile.getDepartment());
        assertEquals("Abdulaziz_Bukhari",          testFile.getOwnerId());
        assertEquals("Sensitive patient report",   testFile.getContent());
        assertTrue(testFile.isEncrypted());
        assertEquals("AES-256",                    testFile.getEncryptionType());
        assertTrue(testFile.isViewOnly());
        assertEquals(3,                            testFile.getMaxViews());
        assertEquals("Viewer Copy",                testFile.getWatermarkText());
    }

    // ── Factory ───────────────────────────────────────────────────────────────
    @Test
    void testDepartmentFactory() {
        assertNotNull(lab);
        assertNotNull(er);
        assertInstanceOf(LabDepartment.class, lab);
        assertInstanceOf(ERDepartment.class,  er);
    }

    // ── Proxy ─────────────────────────────────────────────────────────────────
    @Test
    void testProxyAuthorizedAccessDoesNotCrash() {
        Department proxy = new DepartmentProxy(lab, "Abdulaziz_Bukhari");
        assertDoesNotThrow(() -> proxy.processFile(testFile));
    }

    @Test
    void testProxyUnauthorizedAccessBlocked() {
        Department proxy = new DepartmentProxy(lab, "Unknown_User");
        assertDoesNotThrow(() -> proxy.processFile(testFile));
        // viewCount يجب أن يبقى 0 لأن الوصول محجوب
        assertEquals(0, testFile.getViewCount());
    }

    // ── View Limit ────────────────────────────────────────────────────────────
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
        assertEquals(1, limitedFile.getViewCount());

        assertTrue(limitedFile.canBeOpened());
        assertTrue(limitedFile.openFile());
        assertEquals(2, limitedFile.getViewCount());

        assertFalse(limitedFile.canBeOpened());
        assertTrue(limitedFile.isAccessRevoked());
    }

    // ── Expiry ────────────────────────────────────────────────────────────────
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
        assertTrue(expiredFile.isAccessRevoked());
    }

    // ── Encryption ────────────────────────────────────────────────────────────
    @Test
    void testEncryptionDecryption() throws Exception {
        String originalData = "Sensitive Patient Record Content";
        byte[] encrypted = SecurityManager.encryptData(originalData.getBytes());

        assertNotNull(encrypted);
        assertNotEquals(originalData, new String(encrypted));

        byte[] decrypted = SecurityManager.decryptData(encrypted);
        assertEquals(originalData, new String(decrypted));
    }

    // ── Singleton ─────────────────────────────────────────────────────────────
    @Test
    void testSingletonAuditLogger() {
        AuditLogger instance1 = AuditLogger.getInstance();
        AuditLogger instance2 = AuditLogger.getInstance();
        assertSame(instance1, instance2);
    }

    // ── Observer Manual ───────────────────────────────────────────────────────
    @Test
    void testObserverNotificationManual() {
        AtomicBoolean notified = new AtomicBoolean(false);

        FileObserver testObserver = event -> {
            if (event.getEventType() == FileEventType.FILE_OPENED &&
                    event.getSecureFile().getFileId().equals("TEST-001")) {
                notified.set(true);
            }
        };

        testObserver.update(new FileEvent(
                FileEventType.FILE_OPENED,
                testFile,
                "Viewer123",
                "File opened successfully"
        ));

        assertTrue(notified.get());
    }

    // ── Version Observer ──────────────────────────────────────────────────────
    @Test
    void testVersionObserver() {
        VersionObserver versionObserver = new VersionObserver();

        versionObserver.update(new FileEvent(
                FileEventType.FILE_UPDATED,
                testFile,
                "Abdulaziz_Bukhari",
                "Updated file content"
        ));

        assertEquals(1, testFile.getVersions().size());

        FileVersion version = testFile.getVersions().get(0);
        assertEquals(1,                          version.getVersionNumber());
        assertEquals("Sensitive patient report", version.getContentSnapshot());
        assertEquals("Abdulaziz_Bukhari",        version.getUpdatedBy());
    }

    // ── Recycle Bin Observer ──────────────────────────────────────────────────
    @Test
    void testRecycleBinObserver() {
        RecycleBinObserver recycleBinObserver = new RecycleBinObserver();

        recycleBinObserver.update(new FileEvent(
                FileEventType.FILE_DELETED,
                testFile,
                "Motaz_Alsayed",
                "File moved to recycle bin"
        ));

        assertTrue(testFile.isDeleted());
        assertEquals(1, recycleBinObserver.getRecycleBin().size());

        RecycleBinItem item = recycleBinObserver.getRecycleBin().get(0);
        assertEquals(testFile,       item.getFile());
        assertEquals("Motaz_Alsayed", item.getDeletedBy());
    }

    // ── View Tracking Observer ────────────────────────────────────────────────
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

        observer.update(new FileEvent(
                FileEventType.FILE_OPENED,
                file,
                "ViewerX",
                "File viewed"
        ));

        assertEquals(1, file.getViewCount());
        assertTrue(file.isAccessRevoked());
    }
}