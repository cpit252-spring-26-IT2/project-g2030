package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.core.DepartmentServer;

public class ServerLauncher {
    public static void main(String[] args) {
        String department = args.length > 0 ? args[0] : "ER";
        DepartmentServer server = new DepartmentServer(department);
        server.launchHeadless();
    }
}