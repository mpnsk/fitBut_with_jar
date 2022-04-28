package fitBut.fbReasoningModule.fbGoals.utils;

import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.fbPerceptionModule.data.SimInfo;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.FBGoalAssembleTasks;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.utils.JoinStructure;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;
import fitBut.utils.logging.HorseRider;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class TaskMatchTest {

    private static final String TAG = "TaskMatchTest";
    // create task
    HashMap<Point, BlockType> taskStructure = new HashMap<>();
    SimInfo simInfo = new SimInfo();
    FBTask task = new FBTask("test", 99, 1, taskStructure);
    FBAgent agent = new FBAgent("TestAgent");
    TaskMatch.TaskMatchStructure taskMatchStructure = null;
    Point taskPoint = new Point(0, 0);
    Point bodyPoint = new Point(0, 0);
    Rotation rotation = Rotation.NULL;

    @BeforeEach
    void init() {
        //taskStructure.put(new Point(0,1),simInfo.getBlockType("A"));
    }

    @Test
    @DisplayName("no rotation")
    @Order(1)
    void straightBodyProjection() {
        taskPoint = new Point(0, 0);
        bodyPoint = new Point(0, 0);
        rotation = Rotation.NULL;
        taskMatchStructure = new TaskMatch.TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation);

        assertEquals(new Point(0, 0), taskMatchStructure.bodyEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(1, 1), taskMatchStructure.bodyEquivalentOf(new Point(1, 1)));
        assertEquals(new Point(0, 0), taskMatchStructure.taskEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(1, 1), taskMatchStructure.taskEquivalentOf(new Point(1, 1)));

        taskPoint = new Point(1, 1);
        bodyPoint = new Point(0, 0);
        rotation = Rotation.NULL;
        taskMatchStructure = new TaskMatch.TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation);

        assertEquals(new Point(0, 0), taskMatchStructure.bodyEquivalentOf(new Point(1, 1)));
        assertEquals(new Point(1, 1), taskMatchStructure.bodyEquivalentOf(new Point(2, 2)));
        assertEquals(new Point(1, 1), taskMatchStructure.taskEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(2, 2), taskMatchStructure.taskEquivalentOf(new Point(1, 1)));

        taskPoint = new Point(0, 0);
        bodyPoint = new Point(1, 1);
        rotation = Rotation.NULL;
        taskMatchStructure = new TaskMatch.TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation);

        assertEquals(new Point(0, 0), taskMatchStructure.bodyEquivalentOf(new Point(-1, -1)));
        assertEquals(new Point(1, 1), taskMatchStructure.bodyEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(-1, -1), taskMatchStructure.taskEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(0, 0), taskMatchStructure.taskEquivalentOf(new Point(1, 1)));
    }

    @Test
    @DisplayName("180")
    @Order(2)
    void oppositeBodyProjection() {
        taskPoint = new Point(0, 0);
        bodyPoint = new Point(0, 0);
        rotation = Rotation.OPPOSITE;
        taskMatchStructure = new TaskMatch.TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation);

        assertEquals(new Point(0, 0), taskMatchStructure.bodyEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(1, 1), taskMatchStructure.bodyEquivalentOf(new Point(-1, -1)));
        assertEquals(new Point(0, 0), taskMatchStructure.taskEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(1, 1), taskMatchStructure.taskEquivalentOf(new Point(-1, -1)));

        taskPoint = new Point(0, 2);
        bodyPoint = new Point(1, 1);
        rotation = Rotation.OPPOSITE;
        taskMatchStructure = new TaskMatch.TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation);

        assertEquals(new Point(0, 2), taskMatchStructure.bodyEquivalentOf(new Point(1, 1)));
        assertEquals(new Point(1, 3), taskMatchStructure.bodyEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(1, 3), taskMatchStructure.taskEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(0, 2), taskMatchStructure.taskEquivalentOf(new Point(1, 1)));

    }

    @Test
    @DisplayName("CCW")
    @Order(3)
    void ccwBodyProjection() {
        taskPoint = new Point(0, 0);
        bodyPoint = new Point(0, 0);
        rotation = Rotation.CCW;
        taskMatchStructure = new TaskMatch.TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation);

        assertEquals(new Point(0, 0), taskMatchStructure.bodyEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(1, -1), taskMatchStructure.bodyEquivalentOf(new Point(1, 1)));
        assertEquals(new Point(0, 0), taskMatchStructure.taskEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(-1, 1), taskMatchStructure.taskEquivalentOf(new Point(1, 1)));

        taskPoint = new Point(2, 0);
        bodyPoint = new Point(1, 1);
        rotation = Rotation.CCW;
        taskMatchStructure = new TaskMatch.TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation);

        assertEquals(new Point(1, 2), taskMatchStructure.bodyEquivalentOf(new Point(1, 0)));
        assertEquals(new Point(0, 1), taskMatchStructure.bodyEquivalentOf(new Point(2, -1)));
        assertEquals(new Point(3, -1), taskMatchStructure.taskEquivalentOf(new Point(0, 0)));
        assertEquals(new Point(1, 0), taskMatchStructure.taskEquivalentOf(new Point(1, 2)));


    }

    @Test
    void completeness() {
        HashMap<Point, BlockType> taskStructure_AA = new HashMap<>();
        FBTask task_AA = new FBTask("test_AA", 99, 1, taskStructure_AA);
        taskStructure_AA.put(new Point(0, 1), simInfo.getBlockType("A"));
        taskStructure_AA.put(new Point(-1, 1), simInfo.getBlockType("A"));
        FBAgent agent_A = new FBAgent("TestAgent_A");
        agent_A.getBody().addCell(new Point(0, 0), new Point(0, 1), new FBBlockObject(simInfo.getBlockType("A"), true));

        FBAgent agent_AA = new FBAgent("TestAgent_AA");
        agent_AA.getBody().addCell(new Point(0, 0), new Point(0, 1), new FBBlockObject(simInfo.getBlockType("A"), true));
        agent_AA.getBody().addCell(new Point(0, 1), new Point(-1, 1), new FBBlockObject(simInfo.getBlockType("A"), true));

        FBAgent agent_B = new FBAgent("TestAgent_B");
        agent_B.getBody().addCell(new Point(0, 0), new Point(0, 1), new FBBlockObject(simInfo.getBlockType("B"), true));
        ArrayList<TaskMatch.TaskMatchStructure> hits;

        // AA_task A_agent no accepted task
        hits = TaskMatch.completeness(task_AA, agent_A);
        assertEquals(3, hits.size());

        // AA_task A_agent accepted AA_task
        agent_A.getAgentInfo().setAcceptedTask("test_AA");
        hits = TaskMatch.completeness(task_AA, agent_A);
        assertEquals(4, hits.size());

        // AA_task B_agent no accepted task
        hits = TaskMatch.completeness(task_AA, agent_B);
        assertEquals(0, hits.size());


        // AA_task AA_agent no accepted task
        hits = TaskMatch.completeness(task_AA, agent_AA);
        assertEquals(5, hits.size());

        // AA_task A_agent accepted AA_task
        agent_AA.getAgentInfo().setAcceptedTask("test_AA");
        hits = TaskMatch.completeness(task_AA, agent_AA);
        assertEquals(6, hits.size());

        FBAgent agent_A6 = new FBAgent("TestAgent_A6");
        agent_A6.getBody().addCell(new Point(0, 0), new Point(0, 1), new FBBlockObject(simInfo.getBlockType("B1"), true));

        FBAgent agent_A11 = new FBAgent("TestAgent_A11");
        agent_A11.getBody().addCell(new Point(0, 0), new Point(0, -1), new FBBlockObject(simInfo.getBlockType("B2"), true));
        agent_A11.getAgentInfo().setAcceptedTask("task_2");

        HashMap<Point, BlockType> taskStructure_task_2 = new HashMap<>();
        FBTask task_2 = new FBTask("task_2", 99, 1, taskStructure_task_2);
        taskStructure_task_2.put(new Point(0, 1), simInfo.getBlockType("B2"));
        taskStructure_task_2.put(new Point(-1, 1), simInfo.getBlockType("B1"));


        ArrayList<TaskMatch.TaskMatchStructure> masterHits = TaskMatch.completeness(task_2, agent_A11);
        assertEquals(1, masterHits.size());
        assertTrue(masterHits.get(0).isMaster());

        ArrayList<TaskMatch.TaskMatchStructure> slaveHits = TaskMatch.completeness(task_2, agent_A6);
        assertEquals(3, slaveHits.size());
        for (TaskMatch.TaskMatchStructure hit : slaveHits) {
            assertFalse(hit.isMaster());
        }

        FBGoalAssembleTasks goalAssemble = new FBGoalAssembleTasks(null);
        for (TaskMatch.TaskMatchStructure hit : slaveHits) {
            JoinStructure connectionPoint = goalAssemble.findConnection(masterHits.get(0), hit);
            HorseRider.inform(TAG, "completeness: ");
            //assertEquals(new PointAndDir(new Point(0,0), Direction.E), connectionPoint);
        }


    }
}