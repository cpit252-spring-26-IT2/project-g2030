package sa.edu.kau.fcit.cpit252.project.observer;

import sa.edu.kau.fcit.cpit252.project.model.FileVersion;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

public class VersionObserver implements FileObserver {

    @Override
    public void update(FileEvent event) {
        if (event.getEventType() == FileEventType.FILE_UPDATED) {
            SecureFile file = event.getSecureFile();
            int nextVersion = file.getVersions().size() + 1;
            FileVersion version = new FileVersion(nextVersion, file.getContent(), event.getActorId());
            file.getVersions().add(version);
        }
    }
}