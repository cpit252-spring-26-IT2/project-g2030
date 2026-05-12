package sa.edu.kau.fcit.cpit252.project.observer;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

public class ViewTrackingObserver implements FileObserver {

    @Override
    public void update(FileEvent event) {
        if (event.getEventType() == FileEventType.FILE_OPENED) {
            SecureFile file = event.getSecureFile();
            file.incrementViewCount();

            if (file.getMaxViews() > 0 && file.getViewCount() >= file.getMaxViews()) {
                file.setAccessRevoked(true);
            }
        }
    }
}