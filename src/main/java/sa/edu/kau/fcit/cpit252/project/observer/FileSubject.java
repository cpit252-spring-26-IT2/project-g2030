package sa.edu.kau.fcit.cpit252.project.observer;

public interface FileSubject {
    void addObserver(FileObserver observer);
    void removeObserver(FileObserver observer);
    void notifyObservers(FileEvent event);
}