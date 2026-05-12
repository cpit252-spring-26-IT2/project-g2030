package sa.edu.kau.fcit.cpit252.project.service;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import sa.edu.kau.fcit.cpit252.project.observer.FileEvent;
import sa.edu.kau.fcit.cpit252.project.observer.FileEventType;
import sa.edu.kau.fcit.cpit252.project.observer.FileObserver;
import sa.edu.kau.fcit.cpit252.project.observer.FileSubject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileAccessManager implements FileSubject {

    private final List<FileObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(FileObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(FileObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(FileEvent event) {
        for (FileObserver observer : observers) {
            observer.update(event);
        }
    }

    public boolean openFile(SecureFile file, String userId) {
        if (file.isDeleted()) {
            notifyObservers(new FileEvent(FileEventType.ACCESS_DENIED, file, userId, "File is in recycle bin"));
            return false;
        }

        if (file.isAccessRevoked()) {
            notifyObservers(new FileEvent(FileEventType.ACCESS_DENIED, file, userId, "View limit reached"));
            return false;
        }

        if (file.getExpiryTime() != null && LocalDateTime.now().isAfter(file.getExpiryTime())) {
            file.setAccessRevoked(true);
            notifyObservers(new FileEvent(FileEventType.FILE_EXPIRED, file, userId, "Time-limited access expired"));
            notifyObservers(new FileEvent(FileEventType.ACCESS_DENIED, file, userId, "Expired file"));
            return false;
        }

        notifyObservers(new FileEvent(FileEventType.FILE_OPENED, file, userId, "File opened successfully"));
        return true;
    }

    public void updateFile(SecureFile file, String userId, String newContent) {
        file.setContent(newContent);
        notifyObservers(new FileEvent(FileEventType.FILE_UPDATED, file, userId, "File content updated"));
        notifyObservers(new FileEvent(FileEventType.VERSION_CREATED, file, userId, "New file version saved"));
    }

    public void deleteFile(SecureFile file, String userId) {
        notifyObservers(new FileEvent(FileEventType.FILE_DELETED, file, userId, "File moved to recycle bin"));
    }

    public void restoreFile(SecureFile file, String userId) {
        file.setDeleted(false);
        notifyObservers(new FileEvent(FileEventType.FILE_RESTORED, file, userId, "File restored from recycle bin"));
    }
}