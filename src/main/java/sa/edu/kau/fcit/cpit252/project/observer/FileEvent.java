package sa.edu.kau.fcit.cpit252.project.observer;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileEvent {
    private final FileEventType eventType;
    private final SecureFile secureFile;
    private final String actorId;
    private final String details;
    private final LocalDateTime timestamp;

    public FileEvent(FileEventType eventType, SecureFile secureFile, String actorId, String details) {
        this.eventType = eventType;
        this.secureFile = secureFile;
        this.actorId = actorId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s — %s (%s)",
                timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                eventType,
                secureFile.getFileName(),
                actorId);
    }
    public FileEventType getEventType() {
        return eventType;
    }

    public SecureFile getSecureFile() {
        return secureFile;
    }

    public String getActorId() {
        return actorId;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}