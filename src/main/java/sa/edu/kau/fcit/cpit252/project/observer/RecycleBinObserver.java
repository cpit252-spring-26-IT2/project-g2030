package sa.edu.kau.fcit.cpit252.project.observer;

import sa.edu.kau.fcit.cpit252.project.model.RecycleBinItem;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

import java.util.ArrayList;
import java.util.List;

public class RecycleBinObserver implements FileObserver {

    private final List<RecycleBinItem> recycleBin = new ArrayList<>();

    @Override
    public void update(FileEvent event) {
        if (event.getEventType() == FileEventType.FILE_DELETED) {
            SecureFile file = event.getSecureFile();
            recycleBin.add(new RecycleBinItem(file, event.getActorId()));
            file.setDeleted(true);
        }
    }

    public List<RecycleBinItem> getRecycleBin() {
        return recycleBin;
    }

    public SecureFile restoreFile(String fileId) {
        for (RecycleBinItem item : recycleBin) {
            if (item.getFile().getFileId().equals(fileId)) {
                item.getFile().setDeleted(false);
                recycleBin.remove(item);
                return item.getFile();
            }
        }
        return null;
    }
}