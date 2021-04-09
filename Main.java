package assignment2;


import assignment2.FCFS.FCFSSchedulerManager;
import assignment2.NonPPriority.NonPPrioritySchedulerManager;
import assignment2.NonSJF.NonSJFSchedulerManager;
import assignment2.PreemptivePriority.PreemptivePriority;
import assignment2.PreemptiveSJF.PreemptiveSJF;
import assignment2.ProcessHandler.Burst;
import assignment2.ProcessHandler.BurstType;
import assignment2.ProcessHandler.Process;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {


        //Instances of all the algorithms
    FCFSSchedulerManager fcfs = new FCFSSchedulerManager();
    NonSJFSchedulerManager nonSJF = new NonSJFSchedulerManager();
    NonPPrioritySchedulerManager nonPPriority = new NonPPrioritySchedulerManager();
    PreemptiveSJF preemptiveSJF = new PreemptiveSJF();
    PreemptivePriority preemptivePriority = new PreemptivePriority();

        //Example of Process and Burst instances
        Process p1 = new Process(1,0,3);
            p1.addBurst(new Burst(0, 4, p1, BurstType.CPU));
            p1.addBurst(new Burst(0, 3, p1, BurstType.I_O));
            p1.addBurst(new Burst(0, 2, p1, BurstType.CPU));

        Process p2 = new Process(2,0,  4);
            p2.addBurst(new Burst(0, 1, p2, BurstType.CPU));
            p2.addBurst(new Burst(0, 2, p2, BurstType.I_O));
            p2.addBurst(new Burst(0, 2, p2, BurstType.CPU));

        Process p3 = new Process(3,2,1);
            p3.addBurst(new Burst(0, 2, p3, BurstType.CPU));
            p3.addBurst(new Burst(0, 1, p3, BurstType.I_O));
            p3.addBurst(new Burst(0, 1, p3, BurstType.CPU));
            p3.addBurst(new Burst(1, 2, p3, BurstType.I_O));
            p3.addBurst(new Burst(0, 1, p3, BurstType.CPU));
            p3.addBurst(new Burst(0, 1, p3, BurstType.I_O));
            p3.addBurst(new Burst(0, 2, p3, BurstType.CPU));


        ArrayList<Process> processes = new ArrayList();
            processes.add(p1);
            processes.add(p2);
            processes.add(p3);

        //examples of how to call and algorithm. Please follow this order, simply change the instance e.g. from fcfs to preemptiveSJF etc.
        fcfs.addProcesses(processes);
        fcfs.ganttChart();
        fcfs.averageWaitingTime();
        fcfs.averageTurnAroundTime();











    }
}
