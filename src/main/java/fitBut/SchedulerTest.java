package fitBut;

import eis.exceptions.ActException;
import eis.exceptions.PerceiveException;
import eis.iilang.*;
import fitBut.agents.Agent;
import fitBut.agents.FBAgent;
import fitBut.fbActions.FBAttach;
import fitBut.fbActions.FBConnect;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbReasoningModule.fbGoals.FBGoal;
import fitBut.fbReasoningModule.fbGoals.FBGoalDig;
import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.utils.logging.HorseRider;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {
    private static final String TAG = "SchedulerTest";
    private Map<String, Agent> agents = new HashMap<>();


    @Test
    void step() {
        MailService mailService = new MailService();
        agents.put("A6", new FBAgent("A6", mailService));
        agents.put("A11", new FBAgent("A11", mailService));

        List<Percept> percepts_a6 = new Vector<>();
        // Percept("attached", new Numeral(pos.x), new Numeral(pos.y))));
/**
 * A6 percept: step - [159]
 * A6 percept: thing - [-1, 1, block, b2]
 * A6 percept: thing - [0, 0, entity, A]
 * A6 percept: thing - [1, 0, block, b1]
 * A6 percept: thing - [-1, 2, entity, A]
 * A6 percept: thing - [0, -1, taskboard, ]
 * A6 percept: attached - [-1, 1]
 * A6 percept: attached - [1, 0]
 * A6 percept: obstacle - [1, 2]
 * A6 percept: task - [task1, 232, 4, [req(-1,1,b2),req(0,1,b2)]]
 * A6 percept: task - [task2, 283, 4, [req(0,1,b1),req(1,1,b2)]]
 * A6 percept: task - [task3, 290, 4, [req(-1,1,b2),req(0,1,b1)]]
 * A6 percept: task - [task4, 277, 15, [req(0,1,b1),req(1,1,b0)]]
 * A6 percept: accepted - [task2]
 * A6 percept: actionID - [159]
 * A6 percept: lastAction - [move]
 * A6 percept: lastActionParams - [[e]]
 * A6 percept: lastActionResult - [success]
 * A6 percept: disabled - [false]
 * A6 percept: score - [1]
 * A6 percept: energy - [300]
 * A6 percept: requestAction - []
 * A6 percept: timestamp - [1615277889578]
 * A6 percept: deadline - [1615277892582]
 */

        percepts_a6.add(newPerceptStringParam("team","A"));
        percepts_a6.add(newPerceptStringParam("simStart",""));
        percepts_a6.add(newPerceptParam("teamSize",2));
        percepts_a6.add(newPerceptParam("steps",750));
        percepts_a6.add(newPerceptStringParam("name","agentA6"));
        percepts_a6.add(newPerceptParam("vision",5));

        percepts_a6.add(newPerceptParam("step",156));
        percepts_a6.add(newPerceptThing(-1,1,"block","b2"));
        percepts_a6.add(newPerceptThing(0,0,"entity","A"));
        percepts_a6.add(newPerceptThing(1,0,"block","b1"));
        percepts_a6.add(newPerceptThing(-1,2,"entity","A"));
        percepts_a6.add(newPerceptThing(0,-1,"taskboard",""));
        percepts_a6.add(newPerceptObject("attached",-1,1));
        percepts_a6.add(newPerceptObject("attached",1,0));
        percepts_a6.add(newPerceptObject("obstacle",1,2));
        percepts_a6.add(newPerceptTask("task1",232,4,blocks(new STB(-1,1,"b2"),new STB(0,1,"b2"))));
        percepts_a6.add(newPerceptTask("task2",283,4,blocks(new STB(0,1,"b1"),new STB(1,1,"b2"))));
        percepts_a6.add(newPerceptTask("task3",290,4,blocks(new STB(-1,1,"b2"),new STB(0,1,"b1"))));
        percepts_a6.add(newPerceptTask("task4",277,15,blocks(new STB(0,1,"b0"),new STB(1,1,"b0"))));
        percepts_a6.add(newPerceptStringParam("accepted","task2"));
        percepts_a6.add(newPerceptParam("actionID",159));
        //percepts_a6.add(newPerceptStringParam("lastAction","move"));
        percepts_a6.add(newPerceptStringParam("lastAction","attach"));
        //percepts_a6.add(newPerceptStringParam("lastActionParams",[e]));
        percepts_a6.add(newPerceptStringParam("lastActionResult","success"));
        percepts_a6.add(newPerceptStringParam("disabled","false"));
        percepts_a6.add(newPerceptParam("score",1));
        percepts_a6.add(newPerceptParam("energy",300));
        percepts_a6.add(newPerceptStringParam("requestAction",""));
        percepts_a6.add(newPerceptTimeParam("timestamp",1615277889578L));
        //percepts_a6.add(newPerceptTimeParam("deadline",1615277892582L));
        percepts_a6.add(newPerceptTimeParam("deadline",System.currentTimeMillis()+4*1000));
        Agent agent_a6 = agents.get("A6");
        agent_a6.addPercepts(percepts_a6);
/**
 * A11 percept: team - [A]
 * A11 percept: simStart - []
 * A11 percept: teamSize - [2]
 * A11 percept: steps - [750]
 * A11 percept: name - [agentA11]
 * A11 percept: vision - [5]
 * ------------------------
 * A11 percept: step - [159]
 * A11 percept: thing - [0, 0, entity, A]
 * A11 percept: thing - [1, -3, taskboard, ]
 * A11 percept: thing - [1, -2, entity, A]
 * A11 percept: thing - [0, -1, block, b2]
 * A11 percept: thing - [2, -2, block, b1]
 * A11 percept: attached - [2, -2]
 * A11 percept: attached - [0, -1]
 * A11 percept: obstacle - [5, 0]
 * A11 percept: obstacle - [2, 0]
 * A11 percept: task - [task1, 232, 4, [req(-1,1,b2),req(0,1,b2)]]
 * A11 percept: task - [task2, 283, 4, [req(0,1,b1),req(1,1,b2)]]
 * A11 percept: task - [task3, 290, 4, [req(-1,1,b2),req(0,1,b1)]]
 * A11 percept: task - [task4, 277, 15, [req(0,1,b1),req(1,1,b0)]]
 * A11 percept: actionID - [159]
 * A11 percept: lastAction - [skip]
 * A11 percept: lastActionParams - [[]]
 * A11 percept: lastActionResult - [success]
 * A11 percept: disabled - [false]
 * A11 percept: score - [1]
 * A11 percept: energy - [300]
 * A11 percept: requestAction - []
 * A11 percept: timestamp - [1615277889578]
 * A11 percept: deadline - [1615277892579]
 */
        List<Percept> percepts_a11 = new Vector<>();
        percepts_a11.add(newPerceptStringParam("team","A"));
        percepts_a11.add(newPerceptStringParam("simStart",""));
        percepts_a11.add(newPerceptParam("teamSize",2));
        percepts_a11.add(newPerceptParam("steps",750));
        percepts_a11.add(newPerceptStringParam("name","agentA11"));
        percepts_a11.add(newPerceptParam("vision",5));

        percepts_a11.add(newPerceptParam("step",156));
        percepts_a11.add(newPerceptThing(0,0,"entity","A"));
        percepts_a11.add(newPerceptThing(1,-3,"taskboard",""));
        percepts_a11.add(newPerceptThing(1,-2,"entity","A"));
        percepts_a11.add(newPerceptThing(0,-1,"block","b2"));
        percepts_a11.add(newPerceptThing(2,-2,"block","b1"));
        percepts_a11.add(newPerceptObject("attached",2,-2));
        percepts_a11.add(newPerceptObject("attached",0,-1));
        percepts_a11.add(newPerceptObject("obstacle",5,0));
        percepts_a11.add(newPerceptObject("obstacle",2,0));
        percepts_a11.add(newPerceptTask("task1",232,4,blocks(new STB(-1,1,"b2"),new STB(0,1,"b2"))));
        percepts_a11.add(newPerceptTask("task2",283,4,blocks(new STB(0,1,"b1"),new STB(1,1,"b2"))));
        percepts_a11.add(newPerceptTask("task3",290,4,blocks(new STB(-1,1,"b2"),new STB(0,1,"b1"))));
        percepts_a11.add(newPerceptTask("task4",277,15,blocks(new STB(0,1,"b0"),new STB(1,1,"b0"))));
        percepts_a11.add(newPerceptStringParam("accepted","task2"));
        percepts_a11.add(newPerceptParam("actionID",159));
        //percepts_a11.add(newPerceptStringParam("lastAction","skip"));
        percepts_a11.add(newPerceptStringParam("lastAction","attach"));
        //percepts_a11.add(newPerceptStringParam("lastActionParams",[e]));
        percepts_a11.add(newPerceptStringParam("lastActionResult","success"));
        percepts_a11.add(newPerceptStringParam("disabled","false"));
        percepts_a11.add(newPerceptParam("score",1));
        percepts_a11.add(newPerceptParam("energy",300));
        percepts_a11.add(newPerceptStringParam("requestAction",""));
        percepts_a11.add(newPerceptTimeParam("timestamp",1615277889578L));
        //percepts_a11.add(newPerceptTimeParam("deadline",1615277892582L));
        percepts_a11.add(newPerceptTimeParam("deadline",System.currentTimeMillis()+4*1000));
        Agent agent_a11 = agents.get("A11");
        agent_a11.addPercepts(percepts_a11);

        mailService.registerAgent(agent_a6, "A");
        mailService.registerAgent(agent_a11, "A");

        FBGoal fake_g_a6 = new FBGoalDig();
        fake_g_a6.setPlan(new FBPlan().appendAction(new FBAttach(Direction.E)));
        //((FBAgent)agent_a6).lastAction = fake_g_a6 ;

        FBGoal fake_g_a11 = new FBGoalDig();
        fake_g_a11.setPlan(new FBPlan().appendAction(new FBAttach(Direction.N)));
        //((FBAgent)agent_a11).lastAction = fake_g_a11 ;
/*
        HorseRider.inform(TAG, "++++++++++++++++++++ step test" + 0 + " ++++++++++++++++++++++");
        //agents.values().forEach(agent -> {
            Runnable runnable = () -> {
                Action action = agent_a6.step();
                agent_a6.setRunning(false);
                if (action != null) {
                    System.out.println("Perform action " + action.getName() + " for " + agent_a6.getName());
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        Action action = agent_a11.step();
        agent_a11.setRunning(false);
        if (action != null) {
            System.out.println("Perform action " + action.getName() + " for " + agent_a11.getName());
        }
*/
        //});
    }

    private Percept newPerceptTimeParam(String name, long time) {
        return new Percept(name, new Numeral(time));
    }

    private Percept newPerceptStringParam(String name, String value) {
        return new Percept(name, new Identifier(value));
    }

    private Parameter blocks(STB... blocks) {
        var reqs = new ParameterList();
        for(var req : blocks) {
            reqs.add(new Function("req", new Numeral(req.x), new Numeral(req.y), new Identifier(req.type)));
        }
        return reqs;
    }

    private Percept newPerceptTask(String name, int deadline, int reward, Parameter blocks) {
        return new Percept("task", new Identifier(name), new Numeral(deadline), new Numeral(reward), blocks);
    }

    private Percept newPerceptObject(String name, int x, int y) {
        return new Percept(name, new Numeral(x), new Numeral(y));
    }

    private Percept newPerceptParam(String name, int i) {
        return new Percept(name, new Numeral(i));
    }

    private Percept newPerceptThing(int x, int y, String type, String details) {
        return new Percept("thing", new Numeral(x), new Numeral(y), new Identifier(type), new Identifier(details));
    }

    private class STB {
        public int x;
        public int y;
        public String type;

        public STB(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }
}