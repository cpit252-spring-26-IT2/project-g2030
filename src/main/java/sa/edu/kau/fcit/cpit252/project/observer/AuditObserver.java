package sa.edu.kau.fcit.cpit252.project.observer;

import sa.edu.kau.fcit.cpit252.project.logger.AuditLogger;

public class AuditObserver implements FileObserver {

    @Override
    public void update(FileEvent event) {
        AuditLogger logger = AuditLogger.getInstance();

        String actor = event.getActorId();

        String message = "EVENT = " + event.getEventType()
                + ", FILE = " + event.getSecureFile().getFileName()
                + ", DETAILS = " + event.getDetails()
                + ", TIME = " + event.getTimestamp();

        logger.log(actor, message);
    }
}