package fitBut.utils;

import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbMultiagent.FBRegister;
import fitBut.fbEnvironment.FBCells.objects.FBDispenserObject;
import fitBut.fbEnvironment.FBCells.objects.FBEntityObject;
import fitBut.fbEnvironment.FBCells.objects.FBOtherObject;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbMultiagent.FBGroup;
import fitBut.fbPerceptionModule.data.BlockType;

import fitBut.fbReasoningModule.fbGoals.FBGoalHoard;
import fitBut.utils.logging.HorseRider;

import java.util.ArrayList;

import static fitBut.fbReasoningModule.fbGoals.utils.PlanHelper.generateDirections;

/**
 * @author : Vaclav Uhlir
 * @since : 16/09/2019
 **/

public class Tester {
    private static final String TAG = "Tester";

    public static void main(String[] args) {
        HorseRider.yell(TAG, "main: Tadaaa");
        //FBRegister reg = new FBRegister();
        //TODO: temp sync test
        ArrayList<FBAgent> testAgent = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String agentName = "Agent" + i;
            FBAgent agent = new FBAgent(agentName);
            testAgent.add(agent);
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    agent.getLocalMap().addCellObject(new Point(j, k), new FBEntityObject(FBObjectType.__FBAgent, "a", Integer.toString(i)), 0);
                }
            }
            agent.setMapPosition(new Point(0, -2), 0);
            agent.getLocalMap().addCellObject(new Point(i * (-10) + 5, -2), new FBEntityObject(FBObjectType.__FBEntity_Friend, "a", Integer.toString(i)), 0);
            //agent.getLocalMap().addCellObject(new Point(0, i*5), new FBEntityObject(FBObjectType.__FBEntity_Friend, "a", Integer.toString(i)), 0);
            agent.getLocalMap().setName("" + i);
            agent.getSimInfo().setVision(10);
            agent.getLocalMap().printMap();
        }


        for (int i = 0; i < 2; i++) {
            String agentName = "Agent" + i;
            FBAgent agent = new FBAgent(agentName);
            testAgent.add(agent);
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    agent.getLocalMap().addCellObject(new Point(j, k), new FBEntityObject(FBObjectType.__FBAgent, "a", Integer.toString(i + 2)), 0);
                }
            }
            agent.setMapPosition(new Point(-1, 0), 0);
            agent.getLocalMap().addCellObject(new Point(-1, i * (-10) + 5), new FBEntityObject(FBObjectType.__FBEntity_Friend, "a", Integer.toString(i)), 0);
            //agent.getLocalMap().addCellObject(new Point(0, i*5), new FBEntityObject(FBObjectType.__FBEntity_Friend, "a", Integer.toString(i)), 0);
            agent.getLocalMap().setName("" + i);
            agent.getSimInfo().setVision(10);
            agent.getLocalMap().printMap();
        }


        FBRegister.reportIn(testAgent.get(0), 0);
        FBRegister.reportIn(testAgent.get(1), 0);


        FBRegister.reportIn(testAgent.get(2), 0);
        FBRegister.reportIn(testAgent.get(3), 0);

        testAgent.get(1).getLocalMap().addCellObject(new Point(3, -1), new FBEntityObject(FBObjectType.__FBEntity_Friend, "a", Integer.toString(8)), 0);
        testAgent.get(2).getLocalMap().addCellObject(new Point(-4, -1), new FBEntityObject(FBObjectType.__FBEntity_Friend, "a", Integer.toString(8)), 0);


        FBRegister.reportIn(testAgent.get(0), 1);
        FBRegister.reportIn(testAgent.get(1), 1);


        FBRegister.reportIn(testAgent.get(2), 1);
        FBRegister.reportIn(testAgent.get(3), 1);

        testAgent.get(0).getLocalMap().addCellObject(new Point(0, 4), new FBDispenserObject(new BlockType("x")), 2);
        testAgent.get(1).getLocalMap().addCellObject(
                FBGroup.translate(
                        testAgent.get(0),
                        testAgent.get(1),
                        new Point(0, 5)),
                new FBDispenserObject(new BlockType("x")), 2);

        testAgent.get(0).getGroup().printMap();

        HorseRider.inform(TAG, "main: agent 0 pos: " + testAgent.get(0).getLocalPosition(0).toString());
        HorseRider.inform(TAG, "main: agent 1 pos: " + testAgent.get(1).getLocalPosition(0));

        Point point = new Point(1, 1);
        HorseRider.inform(TAG, "main: point " + point + " seen by 0 is seen by 1 on " +
                FBGroup.getTranslatedEyeToEye(testAgent.get(0), testAgent.get(1), point));

        testAgent.get(1).getLocalMap().insertEmptyVisionCells(5, new Point(2, 5), 3);

        for (int i = 0; i < 6; i++) {
            testAgent.get(1).getLocalMap().addCellObject(new Point(i - 1, 4), new FBOtherObject(FBObjectType.__FBObstacle), 3);
        }
        for (int i = 0; i < 6; i++) {
            testAgent.get(1).getLocalMap().addCellObject(new Point(i - 1, 6), new FBOtherObject(FBObjectType.__FBObstacle), 3);
        }
        for (int i = 0; i < 3; i++) {
            testAgent.get(1).getLocalMap().addCellObject(new Point(-1, 5 + i), new FBOtherObject(FBObjectType.__FBObstacle), 3);
        }
        testAgent.get(0).setMapPosition(new Point(5, 5), 0);
        testAgent.get(1).setMapPosition(new Point(0, 5), 0);

        HorseRider.inform(TAG, "main: hoard plan: " + new FBGoalHoard().makePlan(testAgent.get(1)));
        testAgent.get(1).getLocalMap().addCellObject(new Point(0, 3), new FBBlockObject(new BlockType("1"), false), 3);
        HorseRider.inform(TAG, "main: hoard plan: " + new FBGoalHoard().makePlan(testAgent.get(1)));
        testAgent.get(1).getLocalMap().addCellObject(new Point(1, 5), new FBBlockObject(new BlockType("1"), false), 3);
        HorseRider.inform(TAG, "main: hoard plan: " + new FBGoalHoard().makePlan(testAgent.get(1)));

        testAgent.get(0).getGroup().printMap();
        testAgent.get(0).getGroup().getGroupMapSnapshot().getMapSnapshot(0).printMap();


        for (int j = -8; j < 8; j++) {
            generateDirections(new Point(0, 0), j);
        }


    }
}
