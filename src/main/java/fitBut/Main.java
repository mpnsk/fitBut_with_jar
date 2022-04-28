package fitBut;

import eis.exceptions.ManagementException;
import fitBut.fbMultiagent.FBRegister;
import fitBut.utils.Point;
import massim.eismassim.EnvironmentInterface;

import java.io.File;
import java.util.Scanner;

/**
 * Starts a new scheduler.
 */
public class Main {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args ) {

        String configDir = "";

        System.out.println("PHASE 1: INSTANTIATING SCHEDULER");
        if (args.length != 0) configDir = args[0];
        else {
            File confDir = new File("conf");
            confDir.mkdirs();
            //System.out.println("path: "+confDir.getAbsolutePath());
            File[] confFiles = confDir.listFiles(File::isDirectory);
            if (confFiles == null || confFiles.length == 0) {
                confDir = new File("javaagents/conf");
                confDir.mkdirs();
                confFiles = confDir.listFiles(File::isDirectory);
            }
            if (confFiles == null || confFiles.length == 0) {
                System.out.println("No javaagents config files available - exit JavaAgents.");
                System.exit(0);
            }
            else if(confFiles.length>1) {
                System.out.println("PHASE 1.2: CHOOSE CONFIGURATION");
                System.out.println("Choose a number:");
                for (int i = 0; i < confFiles.length; i++) {
                    System.out.println(i + " " + confFiles[i]);
                }
                Scanner in = new Scanner(System.in);
                Integer confNum = null;
                while (confNum == null) {
                    try {
                        confNum = Integer.parseInt(in.next());
                        if (confNum < 0 || confNum > confFiles.length - 1){
                            System.out.println("No config for that number, try again:");
                            confNum = null;
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid number, try again:");
                    }
                }
                configDir = confFiles[confNum].getPath();
            }else{
                configDir = confFiles[0].getPath();
            }
        }
        Scheduler scheduler = new Scheduler(configDir);

        System.out.println("PHASE 2: INSTANTIATING ENVIRONMENT");
        EnvironmentInterface ei = new EnvironmentInterface(configDir + File.separator + "eismassimconfig.json");

        try {
            ei.start();
        } catch (ManagementException e) {
            e.printStackTrace();
        }

        System.out.println("PHASE 3: CONNECTING SCHEDULER AND ENVIRONMENT");
        scheduler.setEnvironment(ei);


        System.out.println("PHASE 4: RUNNING");

        FBRegister.GlobalVars.setTeamName("B");


        FBRegister.GlobalVars.setTeamSize(15);
        match(scheduler,1);

        FBRegister.GlobalVars.setTeamSize(30);
        match(scheduler,2);

        FBRegister.GlobalVars.setTeamSize(50);
        match(scheduler,3);
        match(scheduler,4);
        match(scheduler,5);
        match(scheduler,6);

    }

    private static void match(Scheduler scheduler, int i) {
        FBRegister.reset();
        Point.LoopLimit.reset();
        scheduler.resetEnvironment(i);
        int step = 0;
        //while ((ei.getState() == EnvironmentState.RUNNING)) {
        while (scheduler.step()) {
            //System.out.println("SCHEDULER STEP " + step);
            //scheduler.step();
            step++;
        }
    }
}
