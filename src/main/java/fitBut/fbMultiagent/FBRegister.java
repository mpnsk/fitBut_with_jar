package fitBut.fbMultiagent;

import fitBut.agents.FBAgent;
import fitBut.fbPerceptionModule.data.SimInfo;
import fitBut.utils.logging.HorseRider;
import fitBut.utils.Point;

import java.util.*;

public class FBRegister {
    private static final String TAG = "FBRegister";
    //private static ArrayList<FBAgent> agentList = new ArrayList<>();
    private static ArrayList<FBAgent> reportedList = new ArrayList<>();
    private static ArrayList<String> acceptedTasks = new ArrayList<>();
    private static int lastStep = -1;
    private static int reportedNum = 0;
    private static int afterReportedNum = 0;
    private static ArrayList<Point> loopPointsToEval = new ArrayList<>();

    public static void reset() {
        reportedList = new ArrayList<>();
        acceptedTasks = new ArrayList<>();
        lastStep = -1;
        reportedNum = 0;
        afterReportedNum = 0;
        loopPointsToEval = new ArrayList<>();
        GlobalVars.reset();
    }

    /**
     * waits for all agents to finish percepts, last agent does sync and returns true
     *
     * @param fbAgent agent reporting
     * @param step    agent's step
     * @return true if all agents are synced
     */
    public static synchronized boolean reportIn(FBAgent fbAgent, int step) {
        if (step > lastStep) { // new step
            newStep();
            reportedList.add(fbAgent);
            reportedNum = 1;
            lastStep = step;
            HorseRider.inquire(TAG, "reportIn: " + fbAgent.getName() + " " + step + " " + reportedNum + "/" + fbAgent.getSimInfo().getTeamSize());
        } else if (step < lastStep) {
            HorseRider.yell(TAG, "reportIn: " + fbAgent.getName() + " reporting with info from previous step: " + step + " vs " + lastStep);
        } else {
            reportedNum++;
            reportedList.add(fbAgent);
            HorseRider.inquire(TAG, "reportIn: " + fbAgent.getName() + " " + step + " " + reportedNum + "/" + fbAgent.getSimInfo().getTeamSize());
            if (reportedNum == fbAgent.getSimInfo().getTeamSize()) { // this agent is last to report
                runPostReport(fbAgent, step);
                return true;
            }
        }
        return false;
    }

    private static void newStep() {
        acceptedTasks.clear();
        reportedList.clear();
        afterReportedNum = 0;
    }

    private static void runPostReport(FBAgent fbAgent, int step) {
        HashMap<Point, ArrayList<FBAgent>> seeingAgentList = new HashMap<>();
        for (FBAgent agent : reportedList) {                         // for all agents
            if (agent.getAgentInfo().getAcceptedTask() != null) {
                acceptedTasks.add(agent.getAgentInfo().getAcceptedTask());
            }
            for (Point agentPoint : agent.getSeeingFriendlies(step)) {   // get seen friend
                if (!seeingAgentList.containsKey(agentPoint)) {       // no one on this vector
                    ArrayList<FBAgent> agentOnPointList = new ArrayList<>();
                    agentOnPointList.add(agent);
                    seeingAgentList.put(agentPoint, agentOnPointList);
                } else {                                              // seeing more on same vector
                    seeingAgentList.get(agentPoint).add(agent);
                }
            }
        }

        for (Point vector : seeingAgentList.keySet()) {
            //HorseRider.challenge(TAG, "reportIn: seeing: "+pVector+" "+seeingAgentList.get(pVector).size());
            Point pVector = new Point(vector);
            if (seeingAgentList.get(pVector).size() == 1) { // got a see-one
                Point rVector = new Point(-pVector.x, -pVector.y);
                if (seeingAgentList.containsKey(rVector) && seeingAgentList.get(rVector).size() == 1) { // just checking
                    FBAgent agent1 = seeingAgentList.get(pVector).get(0);
                    FBAgent agent2 = seeingAgentList.get(rVector).get(0);
                    if (FBRegister.GlobalVars.isBlackListed(agent1)) continue;
                    if (FBRegister.GlobalVars.isBlackListed(agent2)) continue;
                    if (agent1.getGroup() == agent2.getGroup()) {                   // in the same group
                        //check map looping
                        if (agent1.getPosition(step).diff(agent2.getPosition(step)).distance(rVector) != 0) {

                            HorseRider.inform(TAG, "reportIn: looping pos \n " +
                                    agent1 + "\n" +
                                    agent2 + "\n" +
                                    rVector + "\n" +
                                    (agent1.getPosition(step).diff(agent2.getPosition(step)).diff(rVector))
                            );
                            Point loopVec = agent1.getPosition(step).diff(agent2.getPosition(step)).diff(rVector);
                            if ((loopVec.x!=0 && loopVec.x > -10 && loopVec.x < 10 )|| (loopVec.y !=0 && loopVec.y > -10 && loopVec.y < 10)) {
                                HorseRider.yell(TAG, "runPostReport: blacklisted " + agent1 + " " + agent2 + " for loop " + loopVec);
                                FBRegister.GlobalVars.blackList(agent1);
                                FBRegister.GlobalVars.blackList(agent2);
                            } else {
                                loopPointsToEval.add(loopVec);
                            }
                        }
                        continue;
                    }
                    /*HorseRider.warn(TAG, "reportIn: " + agent1.getName() + " " + agent1.getLocalPosition() +
                            "\n" + agent2.getName() + " " + agent2.getLocalPosition());*/
                    if (agent1.getGroup().isActive()) {
                        if (agent2.getGroup().isActive()) {
                            if (agent1.getGroup().getGroupSize() >= agent1.getGroup().getGroupSize()) {
                                agent1.getGroup().importGroup(agent1, agent2, pVector);
                            } else {
                                agent2.getGroup().importGroup(agent2, agent1, rVector);
                            }
                        } else {
                            agent1.getGroup().registerNewMember(agent1, agent2, pVector);
                        }
                    } else if (agent2.getGroup().isActive()) { // todo: is this ever used?
                        agent2.getGroup().registerNewMember(agent2, agent1, rVector);
                    } else { // todo: is this ever used?
                        FBGroup oldGroup = agent2.getGroup();
                        agent1.getGroup().registerNewMember(agent1, agent2, pVector);
                        oldGroup.setInactive();
                    }
                    HorseRider.inquire(TAG, "reportIn: Got match! " +
                            agent1.getName() + " " + agent2.getName() +
                            " " + pVector);
                    HorseRider.challenge(TAG, "reportIn2: " + agent1.getName() + " " + agent1.getLocalPosition(step) +
                            "\n" + agent2.getName() + " " + agent2.getLocalPosition(step));

                    if (agent1.getGroup().isActive()) {
                        HorseRider.challenge(TAG, "runPostReport: GROUP  MAP!");
                        agent1.getGroup().printGroup();
                        agent1.getGroup().printMap();
                    }

                } else {
                    HorseRider.yell(TAG, "reportIn: " + fbAgent.getName() +
                            " ERROR xkcd.com/2200 on agent sync \n" + pVector + "\n" +
                            rVector + " recorded " + seeingAgentList.containsKey(rVector) + ", is only: " +
                            (seeingAgentList.containsKey(rVector) && seeingAgentList.get(rVector).size() == 1));
                }
            }
        }
        /*if(updateLimits) {
            for (FBAgent agent : agentList) {                         // for all agents
                agent.getMap().checkLimitChange();
            }
        }*/
    }

    public static ArrayList<String> getAcceptedTasks() {
        return acceptedTasks;
    }

    /**
     * waits for all agents to finish eval, last agent does limit sync
     *
     * @param fbAgent agent reporting
     * @param step    agent's step
     */
    public static synchronized void agentDone(FBAgent fbAgent, int step) {
        afterReportedNum++;
        HorseRider.inquire(TAG, "agentDone: " + fbAgent.getName() + " " + step + " " + afterReportedNum + "/" + fbAgent.getSimInfo().getTeamSize());
        if (afterReportedNum == fbAgent.getSimInfo().getTeamSize()) { // this agent is last to report
            for (Point loopPoint : loopPointsToEval) {
                //boolean updateLimits;
                if (loopPoint.x != 0) {
                    Point.LoopLimit.setLoopX(Math.abs(loopPoint.x));//updateLimits = true;
                }
                if (loopPoint.y != 0) {
                    Point.LoopLimit.setLoopY(Math.abs(loopPoint.y));
                }
            }
            loopPointsToEval.clear();
        }

    }

    public static class GlobalVars {
        private static int digEnergyReq = -1;
        private static int vision = 0;
        private static boolean timeRestricted = false;
        //private static final HashMap<Integer, HashMap<String, Integer>> decisionTimeReserve = new HashMap<>();
        //private static final HashMap<Integer, HashMap<String, Integer>> reservationTimeReserve = new HashMap<>();
        //private static final HashMap<Integer, TimesAverages> decisionTimeAverages = new HashMap<>();
        //private static final HashMap<Integer, TimesAverages> reservationTimeAverages = new HashMap<>();
        private static final Set<FBAgent> blacklist = new HashSet<>();
        private static String backupParamTeam = "FIT_BUT";
        private static int backupParamTeamSize = 50;
        private static int backupParamSteps = 750;
        private static int backupParamVision = 5;
        private static int consecutiveHitsNeeded = 2;

        public static void setDigEnergyReq(int digEnergyReq) {
            GlobalVars.digEnergyReq = digEnergyReq;
        }

        public static int getDigEnergyReq() {
            return digEnergyReq;
        }

        public static void reset() {
            digEnergyReq = -1;
            vision = 0;
            timeRestricted = false;
            //decisionTimeReserve.clear();
            //reservationTimeReserve.clear();
            //decisionTimeAverages.clear();
            //reservationTimeAverages.clear();
            blacklist.clear();
        }

        public static int getVision() {
            return vision;
        }

        public static void setVision(int vision) {
            GlobalVars.vision = vision;
        }

        public static boolean isTimeRestricted() {
            return timeRestricted;
        }

        public static synchronized void setDecisionTimeReserve(int step, String name, int remainingTime) {
            //decisionTimeReserve.putIfAbsent(step, new HashMap<>());
            //decisionTimeReserve.get(step).put(name, remainingTime);
            //decisionTimeAverages.putIfAbsent(step, new TimesAverages());
            //decisionTimeAverages.get(step).add(remainingTime);
        }

        public static synchronized void setReservationTimeReserve(int step, String name, int remainingTime) {
            //reservationTimeReserve.putIfAbsent(step, new HashMap<>());
            //reservationTimeReserve.get(step).put(name, remainingTime);
            //reservationTimeAverages.putIfAbsent(step, new TimesAverages());
            //reservationTimeAverages.get(step).add(remainingTime);
        }

        public static void blackList(FBAgent agent1) {
            blacklist.add(agent1);
        }

        public static boolean isBlackListed(FBAgent agent) {
            return blacklist.contains(agent);
        }

        public static void getBackupConfig(SimInfo simInfo) {
            simInfo.setTeam(backupParamTeam);
            simInfo.setTeamSize(backupParamTeamSize);
            simInfo.setSteps(backupParamSteps);
            simInfo.setVision(backupParamVision);
            simInfo.setFromBackup(true);
        }

        public static void setTeamName(String a) {
            backupParamTeam = a;
        }

        public static void setTeamSize(int i) {
            backupParamTeamSize = i;
        }

        public static void setConsecutiveHitsNeeded(int consecutiveHitsNeeded) {
            GlobalVars.consecutiveHitsNeeded = consecutiveHitsNeeded;
        }

        public static int getConsecutiveHitsNeeded() {
            return consecutiveHitsNeeded;
        }

        private static class TimesAverages {
            int average = 0;
            int count = 0;

            public void add(int remainingTime) {
                average = (average * count + remainingTime) / ++count;
            }
        }
    }
}
